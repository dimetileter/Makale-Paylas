package com.aliosman.makalepaylas.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.GetPdfInfoModel
import com.aliosman.makalepaylas.roomdb.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.UserInfoDatabase
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfilePageViewModel(application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dao: UserInfoDAO
    private val takenPdfList = ArrayList<GetPdfInfoModel>()

    var takenPdf = MutableLiveData<ArrayList<GetPdfInfoModel>>()
    var isError = MutableLiveData<Boolean>()
    var isLoading = MutableLiveData<Boolean>()

    init {
        val database = Room.databaseBuilder(application, UserInfoDatabase::class.java, "UserInfos").build()
        dao = database.userDao()
    }

    fun getPdfList() {
        getPdfRefListFromFirebase()
    }

    private fun getPdfRefListFromFirebase() {
        isLoading.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userUUID = getUserUUID()
                val dbRef = db.collection("Users").document(userUUID)
                val document = dbRef.get().await()

                if (document.exists()) {
                    val documents = document.get("pdfRef") as? List<String>
                    if (documents != null) {
                        for (docs in documents) {
                            getPdfInfo(docs)
                        }
                        withContext(Dispatchers.Main) {
                            takenPdf.value = ArrayList(takenPdfList)
                            isLoading.value = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoading.value = false
                            ToastMessages(getApplication()).showToastLong("Herhangi paylaşım yok")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    isError.value = true
                    Log.w("FirestoreError", "Kullanıcıdan pdfRef alınırken oluşan hata: ", e)
                }
            }
        }
    }

    private fun getPdfInfo(docs: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val getPdfRef = db.collection("Posts").document(docs)
                val document = getPdfRef.get().await()

                if (document.exists()) {
                    val pdfUrl = document.getString("pdfUrl") ?: ""
                    val pdfBitmapUrl = document.getString("pdfBitmapUrl") ?: ""
                    val pdfName = document.getString("artName") ?: ""
                    val pdfDesc = document.getString("artDesc") ?: ""
                    val createdAt = document.getString("createdAt") ?: ""
                    val nickname = document.getString("nickName") ?: ""

                    val takenPdfInfo = GetPdfInfoModel(
                        pdfName,
                        pdfDesc,
                        pdfUrl,
                        pdfBitmapUrl,
                        createdAt,
                        nickname
                    )

                    takenPdfList.add(takenPdfInfo)
                    withContext(Dispatchers.Main) {
                        takenPdf.value = ArrayList(takenPdfList)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    isError.value = true
                    Log.w("FirestoreError", "Pdf url ve bilgileri alınırken meydana gelen hata:", e)
                }
            }
        }
    }

    private suspend fun getUserUUID(): String {
        return dao.getUserUUID()
    }
}

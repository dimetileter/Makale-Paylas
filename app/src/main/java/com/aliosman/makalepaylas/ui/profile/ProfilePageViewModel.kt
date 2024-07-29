package com.aliosman.makalepaylas.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.GetProfilePdfInfoModel
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDAO
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDatabase
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.BitmapToByteArray
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfilePageViewModel(private val application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val daoUserInfo: UserInfoDAO
    private val daoPdfList: TakenProfilePdfDAO

    private val takenPdfList = ArrayList<GetProfilePdfInfoModel>()

    var takenPdf = MutableLiveData<ArrayList<GetProfilePdfInfoModel>>()
    var isErrorP = MutableLiveData<Boolean>()
    var isLoadingP = MutableLiveData<Boolean>()
    var newProfilePicture = MutableLiveData<Bitmap>()

    init {
        val database = Room.databaseBuilder(application, UserInfoDatabase::class.java, "UserInfos").build()
        daoUserInfo = database.userDao()

        val pdfDatabase= Room.databaseBuilder(application, TakenProfilePdfDatabase::class.java, "TakenProfilePdf").build()
        daoPdfList = pdfDatabase.userDao()
    }

    fun getPdfList() {
        getPdfRefListFromFirebase()
    }

    fun getPdfFromRoom()
    {
        getPdfListFromRoom()
    }

    private fun getPdfRefListFromFirebase() {
        isLoadingP.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userUUID = getUserUUID()
                val dbRef = db.collection("Users").document(userUUID!!)
                val document = dbRef.get().await()

                if (document.exists()) {

                    val documents = document.get("pdfRef") as? List<String>
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    if (documents != null) {

                        // Verileri çekmeden önce room veri tabanını temizle
                        daoPdfList.delteAll()
                        for (docs in documents) {
                            getPdfInfo(docs)
                        }

                        profilePictureUrl?.let {
                            val profilePicture = Picasso.get().load(it).get()
                            val byteArray = BitmapToByteArray().bitmapToByteArray(profilePicture)
                            daoUserInfo.updateProfilePicture(byteArray)
                            withContext(Dispatchers.Main) {
                                newProfilePicture.value = profilePicture
                            }
                        }

                        withContext(Dispatchers.Main) {
                            takenPdf.value = takenPdfList
                            isLoadingP.value = false
                        }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            isLoadingP.value = false
                            ToastMessages(getApplication()).showToastLong("Herhangi paylaşım yok")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingP.value = false
                    isErrorP.value = true
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
                    val pdfUUID = document.getString("pdfUUID") ?: ""

                    val takenPdfInfo = GetProfilePdfInfoModel(
                        pdfName,
                        pdfDesc,
                        pdfUrl,
                        pdfBitmapUrl,
                        createdAt,
                        nickname,
                        pdfUUID
                    )

                    // Alınan verileri array list ve room içine aktar
                    takenPdfList.add(takenPdfInfo)
                    daoPdfList.add(takenPdfInfo)
                    withContext(Dispatchers.Main) {
                        takenPdf.value = takenPdfList
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        isLoadingP.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingP.value = false
                    isErrorP.value = true
                    Log.w("FirestoreError", "Pdf url ve bilgileri alınırken meydana gelen hata:", e)
                }
            }
        }
    }

    private fun getPdfListFromRoom()
    {
        isLoadingP.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            val pdfList = daoPdfList.getAll()
            takenPdfList.addAll(pdfList)
            withContext(Dispatchers.Main) {
                isLoadingP.value = false
                takenPdf.value = takenPdfList
            }
        }
    }


    private suspend fun getUserUUID(): String? {
        return daoUserInfo.getUserUUID()
    }
}
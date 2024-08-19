package com.aliosman.makalepaylas.activities.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class DownloadPageViewModel(private val application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore
    private val auth: FirebaseAuth
    private val roomdb: UserInfoDatabase
    private val dao: UserInfoDAO

    // Kayıt işlemleri
    var isError = MutableLiveData<Boolean>()
    var isSuccess = MutableLiveData<Boolean>()
    // Veri alımı
    var data = MutableLiveData<ArrayList<String>>()

    init {
        db = Firebase.firestore
        auth = Firebase.auth
        roomdb = Room.databaseBuilder(application, UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
    }

    fun addPdfIntoSavesList(pdfUUID: String) {
        saves(pdfUUID)
    }

    fun getPdfInfoFromFirebase(pdfUUID: String) {
        getPdfInfo(pdfUUID)
    }

    fun deletePdfFromSavesList(pdfUUID: String) {
        delete(pdfUUID)
    }

    // Pdf kaydet
    private fun saves(pdfUUID: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userUUID = dao.getUserUUID()
                val ref = db.collection("Users").document(userUUID!!)
                val doc = ref.update("saves", FieldValue.arrayUnion(pdfUUID))

                doc.addOnSuccessListener {
                    viewModelScope.launch(Dispatchers.Main) {
                        isSuccess.value = true
                    }
                }

                doc.addOnFailureListener {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            isError.value = true
                        }
                    }
                }
            }
            catch (e: Exception) {
                Log.w("Firebase saves hatası:", "Kaydedilen pdf kaydedilirken hata oluştu", e)
                withContext(Dispatchers.Main) {
                    isSuccess.value = false
                }
            }
        }
    }

    // Kayıtlı pdf sil
    private fun delete(pdfUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userUUID = auth.currentUser!!.uid
            try {
                val ref = db.collection("Users").document(userUUID)
                ref.update("saves", FieldValue.arrayRemove(pdfUUID)).await()
            }
            catch (e: Exception) {
                Log.w("Firebase saves silme hatası:", "Kaydedilen pdf silinirken hata oluştu", e)
                withContext(Dispatchers.Main) {
                    val msg = application.getString(R.string.toast_hata)
                    ToastMessages(application).showToastShort(msg)
                }
            }
        }
    }

    // Pdf verilerini getir
    private fun getPdfInfo(pdfUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ref = db.collection("Posts").document(pdfUUID)
                val result = ref.get().await()

                if (result.exists()) {
                    val artDesc = result.getString("artDesc") ?: ""
                    val pdfUrl = result.getString("pdfUrl") ?: ""
                    val createdAt = result.get("createdAt") as Timestamp

                    val date = createdAt.toDate()
                    val createdAtString = SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault()).format(date)

                    val list = arrayListOf<String>(
                        pdfUrl,
                        artDesc,
                        createdAtString
                    )

                    withContext(Dispatchers.Main) {
                        data.value = list
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        data.value = arrayListOf()
                    }
                }
            }
            catch (e: Exception) {
                Log.w("FirebaseFirestore", "Dokümanlar alınırken oluşan hata:", e)
                withContext(Dispatchers.Main) {
                    val msg = application.getString(R.string.toast_hata)
                    ToastMessages(application).showToastShort(msg)
                }
            }
        }
    }
}
package com.aliosman.makalepaylas.activities.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.android.material.color.utilities.DislikeAnalyzer
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

class DownloadPageViewModel(private val application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore
    private val auth: FirebaseAuth
    private val roomdb: UserInfoDatabase
    private val dao: UserInfoDAO

    var isError = MutableLiveData<Boolean>()
    var isSuccess = MutableLiveData<Boolean>()

    init {
        db = Firebase.firestore
        auth = Firebase.auth
        roomdb = Room.databaseBuilder(application, UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
    }

    fun addPdfIntoSavesList(pdfUUID: String)
    {
        saves(pdfUUID)
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
            val remove_value = pdfUUID

            try {
                val ref = db.collection("Users").document(userUUID)
                ref.update("saves", FieldValue.arrayRemove(remove_value)).await()
            }
            catch (e: Exception) {
                Log.w("Firebase saves silme hatası:", "Kaydedilen pdf silinirken hata oluştu", e)
                withContext(Dispatchers.Main) {
                    isSuccess.value = false
                }
            }
        }
    }
}
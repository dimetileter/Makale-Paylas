package com.aliosman.makalepaylas.login.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDatabase
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDatabase
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginPageViewModel(private val application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore
    private val auth: FirebaseAuth

    private var roomdb: UserInfoDatabase
    private var daoUser: UserInfoDAO

    var isLoading = MutableLiveData<Boolean>()
    var isExists = MutableLiveData<Boolean>()

    init {
        db = Firebase.firestore
        auth = Firebase.auth

        roomdb = Room.databaseBuilder(application.applicationContext, UserInfoDatabase::class.java, "UserInfos").build()
        daoUser = roomdb.userDao()
    }

//    fun checkInformation(user: FirebaseUser) {
//        checkUserInFirebase(user)
//    }

    // Kullanıcı bilgilerini room ile kontrol et
    fun checkInformationRoom(user: FirebaseUser) {
        checkUserInRoom(user)
    }

    // Kullanıcı bilgilerini kontrol et
    private fun checkUserInFirebase(user: FirebaseUser) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userRef = db.collection("Users").document(user.uid)
                val doc = userRef.get().await()

                if (doc.exists()) {
                    withContext(Dispatchers.Main) {
                        // Kullanıcı mevcut
                        isLoading.value = false
                        isExists.value = true
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        // Kullanıcı mevcut
                        isLoading.value = false
                        isExists.value = false
                    }
                }

                // Kullanici sorgulanirken hata meydan geldi
                // Login ekranında kalmaya devam et
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
//                    isLoading.value = false
//                    isExists.value = false
                    Log.w("FirestoreError", "Kullancıcı varlığı sorgulanırken hata olştu: ", e)
                }
            }
        }
    }

    // Kullanıcı bilgilerini room ile kontrol et
    private fun checkUserInRoom(user: FirebaseUser) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val daoUUID = daoUser.getUserUUID() ?: ""

            try {
                // Eğer kullanıcı kimliği cihazda varsa anasayfaya git
                if (user.uid == daoUUID) {
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                        isExists.value = true
                    }
                }
                else if (daoUUID.isNullOrEmpty()) {
                    checkUserInFirebase(user)
                }
                else {
                    // Anasayfa verileri
                    val dbHome = Room.databaseBuilder(application, TakenHomePdfDatabase::class.java, "TakenHomePdf")
                    val daoHome = dbHome.build().userDao()

                    // Kullanıcı paylaşım verileri
                    val dbProfile = Room.databaseBuilder(application, TakenProfilePdfDatabase::class.java, "TakenProfilePdf")
                    val daoProfile = dbProfile.build().userDao()

                    // Önceki kullanıcının verilerini sil
                    daoUser.deleteAll()
                    daoHome.delteAll()
                    daoProfile.delteAll()

                    // Yeni kullanıcıyı kontrol et
                    checkUserInFirebase(user)
                }
            }
            catch (e: Exception) {
                Log.w("RoomError", "Kullanıcı uuid sorgulanırken hata meydana geldi: ", e)
            }
        }
    }


}
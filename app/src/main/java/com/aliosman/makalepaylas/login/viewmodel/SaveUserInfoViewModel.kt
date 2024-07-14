package com.aliosman.makalepaylas.login.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.SaveUserInfoModel
import com.aliosman.makalepaylas.roomdb.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.UserInfoDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class SaveUserInfoViewModel(private val application: Application): AndroidViewModel(application) {

    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private lateinit var dao: UserInfoDAO
    private lateinit var roomdb: UserInfoDatabase

    var isLoading = MutableLiveData<Boolean>()
    var isPictureError = MutableLiveData<Boolean>()
    var isUserInfoError = MutableLiveData<Boolean>()
    var navigateNextScreen = MutableLiveData<Boolean>()

    init {
        //Firebase başlat
        storage = Firebase.storage
        db = Firebase.firestore
        //Room başlat
        roomdb = Room.databaseBuilder(getApplication(), UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
    }

    //Kullanıcı bilgilerini kaydet
    private fun trySaveUserInfoDatabase(userInfosHashMap: HashMap<String, Any>, profilePictureUri: Uri?, profilePictureBitmap: Bitmap?) {

        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {

            //Eğer resim seçilmişse resmi kaydet seçilmemişse zorunlu bilgileri kaydet
            if (profilePictureUri != null) {
                val uuid = UUID.randomUUID()
                val imageUuid = "${uuid}.jpeg"
                val referance = storage.reference
                val email = userInfosHashMap["email"] as String

                val imageReferance = referance.child("ProfilePicures").child(email).child(imageUuid)

                //Storage içine resim koy
                imageReferance.putFile(profilePictureUri).addOnSuccessListener {
                    imageReferance.downloadUrl.addOnCompleteListener {

                        if (it.isSuccessful) {
                            userInfosHashMap["profilePictureUrl"] = it.result.toString()
                        } else {
                            userInfosHashMap["profilePictureUrl"] = ""
                        }

                        viewModelScope.launch {
                            saveInformationsDatabase(userInfosHashMap, profilePictureBitmap)
                        }
                    }
                }.addOnFailureListener { _ ->
                    viewModelScope.launch(Dispatchers.Main) {
                        isPictureError.value = true
                    }
                    viewModelScope.launch {
                        userInfosHashMap["profilePictureUrl"] = ""
                        saveInformationsDatabase(userInfosHashMap, profilePictureBitmap)
                    }
                }
            } else {
                viewModelScope.launch {
                    userInfosHashMap["profilePictureUrl"] = ""
                    saveInformationsDatabase(userInfosHashMap, profilePictureBitmap)
                }
            }
        }
    }

    //Zorunlu kullanıcı bilgilerini kaydet
    private fun saveInformationsDatabase(userInfosHashMap: HashMap<String, Any>, profilePictureBitmap: Bitmap?)
    {
        db.collection("Users").add(userInfosHashMap).addOnSuccessListener {
           // Yüklemeyid durdur ve ilgili aktiviteye git
            // Room'daki eski verileri sil
            deleteOldUserInfoRoomDatabase()

            if (profilePictureBitmap != null) {
                userInfosHashMap["profilePictureByteArray"] = bitmapToByteArray(profilePictureBitmap)
            }

            saveUserInfoRoomDatabase(userInfosHashMap)

            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = false
                //navigateNextScreen.value = true
            }

        }.addOnFailureListener {
            // Yüklemeyi durdur ve hata mesajı göster
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = false
                isUserInfoError.value = true
            }
        }
    }

    private fun saveUserInfoRoomDatabase(userInfosHashMap: HashMap<String, Any>)
    {
        viewModelScope.launch {

            // Verileri room'a kaydet
            val userName = userInfosHashMap["userName"]
            val nickname = userInfosHashMap["nickName"]
            val birthDate = userInfosHashMap["birthDate"]
            val email = userInfosHashMap["email"]
            val userUID = userInfosHashMap["userUID"]
            val profilePictureUrl = userInfosHashMap["profilePictureUrl"]
            val profilePictureByteArray = userInfosHashMap["profilePictureByteArray"]

            val saveUserInfoModel = SaveUserInfoModel(
                userName as String,
                nickname as String,
                birthDate as String,
                email as String,
                userUID as String,
                profilePictureUrl as String,
                profilePictureByteArray as ByteArray
            )

            dao.insertAll(saveUserInfoModel)
        }
    }

    private fun deleteOldUserInfoRoomDatabase()
    {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }

    private fun bitmapToByteArray(profilePictureBitmap: Bitmap?): ByteArray
    {
        val outputStream = ByteArrayOutputStream()
        profilePictureBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val bytePicture = outputStream.toByteArray()
        return bytePicture
    }

    // Verileri firebase veri tabanına kaydet
    fun saveUserInfos(userInfosHasMap: HashMap<String, Any>, profilePictureUri: Uri?, profilePictureBitmap: Bitmap?) {
        trySaveUserInfoDatabase(userInfosHasMap, profilePictureUri, profilePictureBitmap)
    }

}
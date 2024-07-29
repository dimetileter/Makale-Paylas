package com.aliosman.makalepaylas.login.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.SaveUserInfoModel
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
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

    private var storage: FirebaseStorage
    private var db: FirebaseFirestore
    private var dao: UserInfoDAO
    private var roomdb: UserInfoDatabase

    private var userInfosHashMap =  HashMap<String, Any>()
    private var profilePictureUri: Uri? = null
    private var profilePictureBitmap: Bitmap? = null

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
    private fun trySaveUserInfoDatabase( ) {

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
                imageReferance.putFile(profilePictureUri!!).addOnSuccessListener {
                    imageReferance.downloadUrl.addOnCompleteListener {

                        userInfosHashMap["profilePictureUUID"] = imageUuid

                        if (it.isSuccessful) {
                            userInfosHashMap["profilePictureUrl"] = it.result.toString()
                        } else {
                            userInfosHashMap["profilePictureUrl"] = ""
                        }

                        viewModelScope.launch {
                            saveInformationsDatabase()
                        }
                    }
                }.addOnFailureListener { _ ->
                    viewModelScope.launch(Dispatchers.Main) {
                        isPictureError.value = true
                    }
                    viewModelScope.launch {
                        userInfosHashMap["profilePictureUrl"] = ""
                        saveInformationsDatabase()
                    }
                }
            } else {
                viewModelScope.launch {
                    userInfosHashMap["profilePictureUrl"] = ""
                    saveInformationsDatabase()
                }
            }
        }
    }

    //Zorunlu kullanıcı bilgilerini kaydet
    private fun saveInformationsDatabase()
    {
        val userUid = userInfosHashMap["userUID"] as String
        val userRef = db.collection("Users").document(userUid)

        userRef.set(userInfosHashMap).addOnSuccessListener {

            // Room'daki eski verileri sil
            deleteOldUserInfoRoomDatabase()

            if (profilePictureBitmap != null) {
                userInfosHashMap["profilePictureByteArray"] = bitmapToByteArray(profilePictureBitmap)
            }

            // Yeni verileri toom'a kaydet
            saveUserInfoRoomDatabase(userInfosHashMap)
            viewModelScope.launch {
                isLoading.value = false
                navigateNextScreen.value = true
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
            val nickname = userInfosHashMap["nickname"]
            val birthDate = userInfosHashMap["birthDate"]
            val email = userInfosHashMap["email"]
            val userUID = userInfosHashMap["userUID"]
            val profilePictureUrl = userInfosHashMap["profilePictureUrl"]
            val profilePictureByteArray = userInfosHashMap["profilePictureByteArray"]
            val profilePictureUUID = userInfosHashMap["profilePictureUUID"]

            val saveUserInfoModel = SaveUserInfoModel(
                userName as String,
                nickname as String,
                birthDate as String,
                email as String,
                userUID as String,
                profilePictureUrl as String,
                profilePictureByteArray as ByteArray?,
                profilePictureUUID as String?
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

        this.userInfosHashMap =  userInfosHasMap
        this.profilePictureBitmap = profilePictureBitmap
        this.profilePictureUri = profilePictureUri

        trySaveUserInfoDatabase()
    }

}
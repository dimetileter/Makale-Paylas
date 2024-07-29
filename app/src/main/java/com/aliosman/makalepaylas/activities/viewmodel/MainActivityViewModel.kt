package com.aliosman.makalepaylas.activities.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.SaveUserInfoModel
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.BitmapToByteArray
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.android.material.color.utilities.DislikeAnalyzer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.random.Random

class MainActivityViewModel(private val application: Application): AndroidViewModel(application) {

    private val roomdb: UserInfoDatabase
    private val dao: UserInfoDAO

    private val auth: FirebaseAuth
    private val db: FirebaseFirestore
    private val storage: FirebaseStorage

    var isLoading = MutableLiveData<Boolean>()
    var userInfoList = MutableLiveData<ArrayList<Any?>>()
    var isProfileUpdate = MutableLiveData<Boolean>()


    init {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        roomdb = Room.databaseBuilder(getApplication(), UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
        db = FirebaseFirestore.getInstance()
    }

    fun getUserInfo(){
        tryGetUserInformationFromRoom()
    }

    fun updateProfilePicture(newPPBitmap: Bitmap, newPPUri: Uri)
    {
        val byteArray = BitmapToByteArray().bitmapToByteArray(newPPBitmap)
//        val byteArray = bitmapToByteArray(newPPBitmap) // Eğer burada hata olursa bu yorum satırını aç ve üstteki satırı sil
        saveNewProfilePictureIntoRoom(byteArray)
        saveNewProfilePictureIntoFirebase(newPPUri)
    }

    private fun tryGetUserInformationFromRoom()
    {
        isLoading.value = true

        viewModelScope.launch {
            val nickname = dao.getNickname()
            val profilePictureByteArray = dao.getProfilePicture()
            val userUUID = dao.getUserUUID()
            val currentUserUUID = auth.currentUser?.uid

            // Eğer veri tabanı silinmişse ya da kullanıcı değişmişse verileri internet ile güncelle
            if (nickname != null || userUUID == currentUserUUID)
            {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    userInfoList.value = arrayListOf(nickname, profilePictureByteArray)
                    //TODO: ALINAN VERİLER İLGİLİ SINIFA EKLENECEK
                    Toast.makeText(application,"Kullanıcı verileri room ile alındı. Profil resmi başarıyla alındı", Toast.LENGTH_LONG).show()
                }
            }
            else {
               tryGetUserInformationFromFirebase()
            }
        }
    }

    private fun tryGetUserInformationFromFirebase()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser?.uid
            val userRef = db.collection("Users").document(currentUser!!)
            val it = userRef.get().await()

            var nickname: String? = null
            var profilePictureByteArray: ByteArray? = null

                if (it.exists())
                {
                    val userName = it.getString("userName")
                    nickname = it.getString("nickname")
                    val birthDate = it.getString("birthDate")
                    val email = it.getString("email")
                    val userUID = it.getString("userUID")
                    val profilePictureUrl = it.getString("profilePictureUrl")
                    val profilePictureUUID = it.getString("profilePictureUUID") ?: ""

                    var profilePictureBitmap: Bitmap? = null
                    if (profilePictureUrl != "")
                    {
                        profilePictureBitmap = Picasso.get().load(profilePictureUrl).get()
                        profilePictureByteArray = BitmapToByteArray().bitmapToByteArray(profilePictureBitmap)
                        // Eğer burada hata olursa bu yorum satırını aç ve üstteki satırı sil
//                        profilePictureBitmap?.let {
//                            profilePictureByteArray = bitmapToByteArray(profilePictureBitmap)
//                        }

                    }
                    else {
                        profilePictureByteArray = null
                    }

                    deleteAllUserData()
                    val saveUserInfo = SaveUserInfoModel(
                        userName!!,
                        nickname!!,
                        birthDate!!,
                        email!!,
                        userUID!!,
                        profilePictureUrl!!,
                        profilePictureByteArray,
                        profilePictureUUID
                    )
                    dao.insertAll(saveUserInfo)
                }

            withContext(Dispatchers.Main) {
                isLoading.value = false
                userInfoList.value = arrayListOf(nickname!!, profilePictureByteArray)
                Toast.makeText(application,"Kullanıcı verileri internet ile alındı", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bitmapToByteArray(profilePictureBitmap: Bitmap): ByteArray
    {
        val outputStream = ByteArrayOutputStream()
        profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val bytePicture = outputStream.toByteArray()
        return bytePicture
    }

    private fun saveNewProfilePictureIntoRoom(byteArray: ByteArray)
    {
       viewModelScope.launch(Dispatchers.IO) {
           dao.updateProfilePicture(byteArray)
       }
    }

    private fun saveNewProfilePictureIntoFirebase(uri: Uri)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val userUuid = auth.currentUser!!.uid
            val email = auth.currentUser!!.email
            var imageUUID = getProfilePictureUUID() // null dönebilir

            if (imageUUID == null) {
                val uuid = UUID.randomUUID()
                imageUUID = "${uuid}.jpeg"
            }

            val referance = storage.reference
            val imageReferance =referance.child("ProfilePicures").child(email!!).child(imageUUID)

            imageReferance.putFile(uri).addOnSuccessListener {
                imageReferance.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        val newProfilePicture = it.result.toString()
                        val userRef = db.collection("Users").document(userUuid)

                        userRef.update("profilePictureUrl", newProfilePicture).addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.Main) {
                                isProfileUpdate.value = true
                            }
                        }.addOnFailureListener {
                            viewModelScope.launch(Dispatchers.Main) {
                                ToastMessages(application).showToastShort("Görsel firebase veri tabanına kaydedilemedi")
                            }
                        }
                    }
                    else
                    {
                        viewModelScope.launch(Dispatchers.Main) {
                            ToastMessages(application).showToastShort("Görsel firebase veri tabanına kaydedilemedi")
                        }
                    }
                }
            }
        }
    }

    private suspend fun getProfilePictureUUID(): String?
    {
       return dao.getProfilePictureUUID()
    }

    private fun deleteAllUserData()
    {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAll()
        }
    }

}
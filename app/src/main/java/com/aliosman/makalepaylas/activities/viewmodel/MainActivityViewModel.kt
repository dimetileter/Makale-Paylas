package com.aliosman.makalepaylas.activities.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.datatransfer.DataManager
import com.aliosman.makalepaylas.model.SaveUserInfoModel
import com.aliosman.makalepaylas.roomdb.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.UserInfoDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(private val application: Application): AndroidViewModel(application) {

    private val roomdb: UserInfoDatabase
    private val dao: UserInfoDAO

    private val auth: FirebaseAuth
    private val db: FirebaseFirestore

    var isLoading = MutableLiveData<Boolean>()
    var userInfoList = MutableLiveData<ArrayList<Any>>()


    init {
        auth = FirebaseAuth.getInstance()
        roomdb = Room.databaseBuilder(getApplication(), UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
        db = FirebaseFirestore.getInstance()
    }

    fun getUserInfo(){
        tryGetUserInformationFromRoom()
    }

    private fun tryGetUserInformationFromRoom()
    {
        isLoading.value = true

        viewModelScope.launch {
            val username = dao.getUserName()
            val profilePicture = dao.getProfilePicture()

            if (profilePicture != null) {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    userInfoList.value = arrayListOf(username, profilePicture)
                    //TODO: ALINAN VERİLER İLGİLİ SINIFA EKLENECEK
                    Toast.makeText(application,"Kullanıcı verileri room ile alındı. Profil resmi başarıyla alındı", Toast.LENGTH_LONG).show()
                }
            }
            else {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    userInfoList.value = arrayListOf(username, "null")
                    //TODO: ALINAN VERİLER İLGİLİ SINIFA EKLENECEK
                    Toast.makeText(application,"Kullanıcı verileri room ile alındı ama profil resmi alınmadı", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun tryGetUserInformationFromFirebase()
    {
        val currentUser = auth.currentUser?.uid

        db.collection("User").whereEqualTo("userUID", currentUser).get().addOnCompleteListener {
            if (it.isSuccessful && !it.result.isEmpty && it.result != null)
            {
                val documents = it.result
                for (docs in documents) {
                    val nickName = docs.getString("nickName") as String
                    val profilePictureUrl = docs.getString("profilePictureUrl") as String

                    /*
                    if (profilePictureUrl != "") {
                        Picasso.get().load(profilePictureUrl)
                    }
                    */
                    DataManager.nickname = nickName
                }
            }
        }
    }


}
package com.aliosman.makalepaylas.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.model.ProfilePagePdfInfo
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDAO
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDatabase
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.BitmapToByteArray
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfilePageViewModel(private val application: Application): AndroidViewModel(application) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val daoUserInfo: UserInfoDAO
    private val daoPdfList: TakenProfilePdfDAO

    private val takenPdfList = ArrayList<ProfilePagePdfInfo>()

    var takenPdf = MutableLiveData<ArrayList<ProfilePagePdfInfo>>()
    var isErrorP = MutableLiveData<Boolean>()
    var isLoadingP = MutableLiveData<Boolean>()
    var newProfilePicture = MutableLiveData<Bitmap>()
    var isDeleted = MutableLiveData<Boolean>()

    init {
        val database = Room.databaseBuilder(application, UserInfoDatabase::class.java, "UserInfos").build()
        daoUserInfo = database.userDao()

        val pdfDatabase= Room.databaseBuilder(application, TakenProfilePdfDatabase::class.java, "TakenProfilePdf").build()
        daoPdfList = pdfDatabase.userDao()
    }

    fun getPdfList() {
        getPdfRefListFromFirebase()
    }

    fun getPdfFromRoom() {
        getPdfListFromRoom()
    }

    fun deletePdf(pdfUUID: String) {
        deletePdfFromFirebase(pdfUUID)
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
                    val list = document.get("pdfRef") as? List<String>
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    if (list != null) {

                        // Verileri çekmeden önce room veri tabanını temizle
                        daoPdfList.delteAll()
                        for (docs in list) {
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
                            takenPdf.value = arrayListOf()
                            val msg = application.getString(R.string.toast_paylasim_yok)
                            ToastMessages(application).showToastLong(msg)
                            deleteAllRoom()
                        }
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        isLoadingP.value = false
                        takenPdf.value = arrayListOf()
                        val msg = application.getString(R.string.toast_paylasim_yok)
                        ToastMessages(application).showToastLong(msg)
                        deleteAllRoom()
                    }
                }
            }
            catch (e: Exception) {
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
                    val pdfBitmapUrl = document.getString("pdfBitmapUrl") ?: ""
                    val artName = document.getString("artName") ?: ""
                    val nickname = document.getString("nickName") ?: ""
                    val pdfUUID = document.getString("pdfUUID") ?: ""

                    val takenPdfInfo = ProfilePagePdfInfo(
                        artName,
                        pdfBitmapUrl,
                        nickname,
                        pdfUUID
                    )

                    // Alınan verileri array list ve room içine aktar
                    takenPdfList.add(takenPdfInfo)
                    daoPdfList.add(takenPdfInfo)

                }
                else {
                    withContext(Dispatchers.Main) {
                        isLoadingP.value = false
                        takenPdf.value = arrayListOf()
                        val msg = application.getString(R.string.toast_paylasim_yok)
                        ToastMessages(application).showToastLong(msg)
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

    private fun getPdfListFromRoom() {
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

    private fun deletePdfFromFirebase(pdfUUID: String) {
        isDeleted.value = false
        viewModelScope.launch(Dispatchers.IO) {
            val userUUID = daoUserInfo.getUserUUID()
            val userEmail = auth.currentUser!!.email.toString()
            val remove_value = pdfUUID

            try {
                // Kullanıcı listesinden kaldır
                val userRef = db.collection("Users").document(userUUID!!)
                userRef.update("pdfRef", FieldValue.arrayRemove(remove_value)).await()
                Log.d("Firebase referans silme", "Kaydedilen pdf referansı silindi")

                // Depodan pdf ve pdf kapak resmini kaldır
                val storageRef = storage.reference.child("PDF").child(userEmail).child(remove_value)
                storageRef.child(remove_value).delete().await()
                storageRef.child("pdfBitmap.jpeg").delete().await()
                Log.d("Firebase depodan silme", "Pdf verileri depodan silindi")

                // Posts dokümanından kaldır
                val postsRef = db.collection("Posts").document(pdfUUID)
                postsRef.delete().await()
                Log.d("Firebase koleksiyonlardan silme", "Pdf verileri koleksiyonlardan silindi")

                daoPdfList.deletPdf(pdfUUID)

                withContext(Dispatchers.Main) {
                    isDeleted.value = true
                }
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.w("Firebase saves silme hatası:", "Kaydedilen pdf silinirken hata oluştu", e)
                }
            }
        }
    }

    private suspend fun getUserUUID(): String? {
        return daoUserInfo.getUserUUID()
    }

    private fun deleteAllRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            daoPdfList.delteAll()
        }
    }
}
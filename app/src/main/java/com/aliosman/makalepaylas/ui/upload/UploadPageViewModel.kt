package com.aliosman.makalepaylas.ui.upload

import android.app.Application
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.roomdb.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.UserInfoDatabase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class UploadPageViewModel(private val application: Application): AndroidViewModel(application) {

    private var db: FirebaseFirestore
    private var storage: FirebaseStorage
    private var auth: FirebaseAuth
    private val roomdb: UserInfoDatabase
    private val dao: UserInfoDAO

    private var pdfUri: Uri? = null
    private var pdfBitmap: Bitmap? = null
    private var artName: String? = null
    private var artDesc: String? = null
    private var user: FirebaseUser? = null
    private var pdfNameWithUuid: String? = null

    var isLoading = MutableLiveData<Boolean>()
//  var isError = MutableLiveData<Boolean>()
    var isStorageError = MutableLiveData<Boolean>()
    var isDatabaseError = MutableLiveData<Boolean>()
    var isSuccessfull = MutableLiveData<Boolean>()

    init {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        roomdb = Room.databaseBuilder(getApplication(), UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
    }

    // Bir önceki fragmanda null kontrolleri yapıldı
    fun uploadChosenPDF(pdfUri: Uri, pdfBitmap: Bitmap?, artName: String, artDesc: String)
    {
        this.pdfUri = pdfUri
        this.pdfBitmap = pdfBitmap
        this.artName = artName
        this.artDesc = artDesc
        savePdfIntoStorage()
    }

    private fun savePdfIntoStorage()
    {
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {

            val uuid = UUID.randomUUID()
            pdfNameWithUuid = "${uuid}.pdf"
            val referance = storage.reference

            val pdfRef = referance.child("PDF").child(user!!.email!!).child(pdfNameWithUuid!!).child(pdfNameWithUuid!!)
            // Pdf'i yükle
            pdfRef.putFile(pdfUri!!).addOnSuccessListener {
                pdfRef.downloadUrl.addOnCompleteListener {
                    if(it.isSuccessful) {
                        viewModelScope.launch {
                            // Url linkini ve bilgileri kaydet
                            val pdfUrl = it.result.toString()
                            savePdfBitmap(pdfUrl)
                        }
                    }
                    else {
                        viewModelScope.launch(Dispatchers.Main) {
                            isLoading.value = false
                            isStorageError.value = true
                        }
                    }
                }
            }.addOnFailureListener { e->
                isStorageError.value = true
                Log.w(TAG, "PDF dosyası storage içine yüklenirken oluşan hata:", e)
            }
        }
    }

    // Pdf bitmapi'ni kaydet
    private fun savePdfBitmap(pdfUrl: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfPictureUri = bitmapToUri()
            val referance = storage.reference
            val pdfBitmapRef = referance.child("PDF").child(user!!.email!!).child(pdfNameWithUuid!!).child("pdfBitmap.jpeg")

            pdfBitmapRef.putFile(pdfPictureUri).addOnSuccessListener {
                pdfBitmapRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val pdfBitmapUrl = it.result.toString()
                        viewModelScope.launch {
                            savePDFDataIntoDatabase(pdfUrl, pdfBitmapUrl)
                        }
                    }
                }.addOnFailureListener { e->
                    Log.w(TAG, "PDF kapak resmi URL adresi alınırken oluşan hata:", e)
                }
            }.addOnFailureListener { e->
                Log.w(TAG, "PDF kapak resmi storage içine yüklenirken oluşan hata:", e)
            }
        }
    }

    // Pdf verilerini kaydet
    private suspend fun savePDFDataIntoDatabase(pdfUrl: String, pdfBitmapUrl: String)
    {
        val date = Timestamp.now().toDate()
        val time = SimpleDateFormat("dd.MM.yyy HH:mm", Locale.getDefault()).format(date)
        val nickname = getNickName()

        viewModelScope.launch(Dispatchers.IO) {

            val pdfInfos = HashMap<String, Any>()
            pdfInfos.put("pdfUrl", pdfUrl)
            pdfInfos.put("pdfBitmapUrl", pdfBitmapUrl)
            pdfInfos.put("artName", artName!!)
            pdfInfos.put("artDesc", artDesc!!)
            pdfInfos.put("createdAt", time)
            pdfInfos.put("user", user!!.email!!)
            pdfInfos.put("userUID", user!!.uid)
            pdfInfos.put("nickName", nickname)

            // PDF verilerini database içine koy
            val ref = db.collection("Posts").document(pdfNameWithUuid!!)
            ref.set(pdfInfos).addOnCompleteListener {
                if (it.isSuccessful) {
                    savePdfIntoUserInfo()
                    viewModelScope.launch(Dispatchers.Main) {
                        // Yükleme başarılı
                        isLoading.value = false
                        isSuccessfull.value = true
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        // Yükleme başarısız
                        isLoading.value = false
                        isDatabaseError.value = true
                        isSuccessfull.value = false
                    }
                }
            }.addOnFailureListener { e->
                Log.w(TAG, "Pdf bilgileri firedatabase kaydı yapılırken oluşan hata:", e)
            }
        }
    }

    // Pdf verilerini kişi üzerine kaydet
    private fun savePdfIntoUserInfo()
    {
        viewModelScope.launch {
            val userRef = db.collection("Users").document(user!!.uid)
            userRef.update("pdfRef", FieldValue.arrayUnion(pdfNameWithUuid)).addOnFailureListener { e->
                Log.w(TAG, "Pdf bilgilerini kişi üzerine yazarken hata:", e)
            }
        }
    }

    private fun bitmapToUri(): Uri {
        val file = File(application.cacheDir, "bitmap_image.jpg") // Dosya adı
        val outputStream = FileOutputStream(file)
        pdfBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        val uri = Uri.fromFile(file)
        return uri
    }

    private suspend fun getNickName(): String
    {
        return dao.getNickname()
    }

}
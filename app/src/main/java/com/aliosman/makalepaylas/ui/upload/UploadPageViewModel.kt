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
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
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
    private var categories: ArrayList<String>? = null
    private var user: FirebaseUser? = null
    private var pdfNameWithUuid: String? = null

    var isLoading = MutableLiveData<Boolean>()
//  var isError = MutableLiveData<Boolean>()
    var isSuccessful = MutableLiveData<Boolean>()

    init {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        roomdb = Room.databaseBuilder(getApplication(), UserInfoDatabase::class.java, "UserInfos").build()
        dao = roomdb.userDao()
    }

    // Bir önceki fragmanda null kontrolleri yapıldı
    fun uploadChosenPDF(pdfUri: Uri, pdfBitmap: Bitmap?, artName: String, artDesc: String, categories: ArrayList<String>)
    {
        this.categories = categories
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
                        }
                        Log.w(TAG, "PDF dosyası URL adresi alınırken oluşan hata:", it.exception)
                    }
                }
            }.addOnFailureListener { e->
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
        val time = Timestamp.now()
        //val time = SimpleDateFormat("dd.MM.yyy HH:mm", Locale.getDefault()).format(date)
        val nickname = getNickName()

        viewModelScope.launch(Dispatchers.IO) {

            val pdfInfos = HashMap<String, Any?>()
            pdfInfos["pdfUrl"] = pdfUrl
            pdfInfos["pdfBitmapUrl"] = pdfBitmapUrl
            pdfInfos["artName"] = artName!!
            pdfInfos["artDesc"] = artDesc!!
            pdfInfos["createdAt"] = time
            pdfInfos["user"] = user!!.email!!
            pdfInfos["userUID"] = user!!.uid
            pdfInfos["nickName"] = nickname
            pdfInfos["pdfUUID"] = pdfNameWithUuid!!
            pdfInfos["categories"] = categories!!

            // PDF verilerini database içine koy
            val ref = db.collection("Posts").document(pdfNameWithUuid!!)
            ref.set(pdfInfos).addOnCompleteListener {
                if (it.isSuccessful) {
                    savePdfIntoUserInfo()
                    viewModelScope.launch(Dispatchers.Main) {
                        // Yükleme başarılı
                        isLoading.value = false
                        isSuccessful.value = true
                        isSuccessful.value = false
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        // Yükleme başarısız
                        isLoading.value = false
                        isSuccessful.value = false
                    }
                    Log.w(TAG, "Pdf bilgileri database kaydı yapılırken oluşan hata:", it.exception)
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

    private suspend fun getNickName(): String?
    {
        return dao.getNickname()
    }

}
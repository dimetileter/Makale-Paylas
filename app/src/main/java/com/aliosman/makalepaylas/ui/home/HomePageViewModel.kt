package com.aliosman.makalepaylas.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel
import com.aliosman.makalepaylas.model.HomePagePdfInfo
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDAO
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDatabase
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale


class HomePageViewModel(private val application: Application): AndroidViewModel(application) {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var roomDb: TakenHomePdfDatabase
    private var dao: TakenHomePdfDAO
    private var takenPdfList = ArrayList<HomePagePdfInfo>()

    var pdfList = MutableLiveData<ArrayList<HomePagePdfInfo>>()
    var isLoadingH = MutableLiveData<Boolean>()
    var isErrorH = MutableLiveData<Boolean>()

    init {
        roomDb = Room.databaseBuilder(application, TakenHomePdfDatabase::class.java, "TakenHomePdf").build()
        dao = roomDb.userDao()
    }

    fun getPdfDataInternet() {
        getPdfDataFromInternet()
    }

    fun getPdfDataRoom() {
        getPdfDataFromRoom()
    }

    private fun getPdfDataFromInternet() {
        isLoadingH.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {

            try {
                // Firebase sorgusunu oluştur
                val pdfRef = db.collection("Posts").orderBy("createdAt", Query.Direction.DESCENDING)
                val document = pdfRef.get().await()

                if (!document.isEmpty) {
                    // Dokümanları al
                    val documents = document.documents
                    if (documents.isNotEmpty()) {
                        for (docs in documents) {
                            val artName = docs.getString("artName") ?: ""
                            val nickName = docs.getString("nickName") ?: ""
                            val pdfUrl = docs.getString("pdfBitmapUrl")
                            val pdfUUID = docs.getString("pdfUUID")

                            val pdfData = HomePagePdfInfo(
                                artName,
                                nickName,
                                pdfUrl,
                                pdfUUID!!
                            )
                            takenPdfList.add(pdfData)
                        }

                        // UI güncellemesi
                        withContext(Dispatchers.Main) {
                            isLoadingH.value = false
                            pdfList.value = takenPdfList
                        }
                    }
                    else {
                        // Dokümanlar boşsa UI güncellemesi
                        withContext(Dispatchers.Main) {
                            isLoadingH.value = false
                            pdfList.value = takenPdfList
                            val msg = application.getString(R.string.toast_paylasim_yok)
                            ToastMessages(application).showToastShort(msg)
                        }
                    }
                }
                else {
                    // Koleksiyon boşsa UI güncellemesi
                    withContext(Dispatchers.Main) {
                        isLoadingH.value = false
                        pdfList.value = takenPdfList
                        val msg = application.getString(R.string.toast_paylasim_yok)
                        ToastMessages(application).showToastShort(msg)
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseFirestore", "Dokümanlar alınırken oluşan hata: ", e)
            }
        }
    }

    private fun getDocumentInfos(docs: DocumentSnapshot) {
        viewModelScope.launch(Dispatchers.IO) {

            val artName = docs.getString("artName") ?: ""
            val artDesc = docs.getString("artDesc") ?: ""
            val pdfUrl = docs.getString("pdfUrl") ?: ""
            val pdfBitmapUrl = docs.getString("pdfBitmapUrl") ?: ""
            val createdAt = docs.get("createdAt") as Timestamp
            val nickname = docs.getString("nickName") ?: ""
            val pdfUUID = docs.getString("pdfUUID") ?: ""

            val date = createdAt.toDate()
            val createdAtString = SimpleDateFormat("dd.MM.yyy - HH:mm", Locale.getDefault()).format(date)

            val getHomePdfList = GetHomePdfInfoHModel (
                artName,
                artDesc,
                pdfUrl,
                pdfBitmapUrl,
                createdAtString,
                nickname,
                pdfUUID
            )
            //takenPdfList.add(getHomePdfList)
            //dao.add(getHomePdfList)
        }
    }

    private fun getPdfDataFromRoom() {
        isLoadingH.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val data = dao.getAll()
            //takenPdfList.addAll(data)

            withContext(Dispatchers.Main) {
                isLoadingH.value = false
                pdfList.value = takenPdfList // RecyclerView'i güncelleyin
            }
        }
    }

    private fun deleteAllRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delteAll()
        }
    }
}
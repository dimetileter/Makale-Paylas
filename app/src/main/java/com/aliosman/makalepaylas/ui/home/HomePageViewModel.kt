package com.aliosman.makalepaylas.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDAO
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomePageViewModel(private val application: Application): AndroidViewModel(application) {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var roomDb: TakenHomePdfDatabase
    private var dao: TakenHomePdfDAO
    private var takenPdfList = ArrayList<GetHomePdfInfoHModel>()

    var pdfList = MutableLiveData<ArrayList<GetHomePdfInfoHModel>>()
    var isLoadingH = MutableLiveData<Boolean>()
    var isErrorH = MutableLiveData<Boolean>()

    init {
        roomDb = Room.databaseBuilder(application, TakenHomePdfDatabase::class.java, "TakenHomePdf").build()
        dao = roomDb.userDao()
    }

    fun getPdfDataInternet()
    {
        getPdfDataFromInternet()
    }

    fun getPdfDataRoom()
    {
        getPdfDataFromRoom()
    }

    private fun getPdfDataFromInternet()
    {
        isLoadingH.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            // Firebase sorgusunu oluştur
            val pdfRef = db.collection("Posts").orderBy("createdAt", Query.Direction.DESCENDING)

            //Firebase firestore'dan verileri getir
            pdfRef.get().addOnSuccessListener { querySnapshot ->
                if(querySnapshot != null && !querySnapshot.isEmpty)
                {
                    // deleteData()
                    val documents = querySnapshot.documents
                    for (docs in documents)
                    {
                        getDocumanInfos(docs)
                    }
                    saveDataIntoRoom(takenPdfList)

                    viewModelScope.launch(Dispatchers.Main) {
                        isLoadingH.value = false
                        pdfList.value = takenPdfList
                    }
                }
                else
                {
                    viewModelScope.launch(Dispatchers.Main) {
                        isLoadingH.value = false
                        isErrorH.value = true
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch(Dispatchers.Main) {
                    isLoadingH.value = false
                    isErrorH.value = true
                }
            }
        }
    }

    private fun getDocumanInfos(docs: DocumentSnapshot)
    {
        viewModelScope.launch(Dispatchers.IO) {

            val artName = docs.getString("artName") ?: ""
            val artDesc = docs.getString("artDesc") ?: ""
            val pdfUrl = docs.getString("pdfUrl") ?: ""
            val pdfBitmapUrl = docs.getString("pdfBitmapUrl") ?: ""
            val createdAt = docs.getString("createdAt") ?: ""
            val nickname = docs.getString("nickName") ?: ""
            val pdfUUID = docs.getString("pdfUUID") ?: ""

            val getHomePdfList = GetHomePdfInfoHModel (
                artName,
                artDesc,
                pdfUrl,
                pdfBitmapUrl,
                createdAt,
                nickname,
                pdfUUID
            )
            takenPdfList.add(getHomePdfList)
        }
    }

    private fun getPdfDataFromRoom()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val data = dao.getAll()
            withContext(Dispatchers.Main) {
                takenPdfList.clear() // Önce listeyi temizleyin
                takenPdfList.addAll(data)
                pdfList.value = ArrayList(takenPdfList) // RecyclerView'i güncelleyin
                isLoadingH.value = false
            }
        }
    }

    private fun saveDataIntoRoom(takenPdfList: List<GetHomePdfInfoHModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delteAll()
            dao.add(takenPdfList)
        }
    }
}
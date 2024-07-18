package com.aliosman.makalepaylas.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel
import com.aliosman.makalepaylas.roomdb.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.UserInfoDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomePageViewModel(application: Application): AndroidViewModel(application) {

    private var db: FirebaseFirestore
    private var takenPdfList = ArrayList<GetHomePdfInfoHModel>()

    var pdfList = MutableLiveData<ArrayList<GetHomePdfInfoHModel>>()
    var isLoading = MutableLiveData<Boolean>()
    var isError = MutableLiveData<Boolean>()

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getPdfData()
    {
        getPdfDataFromInternet()
    }

    fun getPdfDataRoom()
    {
        getPdfDataFromRoom()
    }

    private fun getPdfDataFromInternet()
    {
        isLoading.value = true
        takenPdfList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            // Firebase sorgusunu oluştur
            val pdfRef = db.collection("Posts").orderBy("createdAt", Query.Direction.DESCENDING)

            //Firebase firestore'dan verileri getir
            pdfRef.addSnapshotListener { querySnapshot, exception ->
                if(querySnapshot != null && exception == null)
                {
                    if (!querySnapshot.isEmpty)
                    {
                        val documents = querySnapshot.documents
                        for (docs in documents) {
                            getDocumanInfos(docs)
                        }
                    }
                    else {
                        viewModelScope.launch(Dispatchers.Main) {
                            isLoading.value = false
                            isError.value = true
                        }
                    }
                }
                else {
                    viewModelScope.launch(Dispatchers.Main) {
                        isLoading.value = false
                        isError.value = true
                    }
                }
            }
        }
    }

    private fun getPdfDataFromRoom()
    {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    private fun getDocumanInfos(docs: DocumentSnapshot)
    {
        viewModelScope.launch(Dispatchers.IO) {

            val artName = docs.get("artName") as String
            val artDesc = docs.get("artDesc") as String
            val pdfUrl = docs.get("pdfUrl") as String
            val pdfBitmapUrl = docs.get("pdfBitmapUrl") as String?
            val createdAt = docs.get("createdAt") as String
            val nickname = docs.get("nickName") as String

            val getHomePdfList = GetHomePdfInfoHModel (
                artName,
                artDesc,
                pdfUrl,
                pdfBitmapUrl,
                createdAt,
                nickname
            )

            takenPdfList.add(getHomePdfList)
            withContext(Dispatchers.Main) {
                isLoading.value = false
                pdfList.value = takenPdfList
            }
        }
    }
}
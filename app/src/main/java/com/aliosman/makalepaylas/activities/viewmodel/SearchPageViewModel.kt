package com.aliosman.makalepaylas.activities.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliosman.makalepaylas.activities.SearchPageActivity
import com.aliosman.makalepaylas.model.PublicModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPageViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var resultList = ArrayList<PublicModel>()

    val searchResult = MutableLiveData<ArrayList<PublicModel>>()

    fun search(searchText: String) {
        searchQuery(searchText)
    }

    private fun searchQuery(searchText: String) {
        resultList.clear()
        viewModelScope.launch(Dispatchers.IO) {
            // Sorguyu oluştur
            val ref = db.collection("Posts")
                .orderBy("artName")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()

            // Sorgu sonucunu gözlemle
            ref.addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    val document = query.documents
                    for (docs in document) {
                        val artName = docs.getString("artName")
                        val nickname = docs.getString("nickName")
                        val pdfUUID = docs.getString("pdfUUID")
                        val pdfBitmapUrl = docs.getString("pdfBitmapUrl")

                        // Verilerin doluluğunu kontrol et
                        val bool1 = !artName.isNullOrEmpty() && !artName.isNullOrEmpty()
                        val bool2 = !pdfUUID.isNullOrEmpty() && !pdfBitmapUrl.isNullOrEmpty()
                        if (bool1 && bool2) {
                            val list = PublicModel(pdfUUID!!, artName!!, nickname!!, pdfBitmapUrl!!)
                            if (!resultList.contains(list)) {
                                resultList.add(list)
                            }
                        }
                        else {
                            continue
                        }
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        searchResult.value = resultList
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("Firebase Sorgu Hatası", "Arama sorgusu yapılırken oluşan oluştu: ", e)
            }
        }
    }

}
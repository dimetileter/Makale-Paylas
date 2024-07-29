package com.aliosman.makalepaylas.activities.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliosman.makalepaylas.model.SavesPdfModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SavesPageViewModel: ViewModel() {

    private var db: FirebaseFirestore
    private var auth: FirebaseAuth
    private var takenSavesList = ArrayList<SavesPdfModel>()

    var savesList = MutableLiveData<ArrayList<SavesPdfModel>>()

    init {
        db = Firebase.firestore
        auth = Firebase.auth
    }

    fun getSaves()
    {
        getSavesFromFirebase()
    }

    private fun getSavesFromFirebase()
    {
        takenSavesList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            val userUUID = auth.currentUser!!.uid
            val ref = db.collection("Users").document(userUUID)
            val documents = ref.get().await()

            if (documents.exists())
            {
                val documents = documents.get("saves") as? List<String>
                if (documents != null )
                {
                    for (docs in documents)
                    {
                        getPdfInfo(docs)
                    }

                    withContext(Dispatchers.Main) {
                        savesList.value = takenSavesList
                    }
                }
            }
        }
    }

    private suspend fun getPdfInfo(docs: String)
    {
        val getPdfRef = db.collection("Posts").document(docs)
        val document = getPdfRef.get().await()

        if (document.exists()) {
            val pdfUrl = document.getString("pdfUrl") ?: ""
            val pdfBitmapUrl = document.getString("pdfBitmapUrl") ?: ""
            val pdfName = document.getString("artName") ?: ""
            val pdfDesc = document.getString("artDesc") ?: ""
            val createdAt = document.getString("createdAt") ?: ""
            val nickname = document.getString("nickName") ?: ""
            val pdfUUID = document.getString("pdfUUID") ?: ""

            val saves = SavesPdfModel(
                pdfName,
                pdfDesc,
                pdfUrl,
                pdfBitmapUrl,
                createdAt,
                nickname,
                pdfUUID
            )

            takenSavesList.add(saves)

        }
    }
}
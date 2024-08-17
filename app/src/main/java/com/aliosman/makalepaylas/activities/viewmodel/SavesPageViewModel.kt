package com.aliosman.makalepaylas.activities.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliosman.makalepaylas.model.SavesPdfModel
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class SavesPageViewModel: ViewModel() {

    private var db: FirebaseFirestore
    private var auth: FirebaseAuth
    private var takenSavesList = ArrayList<SavesPdfModel>()

    var savesList = MutableLiveData<ArrayList<SavesPdfModel>>()
    var isSuccess = MutableLiveData<Boolean>()
    var isLoading = MutableLiveData<Boolean>()

    init {
        db = Firebase.firestore
        auth = Firebase.auth
    }

    fun getSaves()
    {
        getSavesFromFirebase()
    }

    fun deleteSave(pdfUUID: String)
    {
        deleteSaveFromFirebase(pdfUUID)
    }

    private fun getSavesFromFirebase()
    {
        isLoading.value = true
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
                        isLoading.value = false
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
            val createdAt = document.get("createdAt") as Timestamp
            val nickname = document.getString("nickName") ?: ""
            val pdfUUID = document.getString("pdfUUID") ?: ""

            val date = createdAt.toDate()
            val createdAtString = SimpleDateFormat("dd.MM.yyy - HH:mm", Locale.getDefault()).format(date)

            val saves = SavesPdfModel(
                pdfName,
                pdfDesc,
                pdfUrl,
                pdfBitmapUrl,
                createdAtString,
                nickname,
                pdfUUID
            )

            takenSavesList.add(saves)

        }
        else {
            // Eğer bu pdf kaldırılmışsa kullanıcının kaydedilenlerinden de sil
            deleteSaveFromFirebase(docs)
        }
    }

    private fun deleteSaveFromFirebase(pdfUUID: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val userUUID = auth.currentUser!!.uid
            val remove_value = pdfUUID

            try {
                val ref = db.collection("Users").document(userUUID)
                ref.update("saves", FieldValue.arrayRemove(remove_value)).await()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSuccess.value = false
                    Log.w("Firebase saves silme hatası:", "Kaydedilen pdf silinirken hata oluştu", e)
                }
            }
        }
    }
}
package com.aliosman.makalepaylas.activities

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.DownloadPageViewModel
import com.aliosman.makalepaylas.databinding.ActivityDownloadPageBinding
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.downloadImage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DownloadPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadPageBinding
    private var pdfInfo: ArrayList<String>? = null

    var pdfUrl: String? = null
    var pdfDownloadName: String? = null
    var pdfUUID: String? = null
    private lateinit var viewModel: DownloadPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDownloadPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ögelerin tanımlanması
        val artTitle = binding.txtDownloadPageArticleTitle
        val artCover = binding.downlodPagePdfCoverImage
        val authorName = binding.txtDownloadPageAuthorName
        val createdAt = binding.downloadPageDate
        val artDesc = binding.txtDownloadArticleDescription
        var pdfBitmapUrl: String? = null


        // Gönderilern pdf verilerini al ve ilgili değişkenlere aktar
        val bundle = intent.extras
        bundle?.let {
            artTitle.text = it.getString("artName")
            pdfDownloadName = it.getString("artName")

            artDesc.text = it.getString("artDesc")
            createdAt.text = it.getString("createdAt")
            authorName.text = it.getString("nickname")
            pdfBitmapUrl = it.getString("pdfBitmapUrl")
            pdfUUID = it.getString("pdfUUID")
            pdfUrl = it.getString("pdfUrl")
        }

        CoroutineScope(Dispatchers.Main).launch {
            pdfBitmapUrl?.let {
               artCover.downloadImage(it)
            }
        }

        viewModel = ViewModelProvider(this)[DownloadPageViewModel::class.java]

    }

    //İndirme butonu
    fun download_button(view: View)
    {
        // Eğer pdf ismi boş ise rastgele bir isim ver
        val pdfName = pdfDownloadName
        if (pdfName == null)
        {
            pdfDownloadName = UUID.randomUUID().toString()
        }

        // Url alınmışsa indirme işlemine başla
        if(pdfUrl != null) {

            downloadPdf(this, pdfUrl!!, pdfDownloadName!!)
        }
        else {
            val msg = getString(R.string.toast_pdf_url_null)
            ToastMessages(this).showToastShort(msg)
        }
    }

    //Paylaşma butonu
    fun share_download_button(view: View)
    {
        Toast.makeText(this, "Paylaşma işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }

    //Kaydetme butonu
    fun save_button(view: View)
    {
        observer()
        pdfUUID?.let {
            viewModel.addPdfIntoSavesList(it)
        }
    }

    private fun observer()
    {
        viewModel.isSuccess.observe(this) {
            // Butonun simgesini dolu bayrak ile değiştir
            val savesButton = binding.saveButton
            savesButton.setImageResource(R.drawable.ic_saved)
            // Mesajları göster
            val msg = getString(R.string.toast_kaydedildi)
            ToastMessages(this).showToastShort(msg)
            // TODO: Yanlışlıkla kaydedilmeleri önlemek için bir sncak bar ile geri alınsın mı mesajı gösterilebilir
        }

        viewModel.isError.observe(this) {
            val msg = getString(R.string.toast_kaydedilemedi)
            ToastMessages(this).showToastShort(msg)
        }
    }

    private fun downloadPdf(context: Context, pdfUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
        request.setTitle("PDF İndiriliyor")
        request.setDescription("Lütfen bekleyin...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        val msg = getString(R.string.toast_indirme_baslatildi)
        ToastMessages(this).showToastShort(msg)
    }

}
package com.aliosman.makalepaylas.activities

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.text.style.StyleSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.DownloadPageViewModel
import com.aliosman.makalepaylas.databinding.ActivityDownloadPageBinding
import com.aliosman.makalepaylas.roomdb.homeroom.TakenHomePdfDatabase
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.downloadImage
import com.aliosman.makalepaylas.util.isInternetAvailable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class DownloadPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadPageBinding
    private val REQUEST_WRITE_STORAGE = 112

    private var pdfUrl: String? = null
    private var pdfDownloadName: String? = null
    private var pdfUUID: String? = null
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
        var pdfBitmapUrl: String? = null

        // Gönderilern pdf verilerini al ve ilgili değişkenlere aktar
        val bundle = intent.extras
        bundle?.let {
            binding.txtArticleTitle.text = it.getString("artName")
            binding.txtAuthorName.text = it.getString("nickName")
            pdfDownloadName = it.getString("artName")
            pdfUUID = it.getString("pdfUUID")
            pdfBitmapUrl = it.getString("pdfBitmapUrl")
        }

        viewModel = ViewModelProvider(this)[DownloadPageViewModel::class.java]
        viewModel.getPdfInfoFromFirebase(pdfUUID!!)
        observer()

        val artCover = binding.downlodPagePdfCoverImage
        CoroutineScope(Dispatchers.Main).launch {
            pdfBitmapUrl?.let {
               artCover.downloadImage(it)
            }
        }

        // Pdf açıklamasını kaydırılabilir yap
        binding.txtArticleDescription.setMovementMethod(ScrollingMovementMethod())
        binding.downloadButton.setOnClickListener {
            // Medyaya yaz izni
            checkPermissions()
        }
    }

    //İndirme butonu
    private fun startDownloadPdf() {
        // Eğer pdf ismi boş ise rastgele bir isim ver
        val pdfName = pdfDownloadName
        if (pdfName == null) {
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
    fun share_download_button(view: View) {
        sharePdfByUrl()
    }

    //Kaydetme butonu
    fun save_button(view: View) {
        pdfUUID?.let {
            viewModel.addPdfIntoSavesList(it)
        }
    }

    private fun observer() {
        viewModel.isSuccess.observe(this) {
            // Butonun simgesini dolu bayrak ile değiştir
            val savesButton = binding.saveButton
            savesButton.setImageResource(R.drawable.ic_saved)

            // Tost mesajı göster
            val msg = getString(R.string.toast_kaydedildi)
            ToastMessages(this).showToastShort(msg)

            // Snackbar ile geri al ifadesi göster
            val snackmsg = getString(R.string.toast_geri_al)
            snackbar(binding.root, snackmsg)
        }

        viewModel.isError.observe(this) {
            val msg = getString(R.string.toast_kaydedilemedi)
            ToastMessages(this).showToastShort(msg)
        }

        viewModel.data.observe(this) {

            if (it.isEmpty()) {
                binding.downloadButtonCardView.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
                binding.downloadPageShareButton.visibility = View.GONE

                val msg = application.getString(R.string.toast_pdf_erisimi_yok)
                ToastMessages(application).showToastShort(msg)

                // Kaldırılmış olan pdf'i veri tabanından temizle
                val room = Room.databaseBuilder(this, TakenHomePdfDatabase::class.java, "TakenHomePdf").build()
                val dao = room.userDao()
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deletePdf(pdfUUID!!)
                }
            }
            else {
                pdfUrl = it[0] // Url'yi al
                binding.txtArticleDescription.text = it [1]
                binding.downloadPageDate.text = it[2] // Tarihi al
                binding.txtCategories.text = it[3]
            }
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

    private fun sharePdfByUrl() {
        val url = this.pdfUrl
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        val msg = getString(R.string.pdf_baglantisini_paylas)
        this.startActivity(Intent.createChooser(shareIntent, msg))
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // İzin verilmemişse izin iste
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE)
        } else {
            // İzin verilmişse indirmeyi başlat
            // İnternet bağlantısını kontrol et
            if (isInternetAvailable(this)) {
                startDownloadPdf()
            }
            else {
                val msg = getString(R.string.toast_internet_baglantisi_yok)
                ToastMessages(this).showToastShort(msg)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // İzin verildiyse ve internet varsa indirmeyi başlat
                    if (isInternetAvailable(this)) {
                        startDownloadPdf()
                    }
                    else {
                        val msg = getString(R.string.toast_internet_baglantisi_yok)
                        ToastMessages(this).showToastShort(msg)
                    }
                } else {
                    // İzin verilmediyse kullanıcıyı bilgilendir
                    val msg = getString(R.string.toast_permission_denied)
                    ToastMessages(this).showToastShort(msg)
                }
            }
        }
    }

    private fun snackbar(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.WHITE)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setTextColor(Color.BLACK)
            .setActionTextColor(Color.BLUE)
            .setAction("Ok", View.OnClickListener {
                viewModel.deletePdfFromSavesList(pdfUUID!!)
                val savesButton = binding.saveButton
                savesButton.setImageResource(R.drawable.ic_save)
            }).show()
    }
}

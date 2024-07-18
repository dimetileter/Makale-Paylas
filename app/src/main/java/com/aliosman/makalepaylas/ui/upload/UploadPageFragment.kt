package com.aliosman.makalepaylas.ui.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentUploadPageBinding
import com.aliosman.makalepaylas.util.ToastMessages

class UploadPageFragment : Fragment() {

    private var _binding: FragmentUploadPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UploadPageViewModel
    private lateinit var toastMessages: ToastMessages

//    private val STORAGE_PERMISSION_CODE = 101
    private val FILE_SELECT_CODE = 102
    private var pdfUri: Uri? = null
    private var pdfBitmap: Bitmap? = null
    private var articleName: String? = null
    private var articleDescription: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        toastMessages = ToastMessages(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadPageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UploadPageViewModel::class.java]

        binding.chooseFile.setOnClickListener {
            showFileChooser()
        }

        binding.shareButton.setOnClickListener {

            articleName = binding.paylasilanMakaleAdi.text.toString()
            articleDescription = binding.paylasilanMakaleAciklamasi.text.toString()

            // TODO: İnternet bağlantısı kontrolü ekle
            if(pdfUri == null) {
                val msg = getString(R.string.secim_yailmadi)
                toastMessages.showToastShort(msg)
            }
            else if (articleName.isNullOrEmpty() || articleDescription.isNullOrEmpty()) {
                val message = getString(R.string.yukleme_bilgilerini_giriniz)
                toastMessages.showToastShort(message)
            }
            else {
                binding.shareButton.isEnabled = false
                startSharing()

            }
        }
        observer()
    }

    /*private fun checkMediaFilePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            showFileChooser()
        }
    }*/

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFileChooser()
            } else {
                Toast.makeText(requireContext(), "Depolama izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }
    }
     */

    // View modeli kullanarak paylaşımı başlat
    private fun startSharing()
    {
         viewModel.uploadChosenPDF(pdfUri!!, pdfBitmap, articleName!!, articleDescription!!)
    }

    private fun observer()
    {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                val msg = getString(R.string.toast_yukleniyor)
                toastMessages.showToastShort(msg)
                binding.chooseFile.isEnabled = false
                binding.paylasilanMakaleAdi.isEnabled = false
                binding.paylasilanMakaleAciklamasi.isEnabled = false
                binding.loadingScreen.visibility = View.VISIBLE
            } else {
                someSettings()
            }
        }

        viewModel.isStorageError.observe(viewLifecycleOwner) {
            if (it)
            {
                val msg = getString(R.string.toast_pdf_yuklenmedi)
                toastMessages.showToastShort(msg)
                someSettings()
            }
        }

        viewModel.isDatabaseError.observe(viewLifecycleOwner) {
            if (it)
            {
                val msg = getString(R.string.toast_pdf_yulendi_veri_yuklenmedi)
                toastMessages.showToastShort(msg)
                someSettings()
            }
        }

        // TODO: Herhangi bir başarılı yükleme sonrasında upload sayfasına her
        //  gelindiğinde yüklendi mesajı gösteriliyor.
        viewModel.isSuccessfull.observe(viewLifecycleOwner) {
            if (it) {
                val msg = getString(R.string.toast_yuklendi)
                toastMessages.showToastShort(msg)
                someSettings()
            }
        }
    }

    private fun someSettings() {

        val artNameTxt = binding.paylasilanMakaleAdi
        val artDescTxt = binding.paylasilanMakaleAciklamasi
        val pdfImage = binding.chooseFile

        artNameTxt.isEnabled = true
        artNameTxt.text = null

        artDescTxt.isEnabled = true
        artDescTxt.text = null

        pdfImage.setImageResource(R.drawable.ic_add_circle_plus)
        pdfImage.isEnabled = true

        binding.shareButton.isEnabled = true
        binding.loadingScreen.visibility = View.GONE
    }

//===================================PDF-SEÇİM-İŞLEMLERİ============================================
    // Dosya seçiçiyi çalıştır
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Bir dosya seçin"), FILE_SELECT_CODE)
    }

    // Dosya seçimi için ilgili ekrana git
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                pdfUri = uri
                handleFile(pdfUri!!)
            }
        }
    }

    // Alınan dosyanın türünü kontrol et
    private fun handleFile(uri: Uri) {
        val fileType = requireContext().contentResolver.getType(uri)
        if (fileType == "application/pdf" || fileType == "text/plain") {
            renderPdf(uri)
        } else {
            Toast.makeText(requireContext(), "Lütfen bir PDF veya metin dosyası seçin", Toast.LENGTH_SHORT).show()
        }
    }

    // Alınan pdf'in ilk sayfasını bitmap olarak çevir
    private fun renderPdf(uri: Uri) {
        val fileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r") ?: return
        val pdfRenderer = PdfRenderer(fileDescriptor)

        if (pdfRenderer.pageCount > 0) {
            val page = pdfRenderer.openPage(0)
            val width = page.width
            val height = page.height

            this.pdfBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfBitmap?.let {
                page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                binding.chooseFile.setImageBitmap(it)  // Örneğin, bir ImageView'a yüklemek için
                page.close()
            }
        }

        pdfRenderer.close()
        fileDescriptor.close()
    }
//==================================================================================================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
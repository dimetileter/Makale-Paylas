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
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentUploadPageBinding
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.isInternetAvailable
import com.aliosman.makalepaylas.util.progressBarDrawable

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
            val chosenList = checkboxChecker()

            // TODO: İnternet bağlantısı kontrolü ekle
            if(pdfUri == null) {
                val msg = getString(R.string.secim_yailmadi)
                toastMessages.showToastShort(msg)
            }
            else if (articleName.isNullOrEmpty() || articleDescription.isNullOrEmpty()) {
                val message = getString(R.string.yukleme_bilgilerini_giriniz)
                toastMessages.showToastShort(message)
            }
            else if (chosenList .isNullOrEmpty())
            {
                val msg = getString(R.string.toast_en_az_uc_kategori)
                ToastMessages(requireContext()).showToastShort(msg)
            }
            else {
                if (isInternetAvailable(requireContext())) {
                    binding.shareButton.isEnabled = false
                    startSharing(chosenList)
                }
                else {
                    val msg = getString(R.string.toast_internet_baglantisi_yok)
                    toastMessages.showToastShort(msg)
                }
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
    private fun startSharing(chosenCategories: ArrayList<String>) {
         viewModel.uploadChosenPDF(pdfUri!!, pdfBitmap, articleName!!, articleDescription!!, chosenCategories)
    }

    // Gözlemci
    private fun observer() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.chooseFile.isEnabled = false
                binding.paylasilanMakaleAdi.isEnabled = false
                binding.paylasilanMakaleAciklamasi.isEnabled = false
                binding.shareButton.isEnabled = false
                binding.progressBar.setImageDrawable(progressBarDrawable(requireContext()))
                binding.loadingScreen.visibility = View.VISIBLE
            } else {
                someSettings()
            }
        }

        // TODO: Herhangi bir başarılı yükleme sonrasında upload sayfasına her
        //  gelindiğinde yüklendi mesajı gösteriliyor.
        viewModel.isSuccessful.observe(viewLifecycleOwner) {
            if (it) {
                val msg = getString(R.string.toast_yuklendi)
                toastMessages.showToastShort(msg)
                someSettings()
            }
        }
    }

    // Bazı ayarlar
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

    // CheckBox kontrolleri
    private fun checkboxChecker(): ArrayList<String> {
        val box1 = binding.checkBoxFelsefe
        val box2 = binding.checkBoxKisisel
        val box3 = binding.checkBoxPolitik
        val box4 = binding.checkBoxTeknoloji
        val box5 = binding.checkBoxTeoloji
        val box6 = binding.checkBoxAkademik
        val box7 = binding.checkBoxElestiri

        val list = listOf(box1, box2, box3, box4, box5, box6, box7)
        val checkedList = list.filter { it.isChecked }

        val chosenCategories = ArrayList<String>()
        if (checkedList.size >= 3) {
            checkedList.forEach {
                chosenCategories.add(it.text.toString())
            }
             return chosenCategories
        }
        else {
            return arrayListOf()
        }
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
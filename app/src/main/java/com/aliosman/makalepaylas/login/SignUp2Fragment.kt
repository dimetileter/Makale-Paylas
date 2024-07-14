package com.aliosman.makalepaylas.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aliosman.makalepaylas.activities.MainActivity
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentSignUp2Binding
import com.aliosman.makalepaylas.databinding.InfoBottomSheetDialogBinding
import com.aliosman.makalepaylas.login.viewmodel.SaveUserInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class SignUp2Fragment : Fragment() {

    private var _binding: FragmentSignUp2Binding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var userInfos: Array<String>

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var profilePictureURI: Uri? = null
    private var profilePictureBitmap: Bitmap? = null
    private lateinit var viewModel: SaveUserInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfos = SignUp2FragmentArgs.fromBundle(it).userInformations
        }

        arguments?.let {
             SignUp2FragmentArgs.fromBundle(it).profilePicture?.let { args ->
                profilePictureURI = args
            }
        }

        db = FirebaseFirestore.getInstance()
        activityResultLauncher()
        permissionLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUp2Binding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verileri güncellemeyi yarayan bir fonksiyon ile yenileme yapılailir
        viewModel = ViewModelProvider(requireActivity())[SaveUserInfoViewModel::class.java]
        checkRadioGroup()

        // Uri olarak alınan görseli bitmap olarak çevir ve profil resmi olarak ayarla
        profilePictureURI?.let {
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, profilePictureURI!!)
                profilePictureBitmap = ImageDecoder.decodeBitmap(source)
                binding.profilePicture.setImageBitmap(profilePictureBitmap)
            }
            else {
                profilePictureBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, profilePictureURI)
                binding.profilePicture.setImageBitmap(profilePictureBitmap)
            }
        }

        binding.profilePicture.setOnClickListener {
           checkGalleryPermission()
        }

        binding.signupNextButton2.setOnClickListener {
            saveUserInformations()
        }

        binding.kullaniciAydinlatmaMetni.setOnClickListener{
            bottomSheetDialog()
        }

        observeLiveData()
    }

    // Kullanıcı bilgilerini kaydet
    private fun saveUserInformations()
    {
        val userInfosHasMap = HashMap<String, Any>()

        userInfosHasMap.put("userName", userInfos[0])
        userInfosHasMap.put("nickName", userInfos[1])
        userInfosHasMap.put("birthDate", userInfos[2])
        userInfosHasMap.put("email", userInfos[3])
        userInfosHasMap.put("userUID", userInfos[4])
        userInfosHasMap.put("saves", ArrayList<String>())
        userInfosHasMap.put("verification", false)

        viewModel.saveUserInfos(userInfosHasMap, profilePictureURI, profilePictureBitmap)
    }

    // Veriyi Gözetle
    private fun observeLiveData()
    {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.constraintLayoutSignup2.visibility = View.GONE
                binding.signup2LoadingScreen.visibility = View.VISIBLE
            }
            else {
                binding.constraintLayoutSignup2.visibility = View.VISIBLE
                binding.signup2LoadingScreen.visibility = View.GONE
            }
        }

        viewModel.isPictureError.observe(viewLifecycleOwner) {
            //Görsel yüklenememişse hata varsa mesaj göster(?)
            if (it)
            {
                val message = getString(R.string.toast_profil_resmi_yuklenmedi)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isUserInfoError.observe(viewLifecycleOwner) {
            // Kullanıcı bilgileri veritabanına yüklenmemişse hata mesajı göster
            if (it)
            {
                val message = getString(R.string.toast_hesap_olusturulmadi)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                binding.constraintLayoutSignup2.visibility = View.VISIBLE
                binding.signup2LoadingScreen.visibility = View.GONE
            }
        }

        viewModel.navigateNextScreen.observe(viewLifecycleOwner) {
            if (it) {
                actionToMainActivity()
            }
        }
    }

    // Doğrulanmış hesap mı?
    private fun checkRadioGroup()
    {
        binding.radiogroup.setOnCheckedChangeListener { group, checkedId ->

            val txtVerificate = binding.txtVerification

            when (checkedId)
            {
                binding.radioButtonEvet.id -> {
                    txtVerificate.isEnabled = true
                    txtVerificate.visibility = View.VISIBLE
                    //TODO: Doğrulama kodu gönderilir. Ve doğrulanması beklenir.
                }

                binding.radioButtonHayir.id -> {
                    txtVerificate.isEnabled = false
                    txtVerificate.visibility = View.INVISIBLE
                }
            }
        }
    }

    // Main aktiviteye git
    private fun actionToMainActivity()
    {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    //Bottom diaog
    private fun bottomSheetDialog()
    {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomBinding = InfoBottomSheetDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomBinding.root)
        bottomSheetDialog.show()
    }



    /////////////////////////////////////////////////////////////////////////
    // Galeri İznini kontrol et
    private fun checkGalleryPermission()
    {
        if (Build.VERSION.SDK_INT >= 33)
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                askForGalleryPermission(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else
            {
                galleryIntent()
            }

        }
        else
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                askForGalleryPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else
            {
                galleryIntent()
            }
        }
    }

    // Galeri İznini kontrol et
    private fun askForGalleryPermission(permission: String)
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission))
        {
            val message = getString(R.string.snack_galeri_izni_gerekli)
            val message2 = getString(R.string.snack_izin_ver)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction(message2, View.OnClickListener {
                    permissionLauncher.launch(permission)
                }).show()
        }
        else
        {
            permissionLauncher.launch(permission)
        }
    }

    // Galeryie git
    private fun galleryIntent()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(galleryIntent)

    }

    private fun permissionLauncher()
    {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                galleryIntent()
            }
            else {
                val message = getString(R.string.toast_izin_ayari_ac)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun activityResultLauncher()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val galleryIntentResult = result.data
                galleryIntentResult?.let {

                    galleryIntentResult.data?.let { it ->
                        profilePictureURI = it
                    }

                    try {
                        profilePictureURI?.let {
                            if (Build.VERSION.SDK_INT >= 28)
                            {
                                val source = ImageDecoder.createSource(requireContext().contentResolver, profilePictureURI!!)
                                profilePictureBitmap = ImageDecoder.decodeBitmap(source)
                                binding.profilePicture.setImageBitmap(profilePictureBitmap)
                            }
                            else
                            {
                                profilePictureBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, profilePictureURI)
                                binding.profilePicture.setImageBitmap(profilePictureBitmap)
                            }
                        }
                    }
                    catch (exepciton: Exception) {
                        Toast.makeText(requireContext(), exepciton.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
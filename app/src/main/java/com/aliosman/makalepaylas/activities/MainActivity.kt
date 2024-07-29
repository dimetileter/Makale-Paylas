package com.aliosman.makalepaylas.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.room.Room
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.MainActivityViewModel
import com.aliosman.makalepaylas.databinding.ActivityMainBinding
import com.aliosman.makalepaylas.databinding.BootmSheetDialogBinding
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDAO
import com.aliosman.makalepaylas.roomdb.userroom.UserInfoDatabase
import com.aliosman.makalepaylas.util.DataManager
import com.aliosman.makalepaylas.util.ToastMessages
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var dao: UserInfoDAO
    private lateinit var db: UserInfoDatabase

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        viewModel.getUserInfo()
        observer()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController

        binding.navHomeButton.setOnClickListener {
            actionToHomePage()
        }

        binding.navProfileButton.setOnClickListener {
            actionToProfilePage()
        }

        binding.navUploadButton.setOnClickListener {
            actionToUploadPage()
        }

        permissionLauncher()
        activityResultLauncher()

    }

    private fun observer()
    {
        viewModel.isLoading.observe(this) {
            if (it) {
                // TODO: Login-MainActivity arası geçiş kısmına yükleme ekranı eklenecek
            }
        }

        viewModel.userInfoList.observe(this) {

            it?.let {
                // Alınan verileri aracı sınıfa at
                val nickname = it.get(0) as String
                val byteArray = it.get(1) as ByteArray?

                // ByteArray formatındaki profil resmini bitmap tipine çevir
                byteArray?.let {
                    val profilePictureBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    DataManager.profilePicture = profilePictureBitmap
                }

                nickname?.let {
                    // Kullanıcı ismini tanımla
                    binding.txtKullaniciAdi.text = nickname
                }
            }
        }

        viewModel.isProfileUpdate.observe(this) {
            // Profil sayfasına yönlendir
            //navController.navigate(R.id.profilePageFragment, null, getNavOptions(R.id.profilePageFragment))
            val msg = getString(R.string.toast_profil_resmi_degistirildi)
            ToastMessages(this).showToastShort(msg)
        }
    }

    // Ana sayfaya git
    private fun actionToHomePage()
    {
        navController.navigate(R.id.homePageFragment, null, getNavOptions(R.id.homePageFragment))
    }

    // Profil sayfasına git
    private fun actionToProfilePage()
    {
        navController.navigate(R.id.profilePageFragment, null, getNavOptions(R.id.profilePageFragment))
    }

    // Yükleme sayfasına git
    private fun actionToUploadPage()
    {
        navController.navigate(R.id.uploadPageFragment, null, getNavOptions(R.id.uploadPageFragment))
    }

    // Önceki fragmanları temizle
    private fun getNavOptions(destinationId: Int): NavOptions {
        return NavOptions.Builder()
            .setPopUpTo(destinationId, false)
            .build()
    }

    // Arama sayfasına git
    fun search_button(view: View)
    {
        val intent = Intent(this, SearchPageActivity::class.java)
        startActivity(intent)
    }

    // Giriş sayfasına dön
    private fun actionToLoginActivity()
    {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    // Ayarlar menüsü butonu
    fun settings_button(view: View)
    {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomBinding = BootmSheetDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomBinding.root)
        bottomSheetDialog.show()

        bottomBinding.darkModeButton.setOnClickListener{
            Toast.makeText(this, "Koyu Mod", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.changeProfilePictureButton.setOnClickListener{
            checkGalleryPermission()
        }

        bottomBinding.changeNicknameButton.setOnClickListener{
            Toast.makeText(this, "Kullanıcı Adını Değiştir", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.exitButton.setOnClickListener{
            signOut()
            actionToLoginActivity()
        }
    }

    // Google hesabından çıkış yap
    private fun signOut() {
        // Firebase Authentication oturumunu kapat
        auth.signOut()

        // Google Sign-In oturumunu kapat
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val message = getString(R.string.toast_cikis_basarili)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Kullanıcıyı giriş sayfasına yönlendirme veya başka bir işlem
            } else {
                val message = getString(R.string.toast_cikis_basarisiz)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////
    // Galeri İznini kontrol et
    private fun checkGalleryPermission()
    {
        if (Build.VERSION.SDK_INT >= 33)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
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
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun activityResultLauncher()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val galleryIntentResult = result.data
                var profilePictureURI: Uri? = null
                galleryIntentResult?.let {

                    galleryIntentResult.data?.let { it ->
                        profilePictureURI = it
                    }

                    try {
                        var profilePictureBitmap: Bitmap? = null
                        db = Room.databaseBuilder(this, UserInfoDatabase::class.java,"UserInfos").build()
                        dao = db.userDao()

                        profilePictureURI?.let {
                            if (Build.VERSION.SDK_INT >= 28)
                            {
                                val source = ImageDecoder.createSource(this.contentResolver, profilePictureURI!!)
                                profilePictureBitmap = ImageDecoder.decodeBitmap(source)
                                val profilePictureImageView = findViewById<ImageView>(R.id.profile_picture)
                                profilePictureImageView.setImageBitmap(profilePictureBitmap)
                            }
                            else
                            {
                                profilePictureBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, profilePictureURI)
                                val profilePictureImageView = findViewById<ImageView>(R.id.profile_picture)
                                profilePictureImageView.setImageBitmap(profilePictureBitmap)
                            }

                            // Alınan verileri kaydetmeye gönder
                            viewModel.updateProfilePicture(profilePictureBitmap!!, profilePictureURI!!)
                        }
                    }
                    catch (exepciton: Exception) {
                        Toast.makeText(this, exepciton.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////

    private fun bitmapToByteArray(profilePictureBitmap: Bitmap): ByteArray
    {
        val outputStream = ByteArrayOutputStream()
        profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val bytePicture = outputStream.toByteArray()
        return bytePicture
    }

}
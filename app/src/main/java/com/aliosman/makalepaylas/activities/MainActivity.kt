package com.aliosman.makalepaylas.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.MainActivityViewModel
import com.aliosman.makalepaylas.databinding.ActivityMainBinding
import com.aliosman.makalepaylas.databinding.BootmSheetDialogBinding
import com.aliosman.makalepaylas.util.DataManager
import com.aliosman.makalepaylas.ui.SearchPageActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var auth: FirebaseAuth

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

        observer()
    }

    private fun observer()
    {
        viewModel.isLoading.observe(this) {
            if (it) {
                //Yükleme ekranı
            }
        }

        viewModel.userInfoList.observe(this) {

            it?.let {
                // Alınan verileri aracı sınıfa at
                val nickname = it.get(0) as String?
                val byteArray = it.get(1) as ByteArray?

                // ByteArray formatındaki profil resmini bitmap tipine çevir
                byteArray?.let {
                    val profilePictureBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    DataManager.profilePicture = profilePictureBitmap
                }

                // Kullanıcı ismini tanımla
                nickname?.let {
                    binding.txtKullaniciAdi.text = nickname
                }
            }
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
            Toast.makeText(this, "Profil Resmini Değiştir", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.changeNicknameButton.setOnClickListener{
            Toast.makeText(this, "Kullanıcı Adını Değiştir", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.exitButton.setOnClickListener{
            // TODO: Hesaptan çıkış yapılınca login ekranına dönüldüğü esnada çökme sorunu var
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

}
package com.aliosman.makalepaylas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.aliosman.makalepaylas.databinding.ActivityMainBinding
import com.aliosman.makalepaylas.databinding.BootmSheetDialogBinding
import com.aliosman.makalepaylas.ui.SearchPageActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

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
    }


    //Ana sayfaya git
    private fun actionToHomePage()
    {
        navController.navigate(R.id.homePageFragment)
    }

    //Profil sayfasına git
    private fun actionToProfilePage()
    {
        navController.navigate(R.id.profilePageFragment)
    }

    //Yükleme sayfasına git
    private fun actionToUploadPage()
    {
        navController.navigate(R.id.uploadPageFragment)
    }

    //Arama sayfasına git
    fun search_button(view: View)
    {
        val intent = Intent(this, SearchPageActivity::class.java)
        startActivity(intent)
    }

    //Ayarlar menüsü butonu
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
            signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signOut() {
        // Firebase Authentication oturumunu kapat
        auth.signOut()

        // Google Sign-In oturumunu kapat
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Çıkış başarılı", Toast.LENGTH_SHORT).show()
                // Kullanıcıyı giriş sayfasına yönlendirme veya başka bir işlem
            } else {
                Toast.makeText(this, "Çıkış başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
package com.aliosman.makalepaylas.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.aliosman.makalepaylas.activities.MainActivity
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentLoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class LoginPageFragment : Fragment() {

    private var _binding: FragmentLoginPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    companion object {
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        this.currentUser = auth.currentUser

        if (currentUser != null)
        {
            checkUserInDatabase(currentUser!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginPageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoogleIleGiris.setOnClickListener {
            signIn()
        }
    }

    // Google Sign-In ve Firebase Authentication işlemleri için doğru yapılandırma kontrolü
    private fun signIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                val user = auth.currentUser
                val message = getString(R.string.toast_olarak_giris_yapiliyor)
                Toast.makeText(requireContext(), "${user?.displayName}  $message", Toast.LENGTH_SHORT).show()
                user?.let {
                    checkUserInDatabase(it)
                }
            }
            else {
                val message = getString(R.string.toast_giris_basarisiz)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kullanıcı bilgilerini kontrol et
    fun checkUserInDatabase(user: FirebaseUser)
    {
        val userRef = db.collection("Users").document(user.uid)
        userRef.get().addOnSuccessListener {
            if (it.exists()) {
                // Kullanıcı mevcut
                binding.btnGoogleIleGiris.isEnabled = true
                actionToMainActivity()
            }else {
                // Kullanici mevcut değil
                binding.btnGoogleIleGiris.isEnabled = true
                actionToSignUpPage()
            }
        }
    // Kullanici sorgulanirken hata meydan geldi
    // Login ekranında kalmaya devam et
    }





    // Ana sayfaya git
    private fun actionToMainActivity()
    {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    // SignUp sayfasına git
    private fun actionToSignUpPage()
    {
        val email = auth.currentUser?.email
        val uid = auth.currentUser?.uid
        val displayName = auth.currentUser?.displayName
        val userInfos = arrayOf(email, uid, displayName)

        view?.let {
            val action = LoginPageFragmentDirections.actionLoginPageFragmentToSignUpPageFragment(userInfos)
            Navigation.findNavController(binding.root).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
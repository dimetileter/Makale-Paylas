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
import com.aliosman.makalepaylas.MainActivity
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentLoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore


class LoginPageFragment : Fragment() {

    private var _binding: FragmentLoginPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var googleSignInClient: GoogleSignInClient
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        //TODO: Eğer hesap açılmışsa ama kayıt bilgileri tamamlanmamışsa login'e atsın
        if (currentUser != null)
        {
            checkUserInDatabase(currentUser)
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
                Toast.makeText(requireContext(), "${user?.displayName} olarak giriş yapılıyor", Toast.LENGTH_SHORT).show()
                user?.let {
                    checkUserInDatabase(it)
                }
            }
            else
            {
                Toast.makeText(requireContext(), "Giriş başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kullanıcı bilgilerini kontrol et
    fun checkUserInDatabase(user: FirebaseUser)
    {
        var userUID:String? = null

        val userRef = db.collection("Users").whereEqualTo("userUID", user.uid)
        userRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val documents = querySnapshot.documents
                for (doc in documents) {
                    userUID = doc.getString("userUID")
                }

                if (!userUID.isNullOrEmpty()) {
                    actionToMainActivity()
                } else {
                    actionToSignUpPage()
                }
            } else {
                actionToSignUpPage()
            }
        }
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
        val uıd = auth.currentUser?.uid
        val userInfos = arrayOf(email, uıd)

        val action = LoginPageFragmentDirections.actionLoginPageFragmentToSignUpPageFragment(userInfos)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
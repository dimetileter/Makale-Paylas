package com.aliosman.makalepaylas.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.transition.Visibility
import com.aliosman.makalepaylas.MainActivity
import com.aliosman.makalepaylas.databinding.FragmentSignUp2Binding
import com.google.firebase.firestore.FirebaseFirestore

class SignUp2Fragment : Fragment() {

    private var _binding: FragmentSignUp2Binding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var userInfos: Array<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfos = SignUp2FragmentArgs.fromBundle(it).userInformations

        }

        db = FirebaseFirestore.getInstance()

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

        checkRadioGroup()

        binding.loginProfilePicture.setOnClickListener {
            Toast.makeText(requireContext(), "Profil resmi seçici eklenecek", Toast.LENGTH_SHORT).show()
        }

        binding.signupNextButton2.setOnClickListener {
            saveUserInformations()
        }
    }

    //Save information
    private fun saveUserInformations()
    {
        val userInfosHasMap = HashMap<String, Any>()

        userInfosHasMap.put("userName", userInfos[0])
        userInfosHasMap.put("nickName", userInfos[1])
        userInfosHasMap.put("birthDate", userInfos[2])
        userInfosHasMap.put("email", userInfos[3])
        userInfosHasMap.put("userUID", userInfos[4])
        userInfosHasMap.put("saves", ArrayList<String>())
        //userInfosHasMap.put("verification", )

        db.collection("Users").add(userInfosHasMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                actionToMainActivity()
            }
            else {
                Toast.makeText(requireContext(), "Hesap oluşturulurken bir hata meydana geldi", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { ex ->
            ex?.let {
                Toast.makeText(requireContext(), ex.message, Toast.LENGTH_SHORT).show()
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

    //Action to MainActivity
    private fun actionToMainActivity()
    {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.aliosman.makalepaylas.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.aliosman.makalepaylas.databinding.FragmentSignUpPageBinding

class SignUpPageFragment : Fragment() {

    private var _binding: FragmentSignUpPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var userInfos: Array<String> // email, uıd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfos = SignUpPageFragmentArgs.fromBundle(it).userInfos // email, uıd
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpPageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupNextButton.setOnClickListener {
            saveUserInformations()
        }

        binding.loginProfilePicture.setOnClickListener {
            Toast.makeText(requireContext(), "Profil resmi seçici eklenecek", Toast.LENGTH_SHORT).show()
        }
    }

    // Save user information and action to next fragment
    private fun saveUserInformations()
    {
        val userName = binding.txtName.text.toString()
        val nickName = binding.txtNickname.text.toString()
        val birthDate = binding.txtBirthDate.text.toString()
        val userInfos = arrayOf(userName, nickName, birthDate, userInfos[0], userInfos[1])

        if (!userName.isNullOrEmpty() && !nickName.isNullOrEmpty() && !birthDate.isNullOrEmpty()) {
            actionToSignUpFragment2(userInfos)
        }
        else {
            Toast.makeText(requireContext(), "Kullanıcı bilgileri boş bırakılamaz", Toast.LENGTH_SHORT).show()
        }
    }

    //Action to next fragment
    private fun actionToSignUpFragment2(userIfos: Array<String>)
    {
        val action = SignUpPageFragmentDirections.actionSignUpPageFragmentToSignUp2Fragment(userIfos)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
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
import androidx.navigation.Navigation
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.FragmentSignUpPageBinding
import com.google.android.material.snackbar.Snackbar

class SignUpPageFragment : Fragment() {

    private var _binding: FragmentSignUpPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var userInfos: Array<String> // email, uıd

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var profilePictureURI: Uri? = null
    private var profilePictureBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfos = SignUpPageFragmentArgs.fromBundle(it).userInfos // email, uıd
        }

        activityResultLauncher()
        permissionLauncher()
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

        binding.profilePicture.setOnClickListener {
            checkGalleryPermission()
        }
    }

//===============================GALLERY-PERMISSIONS================================================

    //Galeri İznini kontrol et
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

    //Galeri İznini kontrol et
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

    //Galeryie git
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

//==================================================================================================

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
            val message = getString(R.string.toast_bilgiler_bos_birakilamaz)
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    //Action to next fragment
    private fun actionToSignUpFragment2(userIfos: Array<String>)
    {
        val action = SignUpPageFragmentDirections.actionSignUpPageFragmentToSignUp2Fragment(userIfos, profilePictureURI)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

}
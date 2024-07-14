package com.aliosman.makalepaylas.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aliosman.makalepaylas.R
import com.google.android.material.snackbar.Snackbar

class GalleryPermisions(private val activity: AppCompatActivity, private val view: View) {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var profilePictureURI: Uri

    init {
        activityResultLauncher()
        permissionLauncher()
    }

    //Galeri İznini kontrol et
    fun checkGalleryPermission()
    {
        if (Build.VERSION.SDK_INT >= 33)
        {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
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
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
        {
            Snackbar.make(view, "Glaeriye erişim için izin gerekli", Snackbar.LENGTH_LONG)
                .setAction("İzin ver", View.OnClickListener {
                    permissionLauncher!!.launch(permission)
                })
        }
        else
        {
            permissionLauncher!!.launch(permission)
        }
    }

    //Galeryie git
    private fun galleryIntent()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher!!.launch(galleryIntent)

    }

    private fun permissionLauncher()
    {
        permissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                galleryIntent()
            }
            else {
                Toast.makeText(activity, "Ayarlardan galeri erişim zinini açın", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun activityResultLauncher()
    {
        activityResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val galleryIntentResult = result.data
                galleryIntentResult?.let {

                    galleryIntentResult.data?.let { it ->
                        profilePictureURI = it
                    }

                    try {

                        if (Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(activity.contentResolver, profilePictureURI)
                            val profilePictureBitMap = ImageDecoder.decodeBitmap(source)
                            val profilePicture = activity.findViewById<ImageView>(R.id.profile_picture)
                            profilePicture.setImageBitmap(profilePictureBitMap)
                        }
                        else
                        {
                            val profilePictureBitMap = MediaStore.Images.Media.getBitmap(activity.contentResolver, profilePictureURI)
                            val profilePicture = activity.findViewById<ImageView>(R.id.profile_picture)
                            profilePicture.setImageBitmap(profilePictureBitMap)
                        }
                    }
                    catch (exepciton: Exception) {
                        Toast.makeText(activity, exepciton.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
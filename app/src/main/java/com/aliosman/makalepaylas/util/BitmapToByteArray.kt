package com.aliosman.makalepaylas.util

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class BitmapToByteArray() {

    public fun bitmapToByteArray(bitmap: Bitmap): ByteArray
    {

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val bytePicture = outputStream.toByteArray()
        return bytePicture
    }

    private fun downloadImage(url: String): Bitmap
    {
        return Picasso.get().load(url).get()
    }

}
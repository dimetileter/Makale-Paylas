package com.aliosman.makalepaylas.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aliosman.makalepaylas.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ImageView.downloadImage(url: String)
{
    val progressBar = progressBarDrawable(context)
    val options = RequestOptions().placeholder(progressBar).error(R.drawable.baseline_error_outline_24)
    Glide.with(context).setDefaultRequestOptions(options).load(url).into(this)
}

fun progressBarDrawable(context: Context): Drawable
{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 40f
        start()
    }
}
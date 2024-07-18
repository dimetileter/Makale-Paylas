package com.aliosman.makalepaylas.util

import android.content.Context
import android.widget.Toast

class ToastMessages(val context: Context) {

    fun showToastShort(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showToastLong(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
package com.aliosman.makalepaylas.util

import android.content.Context

class SharedPreferencesManager(context: Context) {

    private var homeSP = context.getSharedPreferences("timeH", Context.MODE_PRIVATE)
    private var profileSP = context.getSharedPreferences("timeP", Context.MODE_PRIVATE)

    fun saveHomeRefreshTime()
    {
        val time = System.currentTimeMillis()
        homeSP.edit().putLong("time", time).apply()
    }

    fun checkHomeRefreshTime(): Boolean
    {
        val currentTime = System.currentTimeMillis()
        val refreshTime = homeSP.getLong("time", 0)
        val refreshLimit = 900000 // 30dk'ya tekamül etmektedir

        return if(currentTime - refreshTime >= refreshLimit) {
            true // Interneti kullan
        } else {
            false // Room kullan
        }
    }

    fun saveProfileRefreshTime()
    {
        val time = System.currentTimeMillis()
        profileSP.edit().putLong("time", time).apply()
    }

    fun checkProfileRefreshTime(): Boolean
    {
        val currentTime = System.currentTimeMillis()
        val refreshTime = profileSP.getLong("time", 0)
        val refreshLimit = 900000 // 30k'ya tekamül etmektedir

        return if (currentTime - refreshTime >= refreshLimit) {
            true // İnternet ile al
        } else {
            false // Room ile getir
        }
    }
}
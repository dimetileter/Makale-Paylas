package com.aliosman.makalepaylas.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aliosman.makalepaylas.R

class DownloadPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_download_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //İndirme butonu
    fun download_button(view: View)
    {
        Toast.makeText(this, "İndirme işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }

    //Paylaşma butonu
    fun share_download_button(view: View)
    {
        Toast.makeText(this, "Paylaşma işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }

    //Kaydetme butonu
    fun save_button(view: View)
    {
        Toast.makeText(this, "Kaydetme işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }

}
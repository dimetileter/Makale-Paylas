package com.aliosman.makalepaylas.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.databinding.ActivitySearchPageBinding
import com.aliosman.makalepaylas.databinding.BootmSheetDialogBinding
import com.aliosman.makalepaylas.util.DataManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class SearchPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.txtKullaniciAdi.text = DataManager.nickname
    }
}
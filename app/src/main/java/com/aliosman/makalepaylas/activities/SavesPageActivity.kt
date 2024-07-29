package com.aliosman.makalepaylas.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.SavesPageViewModel
import com.aliosman.makalepaylas.adapter.ProfilePageRecyclerAdapter
import com.aliosman.makalepaylas.adapter.SavePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.ActivitySavesPageBinding
import com.aliosman.makalepaylas.databinding.BootmSheetDialogBinding
import com.aliosman.makalepaylas.model.SavesPdfModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class SavesPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavesPageBinding

    private lateinit var viewModel: SavesPageViewModel
    private lateinit var adapter: SavePageRecyclerAdapter
    private var saves = ArrayList<SavesPdfModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = SavePageRecyclerAdapter(saves)
        binding.recyclerViewSaves.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSaves.adapter = adapter

        viewModel = ViewModelProvider(this)[SavesPageViewModel::class.java]
        viewModel.getSaves()
        observer()

        binding.savesSwipeRefresh.setOnRefreshListener {
            viewModel.getSaves()
            binding.savesSwipeRefresh.isRefreshing = false
        }
    }

    private fun observer()
    {
        viewModel.savesList.observe(this) {
            it?.let {
                adapter.refreshAdapter(it)
            }
        }
    }

    fun settings_button(view: View)
    {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomBinding = BootmSheetDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomBinding.root)

        bottomBinding.darkModeButton.setOnClickListener{
            Toast.makeText(this, "Koyu Mod", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.changeProfilePictureButton.setOnClickListener{
            Toast.makeText(this, "Profil Resmini Değiştir", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.changeNicknameButton.setOnClickListener{
            Toast.makeText(this, "Kullanıcı Adını Değiştir", Toast.LENGTH_SHORT).show()
        }

        bottomBinding.exitButton.setOnClickListener{
            Toast.makeText(this, "Çıkış", Toast.LENGTH_SHORT).show()
        }
    }
}
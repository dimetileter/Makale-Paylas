package com.aliosman.makalepaylas.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.SavesPageViewModel
import com.aliosman.makalepaylas.adapter.SavePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.ActivitySavesPageBinding
import com.aliosman.makalepaylas.model.SavesPdfModel
import com.aliosman.makalepaylas.util.OnItemClickListener
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.isInternetAvailable

class SavesPageActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivitySavesPageBinding

    private lateinit var viewModel: SavesPageViewModel
    private var adapter = SavePageRecyclerAdapter(arrayListOf(), this)

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

        viewModel = ViewModelProvider(this)[SavesPageViewModel::class.java]

        if (isInternetAvailable(this)) {
            viewModel.getSaves()
        }
        else {
            val msg = getString(R.string.toast_internet_baglantisi_yok)
            ToastMessages(this).showToastShort(msg)
            binding.savesPageKaydedilenPdfYok.visibility = View.VISIBLE
        }


        binding.recyclerViewSaves.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSaves.adapter = adapter

        observer()

        binding.savesSwipeRefresh.setOnRefreshListener {
            if (isInternetAvailable(this)) {
                viewModel.getSaves()
            }
            else {
                val msg = getString(R.string.toast_internet_baglantisi_yok)
                ToastMessages(this).showToastShort(msg)
                binding.savesPageKaydedilenPdfYok.visibility = View.VISIBLE
                binding.savesSwipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onItemClick(pdfUUID: String) {
        viewModel.deleteSave(pdfUUID)
    }

    private fun observer()
    {
        viewModel.savesList.observe(this) {
            adapter.refreshAdapter(it)

            // Eğer kaydedilenler boşsa boş mesajı göster
            if(it.isNullOrEmpty()) {
                binding.savesPageKaydedilenPdfYok.visibility = View.VISIBLE
            }
            else {
                binding.savesPageKaydedilenPdfYok.visibility = View.GONE
            }
        }

        viewModel.isSuccess.observe(this) {
            if (it)
            {
                val msg = getString(R.string.toast_kayit_silme_basarili)
                ToastMessages(this).showToastShort(msg)
            }
            else {
                val msg = getString(R.string.toast_hata)
                ToastMessages(this).showToastShort(msg)
            }
        }

        viewModel.isLoading.observe(this) {
            if (it) {
                binding.savesSwipeRefresh.isRefreshing = true
            }
            else {
                binding.savesSwipeRefresh.isRefreshing = false
            }
        }

//        viewModel.isEmpty.observe(this) {
//            if (it) {
//                val msg = getString(R.string.toast_paylasim_yok)
//                ToastMessages(this).showToastShort(msg)
//            }
//        }
    }
}
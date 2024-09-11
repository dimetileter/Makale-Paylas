package com.aliosman.makalepaylas.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.activities.viewmodel.SearchPageViewModel
import com.aliosman.makalepaylas.adapter.SearchPageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.ActivitySearchPageBinding
import com.aliosman.makalepaylas.model.PublicModel
import com.aliosman.makalepaylas.util.DataManager

class SearchPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPageBinding
    private lateinit var searchBar: EditText
    private lateinit var viewModel: SearchPageViewModel
    private val adapter = SearchPageRecyclerAdapter(arrayListOf())

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

        viewModel = ViewModelProvider(this)[SearchPageViewModel::class.java]
        binding.searchRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.searchRecyclerView.adapter = adapter
        observer()

        binding.txtKullaniciAdi.text = DataManager.nickname
        searchBar = binding.searchBarEditText

        onEnterClickListener()
        textWatcher()
    }

    // TextWatcher
    private fun textWatcher() {
        searchBar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText.isNotEmpty()) {
                    viewModel.search(searchText)
                }
                else {
                    adapter.refreshData(arrayListOf())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    // Observer
    private fun observer() {
        viewModel.searchResult.observe(this) {
            if (!it.isEmpty()) {
                adapter.refreshData(it)
            }
            else {
                adapter.refreshData(arrayListOf())
            }
        }
    }

    // Enter butonuna basınca olacaklar
    private fun onEnterClickListener() {
        val editText = binding.searchBarEditText
        editText.setOnEditorActionListener { v, actionId, event ->

            val bool1 = actionId == EditorInfo.IME_ACTION_SEARCH
            val bool2 = event?.keyCode == KeyEvent.KEYCODE_SEARCH
            if (bool1 || bool2) {
                val searchText = editText.text.toString()
                viewModel.search(searchText)

                // Klavyeyi kapatmak için
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)

                // Eylemi çalıştır
                true
            }
            else {
                // Eylemi çalıştırma
                false
            }
        }
    }
}
package com.aliosman.makalepaylas.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.aliosman.makalepaylas.adapter.ProfilePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.FragmentProfilePageBinding
import com.aliosman.makalepaylas.model.GetProfilePdfInfoModel
import com.aliosman.makalepaylas.util.DataManager
import com.aliosman.makalepaylas.activities.SavesPageActivity
import com.aliosman.makalepaylas.util.SharedPreferencesManager
import com.aliosman.makalepaylas.util.downloadImage

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProfilePageRecyclerAdapter
    private val pdfList = ArrayList<GetProfilePdfInfoModel>()
    private lateinit var viewModel: ProfilePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DataManager.profilePicture?.let {
            val profilePictureBitmap = it
            binding.profilePicture.setImageBitmap(profilePictureBitmap)
        }

        // ViewModel bağlantısı
        viewModel = ViewModelProvider(requireActivity())[ProfilePageViewModel::class.java]

        // RecyclerView
        adapter = ProfilePageRecyclerAdapter(pdfList)
        binding.profileRecyclerview.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.profileRecyclerview.adapter = adapter

        observerProfile()

        val time = SharedPreferencesManager(requireContext())
        if(time.checkProfileRefreshTime())
        {
            viewModel.getPdfList()
            time.saveProfileRefreshTime()
        }
        else
        {
            viewModel.getPdfFromRoom()
        }

        binding.savesButton.setOnClickListener {
            saves_button(it)
        }

        binding.shareProfileButton.setOnClickListener {
            share_profile_button(it)
        }

        binding.profileSwipeRefresh.setOnRefreshListener {
            viewModel.getPdfList()
            time.saveProfileRefreshTime()
        }

    }

    private fun observerProfile(){

        viewModel.isLoadingP.observe(viewLifecycleOwner) {
            // Eğer yükleme yapılıyorsa yükleme tamamlanana kadar yeniden yükleme yapmayı engelle
            if (it)
            {
                val swipeRefresh = binding.profileSwipeRefresh
                swipeRefresh.isRefreshing = true
                swipeRefresh.isEnabled = false
            }
            else
            {
                val swipeRefresh = binding.profileSwipeRefresh
                swipeRefresh.isRefreshing = false
                swipeRefresh.isEnabled = true
            }
        }

        viewModel.isErrorP.observe(viewLifecycleOwner) { isError ->
            if (isError) {
                // TODO:Hata mesajı göster
            }
        }

        viewModel.takenPdf.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty())
            {
                adapter.refreshData(it)
            }
        }
    }

    //Kaydet butonu
    fun saves_button(view: View)
    {
        val intent = Intent(requireContext(), SavesPageActivity::class.java)
        startActivity(intent)
    }

    //Profili paylaşma butonu
    fun share_profile_button(view: View)
    {
        Toast.makeText(requireContext(), "Profili paylaşma işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
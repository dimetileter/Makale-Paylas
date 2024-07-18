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
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliosman.makalepaylas.adapter.ProfilePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.FragmentProfilePageBinding
import com.aliosman.makalepaylas.model.GetPdfInfoModel
import com.aliosman.makalepaylas.util.DataManager
import com.aliosman.makalepaylas.ui.SavesPageActivity

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProfilePageRecyclerAdapter
    private val pdfList = ArrayList<GetPdfInfoModel>()
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

        observer()
        viewModel.getPdfList()

        binding.savesButton.setOnClickListener {
            saves_button(it)
        }

        binding.shareProfileButton.setOnClickListener {
            share_profile_button(it)
        }

        binding.profileSwipeRefresh.setOnRefreshListener {
            viewModel.getPdfList()
        }

    }

    private fun observer(){

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.profileSwipeRefresh.isRefreshing = true
            }
            else {
                binding.profileSwipeRefresh.isRefreshing = false
            }
        }

        viewModel.isError.observe(viewLifecycleOwner) {
            if (it) {
                binding.profileSwipeRefresh.isRefreshing = false
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
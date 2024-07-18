package com.aliosman.makalepaylas.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.aliosman.makalepaylas.adapter.HomePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.FragmentHomePageBinding
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel
import com.aliosman.makalepaylas.model.GetPdfInfoModel
import com.aliosman.makalepaylas.ui.DownloadPageActivity
import com.aliosman.makalepaylas.ui.profile.ProfilePageViewModel
import com.aliosman.makalepaylas.util.ToastMessages

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomePageRecyclerAdapter
    private var model = ArrayList<GetHomePdfInfoHModel>()
    private lateinit var viewModel: HomePageViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false )
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[HomePageViewModel::class.java]

        adapter = HomePageRecyclerAdapter(model)
        binding.homeRecyclerAdapter.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.homeRecyclerAdapter.adapter = adapter

        observer()
        viewModel.getPdfData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getPdfData()
        }
    }

    private fun observer()
    {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it)
            {
                binding.swipeRefreshLayout.isRefreshing = true
            }
            else {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.isError.observe(viewLifecycleOwner) {
            //TODO: Hata mesajı gösterilecek. Geçiçi olarak tost mesajı yerleştirildi
            ToastMessages(requireContext()).showToastShort("HATA!")
        }

        viewModel.pdfList.observe(viewLifecycleOwner) {
            it?.let {
                adapter.refreshData(it)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
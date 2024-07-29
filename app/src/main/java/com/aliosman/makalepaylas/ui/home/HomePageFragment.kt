package com.aliosman.makalepaylas.ui.home

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
import com.aliosman.makalepaylas.util.SharedPreferencesManager
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

        // Son yenileme 30 dakikadan daha önce ise otomatik yenile
        val time = SharedPreferencesManager(requireContext())
        if(time.checkHomeRefreshTime())
        {
            viewModel.getPdfDataInternet()
            time.saveHomeRefreshTime()
        }
        else
        {
            viewModel.getPdfDataRoom()

        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getPdfDataInternet()
            time.saveHomeRefreshTime()
        }
    }

    private fun observer()
    {
        viewModel.isLoadingH.observe(viewLifecycleOwner) {
            // Zaten yükleme yapılırken tekrar yükleme işlemine izin verme
            if (it) {
                binding.swipeRefreshLayout.isEnabled = false
                binding.swipeRefreshLayout.isRefreshing = true
            }
            else {
                binding.swipeRefreshLayout.isEnabled = true
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.isErrorH.observe(viewLifecycleOwner) {
            //TODO: Hata mesajı gösterilecek. Geçiçi olarak tost mesajı yerleştirildi
            if (it)
                ToastMessages(requireContext()).showToastShort("Veri alımı sırasında HATA!")
        }

        viewModel.pdfList.observe(viewLifecycleOwner) {
            if(!it.isNullOrEmpty()) {
                adapter.refreshData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
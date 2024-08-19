package com.aliosman.makalepaylas.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Visibility
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.adapter.HomePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.FragmentHomePageBinding
import com.aliosman.makalepaylas.util.SharedPreferencesManager
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.isInternetAvailable

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private var recyclerAdapter = HomePageRecyclerAdapter(arrayListOf())
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

        // Eğer internet varsa akışı yenile
        viewModel = ViewModelProvider(requireActivity())[HomePageViewModel::class.java]
        if (isInternetAvailable(requireContext())) {
            binding.homePageBaglantiYok.visibility = View.GONE
            checkTime()
        }
        else {
            binding.homePageBaglantiYok.visibility = View.VISIBLE
            val msg = getString(R.string.toast_akis_yenilenmedi)
            ToastMessages(requireContext()).showToastShort(msg)
        }

        // Recyler adapter
        binding.homeRecyclerAdapter.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.homeRecyclerAdapter.adapter = recyclerAdapter
        observer()

        // SwipeRefresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            swipeRefresh()
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
        }

        viewModel.pdfList.observe(viewLifecycleOwner) {
            recyclerAdapter.refreshData(it)

            if (it.isEmpty()) {
                binding.homePagePaylasimYok.visibility = View.VISIBLE
            }
            else {
                binding.homePagePaylasimYok.visibility = View.GONE
            }
        }
    }

    private fun checkTime()
    {
        // Son yenileme  dakikadan daha önce ise otomatik yenile
        val time = SharedPreferencesManager(requireContext())
        if(time.checkHomeRefreshTime()) {
            viewModel.getPdfDataInternet()
            time.saveHomeRefreshTime()
        }
        else {
            viewModel.getPdfDataRoom()
        }
    }

    private fun swipeRefresh() {
        if (isInternetAvailable(requireContext())) {
            binding.homePageBaglantiYok.visibility = View.GONE
            viewModel.getPdfDataInternet()
            val time = SharedPreferencesManager(requireContext())
            time.saveHomeRefreshTime()
        }
        else {
            binding.homePageBaglantiYok.visibility = View.VISIBLE
            val msg = getString(R.string.toast_akis_yenilenmedi)
            ToastMessages(requireContext()).showToastShort(msg)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.aliosman.makalepaylas.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.aliosman.makalepaylas.R
import com.aliosman.makalepaylas.adapter.ProfilePageRecyclerAdapter
import com.aliosman.makalepaylas.databinding.FragmentProfilePageBinding
import com.aliosman.makalepaylas.activities.SavesPageActivity
import com.aliosman.makalepaylas.databinding.DeleteAlertDialogBinding
import com.aliosman.makalepaylas.util.DataManager
import com.aliosman.makalepaylas.util.OnLongClickRecyclerListener
import com.aliosman.makalepaylas.util.SharedPreferencesManager
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.isInternetAvailable
import com.aliosman.makalepaylas.util.progressBarDrawable

class ProfilePageFragment : Fragment(), OnLongClickRecyclerListener {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    private var recyclerAdapter = ProfilePageRecyclerAdapter(arrayListOf(), this)
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

        DataManager.nickname?.let {
            binding.profileNickname.text = it
        }

        // ViewModel bağlantısı
        viewModel = ViewModelProvider(requireActivity())[ProfilePageViewModel::class.java]
        observerProfile()

        val time = SharedPreferencesManager(requireContext())
        if(time.checkProfileRefreshTime())
        {
            if (isInternetAvailable(requireContext())) {
                binding.profilePageKaydedilenPdfYok.visibility = View.GONE
                viewModel.getPdfList()
                time.saveProfileRefreshTime()
            }
            else {
                binding.profilePageKaydedilenPdfYok.visibility = View.VISIBLE
                viewModel.getPdfFromRoom()
            }
        }
        else {
            viewModel.getPdfFromRoom()
        }

        // RecyclerView
        binding.profileRecyclerview.layoutManager = GridLayoutManager(requireContext(),2)
        binding.profileRecyclerview.adapter = recyclerAdapter


        // Kaydedilenler butonu
        binding.savesButton.setOnClickListener {
            saves_button(it)
        }

        // Profil resmi paylaşma butonu
        binding.shareProfileButton.setOnClickListener {
            share_profile_button(it)
        }

        // Swipe refresh
        binding.profileSwipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    override fun onLongClick(pdfUUID: String, position: Int) {
        deleteAlert(pdfUUID,position)
    }

    // Observer
    private fun observerProfile(){

        viewModel.isLoadingP.observe(viewLifecycleOwner) {
            // Eğer yükleme yapılıyorsa yükleme tamamlanana kadar yeniden yükleme yapmayı engelle
            if (it) {
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
                val msg = getString(R.string.toast_hata)
                ToastMessages(requireContext()).showToastShort(msg)
            }
        }

        viewModel.takenPdf.observe(viewLifecycleOwner) {
                recyclerAdapter.refreshData(it)
        }

        viewModel.newProfilePicture.observe(viewLifecycleOwner) {
            it?.let {
                binding.profilePicture.setImageBitmap(it)
            }
        }

//        viewModel.isDeleted.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.loadingScreen.visibility = View.GONE
//                binding.profileRecyclerview.visibility = View.VISIBLE
//            }
//            else {
//                binding.profileRecyclerview.visibility = View.GONE
//                binding.progressBar.setImageDrawable(progressBarDrawable(requireContext()))
//                binding.loadingScreen.visibility = View.VISIBLE
//            }
//        }
    }

    //Kaydet butonu
    fun saves_button(view: View) {
        val intent = Intent(requireContext(), SavesPageActivity::class.java)
        startActivity(intent)
    }

    //Profili paylaşma butonu
    fun share_profile_button(view: View) {
        Toast.makeText(requireContext(), "[Test]Profili paylaşma işlemi tamamlandı", Toast.LENGTH_SHORT).show()
    }

    // Silme işlemi uyarısı
    private fun deleteAlert(pdfUUID: String, position: Int) {
        val alertBinding = DeleteAlertDialogBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(alertBinding.root)
            .create()

        // Bildiri gösterilmeden önece titreşim ver
        triggerVibration()
        alertDialog.show()

        val backgroundDrawable = ColorDrawable(Color.TRANSPARENT)
        alertDialog.window?.setBackgroundDrawable(backgroundDrawable)

        alertBinding.alertDelete.setOnClickListener {
            viewModel.deletePdf(pdfUUID)
            // Silme işleminden sonra recycler adapteri yenile
            recyclerAdapter.deleteItem(position)
            alertDialog.dismiss()
        }

        alertBinding.alertCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    // Titreşim tetikleyicisi
    private fun triggerVibration() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
           vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else {
            vibrator.vibrate(100)
        }
    }

    // Verileri yenile
    private fun refreshData()
    {
        val time = SharedPreferencesManager(requireContext())
        if (isInternetAvailable(requireContext())) {
            binding.profilePageKaydedilenPdfYok.visibility = View.GONE
            viewModel.getPdfList()
            time.saveProfileRefreshTime()
        }
        else {
            binding.profilePageKaydedilenPdfYok.visibility = View.VISIBLE
            viewModel.getPdfFromRoom()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
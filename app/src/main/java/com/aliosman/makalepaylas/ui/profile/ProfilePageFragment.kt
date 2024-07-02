package com.aliosman.makalepaylas.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aliosman.makalepaylas.databinding.FragmentProfilePageBinding
import com.aliosman.makalepaylas.ui.SavesPageActivity

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

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

        binding.savesButton.setOnClickListener {
            saves_button(it)
        }

        binding.shareProfileButton.setOnClickListener {
            share_profile_button(it)
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
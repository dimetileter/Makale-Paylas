package com.aliosman.makalepaylas.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.makalepaylas.activities.DownloadPageActivity
import com.aliosman.makalepaylas.databinding.RecyclerViewHomeBinding
import com.aliosman.makalepaylas.model.PublicModel
import com.aliosman.makalepaylas.util.downloadImage
import com.bumptech.glide.Glide

class SearchPageRecyclerAdapter(private val searchList: ArrayList<PublicModel>): RecyclerView.Adapter<SearchPageRecyclerAdapter.ViewHolder>()  {

    class ViewHolder(val binding: RecyclerViewHomeBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val binding = RecyclerViewHomeBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.binding.txtMakaleBasligi.text = searchList[p1].artName
        p0.binding.txtYazar.text = searchList[p1].nickname
        p0.binding.pdfCoverPicture.downloadImage(searchList[p1].pdfBitmapUrl)
        p0.itemView.setOnClickListener {
            val intent = Intent(p0.itemView.context, DownloadPageActivity::class.java)
            val bundle = Bundle().apply {
                putString("artName", searchList[p1].artName)
                putString("pdfBitmapUrl", searchList[p1].pdfBitmapUrl)
                putString("nickname", searchList[p1].nickname)
                putString("pdfUUID", searchList[p1].pdfUUID)
            }
            intent.putExtras(bundle)
            p0.itemView.context.startActivity(intent)
        }
    }

    fun refreshData(data: ArrayList<PublicModel>) {
        searchList.clear()
        searchList.addAll(data)
        notifyDataSetChanged()
    }
}
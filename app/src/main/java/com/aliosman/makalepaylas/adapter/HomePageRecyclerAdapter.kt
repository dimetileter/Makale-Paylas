package com.aliosman.makalepaylas.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.makalepaylas.databinding.RecyclerViewHomeBinding
import com.aliosman.makalepaylas.model.HomePagePdfInfo
import com.aliosman.makalepaylas.activities.DownloadPageActivity
import com.aliosman.makalepaylas.util.downloadImage

class HomePageRecyclerAdapter(private val pdfList: ArrayList<HomePagePdfInfo>): RecyclerView.Adapter<HomePageRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val binding: RecyclerViewHomeBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerViewHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtMakaleBasligi.text = pdfList[position].artName
        holder.binding.txtYazar.text = pdfList[position].nickname
        pdfList[position].pdfBitmapUrl?.let {
            holder.binding.pdfCoverPicture.downloadImage(it)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DownloadPageActivity::class.java)
            // Verielri bundle olarak DownloadPage aktivitesine gönder
            val bundle = Bundle().apply {
                putString("pdfUUID", pdfList[position].pdfUUID)
                putString("artName", pdfList[position].artName)
                putString("nickName", pdfList[position].nickname)
                putString("pdfBitmapUrl", pdfList[position].pdfBitmapUrl)
            }
            intent.putExtras(bundle)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun refreshData(newPdfList: List<HomePagePdfInfo>) {
        pdfList.clear()
        pdfList.addAll(newPdfList)
        notifyDataSetChanged()
        //notifyItemRangeChanged(0, newPdfList.size)
    }
}
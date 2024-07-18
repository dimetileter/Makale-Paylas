package com.aliosman.makalepaylas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.makalepaylas.databinding.RecyclerViewHomeBinding
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel
import com.aliosman.makalepaylas.ui.DownloadPageActivity
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.downloadImage

class HomePageRecyclerAdapter(val pdfList: ArrayList<GetHomePdfInfoHModel>): RecyclerView.Adapter<HomePageRecyclerAdapter.ViewHolder>() {

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
            holder.itemView.context.startActivity(intent)
            ToastMessages(holder.itemView.context).showToastLong("Önizleme Modu (Yakında doğru veriler getirilecek)")
        }
    }

    fun refreshData(newPdfList: ArrayList<GetHomePdfInfoHModel>)
    {
        pdfList.clear()
        pdfList.addAll(newPdfList)
        notifyDataSetChanged()
    }
}
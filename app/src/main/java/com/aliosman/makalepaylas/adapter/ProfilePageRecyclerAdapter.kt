package com.aliosman.makalepaylas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.makalepaylas.databinding.RecyclerViewHomeBinding
import com.aliosman.makalepaylas.model.GetPdfInfoModel
import com.aliosman.makalepaylas.ui.DownloadPageActivity
import com.aliosman.makalepaylas.util.ToastMessages
import com.aliosman.makalepaylas.util.downloadImage

class ProfilePageRecyclerAdapter(private var pdfList: ArrayList<GetPdfInfoModel>): RecyclerView.Adapter<ProfilePageRecyclerAdapter.ViewHolder>() {

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
        holder.binding.txtYazar.text = pdfList[position].nickname
        holder.binding.txtMakaleBasligi.text = pdfList[position].artName
        holder.binding.pdfCoverPicture.downloadImage(pdfList[position].pdfBitmapUrl)
        holder.binding.pdf.setOnClickListener {

            val intent = Intent(holder.itemView.context, DownloadPageActivity::class.java)
//            intent.putExtra("pdfInfo", pdfList)
            holder.itemView.context.startActivity(intent)
            ToastMessages(holder.itemView.context).showToastLong("Önizleme Modu (Yakında doğru veriler getirilecek)")
        }
    }

    fun refreshData(newPdfList: ArrayList<GetPdfInfoModel>)
    {
        pdfList.clear()
        pdfList.addAll(newPdfList)
        notifyDataSetChanged()
    }
}
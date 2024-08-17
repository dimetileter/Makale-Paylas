package com.aliosman.makalepaylas.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.makalepaylas.activities.DownloadPageActivity
import com.aliosman.makalepaylas.activities.SavesPageActivity
import com.aliosman.makalepaylas.databinding.RecyclerViewSavesBinding
import com.aliosman.makalepaylas.model.SavesPdfModel
import com.aliosman.makalepaylas.util.OnItemClickListener

class SavePageRecyclerAdapter(private val savesList: ArrayList<SavesPdfModel>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<SavePageRecyclerAdapter.ViewHolder>(){

    class ViewHolder(val binding: RecyclerViewSavesBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(pdf: SavesPdfModel, clickListener: OnItemClickListener) {
            clickListener.onItemClick(pdf.pdfUUID)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerViewSavesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return savesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtPdfName.text = savesList[position].artName
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DownloadPageActivity::class.java)
            // Verielri bundle olarak DownloadPage aktivitesine gönder
            val bundle = Bundle().apply {
                putString("artName", savesList[position].artName)
                putString("artDesc", savesList[position].artDesc)
                putString("pdfUrl", savesList[position].pdfUrl)
                putString("pdfBitmapUrl", savesList[position].pdfBitmapUrl)
                putString("createdAt", savesList[position].createdAt)
                putString("nickname", savesList[position].nickname)
                putString("pdfUUID", savesList[position].pdfUUID)
            }
            intent.putExtras(bundle)
            holder.itemView.context.startActivity(intent)
        }

        //Tıklama durumunda kayıtlı verinin silinmesi için pdfUUID'sini gönder
        holder.binding.unsaveButton.setOnClickListener {
            holder.bind(savesList[position], itemClickListener)
            savesList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun refreshAdapter(newList: ArrayList<SavesPdfModel>)
    {
        savesList.clear()
        savesList.addAll(newList)
        notifyDataSetChanged()
    }
}
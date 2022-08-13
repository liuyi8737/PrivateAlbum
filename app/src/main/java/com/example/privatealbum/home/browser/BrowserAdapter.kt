package com.example.privatealbum.home.browser

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.privatealbum.databinding.BrowserItemBinding
import com.example.privatealbum.db.ThumbImage
import com.example.privatealbum.home.thumb.ThumbViewModel

class BrowserAdapter(val model:ThumbViewModel): RecyclerView.Adapter<BrowserAdapter.MyViewHolder>(){
    private var imageList = emptyList<ThumbImage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = BrowserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(imageList[position],model)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class MyViewHolder(val binding:BrowserItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(thumbImage: ThumbImage,model: ThumbViewModel){
            val filePath = model.getOriginImageFilePath(thumbImage.imageName)
            Glide.with(binding.orgImageView)
                .load(filePath)
                .into(binding.orgImageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newData: List<ThumbImage>){
        imageList = newData
        notifyDataSetChanged()
    }
}
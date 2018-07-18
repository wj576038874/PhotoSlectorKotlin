package com.example.admin.photoslectorkotlin

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.winfo.photoselector.utils.find
import java.io.File
import java.util.ArrayList

class ImageAdapter(private var mContext: Context) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var mImages: ArrayList<String>? = null
    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.adapter_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (mImages == null) 0 else mImages!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = mImages!![position]
        Glide.with(mContext).load(File(image)).into(holder.ivImage)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.find(R.id.iv_image)
    }

    fun refresh(images: ArrayList<String>) {
        mImages = images
        notifyDataSetChanged()
    }
}
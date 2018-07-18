package com.winfo.photoselector.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.winfo.photoselector.R
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.utils.find

class BottomPreviewAdapter(private var context: Context, private var imagesList: List<Image>) : RecyclerView.Adapter<BottomPreviewAdapter.CustomeHolder>() {


    interface OnItemClcikLitener {
        fun onItemClcik(position: Int, image: Image)
    }

    private var onItemClcikLitener: OnItemClcikLitener? = null

    fun setOnItemClcikLitener(onItemClcikLitener: OnItemClcikLitener) {
        this.onItemClcikLitener = onItemClcikLitener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomeHolder {
        return CustomeHolder(LayoutInflater.from(context).inflate(R.layout.bootm_preview_item, parent, false))
    }

    override fun onBindViewHolder(holder: CustomeHolder, position: Int) {
        imagesList[position].selectPosition = position
        Glide.with(context).load(imagesList[holder.adapterPosition].path)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .override(800, 800))
                .thumbnail(0.5f)
                .into(holder.imageView)
        holder.imageView.setOnClickListener({
            for (image in imagesList) {
                image.isChecked = false
            }
            imagesList[holder.adapterPosition].isChecked = true

        })
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    class CustomeHolder(iteView: View) : RecyclerView.ViewHolder(iteView) {
        var imageView: ImageView = itemView.find(R.id.bottom_imageview_item)
    }


    fun referesh(newData: List<Image>) {
        this.imagesList = newData
        notifyDataSetChanged()
    }
}
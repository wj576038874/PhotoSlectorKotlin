package com.winfo.photoselector.adapter

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.winfo.photoselector.R
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.utils.find
import java.io.File

class PreviewImageAdapter(private var mContext: Context, private var mImgList: List<Image>) : RecyclerView.Adapter<PreviewImageAdapter.ImageHolder>() {

    interface OnItemClcikLitener {
        fun onItemClcik(previewImageAdapter: PreviewImageAdapter, iteView: View, position: Int)
    }

    private var onItemClcikLitener: OnItemClcikLitener? = null

    fun setOnItemClcikLitener(onItemClcikLitener: OnItemClcikLitener) {
        this.onItemClcikLitener = onItemClcikLitener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val imageHolder = ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.preview_item, parent, false))
        imageHolder.itemView.setOnClickListener({
            onItemClcikLitener!!.onItemClcik(this@PreviewImageAdapter, imageHolder.itemView, imageHolder.layoutPosition)
        })
        return imageHolder
    }

    override fun getItemCount(): Int {
        return mImgList.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val path = mImgList[position].path
        val uri = if (path.startsWith("httf")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(File(path))
        }
        Glide.with(mContext).setDefaultRequestOptions(RequestOptions()
                .dontAnimate()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_img_load_fail)
                .override(800,1200))
                .load(uri)
                .into(holder.imageView)
    }


    fun getData(): List<Image> {
        return mImgList
    }

    class ImageHolder(iteView: View) : RecyclerView.ViewHolder(iteView) {
        val imageView: ImageView = iteView.find(R.id.iv_itemimg)
    }
}
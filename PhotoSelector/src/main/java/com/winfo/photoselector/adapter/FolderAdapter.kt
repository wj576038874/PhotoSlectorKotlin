package com.winfo.photoselector.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.winfo.photoselector.R
import com.winfo.photoselector.entity.Folder
import com.winfo.photoselector.utils.find
import java.io.File
import java.util.ArrayList

class FolderAdapter(private var mContext: Context, private var mFolders: ArrayList<Folder>, private var mInflater: LayoutInflater = LayoutInflater.from(mContext)) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {


    private var mSelectItem: Int = 0
    private var mListener: OnFolderSelectListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.adapter_folder, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFolders.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = mFolders[position]
        val images = folder.images
        holder.tvFolderName.text = folder.name
        holder.ivSelect.visibility = if (mSelectItem == position) View.VISIBLE else View.GONE
        if (images != null && !images.isEmpty()) {
            holder.tvFolderSize.text = images.size.toString() + "张"
            Glide.with(mContext).load(File(images[0].path))
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.ivImage)
        } else {
            holder.tvFolderSize.text = "0张"
            holder.ivImage.setImageBitmap(null)
        }
        holder.itemView.setOnClickListener({
            mSelectItem = holder.adapterPosition
            notifyDataSetChanged()
            mListener!!.onFolderSelect(folder)
        })
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.find(R.id.iv_image)
        var ivSelect: ImageView = itemView.find(R.id.iv_select)
        var tvFolderName: TextView = itemView.find(R.id.tv_folder_name)
        var tvFolderSize: TextView = itemView.find(R.id.tv_folder_size)
    }

    fun setOnFolderSelectListener(listener: OnFolderSelectListener) {
        this.mListener = listener
    }

    interface OnFolderSelectListener {
        fun onFolderSelect(folder: Folder)
    }
}
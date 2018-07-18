package com.winfo.photoselector.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.winfo.photoselector.R
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.utils.find

class ImageAdapter(private var context: Context, private var mMaxCount: Int, private var isSingle: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mImages: ArrayList<Image>? = null
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private var onCameraClickListener: View.OnClickListener? = null

    companion object {
        const val ITEM_TYPE_CAMERA: Int = 100
        const val ITEM_TYPE_PHOTO: Int = 101
    }

    //是否是显示全部图片  只有显示全部图片的时候 才会去显示牌照，否则不显示牌拍照
    private var showCamera: Boolean = false

    //保存选中的图片
    private var mSelectImages: ArrayList<Image> = ArrayList()
    private var mSelectListener: OnImageSelectListener? = null
    private var mItemClickListener: OnItemClickListener? = null

    override fun getItemViewType(position: Int): Int {
        return if (showCamera && position == 0) {
            ITEM_TYPE_CAMERA
        } else {
            ITEM_TYPE_PHOTO
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_CAMERA) {
            val cameraHolder = CameraHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false))
            cameraHolder.itemView.setOnClickListener({
                onCameraClickListener!!.onClick(it)
            })
            cameraHolder
        } else {
            ImageHolder(mInflater.inflate(R.layout.adapter_images_item, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (showCamera) {
            if (mImages == null) 0 else mImages!!.size + 1
        } else {
            if (mImages == null) 0 else mImages!!.size
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //如果是照片则加载照片显示  否则的话就是拍照就不用处理，默认用布局显示样式
        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {
            val imageHolder: ImageHolder = holder as ImageHolder
            val image: Image
            //如果是显示拍照
            if (showCamera) {
                image = mImages!![position - 1]
                image.position = position - 1
            } else {
                image = mImages!![position]
                image.position = position
            }
            Glide.with(context).load(image.path)
                    .transition(GenericTransitionOptions<Drawable>().transition(android.R.anim.slide_in_left))
                    .transition(DrawableTransitionOptions().crossFade(150))
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .placeholder(R.drawable.ic_image).error(R.drawable.ic_img_load_fail))
                    .thumbnail(0.5f)
                    .into(imageHolder.ivImage)

            setItemSelect(imageHolder, mSelectImages.contains(image))
            //点击选中/取消选中图片
            imageHolder.ivSelectIcon.setOnClickListener({
                if (mSelectImages.contains(image)) {
                    //如果图片已经选中，就取消选中
                    unSelectImage(image)
                    setItemSelect(imageHolder, false)
                } else if (isSingle) {
                    //如果是单选，就先清空已经选中的图片，再选中当前图片
                    clearImageSelect()
                    selectImage(image)
                    setItemSelect(imageHolder, true)
                } else if (mMaxCount <= 0 || mSelectImages.size < mMaxCount) {
                    //如果不限制图片的选中数量，或者图片的选中数量
                    // 还没有达到最大限制，就直接选中当前图片。
                    selectImage(image)
                    setItemSelect(imageHolder, true)
                } else if (mSelectImages.size == mMaxCount) {
                    Toast.makeText(context, "最多只能选" + mMaxCount + "张", Toast.LENGTH_SHORT).show()
                }
            })
            holder.itemView.setOnClickListener({
                //如果是显示拍照
                if (showCamera) {
                    mItemClickListener!!.onItemClick(image, imageHolder.itemView, imageHolder.adapterPosition - 1)
                } else {
                    mItemClickListener!!.onItemClick(image, imageHolder.itemView, imageHolder.adapterPosition)
                }
            })
        }
    }

    fun setOnCameraClickListener(onCameraClickListener: View.OnClickListener) {
        this.onCameraClickListener = onCameraClickListener
    }

    /**
     * 选中图片
     *
     * @param image image
     */
    private fun selectImage(image: Image) {
        mSelectImages.add(image)
        mSelectListener!!.onImageSelect(image, true, mSelectImages.size)
    }

    /**
     * 取消选中图片
     *
     * @param image image
     */
    private fun unSelectImage(image: Image) {
        mSelectImages.remove(image)
        mSelectListener!!.onImageSelect(image, false, mSelectImages.size)
    }

    fun getData(): ArrayList<Image> {
        return mImages!!
    }

    /**
     * 刷新数据
     *
     * @param data       data
     * @param showCamera 是否显示拍照功能
     */
    fun refresh(data: ArrayList<Image>, showCamera: Boolean) {
        this.showCamera = showCamera
        mImages = data
        notifyDataSetChanged()
    }

    /**
     * 设置图片选中和未选中的效果
     */
    private fun setItemSelect(imageHolder: ImageHolder, isSelect: Boolean) {
        if (isSelect) {
            imageHolder.ivSelectIcon.setImageResource(R.drawable.ic_image_select)
            imageHolder.ivMasking.alpha = 0.5f
        } else {
            imageHolder.ivSelectIcon.setImageResource(R.drawable.ic_image_un_select)
            imageHolder.ivMasking.alpha = 0.2f
        }
    }

    private fun clearImageSelect() {
        mSelectImages.clear()
        notifyDataSetChanged()
    }

    fun setSelectedImages(selected: ArrayList<String>) {
        mSelectImages.clear()
        if (mImages != null) {
            for (path in selected) {
                if (isFull()) {
                    return
                }
                for (image in mImages!!) {
                    if (path == image.path) {
                        if (!mSelectImages.contains(image)) {
                            mSelectImages.add(image)
                        }
                        break
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun isFull(): Boolean {
        return isSingle && mSelectImages.size == 1 || mMaxCount > 0 && mSelectImages.size == mMaxCount
    }

    fun getSelectImages(): ArrayList<Image> {
        return mSelectImages
    }

    fun setOnImageSelectListener(listener: OnImageSelectListener) {
        this.mSelectListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    interface OnImageSelectListener {
        fun onImageSelect(image: Image, isSelect: Boolean, selectCount: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(image: Image, itemView: View, position: Int)
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.find(R.id.iv_image)
        var ivSelectIcon: ImageView = itemView.find(R.id.iv_select)
        var ivMasking: ImageView = itemView.find(R.id.iv_masking)
    }

    class CameraHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
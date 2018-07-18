package com.winfo.photoselector.model

import android.content.Context
import android.provider.MediaStore
import com.winfo.photoselector.entity.Folder
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.utils.StringUtils
import java.io.File
import java.util.*

object ImageModel {


    /**
     * 从SDCard加载图片
     *
     * @param context  context
     * @param callback 回调
     */
    fun loadImageForSDCard(context: Context, callback: DataCallback) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        Thread({
            //扫描图片
            val mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val mContentResolver = context.contentResolver
            val mCursor = mContentResolver.query(mImageUri, arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID),
                    null, null,
                    MediaStore.Images.Media.DATE_ADDED)
            val images = ArrayList<Image>()
            //读取扫描到的图片
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    val path = mCursor.getString(
                            mCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    //获取图片名称
                    val name = mCursor.getString(
                            mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    //获取图片时间
                    val time = mCursor.getLong(
                            mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                    if (".downloading" != getExtensionName(path)) { //过滤未下载完成的文件
                        images.add(Image(path, time, name))
                    }
                }
                mCursor.close()
            }
            images.reverse()
            callback.onSuccess(splitFolder(images))
        }).start()
    }

    /**
     * Java文件操作 获取文件扩展名
     */
    private fun getExtensionName(filename: String?): String {
        if (filename != null && filename.isNotEmpty()) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return ""
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     *
     * @param images 集合
     * @return 图片集合
     */
    private fun splitFolder(images: ArrayList<Image>?): ArrayList<Folder> {
        val folders = ArrayList<Folder>()
        val folder = Folder()
        folder.name = "全部图片"
        folder.images = images
        folders.add(folder)
        if (images != null && !images.isEmpty()) {
            val size = images.size
            for (i in 0 until size) {
                val path = images[i].path
                val name = getFolderName(path)
                if (StringUtils.isNotEmptyString(name)) {
                    val folder = getFolder(name, folders)
                    folder.addImage(images[i])
                }
            }
        }
        return folders
    }

    private fun getFolder(name: String, folders: MutableList<Folder>): Folder {
        if (!folders.isEmpty()) {
            val size = folders.size
            for (i in 0 until size) {
                val folder = folders[i]
                if (name == folder.name) {
                    return folder
                }
            }
        }

        val newFolder = Folder()
        newFolder.name = name
        folders.add(newFolder)
        return newFolder
    }

    /**
     * 根据图片路径，获取图片文件夹名称
     *
     * @param path 文件路径
     * @return 文件夹名称
     */
    private fun getFolderName(path: String): String {
        if (StringUtils.isNotEmptyString(path)) {
            val strings = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strings.size >= 2) {
                return strings[strings.size - 2]
            }
        }
        return ""
    }

    interface DataCallback {
        fun onSuccess(folders: ArrayList<Folder>)
    }

}
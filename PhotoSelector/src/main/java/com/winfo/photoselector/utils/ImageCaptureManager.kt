package com.winfo.photoselector.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageCaptureManager(private var mContext: Context) {
    companion object {
        //        const val CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath"
        const val PHOTO_PATH = "photo_path"
    }

    private var mCurrentPhotoPath: String? = null


    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                Log.e("TAG", "Throwing Errors....")
                throw IOException()
            }
        }
        val image = File(storageDir, imageFileName)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun dispatchTakePictureIntent(): Intent {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(mContext.packageManager) != null) {
            val file = createImageFile()
            var photoFile: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //兼容android7.0 使用共享文件的形式
                val contentValues = ContentValues(1)
                contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
                val uri = mContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            } else {
                photoFile = Uri.fromFile(file)
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
            }
            takePictureIntent.putExtra(PHOTO_PATH, file.absolutePath)
        }
        return takePictureIntent
    }

}
package com.winfo.photoselector.utils

import android.Manifest
import android.os.Build
import android.support.annotation.RequiresApi

object PermissionsConstant {

    const val REQUEST_CAMERA = 1
    const val REQUEST_EXTERNAL_READ = 2
    const val REQUEST_EXTERNAL_WRITE = 3

    val PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val PERMISSIONS_EXTERNAL_WRITE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    val PERMISSIONS_EXTERNAL_READ = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

}
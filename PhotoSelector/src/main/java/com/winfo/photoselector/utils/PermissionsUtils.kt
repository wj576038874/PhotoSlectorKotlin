package com.winfo.photoselector.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

object PermissionsUtils {

    /**
     * 检查是否有读取文件的去权限
     */
    fun checkReadStoragePermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true
        }
        val readStoragePermissionState = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val readStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED
        if (!readStoragePermissionGranted) {
            ActivityCompat.requestPermissions(activity,
                    PermissionsConstant.PERMISSIONS_EXTERNAL_READ,
                    PermissionsConstant.REQUEST_EXTERNAL_READ)
        }
        return readStoragePermissionGranted
    }


    /**
     * 是否有写入文件的权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkWriteStoragePermission(activity: Activity):Boolean{
        val writeStoragePermissionState= ContextCompat.checkSelfPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val writeStoragePermissionGranted = writeStoragePermissionState == PackageManager.PERMISSION_GRANTED
        if (!writeStoragePermissionGranted) {
            activity.requestPermissions(PermissionsConstant.PERMISSIONS_EXTERNAL_WRITE,
                    PermissionsConstant.REQUEST_EXTERNAL_WRITE)
        }
        return writeStoragePermissionGranted
    }

    /**
     * 是否有拍照的权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkCameraPermission(activity: Activity): Boolean {
        val cameraPermissionState = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)

        val cameraPermissionGranted = cameraPermissionState == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted) {
            activity.requestPermissions(PermissionsConstant.PERMISSIONS_CAMERA,
                    PermissionsConstant.REQUEST_CAMERA)
        }
        return cameraPermissionGranted
    }
}
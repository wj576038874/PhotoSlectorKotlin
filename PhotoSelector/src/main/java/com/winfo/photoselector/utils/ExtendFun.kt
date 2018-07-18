package com.winfo.photoselector.utils

import android.app.Activity
import android.view.View

/**
 *  ProjectName：MvpKotlin-master
 *  PackageName：com.wenjie.kotlin.utils
 *  Author：wenjie
 *  Date：2018-05-05 18:41
 *  Description：扩展函数
 */

/**
 * findViewById
 * @param resId 资源id
 */
fun <T : View> View.find(resId: Int): T {
    return this.findViewById(resId)
}

/**
 * findViewById
 * @param resId 资源id
 */
fun <T : View> Activity.find(resId: Int): T {
    return this.findViewById(resId)
}


package com.winfo.photoselector.utils

import android.app.Activity
import android.os.Build
import android.support.annotation.ColorInt
import android.view.View
import android.view.WindowManager

object StatusBarUtils {

    /**
     * 设置状态栏颜色
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    fun setColor(activity: Activity, @ColorInt color: Int) {
        setBarColor(activity, color)
    }

    /**
     * 设置状态栏背景色
     * 4.4以下不处理
     * 4.4使用默认沉浸式状态栏
     *
     * @param color 要为状态栏设置的颜色值
     */
    fun setBarColor(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val win = activity.window
            val decorView = win.decorView
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)//沉浸式状态栏(4.4-5.0透明，5.0以上半透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //清除flag，为了android5.0以上也全透明效果
                //让应用的主体内容占用系统状态栏的空间
                val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = decorView.systemUiVisibility or option
                win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                win.statusBarColor = color//设置状态栏背景色
            }
        }
    }
}
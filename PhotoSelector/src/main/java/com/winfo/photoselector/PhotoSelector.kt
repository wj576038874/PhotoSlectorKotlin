package com.winfo.photoselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.NonNull
import com.winfo.photoselector.utils.PermissionsUtils
import com.yalantis.ucrop.UCrop
import java.util.ArrayList


object PhotoSelector {

    const val CROP_RECTANG = 1
    const val CROP_CIRCLE = 2

    /**
     * 默认最大选择数
     */
    const val DEFAULT_MAX_SELECTED_COUNT = 9

    /**
     * 默认显示的列数
     */
    const val DEFAULT_GRID_COLUMN = 3

    /**
     * 默认的requesrCode
     */
    const val DEFAULT_REQUEST_CODE = 999

    const val RESULT_CODE = 1000

    /**
     * 拍照裁剪
     */
    const val TAKE_PHOTO_CROP_REQUESTCODE = 1001

    /**
     * 拍照 不裁剪
     */
    const val TAKE_PHOTO_REQUESTCODE = 1002

    const val CROP_REQUESTCODE = 1003

    /**
     * 图片选择的结果
     */
    const val SELECT_RESULT = "select_result"

    /**
     * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     */
    const val EXTRA_MAX_SELECTED_COUNT = "max_selected_count"

    /**
     * 显示列数
     */
    const val EXTRA_GRID_COLUMN = "column"

    /**
     * 是否显示拍照
     */
    const val EXTRA_SHOW_CAMERA = "show_camera"

    /**
     * 已经选择的照片集合
     */
    const val EXTRA_SELECTED_IMAGES = "selected_images"

    /**
     * 是否单选
     */
    const val EXTRA_SINGLE = "single"

    /**
     * 是否裁剪
     */
    const val EXTRA_CROP = "is_crop"

    const val EXTRA_CROP_MODE = "crop_mode"

    /**
     * toolbar和bottombar是否为material design风格
     */
    const val EXTRA_MATERIAL_DESIGN = "material_design"

    /**
     * toolBar的颜色值
     */
    const val EXTRA_TOOLBARCOLOR = "toolBarColor"

    /**
     * bottomBar的颜色值
     */
    const val EXTRA_BOTTOMBARCOLOR = "bottomBarColor"

    /**
     * 状态栏的颜色值
     */
    const val EXTRA_STATUSBARCOLOR = "statusBarColor"

    /**
     * 初始位置
     */
    const val EXTRA_POSITION = "position"

    /**
     * true是点击预览按钮进入到的预览界面
     * false是点击item进入到的预览界面
     */
    const val EXTRA_ISPREVIEW = "isPreview"

    const val IS_CONFIRM = "is_confirm"


    /**
     * 获取裁剪之后的图片的uri
     *
     * @param intent data
     * @return uri
     */
    fun getCropImageUri(intent: Intent): Uri? {
        return UCrop.getOutput(intent)
    }

    fun builder(): PhotoSelectorBuilder {
        return PhotoSelectorBuilder(Bundle(), Intent())
    }

    class PhotoSelectorBuilder(bundle: Bundle, intent: Intent) {
        private var mPickerOptionsBundle: Bundle = bundle
        private var mPickerIntent: Intent = intent

        /**
         * Send the Intent from an Activity with a custom request code
         *
         * @param activity    Activity to receive result
         * @param requestCode requestCode for result
         */
        fun start(@NonNull activity: Activity, requestCode: Int) {
            if (PermissionsUtils.checkReadStoragePermission(activity)) {
                activity.startActivityForResult(getIntent(activity), requestCode)
            }
        }


        private fun getIntent(@NonNull context: Context): Intent {
            mPickerIntent.setClass(context, ImageSelectorActivity::class.java)
            mPickerIntent.putExtras(mPickerOptionsBundle)
            return mPickerIntent
        }

        fun start(@NonNull activity: Activity) {
            start(activity, DEFAULT_REQUEST_CODE)
        }

        /**
         * 设置最大选择数量
         *
         * @param maxSelectCount 数量
         * @return PhotoSelectorBuilder
         */
        fun setMaxSelectCount(maxSelectCount: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_MAX_SELECTED_COUNT, maxSelectCount)
            return this
        }

        /**
         * 是否是单选
         *
         * @param isSingle 是否是单选
         * @return PhotoSelectorBuilder
         */
        fun setSingle(isSingle: Boolean): PhotoSelectorBuilder {
            mPickerOptionsBundle.putBoolean(EXTRA_SINGLE, isSingle)
            return this
        }

        /**
         * 设置列数
         *
         * @param columnCount 列数
         * @return PhotoSelectorBuilder
         */
        fun setGridColumnCount(columnCount: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_GRID_COLUMN, columnCount)
            return this
        }

        /**
         * 是否显示拍照
         *
         * @param showCamera 是否显示拍照
         * @return PhotoSelectorBuilder
         */
        fun setShowCamera(showCamera: Boolean): PhotoSelectorBuilder {
            mPickerOptionsBundle.putBoolean(EXTRA_SHOW_CAMERA, showCamera)
            return this
        }

        /**
         * 已经选择的照片集合
         *
         * @param selected 已经选择的照片集合
         * @return PhotoSelectorBuilder
         */
        fun setSelected(selected: ArrayList<String>): PhotoSelectorBuilder {
            mPickerOptionsBundle.putStringArrayList(EXTRA_SELECTED_IMAGES, selected)
            return this
        }

        /**
         * toolBar的颜色
         *
         * @param toolBarColor toolBar的颜色
         * @return PhotoSelectorBuilder
         */
        fun setToolBarColor(@ColorInt toolBarColor: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_TOOLBARCOLOR, toolBarColor)
            return this
        }

        /**
         * bottomBar的颜色
         *
         * @param bottomBarColor bottomBar的颜色
         * @return PhotoSelectorBuilder
         */
        fun setBottomBarColor(@ColorInt bottomBarColor: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_BOTTOMBARCOLOR, bottomBarColor)
            return this
        }

        /**
         * 状态栏的颜色
         *
         * @param statusBarColor 状态栏的颜色
         * @return PhotoSelectorBuilder
         */
        fun setStatusBarColor(@ColorInt statusBarColor: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_STATUSBARCOLOR, statusBarColor)
            return this
        }

        /**
         * oolbar和bototmbar是否显示materialDesign风格
         *
         * @param materialDesign toolbar和bototmbar是否显示materialDesign风格
         * @return PhotoSelectorBuilder
         */
        fun setMaterialDesign(materialDesign: Boolean): PhotoSelectorBuilder {
            mPickerOptionsBundle.putBoolean(EXTRA_MATERIAL_DESIGN, materialDesign)
            return this
        }

        /**
         * 是否裁剪，剪切，修剪
         *
         * @return PhotoSelectorBuilder
         */
        fun setCrop(isCrop: Boolean): PhotoSelectorBuilder {
            mPickerOptionsBundle.putBoolean(EXTRA_CROP, isCrop)
            return this
        }

        /**
         * 设置裁剪的样式
         *
         * @param mode 圆形 矩形
         * @return PhotoSelectorBuilder
         */
        fun setCropMode(mode: Int): PhotoSelectorBuilder {
            mPickerOptionsBundle.putInt(EXTRA_CROP_MODE, mode)
            return this
        }

    }

}
package com.winfo.photoselector

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.*
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.winfo.photoselector.adapter.FolderAdapter
import com.winfo.photoselector.adapter.ImageAdapter
import com.winfo.photoselector.entity.Folder
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.model.ImageModel
import com.winfo.photoselector.utils.*
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageSelectorActivity : AppCompatActivity() {

    private var tvTime: TextView? = null
    private var tvFolderName: TextView? = null
    private var tvConfirm: TextView? = null
    private var tvPreview: TextView? = null

    private var btnConfirm: FrameLayout? = null
    private var btnPreview: FrameLayout? = null

    private var rvImage: RecyclerView? = null
    private var rvFolder: RecyclerView? = null
    private var masking: View? = null

    private var mAdapter: ImageAdapter? = null
    private var mLayoutManager: GridLayoutManager? = null
    private var captureManager: ImageCaptureManager? = null

    private var mFolders: ArrayList<Folder>? = null
    private var mFolder: Folder? = null
    private var isToSettings: Boolean = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 0X00000011
    }

    private var isShowTime: Boolean = false
    private var isInitFolder: Boolean = false
    private var rlBottomBar: RelativeLayout? = null

    private var toolBarColor: Int = 0
    private var bottomBarColor: Int = 0
    private var statusBarColor: Int = 0
    private var column: Int = 0
    private var isSingle: Boolean = false
    private var showCamera: Boolean = false
    //    private boolean cutAfterPhotograph;
    private var mMaxCount: Int = 0
    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private var mSelectedImages: ArrayList<String>? = null
    private var isCrop: Boolean = false//是否裁剪
    private var cropMode: Int = 0//裁剪样式

    private var toolbar: Toolbar? = null

    private val mHideHandler = Handler()
    private var mHide = Runnable {
        hideTime()
    }

    /**
     * 两种方式从底部弹出文件夹列表
     * 1、使用BottomSheetDialog交互性好
     * 2、使用recycleview控制显示和隐藏加入动画即可
     */
    private var bottomSheetDialog: BottomSheetDialog? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val bundle = intent.extras!!
        mMaxCount = bundle.getInt(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, PhotoSelector.DEFAULT_MAX_SELECTED_COUNT)
        column = bundle.getInt(PhotoSelector.EXTRA_GRID_COLUMN, PhotoSelector.DEFAULT_GRID_COLUMN)
        isSingle = bundle.getBoolean(PhotoSelector.EXTRA_SINGLE, false)
        cropMode = bundle.getInt(PhotoSelector.EXTRA_CROP_MODE, 1)
        showCamera = bundle.getBoolean(PhotoSelector.EXTRA_SHOW_CAMERA, true)
        isCrop = bundle.getBoolean(PhotoSelector.EXTRA_CROP, false)
        mSelectedImages = bundle.getStringArrayList(PhotoSelector.EXTRA_SELECTED_IMAGES)
        captureManager = ImageCaptureManager(this)
        toolBarColor = bundle.getInt(PhotoSelector.EXTRA_TOOLBARCOLOR, ContextCompat.getColor(this, R.color.blue))
        bottomBarColor = bundle.getInt(PhotoSelector.EXTRA_BOTTOMBARCOLOR, ContextCompat.getColor(this, R.color.blue))
        statusBarColor = bundle.getInt(PhotoSelector.EXTRA_STATUSBARCOLOR, ContextCompat.getColor(this, R.color.blue))
        val materialDesign = bundle.getBoolean(PhotoSelector.EXTRA_MATERIAL_DESIGN, false)
        if (materialDesign) {
            setContentView(R.layout.activity_image_select)
        } else {
            setContentView(R.layout.activity_image_select2)
        }
        initView()
        StatusBarUtils.setColor(this, statusBarColor)
        setToolBarColor(toolBarColor)
        setBottomBarColor(bottomBarColor)
        initListener()
        initImageList()
        checkPermissionAndLoadImages()
        hideFolderList()
        if (mSelectedImages != null) {
            setSelectImageCount(mSelectedImages!!.size)
        } else {
            setSelectImageCount(0)
        }
    }


    private fun initView() {
        toolbar = find(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar = this.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        rlBottomBar = find(R.id.rl_bottom_bar)
        rvImage = find(R.id.rv_image)
        //第一种方式
        bottomSheetDialog = BottomSheetDialog(this)
        @SuppressLint("InflateParams")
        val bsdFolderDialogView = layoutInflater.inflate(R.layout.bsd_folder_dialog, null)
        bottomSheetDialog!!.setContentView(bsdFolderDialogView)
        rvFolder = bsdFolderDialogView.find(R.id.rv_folder)

        //第二种方式  保留使用recycleview布局显示和隐藏添加动画
        tvConfirm = find(R.id.tv_confirm)
        tvPreview = find(R.id.tv_preview)
        btnConfirm = find(R.id.btn_confirm)
        btnPreview = find(R.id.btn_preview)
        tvFolderName = find(R.id.tv_folder_name)
        tvTime = find(R.id.tv_time)
        masking = find(R.id.masking)
    }

    private fun initListener() {
        toolbar!!.setOnClickListener({
            finish()
        })

        btnPreview!!.setOnClickListener({
            val images = ArrayList(mAdapter!!.getSelectImages())
            toPreviewActivity(true, images, 0)
        })

        btnConfirm!!.setOnClickListener({
            if (isCrop && isSingle) {
                //选择之后
                crop(mAdapter!!.getSelectImages()[0].path, UCrop.REQUEST_CROP)
            } else {
                confirm()
            }
        })

        findViewById<View>(R.id.btn_folder).setOnClickListener {
            if (isInitFolder) {
                openFolder()
            }
        }

        rvImage!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                changeTime()
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                changeTime()
            }
        })
    }

    /**
     * 裁剪
     *
     * @param imagePath   照片的路径
     * @param requestCode 请求code 分选择之后  和 拍照之后
     */
    private fun crop(imagePath: String, requestCode: Int) {
        //选择之后剪切
        val selectUri = Uri.fromFile(File(imagePath))
        val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val time = System.currentTimeMillis()
        val imageName = timeFormatter.format(Date(time))
        val uCrop = UCrop.of(selectUri, Uri.fromFile(File(cacheDir, "$imageName.jpg")))
        val options = UCrop.Options()
        if (cropMode == 2) {
            options.setCircleDimmedLayer(true)//是否显示圆形裁剪的layer
            options.setShowCropGrid(false)//是否显示分割线
            options.setShowCropFrame(false)//是否显示矩形边框
        }
        options.setToolbarColor(toolBarColor)
        options.setStatusBarColor(statusBarColor)
        options.setActiveWidgetColor(bottomBarColor)
        options.setCompressionQuality(100)
        uCrop.withOptions(options)
        uCrop.start(this@ImageSelectorActivity, requestCode)
    }


    /**
     * 修改topbar的颜色
     *
     * @param color 颜色值
     */
    private fun setToolBarColor(@ColorInt color: Int) {
        toolbar!!.setBackgroundColor(color)
    }

    /**
     * 修改bottombar的颜色
     *
     * @param color 颜色值
     */
    private fun setBottomBarColor(@ColorInt color: Int) {
        rlBottomBar!!.setBackgroundColor(color)
    }


    /**
     * 初始化图片列表
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initImageList() {
        // 判断屏幕方向
        val configuration = resources.configuration
        mLayoutManager = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager(this, column)
        } else {
            GridLayoutManager(this, 5)
        }
        rvImage!!.layoutManager = mLayoutManager
        mAdapter = ImageAdapter(this, mMaxCount, isSingle)
        rvImage!!.adapter = mAdapter
        (rvImage!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        if (mFolders != null && !mFolders!!.isEmpty()) {
            setFolder(mFolders!![0])
        }
        mAdapter!!.setOnImageSelectListener(object : ImageAdapter.OnImageSelectListener {
            override fun onImageSelect(image: Image, isSelect: Boolean, selectCount: Int) {
                setSelectImageCount(selectCount)
            }
        })

        mAdapter!!.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(image: Image, itemView: View, position: Int) {
                toPreviewActivity(false, mAdapter!!.getData(), position)
            }
        })


        mAdapter!!.setOnCameraClickListener(View.OnClickListener {
            if (!PermissionsUtils.checkCameraPermission(this@ImageSelectorActivity)) return@OnClickListener
            if (!PermissionsUtils.checkWriteStoragePermission(this@ImageSelectorActivity))
                return@OnClickListener
            openCamera()
        })
    }

    private var filePath: String? = null

    private fun openCamera() {
        try {
            val intent = captureManager!!.dispatchTakePictureIntent()
            //如果设置了裁剪 拍照成功之后直接进行剪切界面，则传递 TAKE_PHOTO_CROP_REQUESTCODE 然后再onActivityResult中进行判断
            if (isCrop && isSingle) {
                //获取拍照保存的照片的路径
                filePath = intent.getStringExtra(ImageCaptureManager.PHOTO_PATH)
                startActivityForResult(intent, PhotoSelector.TAKE_PHOTO_CROP_REQUESTCODE)
            } else {
                startActivityForResult(intent, PhotoSelector.TAKE_PHOTO_REQUESTCODE)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ActivityNotFoundException) {
            Log.e("PhotoPickerFragment", "No Activity Found to handle Intent", e)
        }
    }

    /**
     * 初始化图片文件夹列表
     */
    private fun initFolderList() {
        if (mFolders != null && !mFolders!!.isEmpty()) {
            isInitFolder = true
            rvFolder!!.layoutManager = LinearLayoutManager(this@ImageSelectorActivity)
            val adapter = FolderAdapter(this@ImageSelectorActivity, mFolders!!)
            adapter.setOnFolderSelectListener(object : FolderAdapter.OnFolderSelectListener {
                override fun onFolderSelect(folder: Folder) {
                    setFolder(folder)
                    closeFolder()
                }
            })
            rvFolder!!.adapter = adapter
        }
    }

    /**
     * 刚开始的时候文件夹列表默认是隐藏的
     */
    private fun hideFolderList() {}


    /**
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder 文件夹
     */
    private fun setFolder(folder: Folder) {
        if (mAdapter != null && folder != mFolder) {
            mFolder = folder
            tvFolderName!!.text = folder.name
            rvImage!!.scrollToPosition(0)
            //如果不是文件夹不是全部图片那么不需要显示牌照
            mAdapter!!.refresh(folder.images!!, folder.useCamera)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSelectImageCount(count: Int) {
        if (count == 0) {
            btnConfirm!!.isEnabled = false
            btnPreview!!.isEnabled = false
            tvConfirm!!.text = getString(R.string.confirm)
            tvPreview!!.text = getString(R.string.preview)
        } else {
            btnConfirm!!.isEnabled = true
            btnPreview!!.isEnabled = true
            tvPreview!!.text = getString(R.string.preview_count, count)
            when {
                isSingle -> tvConfirm!!.text = getString(R.string.confirm)
                mMaxCount > 0 -> tvConfirm!!.text = getString(R.string.confirm_maxcount, count, mMaxCount)
                else -> tvConfirm!!.text = getString(R.string.confirm_count, count)
            }
        }
    }

    /**
     * 弹出文件夹列表
     */
    private fun openFolder() {
        bottomSheetDialog!!.show()
    }

    /**
     * 收起文件夹列表
     */
    private fun closeFolder() {
        bottomSheetDialog!!.dismiss()
    }

    /**
     * 隐藏时间条
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun hideTime() {
        if (isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 1f, 0f).setDuration(300).start()
            isShowTime = false
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun showTime() {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 0f, 1f).setDuration(300).start()
            isShowTime = true
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private fun changeTime() {
        val firstVisibleItem = getFirstVisibleItem()
        if (firstVisibleItem > 0 && firstVisibleItem < mAdapter!!.getData().size) {
            val image = mAdapter!!.getData()[firstVisibleItem]
            val time = DateUtils.getImageTime(image.time * 1000)
            tvTime!!.text = time
            showTime()
            mHideHandler.removeCallbacks(mHide)
            mHideHandler.postDelayed(mHide, 1500)
        }
    }

    private fun getFirstVisibleItem(): Int {
        return mLayoutManager!!.findFirstVisibleItemPosition()
    }

    private fun confirm() {
        if (mAdapter == null) {
            return
        }
        //因为图片的实体类是Image，而我们返回的是String数组，所以要进行转换。
        val selectImages = mAdapter!!.getSelectImages()
        val images = ArrayList<String>()
        for (image in selectImages) {
            images.add(image.path)
        }
        //点击确定，把选中的图片通过Intent传给上一个Activity。
        val intent = Intent()
        intent.putStringArrayListExtra(PhotoSelector.SELECT_RESULT, images)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun toPreviewActivity(isPreview: Boolean, images: ArrayList<Image>?, position: Int) {
        if (images != null && !images.isEmpty()) {
            RvPreviewActivity.openActivity(isPreview, this, images,
                    mAdapter!!.getSelectImages(), isSingle, mMaxCount, position, toolBarColor, bottomBarColor, statusBarColor)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isToSettings) {
            isToSettings = false
            checkPermissionAndLoadImages()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PhotoSelector.RESULT_CODE -> {//预览
                if (data != null && data.getBooleanExtra(PhotoSelector.IS_CONFIRM, false)) {
                    //如果用户在预览界面点击了确定，并且是单选裁剪模式那么就进行裁剪
                    if (isSingle && isCrop) {
                        crop(mAdapter!!.getSelectImages()[0].path, UCrop.REQUEST_CROP)
                    } else {
                        //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                        confirm()
                    }
                } else {
                    //否则，就刷新当前页面。
                    mAdapter!!.notifyDataSetChanged()
                    setSelectImageCount(mAdapter!!.getSelectImages().size)
                }
            }
            PhotoSelector.TAKE_PHOTO_REQUESTCODE -> {//拍照不裁剪
                //拍照完成了，重新加载照片的列表，不进入剪切界面
                loadImageForSDCard()
                setSelectImageCount(mAdapter!!.getSelectImages().size)
                mSelectedImages = ArrayList()
                for (image in mAdapter!!.getSelectImages()) {
                    mSelectedImages!!.add(image.path)
                }
                mAdapter!!.setSelectedImages(mSelectedImages!!)
                mAdapter!!.notifyDataSetChanged()
            }

            PhotoSelector.TAKE_PHOTO_CROP_REQUESTCODE -> {//拍照裁剪，进入裁剪界面传递requestcode
                //拍照完成了，获取到照片之后直接进入剪切界面,用户可能没有确定剪切
                crop(filePath!!, PhotoSelector.CROP_REQUESTCODE)
            }
            UCrop.REQUEST_CROP -> {
                //选择之后裁剪，获取到裁剪的数据直接返回
                if (data != null) {
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } else {
                    //如果选择之后没有裁剪 用户按返回键的话，那么data就是null做下判断，就刷新当前页面。
                    mAdapter!!.notifyDataSetChanged()
                    setSelectImageCount(mAdapter!!.getSelectImages().size)
                }
            }

            PhotoSelector.CROP_REQUESTCODE -> {//拍照成功之后去裁剪，裁剪返回的结果
                //拍照之后裁剪，如果拍照成功之后没有裁剪 用户按返回键的话，那么data就是null的做下判断，就刷新列表加载出用户拍照的照片
                // 如果data不为null就说明他裁剪了
                if (data != null) {
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } else {
                    loadImageForSDCard()
                    setSelectImageCount(mAdapter!!.getSelectImages().size)
                    mSelectedImages = ArrayList()
                    for (image in mAdapter!!.getSelectImages()) {
                        mSelectedImages!!.add(image.path)
                    }
                    mAdapter!!.setSelectedImages(mSelectedImages!!)
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 横竖屏切换处理
     *
     * @param newConfig newConfig
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (mLayoutManager != null && mAdapter != null) {
            //切换为竖屏
            if (newConfig!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLayoutManager!!.spanCount = 3

            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//切换为横屏
                mLayoutManager!!.spanCount = 5
            }
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private fun checkPermissionAndLoadImages() {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return
        }
        val hasWriteContactsPermission = ContextCompat.checkSelfPermission(application,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，加载图片。
            loadImageForSDCard()
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this@ImageSelectorActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，加载图片。
                loadImageForSDCard()
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog()
            }
        }
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private fun showExceptionDialog() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("该相册需要赋予访问存储的权限，请到“设置”>“应用”>“权限”中配置权限。")
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.cancel()
                    finish()
                }.setPositiveButton("确定") { dialog, _ ->
                    dialog.cancel()
                    startAppSettings()
                    isToSettings = true
                }.show()
    }


    /**
     * 从SDCard加载图片。
     */
    private fun loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this, object : ImageModel.DataCallback {
            override fun onSuccess(folders: ArrayList<Folder>) {
                mFolders = folders
                runOnUiThread {
                    if (mFolders != null && !mFolders!!.isEmpty()) {
                        initFolderList()
                        mFolders!![0].useCamera = showCamera
                        setFolder(mFolders!![0])
                        if (mSelectedImages != null && mAdapter != null) {
                            mAdapter!!.setSelectedImages(mSelectedImages!!)
                            mSelectedImages = null
                        }
                    }
                }
            }
        })
    }

    /**
     * 启动应用的设置
     */
    private fun startAppSettings() {
        val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }


}

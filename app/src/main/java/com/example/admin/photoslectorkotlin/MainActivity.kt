package com.example.admin.photoslectorkotlin

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.winfo.photoselector.PhotoSelector
import java.util.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {


    companion object {
        private const val SINGLE_CODE = 1//单选
        private const val LIMIT_CODE = 2//多选限制数量
        private const val CROP_CODE = 3//剪切裁剪
        private const val UN_LIMITT_CODE = 4//多选不限制数量
    }

    private var mAdapter: ImageAdapter? = null
    private var imageView: ImageView? = null
    private var images: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageview)
        val rvImage = findViewById<RecyclerView>(R.id.rv_image)
        rvImage.layoutManager = GridLayoutManager(this, 3)
        mAdapter = ImageAdapter(this)
        rvImage.adapter = mAdapter

        findViewById<View>(R.id.btn_single).setOnClickListener(this)
        findViewById<View>(R.id.btn_limit).setOnClickListener(this)
        findViewById<View>(R.id.btn_unlimited).setOnClickListener(this)
        findViewById<View>(R.id.btn_clip).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_single ->
                //单选
                PhotoSelector.builder()
                        .setSingle(true)
                        .start(this@MainActivity, SINGLE_CODE)

            R.id.btn_limit ->
                //多选(最多9张)
                PhotoSelector.builder()
                        .setShowCamera(true)//显示拍照
                        .setMaxSelectCount(9)//最大选择9 默认9，如果这里设置为-1则是不限数量
                        .setSelected(images)//已经选择的照片
                        .setGridColumnCount(3)//列数
                        .setMaterialDesign(true)//design风格
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//toolbar的颜色
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//底部bottombar的颜色
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//状态栏的颜色
                        .start(this@MainActivity, LIMIT_CODE)//当前activity 和 requestCode，不传requestCode则默认为PhotoSelector.DEFAULT_REQUEST_CODE

            R.id.btn_unlimited ->
                //多选(不限数量)
                PhotoSelector.builder()
                        .setMaxSelectCount(-1)//-1不限制数量
                        .setSelected(images)
                        .start(this@MainActivity, UN_LIMITT_CODE)

            R.id.btn_clip ->
                //单选后剪裁 裁剪的话都是针对一张图片所以要设置setSingle(true)
                PhotoSelector.builder()
                        .setSingle(true)//单选，裁剪都是单选
                        .setCrop(true)//是否裁剪
                        .setCropMode(PhotoSelector.CROP_RECTANG)//设置裁剪模式 矩形还是圆形
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .start(this@MainActivity, CROP_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                SINGLE_CODE -> {
                    //单选的话 images就只有一条数据直接get(0)即可
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT)
                    mAdapter!!.refresh(images)
                }
                LIMIT_CODE -> {
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT)
                    mAdapter!!.refresh(images)
                }
                CROP_CODE -> {
                    //获取到裁剪后的图片的Uri进行处理
                    val resultUri = PhotoSelector.getCropImageUri(data)
                    Glide.with(this).load(resultUri).into(imageView!!)
                }
                UN_LIMITT_CODE -> {
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT)
                    mAdapter!!.refresh(images)
                }
            }
        }
    }
}

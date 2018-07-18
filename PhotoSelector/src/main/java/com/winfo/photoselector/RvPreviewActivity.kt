package com.winfo.photoselector

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ObjectAnimator.ofFloat
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.*
import com.winfo.photoselector.adapter.BottomPreviewAdapter
import com.winfo.photoselector.adapter.PreviewImageAdapter
import com.winfo.photoselector.entity.Image
import com.winfo.photoselector.utils.ImageUtil
import com.winfo.photoselector.utils.StatusBarUtils
import com.winfo.photoselector.utils.find
import java.util.ArrayList

class RvPreviewActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var tvConfirm: TextView? = null
    private var btnConfirm: FrameLayout? = null
    private var tvSelect: TextView? = null
    private var rlBottomBar: RelativeLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null

    //tempImages和tempSelectImages用于图片列表数据的页面传输。
    //之所以不要Intent传输这两个图片列表，因为要保证两位页面操作的是同一个列表数据，同时可以避免数据量大时，
    // 用Intent传输发生的错误问题。
    companion object {
        private var tempImages: ArrayList<Image>? = null
        private var tempSelectImages: ArrayList<Image>? = null

        /**
         * @param activity       activity
         * @param images         images
         * @param selectImages   选中的图片
         * @param isSingle       是否单选
         * @param maxSelectCount 最大选择数
         * @param position       posttion
         * @param toolBarColor   toolBarColor颜色值
         * @param bottomBarColor bottomBarColor颜色值
         * @param statusBarColor statusBarColor颜色值
         */

        fun openActivity(isPreview: Boolean, activity: Activity, images: ArrayList<Image>,
                         selectImages: ArrayList<Image>,
                         isSingle: Boolean,
                         maxSelectCount: Int,
                         position: Int,
                         @ColorInt toolBarColor: Int,
                         @ColorInt bottomBarColor: Int,
                         @ColorInt statusBarColor: Int) {

            tempImages = images
            tempSelectImages = selectImages
            val intent = Intent(activity, RvPreviewActivity::class.java)
            intent.putExtra(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, maxSelectCount)
            intent.putExtra(PhotoSelector.EXTRA_SINGLE, isSingle)
            intent.putExtra(PhotoSelector.EXTRA_POSITION, position)
            intent.putExtra(PhotoSelector.EXTRA_ISPREVIEW, isPreview)
            intent.putExtra(PhotoSelector.EXTRA_TOOLBARCOLOR, toolBarColor)
            intent.putExtra(PhotoSelector.EXTRA_BOTTOMBARCOLOR, bottomBarColor)
            intent.putExtra(PhotoSelector.EXTRA_STATUSBARCOLOR, statusBarColor)
            activity.startActivityForResult(intent, PhotoSelector.RESULT_CODE)
        }
    }

    private var mImages: ArrayList<Image>? = null
    private var mSelectImages: ArrayList<Image>? = null
    private var isShowBar = true
    private var isConfirm = false
    private var isSingle: Boolean = false
    private var mMaxCount: Int = 0

    private var mSelectDrawable: BitmapDrawable? = null
    private var mUnSelectDrawable: BitmapDrawable? = null

    /*-----------------------------------*/
    private var bottomRecycleview: RecyclerView? = null
    private var bottomPreviewAdapter: BottomPreviewAdapter? = null
    private var line: View? = null
    private var isPreview: Boolean = false//是否点击预览按钮进入此页面


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rv_preview)
        appBarLayout = find(R.id.appbar)
        toolbar = find(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener({
            finish()
        })

        setStatusBarVisible(true)
        mImages = tempImages
        tempImages = null
        mSelectImages = tempSelectImages
        tempSelectImages = null

        val intent = intent
        mMaxCount = intent.getIntExtra(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, 0)
        isSingle = intent.getBooleanExtra(PhotoSelector.EXTRA_SINGLE, false)
        isPreview = intent.getBooleanExtra(PhotoSelector.EXTRA_ISPREVIEW, false)
        val resources: Resources = resources
        val selectBitmap = ImageUtil.getBitmap(this, R.drawable.ic_image_select)
        mSelectDrawable = BitmapDrawable(resources, selectBitmap)
        mSelectDrawable!!.setBounds(0, 0, selectBitmap.width, selectBitmap.height)

        val unSelectBitmap = ImageUtil.getBitmap(this, R.drawable.ic_image_un_select)
        mUnSelectDrawable = BitmapDrawable(resources, unSelectBitmap)
        mUnSelectDrawable!!.setBounds(0, 0, unSelectBitmap.width, unSelectBitmap.height)

        val toolBarColor = intent.getIntExtra(PhotoSelector.EXTRA_TOOLBARCOLOR, ContextCompat.getColor(this, R.color.blue))
        val bottomBarColor = intent.getIntExtra(PhotoSelector.EXTRA_BOTTOMBARCOLOR, ContextCompat.getColor(this, R.color.blue))
        val statusBarColor = intent.getIntExtra(PhotoSelector.EXTRA_STATUSBARCOLOR, ContextCompat.getColor(this, R.color.blue))

        initView()

        StatusBarUtils.setBarColor(this, statusBarColor)
        setToolBarColor(toolBarColor)
        setBottomBarColor(bottomBarColor)
        initListener()
        initViewPager()

        changeSelect(mImages!![intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0)])
        recyclerView!!.scrollToPosition(intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0))
        toolbar!!.title = (intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0) + 1).toString() + "/" + mImages!!.size
        if (isPreview) {
            bottomRecycleview!!.smoothScrollToPosition(0)
        }
    }

    private fun initView() {
        recyclerView = find(R.id.rv_preview)
        tvConfirm = find(R.id.tv_confirm)
        btnConfirm = find(R.id.btn_confirm)
        tvSelect = find(R.id.tv_select)
        rlBottomBar = find(R.id.rl_bottom_bar)
        bottomRecycleview = find(R.id.bottom_recycleview)
        line = find(R.id.line)
        bottomRecycleview!!.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        if (mSelectImages!!.size == 0) {
            bottomRecycleview!!.visibility = View.GONE
            line!!.visibility = View.GONE
        }
        bottomPreviewAdapter = BottomPreviewAdapter(this, mSelectImages!!)
        bottomRecycleview!!.adapter = bottomPreviewAdapter

    }

    private fun initListener() {
        btnConfirm!!.setOnClickListener({
            isConfirm = true
            finish()
        })

        tvSelect!!.setOnClickListener({
            clickSelect()
        })

        bottomPreviewAdapter!!.setOnItemClcikLitener(object : BottomPreviewAdapter.OnItemClcikLitener {
            override fun onItemClcik(position: Int, image: Image) {
                if (isPreview) {
                    val imageList = previewImageAdapter!!.getData()
                    for (i in imageList.indices) {
                        if (imageList[i] == image) {
                            recyclerView!!.smoothScrollToPosition(i)
                        }
                    }
                } else {
                    recyclerView!!.smoothScrollToPosition(mSelectImages!![position].position)
                }
                bottomPreviewAdapter!!.notifyDataSetChanged()
            }
        })
    }

    /**
     * 初始化ViewPager
     */
    private var previewImageAdapter: PreviewImageAdapter? = null

    private fun initViewPager() {
        recyclerView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        linearLayoutManager = recyclerView!!.layoutManager as LinearLayoutManager
        previewImageAdapter = PreviewImageAdapter(this, mImages!!)
        recyclerView!!.adapter = previewImageAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        previewImageAdapter!!.setOnItemClcikLitener(object : PreviewImageAdapter.OnItemClcikLitener {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onItemClcik(previewImageAdapter: PreviewImageAdapter, iteView: View, position: Int) {
                if (isShowBar) {
                    hideBar()
                } else {
                    showBar()
                }
            }
        })

        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = linearLayoutManager!!.findLastVisibleItemPosition()
                    mImages!![position].position = position
                    toolbar!!.title = (position + 1).toString() + "/" + mImages!!.size
                    changeSelect(mImages!![position])
                }
            }
        })
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
     * 显示和隐藏状态栏
     *
     * @param show 是否显示
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun setStatusBarVisible(show: Boolean) {
        if (show) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    /**
     * 显示头部和尾部栏
     */
    @SuppressLint("ObjectAnimatorBinding")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun showBar() {
        isShowBar = true
        setStatusBarVisible(true)
        //添加延时，保证StatusBar完全显示后再进行动画。
        appBarLayout!!.postDelayed({
            val animator = ofFloat(appBarLayout, "translationY",
                    appBarLayout!!.translationY, 0f).setDuration(300)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    appBarLayout!!.visibility = View.VISIBLE
                }
            })
            animator.start()
            ofFloat(rlBottomBar, "translationY", rlBottomBar!!.translationY, 0f)
                    .setDuration(300).start()
        }, 100)
    }

    /**
     * 隐藏头部和尾部栏
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun hideBar() {
        isShowBar = false
        val animator = ObjectAnimator.ofFloat(appBarLayout, "translationY",
                0f, (-appBarLayout!!.height).toFloat()).setDuration(300)
        animator.addListener(object : AnimatorListenerAdapter() {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                appBarLayout!!.visibility = View.GONE
                //添加延时，保证rlTopBar完全隐藏后再隐藏StatusBar。
                appBarLayout!!.postDelayed({
                    setStatusBarVisible(false)
                }, 100)
            }
        })
        animator.start()
        ofFloat(rlBottomBar, "translationY", 0f, rlBottomBar!!.height.toFloat())
                .setDuration(300).start()
    }

    private fun clickSelect() {
        val position = linearLayoutManager!!.findFirstVisibleItemPosition()
        if (mImages != null && mImages!!.size > position) {
            val image = mImages!![position]
            if (mSelectImages!!.contains(image)) {
                mSelectImages!!.remove(image)
            } else if (isSingle) {
                mSelectImages!!.clear()
                mSelectImages!!.add(image)
            } else if (mMaxCount <= 0 || mSelectImages!!.size < mMaxCount) {
                mSelectImages!!.add(image)
            } else {
                Toast.makeText(this@RvPreviewActivity, "最多只能选" + mMaxCount + "张", Toast.LENGTH_SHORT).show()
            }
            bottomPreviewAdapter!!.referesh(mSelectImages!!)
            bottomPreviewAdapter!!.notifyDataSetChanged()
            changeSelect(image)
        }
        if (mSelectImages!!.size > 0) {
            bottomRecycleview!!.visibility = View.VISIBLE
            line!!.visibility = View.VISIBLE
        } else {
            bottomRecycleview!!.visibility = View.GONE
            line!!.visibility = View.GONE
        }
    }

    private fun changeSelect(image: Image) {
        tvSelect!!.setCompoundDrawables(if (mSelectImages!!.contains(image))
            mSelectDrawable
        else
            mUnSelectDrawable, null, null, null)

        setSelectImageCount(mSelectImages!!.size)
        //清空所有选择的照片的边框背景
        for (image1 in mSelectImages!!) {
            image1.isChecked = false
        }
        //设置当前选中打的照片的背景
        image.isChecked = true
        bottomPreviewAdapter!!.referesh(mSelectImages!!)
        bottomPreviewAdapter!!.notifyDataSetChanged()
        if (mSelectImages!!.contains(image)) {
            bottomRecycleview!!.smoothScrollToPosition(image.selectPosition)
        }
    }

    private fun setSelectImageCount(count: Int) {
        if (count == 0) {
            btnConfirm!!.isEnabled = false
            tvConfirm!!.text = getString(R.string.confirm)
        } else {
            btnConfirm!!.isEnabled = true
            when {
                isSingle -> tvConfirm!!.text = getString(R.string.confirm)
                mMaxCount > 0 -> tvConfirm!!.text = getString(R.string.confirm_maxcount, count, mMaxCount)
                else -> tvConfirm!!.text = getString(R.string.confirm_count, count)
            }
        }
    }

    override fun finish() {
        //Activity关闭时，通过Intent把用户的操作(确定/返回)传给ImageSelectActivity。
        val intent = Intent()
        intent.putExtra(PhotoSelector.IS_CONFIRM, isConfirm)
        setResult(PhotoSelector.RESULT_CODE, intent)
        super.finish()
    }
}

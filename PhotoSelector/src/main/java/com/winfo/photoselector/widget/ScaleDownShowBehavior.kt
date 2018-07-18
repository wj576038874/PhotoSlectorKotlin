package com.winfo.photoselector.widget

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.util.AttributeSet
import android.view.View

@SuppressWarnings("unused")
class ScaleDownShowBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var isAnimateIng = false   // 是否正在动画
    private var isShow = true  // 是否已经显示

//    constructor() : super()
//
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        if ((dyConsumed > 0 || dyUnconsumed > 0) && !isAnimateIng && isShow) {
            AnimatorUtil.translateHide(child, object : StateListener() {
                override fun onAnimationStart(view: View) {
                    super.onAnimationStart(view)
                    isShow = false
                }
            })
        } else if ((dyConsumed < 0 || dyUnconsumed < 0 && !isAnimateIng && !isShow)) {
            AnimatorUtil.translateShow(child, object : StateListener() {
                override fun onAnimationStart(view: View) {
                    super.onAnimationStart(view)
                    isShow = true
                }
            })
        }
    }

    internal open inner class StateListener : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {
            isAnimateIng = true
        }

        override fun onAnimationEnd(view: View) {
            isAnimateIng = false
        }

        override fun onAnimationCancel(view: View) {
            isAnimateIng = false
        }
    }
}
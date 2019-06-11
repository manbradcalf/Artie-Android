package com.bookyrself.bookyrself.utils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Created by benmedcalf on 4/2/18.
 */

class FragmentViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var isPagingEnabled: Boolean = false

    init {
        this.isPagingEnabled = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onTouchEvent(event)
    }

    //for samsung phones to prevent tab switching keys to show on keyboard
    override fun executeKeyEvent(event: KeyEvent): Boolean {
        return isPagingEnabled && super.executeKeyEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.isPagingEnabled = enabled
    }
}

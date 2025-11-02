/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.SeekBar
import kotlin.math.max

class BacklightTimeoutSeekBar : SeekBar {
    private var mMax: Int = 0
    private var mGap: Int = 0
    private var mUpdatingThumb: Boolean = false

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        super(context, attrs, defStyle)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mUpdatingThumb = true
        super.onSizeChanged(w, h, oldw, oldh)
        mUpdatingThumb = false
    }

    override fun setThumb(thumb: Drawable?) {
        mUpdatingThumb = true
        super.setThumb(thumb)
        mUpdatingThumb = false
    }

    override fun setMax(max: Int) {
        mMax = max
        mGap = max / 10
        // Call super.setMax with the calculated value
        super.setMax(max + 2 * mGap - 1)
    }
    
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("updateTouchProgress")
    protected fun updateTouchProgress(lastProgress: Int, newProgress: Int): Int {
        if (newProgress < mMax) {
            return newProgress
        }
        if (newProgress < mMax + mGap) {
            return mMax - 1
        }
        return getMax()
    }
}


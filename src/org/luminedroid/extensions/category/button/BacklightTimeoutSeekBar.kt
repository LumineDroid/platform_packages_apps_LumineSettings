/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.button

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.SeekBar

class BacklightTimeoutSeekBar : SeekBar {
    private var mMax: Int = 0
    private var mGap: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun setThumb(thumb: Drawable?) {
        super.setThumb(thumb)
    }

    override fun setMax(max: Int) {
        mMax = max
        mGap = max / 10
        super.setMax(max + 2 * mGap - 1)
    }

    override fun updateTouchProgress(lastProgress: Int, newProgress: Int): Int {
        if (newProgress < mMax) {
            return newProgress
        }
        if (newProgress < mMax + mGap) {
            return mMax - 1
        }
        return getMax()
    }
}


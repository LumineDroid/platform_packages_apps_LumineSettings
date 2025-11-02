/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.math.ceil

class AlphaPatternDrawable(private val mRectangleSize: Int = 10) : Drawable() {

    private val mPaint: Paint = Paint()
    private val mPaintWhite: Paint = Paint().apply { color = 0xffffffff.toInt() }
    private val mPaintGray: Paint = Paint().apply { color = 0xffcbcbcb.toInt() }

    private var numRectanglesHorizontal: Int = 0
    private var numRectanglesVertical: Int = 0

    private lateinit var mBitmap: Bitmap

    init {
    }

    override fun draw(canvas: Canvas) {
        if (::mBitmap.isInitialized) {
            canvas.drawBitmap(mBitmap, null, bounds, mPaint)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return 0
    }

    override fun setAlpha(alpha: Int) {
        throw UnsupportedOperationException("Alpha is not supported by this drawable.")
    }

    override fun setColorFilter(cf: ColorFilter?) {
        throw UnsupportedOperationException("ColorFilter is not supported by this drawable.")
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        val height = bounds.height()
        val width = bounds.width()

        numRectanglesHorizontal = ceil(width.toFloat() / mRectangleSize.toFloat()).toInt()
        numRectanglesVertical = ceil(height.toFloat() / mRectangleSize.toFloat()).toInt()

        generatePatternBitmap()
    }

    private fun generatePatternBitmap() {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            return
        }

        mBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Config.ARGB_8888)
        val canvas = Canvas(mBitmap)

        val r = Rect()
        var verticalStartWhite = true
        for (i in 0..numRectanglesVertical) {

            var isWhite = verticalStartWhite
            for (j in 0..numRectanglesHorizontal) {

                r.top = i * mRectangleSize
                r.left = j * mRectangleSize
                r.bottom = r.top + mRectangleSize
                r.right = r.left + mRectangleSize

                canvas.drawRect(r, if (isWhite) mPaintWhite else mPaintGray)

                isWhite = !isWhite
            }

            verticalStartWhite = !verticalStartWhite
        }
    }
}


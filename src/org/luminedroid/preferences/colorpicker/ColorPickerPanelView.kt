/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class ColorPickerPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private companion object {
        private const val BORDER_WIDTH_PX = 1f
    }

    private var mDensity: Float = 1f

    private var mBorderColor: Int = -0x919192 // 0xff6E6E6E
    private var mColor: Int = -0x1000000 // 0xff000000

    private lateinit var mBorderPaint: Paint
    private lateinit var mColorPaint: Paint

    private var mDrawingRect: RectF? = null
    private var mColorRect: RectF? = null

    private var mAlphaPattern: AlphaPatternDrawable? = null

    init {
        init()
    }

    private fun init() {
        mBorderPaint = Paint()
        mColorPaint = Paint()
        mDensity = context.resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {

        val rect = mColorRect
        val drawingRect = mDrawingRect

        if (drawingRect == null || rect == null) return

        if (BORDER_WIDTH_PX > 0) {
            mBorderPaint.color = mBorderColor
            canvas.drawRect(drawingRect, mBorderPaint)
        }

        mAlphaPattern?.draw(canvas)

        mColorPaint.color = mColor

        canvas.drawRect(rect, mColorPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mDrawingRect = RectF().apply {
            left = paddingLeft.toFloat()
            right = (w - paddingRight).toFloat()
            top = paddingTop.toFloat()
            bottom = (h - paddingBottom).toFloat()
        }

        setUpColorRect()
    }

    private fun setUpColorRect() {
        val dRect = mDrawingRect ?: return

        val left = dRect.left + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        val bottom = dRect.bottom - BORDER_WIDTH_PX
        val right = dRect.right - BORDER_WIDTH_PX

        mColorRect = RectF(left, top, right, bottom)

        mAlphaPattern = AlphaPatternDrawable((5 * mDensity).toInt())

        val cRect = mColorRect ?: return

        mAlphaPattern?.setBounds(
            cRect.left.roundToInt(),
            cRect.top.roundToInt(),
            cRect.right.roundToInt(),
            cRect.bottom.roundToInt()
        )
    }

    fun setColor(color: Int) {
        mColor = color
        invalidate()
    }

    fun getColor(): Int = mColor

    fun setBorderColor(color: Int) {
        mBorderColor = color
        invalidate()
    }

    fun getBorderColor(): Int = mBorderColor
}


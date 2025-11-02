/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private companion object {
        private const val PANEL_SAT_VAL = 0
        private const val PANEL_HUE = 1
        private const val PANEL_ALPHA = 2

        private const val BORDER_WIDTH_PX = 1f
    }

    private var HUE_PANEL_WIDTH = 30f

    private var ALPHA_PANEL_HEIGHT = 20f

    private var PANEL_SPACING = 10f

    private var PALETTE_CIRCLE_TRACKER_RADIUS = 5f

    private var RECTANGLE_TRACKER_OFFSET = 2f

    private var mDensity = 1f

    private var mListener: OnColorChangedListener? = null

    private lateinit var mSatValPaint: Paint
    private lateinit var mSatValTrackerPaint: Paint

    private lateinit var mHuePaint: Paint
    private lateinit var mHueTrackerPaint: Paint

    private lateinit var mAlphaPaint: Paint
    private lateinit var mAlphaTextPaint: Paint

    private lateinit var mBorderPaint: Paint

    private var mValShader: Shader? = null
    private var mSatShader: Shader? = null
    private var mHueShader: Shader? = null
    private var mAlphaShader: Shader? = null

    private var mAlpha: Int = 0xff
    private var mHue: Float = 360f
    private var mSat: Float = 0f
    private var mVal: Float = 0f

    private var mAlphaSliderText: String = ""
    private var mSliderTrackerColor: Int = 0xff1c1c1c.toInt()
    private var mBorderColor: Int = 0xff6E6E6E.toInt()
    private var mShowAlphaPanel: Boolean = false

    private var mLastTouchedPanel: Int = PANEL_SAT_VAL

    private var mDrawingOffset: Float = 0f

    private var mDrawingRect: RectF? = null

    private var mSatValRect: RectF? = null
    private var mHueRect: RectF? = null
    private var mAlphaRect: RectF? = null

    private var mAlphaPattern: AlphaPatternDrawable? = null

    private var mStartTouchPoint: Point? = null

    interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    init {
        init()
    }

    private fun init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        mDensity = context.resources.displayMetrics.density
        PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity
        RECTANGLE_TRACKER_OFFSET *= mDensity
        HUE_PANEL_WIDTH *= mDensity
        ALPHA_PANEL_HEIGHT *= mDensity
        PANEL_SPACING = PANEL_SPACING * mDensity

        mDrawingOffset = calculateRequiredOffset()

        initPaintTools()

        isFocusable = true
        isFocusableInTouchMode = true
    }

    private fun initPaintTools() {

        mSatValPaint = Paint()
        mSatValTrackerPaint = Paint()
        mHuePaint = Paint()
        mHueTrackerPaint = Paint()
        mAlphaPaint = Paint()
        mAlphaTextPaint = Paint()
        mBorderPaint = Paint()

        mSatValTrackerPaint.style = Style.STROKE
        mSatValTrackerPaint.strokeWidth = 2f * mDensity
        mSatValTrackerPaint.isAntiAlias = true

        mHueTrackerPaint.color = mSliderTrackerColor
        mHueTrackerPaint.style = Style.STROKE
        mHueTrackerPaint.strokeWidth = 2f * mDensity
        mHueTrackerPaint.isAntiAlias = true

        mAlphaTextPaint.color = 0xff1c1c1c.toInt()
        mAlphaTextPaint.textSize = 14f * mDensity
        mAlphaTextPaint.isAntiAlias = true
        mAlphaTextPaint.textAlign = Align.CENTER
        mAlphaTextPaint.isFakeBoldText = true
    }

    private fun calculateRequiredOffset(): Float {
        var offset = max(PALETTE_CIRCLE_TRACKER_RADIUS, RECTANGLE_TRACKER_OFFSET)
        offset = max(offset, BORDER_WIDTH_PX * mDensity)

        return offset * 1.5f
    }

    private fun buildHueColorArray(): IntArray {

        val hue = IntArray(361)

        var count = 0
        for (i in hue.indices.reversed()) {
            hue[count] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
            count++
        }

        return hue
    }

    override fun onDraw(canvas: Canvas) {

        if (mDrawingRect?.width() ?: 0f <= 0 || mDrawingRect?.height() ?: 0f <= 0) return

        drawSatValPanel(canvas)
        drawHuePanel(canvas)
        drawAlphaPanel(canvas)
    }

    private fun drawSatValPanel(canvas: Canvas) {

        val rect = mSatValRect ?: return
        val dRect = mDrawingRect ?: return

        if (BORDER_WIDTH_PX > 0) {
            mBorderPaint.color = mBorderColor
            canvas.drawRect(
                dRect.left,
                dRect.top,
                rect.right + BORDER_WIDTH_PX,
                rect.bottom + BORDER_WIDTH_PX,
                mBorderPaint
            )
        }

        if (mValShader == null) {
            mValShader =
                LinearGradient(
                    rect.left, rect.top, rect.left, rect.bottom, 0xffffffff.toInt(), 0xff000000.toInt(), TileMode.CLAMP
                )
        }

        val rgb = Color.HSVToColor(floatArrayOf(mHue, 1f, 1f))

        mSatShader =
            LinearGradient(
                rect.left, rect.top, rect.right, rect.top, 0xffffffff.toInt(), rgb, TileMode.CLAMP
            )
        val mShader = ComposeShader(mValShader!!, mSatShader!!, PorterDuff.Mode.MULTIPLY)
        mSatValPaint.shader = mShader

        canvas.drawRect(rect, mSatValPaint)

        val p = satValToPoint(mSat, mVal)

        mSatValTrackerPaint.color = 0xff000000.toInt()
        canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), PALETTE_CIRCLE_TRACKER_RADIUS - 1f * mDensity, mSatValTrackerPaint)

        mSatValTrackerPaint.color = 0xffdddddd.toInt()
        canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), PALETTE_CIRCLE_TRACKER_RADIUS, mSatValTrackerPaint)
    }

    private fun drawHuePanel(canvas: Canvas) {

        val rect = mHueRect ?: return

        if (BORDER_WIDTH_PX > 0) {
            mBorderPaint.color = mBorderColor
            canvas.drawRect(
                rect.left - BORDER_WIDTH_PX,
                rect.top - BORDER_WIDTH_PX,
                rect.right + BORDER_WIDTH_PX,
                rect.bottom + BORDER_WIDTH_PX,
                mBorderPaint
            )
        }

        if (mHueShader == null) {
            mHueShader =
                LinearGradient(
                    rect.left,
                    rect.top,
                    rect.left,
                    rect.bottom,
                    buildHueColorArray(),
                    null,
                    TileMode.CLAMP
                )
            mHuePaint.shader = mHueShader
        }

        canvas.drawRect(rect, mHuePaint)

        val rectHeight = 4 * mDensity / 2

        val p = hueToPoint(mHue)

        val r = RectF()
        r.left = rect.left - RECTANGLE_TRACKER_OFFSET
        r.right = rect.right + RECTANGLE_TRACKER_OFFSET
        r.top = p.y.toFloat() - rectHeight
        r.bottom = p.y.toFloat() + rectHeight

        canvas.drawRoundRect(r, 2f, 2f, mHueTrackerPaint)
    }

    private fun drawAlphaPanel(canvas: Canvas) {

        if (!mShowAlphaPanel) return
        val rect = mAlphaRect ?: return
        val alphaPattern = mAlphaPattern ?: return

        if (BORDER_WIDTH_PX > 0) {
            mBorderPaint.color = mBorderColor
            canvas.drawRect(
                rect.left - BORDER_WIDTH_PX,
                rect.top - BORDER_WIDTH_PX,
                rect.right + BORDER_WIDTH_PX,
                rect.bottom + BORDER_WIDTH_PX,
                mBorderPaint
            )
        }

        alphaPattern.draw(canvas)

        val hsv = floatArrayOf(mHue, mSat, mVal)
        val color = Color.HSVToColor(hsv)
        val acolor = Color.HSVToColor(0, hsv)

        mAlphaShader =
            LinearGradient(
                rect.left, rect.top, rect.right, rect.top, color, acolor, TileMode.CLAMP
            )

        mAlphaPaint.shader = mAlphaShader

        canvas.drawRect(rect, mAlphaPaint)

        if (mAlphaSliderText.isNotEmpty()) {
            canvas.drawText(
                mAlphaSliderText, rect.centerX(), rect.centerY() + 4 * mDensity, mAlphaTextPaint
            )
        }

        val rectWidth = 4 * mDensity / 2

        val p = alphaToPoint(mAlpha)

        val r = RectF()
        r.left = p.x.toFloat() - rectWidth
        r.right = p.x.toFloat() + rectWidth
        r.top = rect.top - RECTANGLE_TRACKER_OFFSET
        r.bottom = rect.bottom + RECTANGLE_TRACKER_OFFSET

        canvas.drawRoundRect(r, 2f, 2f, mHueTrackerPaint)
    }

    private fun hueToPoint(hue: Float): Point {

        val rect = mHueRect ?: return Point(0, 0)
        val height = rect.height()

        val p = Point()

        p.y = (height - (hue * height / 360f) + rect.top).roundToInt()
        p.x = rect.left.roundToInt()

        return p
    }

    private fun satValToPoint(sat: Float, valF: Float): Point {

        val rect = mSatValRect ?: return Point(0, 0)
        val height = rect.height()
        val width = rect.width()

        val p = Point()

        p.x = (sat * width + rect.left).roundToInt()
        p.y = ((1f - valF) * height + rect.top).roundToInt()

        return p
    }

    private fun alphaToPoint(alpha: Int): Point {

        val rect = mAlphaRect ?: return Point(0, 0)
        val width = rect.width()

        val p = Point()

        p.x = (width - (alpha * width / 0xff) + rect.left).roundToInt()
        p.y = rect.top.roundToInt()

        return p
    }

    private fun pointToSatVal(x: Float, y: Float): FloatArray {

        val rect = mSatValRect ?: return floatArrayOf(0f, 0f)
        val result = FloatArray(2)

        val width = rect.width()
        val height = rect.height()

        val finalX = when {
            x < rect.left -> 0f
            x > rect.right -> width
            else -> x - rect.left
        }

        val finalY = when {
            y < rect.top -> 0f
            y > rect.bottom -> height
            else -> y - rect.top
        }

        result[0] = 1.f / width * finalX
        result[1] = 1.f - (1.f / height * finalY)

        return result
    }

    private fun pointToHue(y: Float): Float {

        val rect = mHueRect ?: return 0f

        val height = rect.height()

        val finalY = when {
            y < rect.top -> 0f
            y > rect.bottom -> height
            else -> y - rect.top
        }

        return 360f - (finalY * 360f / height)
    }

    private fun pointToAlpha(x: Int): Int {

        val rect = mAlphaRect ?: return 0
        val width = rect.width().toInt()

        val finalX = when {
            x < rect.left -> 0
            x > rect.right -> width
            else -> x - rect.left.roundToInt()
        }

        return if (width == 0) 0 else 0xff - (finalX * 0xff / width)
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        var update = false

        if (event.action == MotionEvent.ACTION_MOVE) {

            when (mLastTouchedPanel) {
                PANEL_SAT_VAL -> {
                    var sat = mSat + x / 50f
                    var valF = mVal - y / 50f

                    if (sat < 0f) {
                        sat = 0f
                    } else if (sat > 1f) {
                        sat = 1f
                    }

                    if (valF < 0f) {
                        valF = 0f
                    } else if (valF > 1f) {
                        valF = 1f
                    }

                    mSat = sat
                    mVal = valF

                    update = true
                }

                PANEL_HUE -> {
                    var hue = mHue - y * 10f

                    if (hue < 0f) {
                        hue = 0f
                    } else if (hue > 360f) {
                        hue = 360f
                    }

                    mHue = hue

                    update = true
                }

                PANEL_ALPHA -> {
                    if (!mShowAlphaPanel || mAlphaRect == null) {
                        update = false
                    } else {
                        var alpha = (mAlpha - x * 10).roundToInt()

                        if (alpha < 0) {
                            alpha = 0
                        } else if (alpha > 0xff) {
                            alpha = 0xff
                        }

                        mAlpha = alpha

                        update = true
                    }
                }
            }
        }

        if (update) {

            mListener?.onColorChanged(Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal)))

            invalidate()
            return true
        }

        return super.onTrackballEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        var update = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartTouchPoint = Point(event.x.roundToInt(), event.y.roundToInt())
                update = moveTrackersIfNeeded(event)
            }

            MotionEvent.ACTION_MOVE -> {
                update = moveTrackersIfNeeded(event)
            }

            MotionEvent.ACTION_UP -> {
                mStartTouchPoint = null
                update = moveTrackersIfNeeded(event)
            }
        }

        if (update) {

            mListener?.onColorChanged(Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal)))

            invalidate()
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun moveTrackersIfNeeded(event: MotionEvent): Boolean {

        val startTouchPoint = mStartTouchPoint ?: return false

        var update = false

        val startX = startTouchPoint.x.toFloat()
        val startY = startTouchPoint.y.toFloat()

        if (mHueRect?.contains(startX, startY) == true) {
            mLastTouchedPanel = PANEL_HUE

            mHue = pointToHue(event.y)

            update = true
        } else if (mSatValRect?.contains(startX, startY) == true) {

            mLastTouchedPanel = PANEL_SAT_VAL

            val result = pointToSatVal(event.x, event.y)

            mSat = result[0]
            mVal = result[1]

            update = true
        } else if (mAlphaRect?.contains(startX, startY) == true) {

            mLastTouchedPanel = PANEL_ALPHA

            mAlpha = pointToAlpha(event.x.roundToInt())

            update = true
        }

        return update
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var width = 0
        var height = 0

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthAllowed = MeasureSpec.getSize(widthMeasureSpec)
        var heightAllowed = MeasureSpec.getSize(heightMeasureSpec)

        widthAllowed = chooseWidth(widthMode, widthAllowed)
        heightAllowed = chooseHeight(heightMode, heightAllowed)

        if (!mShowAlphaPanel) {

            height = (widthAllowed - PANEL_SPACING - HUE_PANEL_WIDTH).roundToInt()

            if (height > heightAllowed) {
                height = heightAllowed
                width = (height + PANEL_SPACING + HUE_PANEL_WIDTH).roundToInt()
            } else {
                width = widthAllowed
            }
        } else {

            width = (heightAllowed - ALPHA_PANEL_HEIGHT + HUE_PANEL_WIDTH).roundToInt()

            if (width > widthAllowed) {
                width = widthAllowed
                height = (widthAllowed - HUE_PANEL_WIDTH + ALPHA_PANEL_HEIGHT).roundToInt()
            } else {
                height = heightAllowed
            }
        }

        setMeasuredDimension(width, height)
    }

    private fun chooseWidth(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            preferredWidth
        }
    }

    private fun chooseHeight(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            preferredHeight
        }
    }

    private val preferredWidth: Int
        get() {
            var width = preferredHeight

            if (mShowAlphaPanel) {
                width -= (PANEL_SPACING + ALPHA_PANEL_HEIGHT).roundToInt()
            }

            return (width + HUE_PANEL_WIDTH + PANEL_SPACING).roundToInt()
        }

    private val preferredHeight: Int
        get() {
            var height = (200 * mDensity).roundToInt()

            if (mShowAlphaPanel) {
                height += (PANEL_SPACING + ALPHA_PANEL_HEIGHT).roundToInt()
            }

            return height
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mDrawingRect = RectF().apply {
            left = mDrawingOffset + paddingLeft
            right = w - mDrawingOffset - paddingRight
            top = mDrawingOffset + paddingTop
            bottom = h - mDrawingOffset - paddingBottom
        }

        setUpSatValRect()
        setUpHueRect()
        setUpAlphaRect()
    }

    private fun setUpSatValRect() {

        val dRect = mDrawingRect ?: return
        var panelSide = dRect.height() - BORDER_WIDTH_PX * 2

        if (mShowAlphaPanel) {
            panelSide -= PANEL_SPACING + ALPHA_PANEL_HEIGHT
        }

        val left = dRect.left + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        val bottom = top + panelSide
        val right = left + panelSide

        mSatValRect = RectF(left, top, right, bottom)
    }

    private fun setUpHueRect() {
        val dRect = mDrawingRect ?: return

        val left = dRect.right - HUE_PANEL_WIDTH + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        val bottom =
            dRect.bottom - BORDER_WIDTH_PX - (if (mShowAlphaPanel) (PANEL_SPACING + ALPHA_PANEL_HEIGHT) else 0f)
        val right = dRect.right - BORDER_WIDTH_PX

        mHueRect = RectF(left, top, right, bottom)
    }

    private fun setUpAlphaRect() {

        if (!mShowAlphaPanel) return

        val dRect = mDrawingRect ?: return

        val left = dRect.left + BORDER_WIDTH_PX
        val top = dRect.bottom - ALPHA_PANEL_HEIGHT + BORDER_WIDTH_PX
        val bottom = dRect.bottom - BORDER_WIDTH_PX
        val right = dRect.right - BORDER_WIDTH_PX

        mAlphaRect = RectF(left, top, right, bottom)

        mAlphaPattern = AlphaPatternDrawable((5 * mDensity).roundToInt())
        mAlphaPattern?.let {
            it.setBounds(
                mAlphaRect!!.left.roundToInt(),
                mAlphaRect!!.top.roundToInt(),
                mAlphaRect!!.right.roundToInt(),
                mAlphaRect!!.bottom.roundToInt()
            )
        }
    }

    fun setOnColorChangedListener(listener: OnColorChangedListener?) {
        mListener = listener
    }

    fun setBorderColor(color: Int) {
        mBorderColor = color
        invalidate()
    }

    fun getBorderColor(): Int {
        return mBorderColor
    }

    fun getColor(): Int {
        return Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal))
    }

    fun setColor(color: Int) {
        setColor(color, false)
    }

    fun setColor(color: Int, callback: Boolean) {

        val alpha = Color.alpha(color)
        val red = Color.red(color)
        val blue = Color.blue(color)
        val green = Color.green(color)

        val hsv = FloatArray(3)

        Color.RGBToHSV(red, green, blue, hsv)

        mAlpha = alpha
        mHue = hsv[0]
        mSat = hsv[1]
        mVal = hsv[2]

        if (callback) {
            mListener?.onColorChanged(Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal)))
        }

        invalidate()
    }

    fun getDrawingOffset(): Float {
        return mDrawingOffset
    }

    fun setAlphaSliderVisible(visible: Boolean) {

        if (mShowAlphaPanel != visible) {
            mShowAlphaPanel = visible

            mValShader = null
            mSatShader = null
            mHueShader = null
            mAlphaShader = null
            // Baris Java: ; dihilangkan

            requestLayout()
        }
    }

    fun setSliderTrackerColor(color: Int) {
        mSliderTrackerColor = color

        mHueTrackerPaint.color = mSliderTrackerColor

        invalidate()
    }

    fun getSliderTrackerColor(): Int {
        return mSliderTrackerColor
    }

    fun setAlphaSliderText(res: Int) {
        val text = context.getString(res)
        setAlphaSliderText(text)
    }

    fun setAlphaSliderText(text: String?) {
        mAlphaSliderText = text ?: ""
        invalidate()
    }

    fun getAlphaSliderText(): String {
        return mAlphaSliderText
    }
}


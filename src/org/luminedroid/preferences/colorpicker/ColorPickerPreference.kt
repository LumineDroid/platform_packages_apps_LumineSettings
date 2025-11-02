/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import kotlin.math.roundToInt

class ColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes),
    ColorPickerDialog.OnColorChangedListener {

    private companion object {
        private const val ANDROIDNS = "http://schemas.android.com/apk/res/android"
        private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
        
        fun createOvalShape(size: Int, color: Int): ShapeDrawable {
            val shape = ShapeDrawable(OvalShape())
            shape.intrinsicHeight = size
            shape.intrinsicWidth = size
            shape.paint.color = color
            return shape
        }

        @JvmStatic
        fun convertToARGB(color: Int): String {
            var alpha = Integer.toHexString(Color.alpha(color))
            var red = Integer.toHexString(Color.red(color))
            var green = Integer.toHexString(Color.green(color))
            var blue = Integer.toHexString(Color.blue(color))

            if (alpha.length == 1) {
                alpha = "0$alpha"
            }

            if (red.length == 1) {
                red = "0$red"
            }

            if (green.length == 1) {
                green = "0$green"
            }

            if (blue.length == 1) {
                blue = "0$blue"
            }

            return "#$alpha$red$green$blue"
        }

        @JvmStatic
        @Throws(NumberFormatException::class)
        fun convertToColorInt(argb: String): Int {
            var argbValue = if (argb.startsWith("#")) argb.replace("#", "") else argb

            var alpha = -1
            var red = -1
            var green = -1
            var blue = -1

            when (argbValue.length) {
                8 -> {
                    alpha = Integer.parseInt(argbValue.substring(0, 2), 16)
                    red = Integer.parseInt(argbValue.substring(2, 4), 16)
                    green = Integer.parseInt(argbValue.substring(4, 6), 16)
                    blue = Integer.parseInt(argbValue.substring(6, 8), 16)
                }
                6 -> {
                    alpha = 255
                    red = Integer.parseInt(argbValue.substring(0, 2), 16)
                    green = Integer.parseInt(argbValue.substring(2, 4), 16)
                    blue = Integer.parseInt(argbValue.substring(4, 6), 16)
                }
            }

            if (alpha == -1 || red == -1 || green == -1 || blue == -1) {
                throw NumberFormatException("Invalid ARGB string length or format")
            }
            
            return Color.argb(alpha, red, green, blue)
        }
    }

    private var mView: PreferenceViewHolder? = null
    private var mWidgetFrameView: LinearLayout? = null
    private var mDialog: ColorPickerDialog? = null
    private var mDefaultValue: Int = Color.BLACK
    private var mCurrentValue: Int = mDefaultValue
    private var mCurrentHexValue: String? = null
    private var mDensity: Float = 0f
    private var mAlphaSliderEnabled: Boolean = false
    private var mShowReset: Boolean = false
    private var mShowPreview: Boolean = false
    private var mDividerAbove: Boolean = false
    private var mDividerBelow: Boolean = false
    private var mAutoSummary: Boolean = true
    private var mEditText: EditText? = null

    init {
        if (attrs == null) {
        }
        
        setLayoutResource(R.layout.preference_material_settings)
        init(context, attrs)
    }

    override fun onGetDefaultValue(ta: TypedArray, index: Int): Any {
        return ta.getInt(index, Color.BLACK)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        val defValue = (defaultValue as? Int) ?: Color.BLACK
        mCurrentValue = getPersistedInt(defValue)
        mCurrentHexValue = convertToARGB(mCurrentValue)
        if (mAutoSummary) setSummary(mCurrentHexValue)
        onColorChanged(mCurrentValue)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mDensity = context.resources.displayMetrics.density
        attrs?.let {
            mAlphaSliderEnabled = it.getAttributeBooleanValue(null, "alphaSlider", false)
            mDefaultValue = it.getAttributeIntValue(ANDROIDNS, "defaultValue", Color.BLACK)
            mShowReset = it.getAttributeBooleanValue(SETTINGS_NS, "showReset", true)
            mShowPreview = it.getAttributeBooleanValue(SETTINGS_NS, "showPreview", true)
            mDividerAbove = it.getAttributeBooleanValue(SETTINGS_NS, "dividerAbove", false)
            mDividerBelow = it.getAttributeBooleanValue(SETTINGS_NS, "dividerBelow", false)
        }
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        mView = view
        super.onBindViewHolder(view)
        view.isDividerAllowedAbove = mDividerAbove
        view.isDividerAllowedBelow = mDividerBelow

        view.itemView.setOnClickListener {
            showDialog(null)
        }

        val widgetFrame = view.findViewById(android.R.id.widget_frame) as? LinearLayout
        mWidgetFrameView = widgetFrame
        widgetFrame?.let { frame ->
            frame.orientation = LinearLayout.HORIZONTAL
            frame.visibility = View.VISIBLE
            frame.minimumWidth = 0
            frame.setPadding(
                frame.paddingLeft,
                frame.paddingTop,
                (mDensity * 8).roundToInt(),
                frame.paddingBottom
            )
        }
        setDefaultButton()
        setPreviewColor()
    }

    private fun setDefaultButton() {
        if (!mShowReset || mView == null || mWidgetFrameView == null) return

        mWidgetFrameView?.let { frame ->
            val oldView = frame.findViewWithTag<View>("default")
            val spacer = frame.findViewWithTag<View>("spacer")
            oldView?.let { frame.removeView(it) }
            spacer?.let { frame.removeView(it) }

            if (!isEnabled) return

            val defView = ImageView(context)
            frame.addView(defView)
            defView.setImageDrawable(context.getDrawable(R.drawable.ic_settings_backup_restore))
            defView.tag = "default"
            defView.setOnClickListener {
                onColorChanged(mDefaultValue)
            }

            val newSpacer = View(context)
            newSpacer.tag = "spacer"
            newSpacer.layoutParams =
                LinearLayout.LayoutParams((mDensity * 16).roundToInt(), LayoutParams.MATCH_PARENT)
            frame.addView(newSpacer)
        }
    }

    private fun setPreviewColor() {
        if (!mShowPreview || mView == null || mWidgetFrameView == null) return

        mWidgetFrameView?.let { frame ->
            val preview = frame.findViewWithTag<View>("preview")
            preview?.let { frame.removeView(it) }

            if (!isEnabled) return

            val iView = ImageView(context)
            frame.addView(iView)
            val size = context.resources.getDimension(R.dimen.oval_notification_size).roundToInt()
            
            val imageColor = if ((mCurrentValue and 0xF0F0F0) == 0xF0F0F0) {
                mCurrentValue - 0x101010
            } else {
                mCurrentValue
            }

            iView.setImageDrawable(createOvalShape(size, 0xFF000000.toInt() + imageColor))
            iView.tag = "preview"
        }
    }

    override fun onColorChanged(color: Int) {
        mCurrentValue = color
        mCurrentHexValue = convertToARGB(color)
        if (mAutoSummary) setSummary(mCurrentHexValue)
        setPreviewColor()
        persistInt(color)
        
        try {
            onPreferenceChangeListener?.onPreferenceChange(this, color)
        } catch (e: NullPointerException) {
        }
        
        try {
            mEditText?.setText(Integer.toHexString(color))
        } catch (e: NullPointerException) {
        }
    }

    protected fun showDialog(state: Bundle?) {
        if (!isEnabled) return

        mDialog = ColorPickerDialog(context, mCurrentValue)
        mDialog?.setOnColorChangedListener(this)
        if (mAlphaSliderEnabled) {
            mDialog?.setAlphaSliderVisible(true)
        }
        state?.let {
            mDialog?.onRestoreInstanceState(it)
        }
        mDialog?.show()
        mDialog?.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    fun setAlphaSliderEnabled(enable: Boolean) {
        mAlphaSliderEnabled = enable
    }

    fun setNewPreviewColor(color: Int) {
        onColorChanged(color)
    }

    fun setDefaultValue(value: Int) {
        mDefaultValue = value
    }

    fun setAutoSummaryEnabled(enable: Boolean) {
        mAutoSummary = enable
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (mDialog == null || mDialog?.isShowing == false) {
            return superState
        }

        val myState = SavedState(superState)
        myState.dialogBundle = mDialog?.onSaveInstanceState()
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        showDialog(myState.dialogBundle)
    }

    private class SavedState : BaseSavedState {
        var dialogBundle: Bundle? = null

        constructor(source: Parcel) : super(source) {
            dialogBundle = source.readBundle(javaClass.classLoader) // Perlu ClassLoader
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBundle(dialogBundle)
        }

        constructor(superState: Parcelable?) : super(superState)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setPreviewColor()
        setDefaultButton()
    }
}


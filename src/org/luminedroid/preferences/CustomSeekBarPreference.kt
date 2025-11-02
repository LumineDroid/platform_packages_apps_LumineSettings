/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.Utils
import kotlin.math.floor

class CustomSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.seekBarPreferenceStyle,
        com.android.internal.R.attr.seekBarPreferenceStyle
    ),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    protected val TAG: String = javaClass.name
    
    companion object {
        private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"
        private const val ANDROIDNS = "http://schemas.android.com/apk/res/android"
    }

    protected var mInterval: Int = 1
    protected var mShowSign: Boolean = false
    protected var mUnits: String = ""
    protected var mContinuousUpdates: Boolean = false

    protected var mMinValue: Int = 0
    protected var mMaxValue: Int = 100
    protected var mDefaultValueExists: Boolean = false
    protected var mDefaultValue: Int = 0
    protected var mDefaultValueTextExists: Boolean = false
    protected lateinit var mDefaultValueText: String // lateinit karena hanya diinisialisasi dalam konstruktor setelah cek null

    protected var mValue: Int = 0

    protected lateinit var mValueTextView: TextView
    protected lateinit var mResetImageView: ImageView
    protected lateinit var mMinusImageView: ImageView
    protected lateinit var mPlusImageView: ImageView
    protected val mSeekBar: SeekBar

    protected var mTrackingTouch: Boolean = false
    protected var mTrackingValue: Int = 0

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference)
        try {
            mShowSign = a.getBoolean(R.styleable.CustomSeekBarPreference_showSign, mShowSign)
            val units = a.getString(R.styleable.CustomSeekBarPreference_units)
            if (units != null) mUnits = " $units"
            mContinuousUpdates =
                a.getBoolean(R.styleable.CustomSeekBarPreference_continuousUpdates, mContinuousUpdates)
            val defaultValueText = a.getString(R.styleable.CustomSeekBarPreference_defaultValueText)
            mDefaultValueTextExists = defaultValueText != null && defaultValueText.isNotEmpty()
            if (mDefaultValueTextExists) {
                mDefaultValueText = defaultValueText!! // Djamin non-null karena mDefaultValueTextExists=true
            }
        } finally {
            a.recycle()
        }

        try {
            val newInterval = attrs?.getAttributeValue(SETTINGS_NS, "interval")
            if (newInterval != null) mInterval = newInterval.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Invalid interval value", e)
        }
        
        mMinValue = attrs?.getAttributeIntValue(SETTINGS_NS, "min", mMinValue) ?: mMinValue
        mMaxValue = attrs?.getAttributeIntValue(ANDROIDNS, "max", mMaxValue) ?: mMaxValue
        if (mMaxValue < mMinValue) mMaxValue = mMinValue
        
        val defaultValue = attrs?.getAttributeValue(ANDROIDNS, "defaultValue")
        mDefaultValueExists = defaultValue != null && defaultValue.isNotEmpty()
        if (mDefaultValueExists) {
            mDefaultValue = getLimitedValue(defaultValue!!.toInt())
            mValue = mDefaultValue
        } else {
            mValue = mMinValue
        }

        mSeekBar = SeekBar(context, attrs)
        setLayoutResource(R.layout.preference_custom_seekbar)
    }

    override fun onDependencyChanged(dependency: Preference, disableDependent: Boolean) {
        super.onDependencyChanged(dependency, disableDependent)
        this.setShouldDisableView(true)
        mSeekBar.isEnabled = !disableDependent
        if (::mResetImageView.isInitialized) mResetImageView.isEnabled = !disableDependent
        if (::mPlusImageView.isInitialized) mPlusImageView.isEnabled = !disableDependent
        if (::mMinusImageView.isInitialized) mMinusImageView.isEnabled = !disableDependent
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        try {
            // move our seekbar to the new view we've been given
            val oldContainer: ViewParent? = mSeekBar.parent
            val newContainer: ViewGroup = holder.findViewById(R.id.seekbar) as ViewGroup
            if (oldContainer != newContainer) {
                if (oldContainer != null) {
                    (oldContainer as ViewGroup).removeView(mSeekBar)
                }
                newContainer.removeAllViews()
                newContainer.addView(
                    mSeekBar,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error binding view: $ex")
        }

        mSeekBar.max = getSeekValue(mMaxValue)
        mSeekBar.progress = getSeekValue(mValue)
        mSeekBar.isEnabled = isEnabled

        mValueTextView = holder.findViewById(R.id.value) as TextView
        mResetImageView = holder.findViewById(R.id.reset) as ImageView
        mMinusImageView = holder.findViewById(R.id.minus) as ImageView
        mPlusImageView = holder.findViewById(R.id.plus) as ImageView

        updateValueViews()

        mSeekBar.setOnSeekBarChangeListener(this)
        
        mResetImageView.setOnClickListener {
            Toast.makeText(
                context,
                context.getString(
                    R.string.custom_seekbar_default_value_to_set,
                    getTextValue(mDefaultValue)
                ),
                Toast.LENGTH_LONG
            ).show()
        }
        
        mResetImageView.setOnLongClickListener {
            setValue(mDefaultValue, true)
            true
        }
        
        mMinusImageView.setOnClickListener {
            setValue(mValue - mInterval, true)
        }
        
        mMinusImageView.setOnLongClickListener {
            val newValue = if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue < mValue * 2) {
                (mMaxValue + mMinValue).floorDiv(2)
            } else {
                mMinValue
            }
            setValue(newValue, true)
            true
        }
        
        mPlusImageView.setOnClickListener {
            setValue(mValue + mInterval, true)
        }
        
        mPlusImageView.setOnLongClickListener {
            val newValue = if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue > mValue * 2) {
                -1 * (-1 * (mMaxValue + mMinValue)).floorDiv(2)
            } else {
                mMaxValue
            }
            setValue(newValue, true)
            true
        }
    }

    protected fun getLimitedValue(v: Int): Int {
        return when {
            v < mMinValue -> mMinValue
            v > mMaxValue -> mMaxValue
            else -> v
        }
    }

    protected fun Int.floorDiv(other: Int): Int = floor(this.toDouble() / other).toInt()

    protected fun getSeekValue(v: Int): Int {
        return 0 - (mMinValue - v).floorDiv(mInterval)
    }

    protected fun getTextValue(v: Int): String {
        if (mDefaultValueTextExists && mDefaultValueExists && v == mDefaultValue) {
            return mDefaultValueText
        }
        return (if (mShowSign && v > 0) "+" else "") + v.toString() + mUnits
    }

    protected fun updateValueViews() {
        if (::mValueTextView.isInitialized) {
            val value: Int = if (mTrackingTouch && !mContinuousUpdates) mTrackingValue else mValue
            
            val text: String = if (mDefaultValueTextExists && mDefaultValueExists && value == mDefaultValue) {
                if (mTrackingTouch && !mContinuousUpdates) {
                    "[${mDefaultValueText}]"
                } else {
                    mDefaultValueText + " (" + context.getString(R.string.custom_seekbar_default_value) + ")"
                }
            } else {
                val valueText = getTextValue(value)
                val formattedValue = if (mTrackingTouch && !mContinuousUpdates) "[$valueText]" else valueText
                
                context.getString(
                    R.string.custom_seekbar_value,
                    formattedValue
                ) + (if (mDefaultValueExists && value == mDefaultValue) " (" + context.getString(R.string.custom_seekbar_default_value) + ")" else "")
            }
            mValueTextView.text = text
        }
        
        if (::mResetImageView.isInitialized) {
            mResetImageView.visibility =
                if (!mDefaultValueExists || mValue == mDefaultValue || mTrackingTouch) View.INVISIBLE else View.VISIBLE
        }
        
        if (::mMinusImageView.isInitialized) {
            if (mValue == mMinValue || mTrackingTouch) {
                mMinusImageView.isClickable = false
                mMinusImageView.setColorFilter(
                    Utils.getColorAttrDefaultColor(context, android.R.attr.textColorTertiary),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                mMinusImageView.isClickable = true
                mMinusImageView.clearColorFilter()
            }
        }
        
        if (::mPlusImageView.isInitialized) {
            if (mValue == mMaxValue || mTrackingTouch) {
                mPlusImageView.isClickable = false
                mPlusImageView.setColorFilter(
                    Utils.getColorAttrDefaultColor(context, android.R.attr.textColorTertiary),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                mPlusImageView.isClickable = true
                mPlusImageView.clearColorFilter()
            }
        }
    }

    protected fun changeValue(newValue: Int) {
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val newValue = getLimitedValue(mMinValue + (progress * mInterval))
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue
            updateValueViews()
        } else if (mValue != newValue) {
            if (!callChangeListener(newValue)) {
                mSeekBar.progress = getSeekValue(mValue)
                return
            }
            changeValue(newValue)
            persistInt(newValue)

            mValue = newValue
            updateValueViews()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mTrackingValue = mValue
        mTrackingTouch = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mTrackingTouch = false
        if (!mContinuousUpdates) onProgressChanged(mSeekBar, getSeekValue(mTrackingValue), false)
        notifyChanged()
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (restoreValue) mValue = getPersistedInt(mValue)
    }

    override fun setDefaultValue(defaultValue: Any?) {
        when (defaultValue) {
            is Int -> setDefaultValue(defaultValue, mSeekBar != null)
            is String -> setDefaultValue(defaultValue, mSeekBar != null)
            else -> setDefaultValue(defaultValue as String?, mSeekBar != null)
        }
    }

    fun setDefaultValue(newValue: Int, update: Boolean) {
        val limitedNewValue = getLimitedValue(newValue)
        if (!mDefaultValueExists || mDefaultValue != limitedNewValue) {
            mDefaultValueExists = true
            mDefaultValue = limitedNewValue
            if (update) updateValueViews()
        }
    }

    fun setDefaultValue(newValue: String?, update: Boolean) {
        if (mDefaultValueExists && (newValue == null || newValue.isEmpty())) {
            mDefaultValueExists = false
            if (update) updateValueViews()
        } else if (newValue != null && newValue.isNotEmpty()) {
            setDefaultValue(newValue.toInt(), update)
        }
    }

    fun setValue(newValue: Int) {
        mValue = getLimitedValue(newValue)
        mSeekBar.progress = getSeekValue(mValue)
    }

    fun setValue(newValue: Int, update: Boolean) {
        val limitedNewValue = getLimitedValue(newValue)
        if (mValue != limitedNewValue) {
            if (update) mSeekBar.progress = getSeekValue(limitedNewValue)
            else mValue = limitedNewValue
        }
    }

    fun getValue(): Int {
        return mValue
    }

    fun refresh(newValue: Int) {
        setValue(newValue, mSeekBar != null)
    }
}


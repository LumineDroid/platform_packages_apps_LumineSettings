/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.NonNull
import com.android.settings.R

class ColorPickerDialog(
    context: Context,
    initialColor: Int
) : AlertDialog(context), ColorPickerView.OnColorChangedListener, View.OnClickListener {

    private lateinit var mColorPicker: ColorPickerView
    private lateinit var mOldColor: ColorPickerPanelView
    private lateinit var mNewColor: ColorPickerPanelView
    private var mHex: EditText? = null // Bisa null karena ada pengecekan findViewById

    private var mWhite: ColorPickerPanelView? = null
    private var mBlack: ColorPickerPanelView? = null
    private var mCyan: ColorPickerPanelView? = null
    private var mRed: ColorPickerPanelView? = null
    private var mGreen: ColorPickerPanelView? = null
    private var mYellow: ColorPickerPanelView? = null

    private var mListener: OnColorChangedListener? = null

    interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    init {
        init(initialColor)
    }

    private fun init(color: Int) {
        window?.let { window ->
            window.setFormat(PixelFormat.RGBA_8888)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setUp(color)
        }
    }

    private fun setUp(color: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        
        requireNotNull(inflater) { "LayoutInflater must not be null" }

        val layout = inflater.inflate(R.layout.dialog_color_picker, null)

        mColorPicker = layout.findViewById(R.id.color_picker_view)
        mOldColor = layout.findViewById(R.id.old_color_panel)
        mNewColor = layout.findViewById(R.id.new_color_panel)

        mWhite = layout.findViewById(R.id.white_panel)
        mBlack = layout.findViewById(R.id.black_panel)
        mCyan = layout.findViewById(R.id.cyan_panel)
        mRed = layout.findViewById(R.id.red_panel)
        mGreen = layout.findViewById(R.id.green_panel)
        mYellow = layout.findViewById(R.id.yellow_panel)

        mHex = layout.findViewById(R.id.hex)
        val mSetButton: ImageButton? = layout.findViewById(R.id.enter)

        (mOldColor.parent as LinearLayout).setPadding(
            mColorPicker.getDrawingOffset().toInt(),
            0,
            mColorPicker.getDrawingOffset().toInt(),
            0
        )

        mOldColor.setOnClickListener(this)
        mNewColor.setOnClickListener(this)
        mColorPicker.setOnColorChangedListener(this)
        mOldColor.setColor(color)
        mColorPicker.setColor(color, true)

        setColorAndClickAction(mWhite, Color.WHITE)
        setColorAndClickAction(mBlack, Color.BLACK)
        setColorAndClickAction(mCyan, 0xff24b7d6.toInt())
        setColorAndClickAction(mRed, 0xfff90028.toInt())
        setColorAndClickAction(mGreen, 0xff76c124.toInt())
        setColorAndClickAction(mYellow, 0xffffc90f.toInt())

        mHex?.let {
            it.setText(ColorPickerPreference.convertToARGB(color))
        }

        mSetButton?.setOnClickListener {
            val text = mHex?.text.toString()
            try {
                val newColor = ColorPickerPreference.convertToColorInt(text)
                mColorPicker.setColor(newColor, true)
            } catch (e: Exception) {
            }
        }

        setView(layout)
    }

    override fun onColorChanged(color: Int) {
        mNewColor.setColor(color)
        try {
            mHex?.setText(ColorPickerPreference.convertToARGB(color))
        } catch (e: Exception) {
        }
    }

    fun setAlphaSliderVisible(visible: Boolean) {
        mColorPicker.setAlphaSliderVisible(visible)
    }

    fun setColorAndClickAction(previewRect: ColorPickerPanelView?, color: Int) {
        previewRect?.let { rect ->
            rect.setColor(color)
            rect.setOnClickListener {
                try {
                    mColorPicker.setColor(color, true)
                } catch (e: Exception) {
                    // Ignore exception
                }
            }
        }
    }

    fun setOnColorChangedListener(listener: OnColorChangedListener?) {
        mListener = listener
    }

    fun getColor(): Int {
        return mColorPicker.getColor()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.new_color_panel) {
            mListener?.onColorChanged(mNewColor.getColor())
        }
        dismiss()
    }

    @NonNull
    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt("old_color", mOldColor.getColor())
        state.putInt("new_color", mNewColor.getColor())
        dismiss()
        return state
    }

    override fun onRestoreInstanceState(@NonNull savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mOldColor.setColor(savedInstanceState.getInt("old_color"))
        mColorPicker.setColor(savedInstanceState.getInt("new_color"), true)
    }
}


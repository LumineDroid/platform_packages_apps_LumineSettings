/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.button

import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.View.BaseSavedState
import android.view.WindowManager.LayoutParams
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.android.settings.R
import lineageos.providers.LineageSettings
import org.luminedroid.preferences.CustomDialogPref
import org.luminedroid.utils.DeviceUtils

class ButtonBacklightBrightness(context: Context, attrs: AttributeSet) :
    CustomDialogPref<AlertDialog>(context, attrs), SeekBar.OnSeekBarChangeListener {

  companion object {
    private const val BUTTON_BRIGHTNESS_TOGGLE_MODE_ONLY = 1
    private const val DEFAULT_BUTTON_TIMEOUT = 5
    private const val KEYBOARD_BRIGHTNESS_TOGGLE_MODE_ONLY = 1

    const val KEY_BUTTON_BACKLIGHT = "pre_navbar_button_backlight"
  }

  private var mButtonBrightness: ButtonBrightnessControl? = null
  private var mKeyboardBrightness: BrightnessControl? = null
  private var mActiveControl: BrightnessControl? = null

  private lateinit var mTimeoutContainer: ViewGroup
  private lateinit var mTimeoutBar: SeekBar
  private lateinit var mTimeoutValue: TextView

  private val mResolver: ContentResolver = context.contentResolver

  private var mOriginalTimeout: Int = 0

  init {
    setDialogLayoutResource(R.layout.button_backlight)

    if (DeviceUtils.hasKeyboardBacklightSupport(context)) {
      val isSingleValue =
          KEYBOARD_BRIGHTNESS_TOGGLE_MODE_ONLY == context.resources.getInteger(
              org.lineageos.platform.internal.R.integer.config_deviceSupportsKeyboardBrightnessControl)
      mKeyboardBrightness = BrightnessControl(LineageSettings.Secure.KEYBOARD_BRIGHTNESS, isSingleValue)
      mActiveControl = mKeyboardBrightness
    }
    if (DeviceUtils.hasButtonBacklightSupport(context)) {
      val isSingleValue =
          BUTTON_BRIGHTNESS_TOGGLE_MODE_ONLY == context.resources.getInteger(
              org.lineageos.platform.internal.R.integer.config_deviceSupportsButtonBrightnessControl)

      val defaultBrightness = context.resources.getFloat(
          org.lineageos.platform.internal.R.dimen.config_buttonBrightnessSettingDefaultFloat)

      mButtonBrightness = ButtonBrightnessControl(
          LineageSettings.Secure.BUTTON_BRIGHTNESS,
          LineageSettings.System.BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED,
          isSingleValue,
          defaultBrightness)
      mActiveControl = mButtonBrightness
    }

    updateSummary()
  }

  protected override fun onClick(d: AlertDialog, which: Int) {
    super.onClick(d, which)

    updateBrightnessPreview()
  }

  protected override fun onPrepareDialogBuilder(
      builder: AlertDialog.Builder,
      listener: DialogInterface.OnClickListener
  ) {
    super.onPrepareDialogBuilder(builder, listener)
    builder.setNeutralButton(R.string.reset, null)
    builder.setNegativeButton(R.string.cancel, null)
    builder.setPositiveButton(R.string.dlg_ok, null)
  }

  protected override fun onDismissDialog(dialog: AlertDialog, which: Int): Boolean {
    if (which == DialogInterface.BUTTON_NEUTRAL) {
      mTimeoutBar.progress = DEFAULT_BUTTON_TIMEOUT
      applyTimeout(DEFAULT_BUTTON_TIMEOUT)
      mButtonBrightness?.reset()
      mKeyboardBrightness?.reset()
      return false
    }
    return true
  }

  protected override fun onBindDialogView(view: View) {
    super.onBindDialogView(view)

    mTimeoutContainer = view.findViewById(R.id.timeout_container)
    mTimeoutBar = view.findViewById(R.id.timeout_seekbar)
    mTimeoutValue = view.findViewById(R.id.timeout_value)
    mTimeoutBar.max = 30
    mTimeoutBar.setOnSeekBarChangeListener(this)
    mOriginalTimeout = getTimeout()
    mTimeoutBar.progress = mOriginalTimeout
    handleTimeoutUpdate(mTimeoutBar.progress)

    val buttonContainer = view.findViewById<ViewGroup>(R.id.button_container)
    if (mButtonBrightness != null) {
      mButtonBrightness!!.init(buttonContainer)
    } else {
      buttonContainer.visibility = View.GONE
      mTimeoutContainer.visibility = View.GONE
    }

    val keyboardContainer = view.findViewById<ViewGroup>(R.id.keyboard_container)
    if (mKeyboardBrightness != null) {
      mKeyboardBrightness!!.init(keyboardContainer)
    } else {
      keyboardContainer.visibility = View.GONE
    }

    if (mButtonBrightness == null || mKeyboardBrightness == null) {
      view.findViewById<View>(R.id.button_keyboard_divider).visibility = View.GONE
    }
  }

  protected override fun onDialogClosed(positiveResult: Boolean) {
    super.onDialogClosed(positiveResult)

    if (!positiveResult) {
      applyTimeout(mOriginalTimeout)
      return
    }

    if (mButtonBrightness != null) {
      PreferenceManager.getDefaultSharedPreferences(context)
          .edit()
          .putFloat(KEY_BUTTON_BACKLIGHT, mButtonBrightness!!.getBrightness(false))
          .apply()
    }

    applyTimeout(mTimeoutBar.progress)
    mButtonBrightness?.applyBrightness()
    mKeyboardBrightness?.applyBrightness()

    updateSummary()
  }

  protected override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()
    if (dialog == null || !dialog!!.isShowing) {
      return superState
    }

    val myState = SavedState(superState)
    myState.timeout = mTimeoutBar.progress
    if (mButtonBrightness != null) {
      myState.button = mButtonBrightness!!.getBrightness(false)
    }
    if (mKeyboardBrightness != null) {
      myState.keyboard = mKeyboardBrightness!!.getBrightness(false)
    }

    return myState
  }

  protected override fun onRestoreInstanceState(state: Parcelable?) {
    if (state == null || state::class.java != SavedState::class.java) {
      super.onRestoreInstanceState(state)
      return
    }

    val myState = state as SavedState
    super.onRestoreInstanceState(myState.superState)

    mTimeoutBar.progress = myState.timeout
    mButtonBrightness?.setBrightness(myState.button)
    mKeyboardBrightness?.setBrightness(myState.keyboard)
  }

  fun updateSummary() {
    if (mButtonBrightness != null) {
      val buttonBrightness = mButtonBrightness!!.getBrightness(true)
      val timeout = getTimeout()

      if (buttonBrightness == 0.0f) {
        summary = context.getString(R.string.backlight_summary_disabled)
      } else if (timeout == 0) {
        summary = context.getString(R.string.backlight_timeout_unlimited)
      } else {
        summary = context.getString(
            R.string.backlight_summary_enabled_with_timeout, getTimeoutString(timeout))
      }
    } else if (mKeyboardBrightness != null && mKeyboardBrightness!!.getBrightness(true) != 0.0f) {
      summary = context.getString(R.string.backlight_summary_enabled)
    } else {
      summary = context.getString(R.string.backlight_summary_disabled)
    }
  }

  private fun getTimeoutString(timeout: Int): String {
    return context.resources.getQuantityString(R.plurals.backlight_timeout_time, timeout, timeout)
  }

  private fun getTimeout(): Int {
    return LineageSettings.Secure.getInt(
        mResolver,
        LineageSettings.Secure.BUTTON_BACKLIGHT_TIMEOUT,
        DEFAULT_BUTTON_TIMEOUT * 1000) / 1000
  }

  private fun applyTimeout(timeout: Int) {
    LineageSettings.Secure.putInt(mResolver, LineageSettings.Secure.BUTTON_BACKLIGHT_TIMEOUT, timeout * 1000)
  }

  private fun updateBrightnessPreview() {
    if (dialog == null || dialog!!.window == null) {
      return
    }
    val window: Window = dialog!!.window!!
    val params: LayoutParams = window.attributes
    params.buttonBrightness = mActiveControl?.getBrightness(false) ?: -1.0f
    window.attributes = params
  }

  private fun updateTimeoutEnabledState() {
    val buttonBrightness = mButtonBrightness?.getBrightness(false) ?: 0.0f
    val count = mTimeoutContainer.childCount
    for (i in 0 until count) {
      mTimeoutContainer.getChildAt(i).isEnabled = buttonBrightness != 0.0f
    }
  }

  private fun handleTimeoutUpdate(timeout: Int) {
    if (timeout == 0) {
      mTimeoutValue.setText(R.string.backlight_timeout_unlimited)
    } else {
      mTimeoutValue.text = getTimeoutString(timeout)
    }
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    handleTimeoutUpdate(progress)
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {
  }

  override fun onStopTrackingTouch(seekBar: SeekBar) {
    applyTimeout(seekBar.progress)
  }

  private class SavedState : BaseSavedState {
    var timeout: Int = 0
    var button: Float = 0.0f
    var keyboard: Float = 0.0f

    constructor(superState: Parcelable?) : super(superState)

    constructor(source: Parcel) : super(source) {
      timeout = source.readInt()
      button = source.readFloat()
      keyboard = source.readFloat()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
      super.writeToParcel(dest, flags)
      dest.writeInt(timeout)
      dest.writeFloat(button)
      dest.writeFloat(keyboard)
    }

    companion object {
      @JvmField
      val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
        override fun createFromParcel(inParcel: Parcel): SavedState {
          return SavedState(inParcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
          return arrayOfNulls(size)
        }
      }
    }
  }

  inner class BrightnessControl(private val mSetting: String, private val mIsSingleValue: Boolean, private val mDefaultBrightness: Float = 1.0f) :
      SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private var mCheckBox: CheckBox? = null
    private var mSeekBar: SeekBar? = null
    private var mValue: TextView? = null

    constructor(setting: String, singleValue: Boolean) : this(setting, singleValue, 1.0f)

    fun init(container: ViewGroup) {
      val brightness = getBrightness(true)

      if (mIsSingleValue) {
        container.findViewById<View>(R.id.seekbar_container).visibility = View.GONE
        mCheckBox = container.findViewById(R.id.backlight_switch)
        mCheckBox?.isChecked = brightness != 0.0f
        mCheckBox?.setOnCheckedChangeListener(this)
      } else {
        container.findViewById<View>(R.id.checkbox_container).visibility = View.GONE
        mSeekBar = container.findViewById(R.id.seekbar)
        mValue = container.findViewById(R.id.value)

        mSeekBar?.max = 100
        mSeekBar?.progress = (brightness * 100.0f).toInt()
        mSeekBar?.setOnSeekBarChangeListener(this)
      }

      handleBrightnessUpdate((brightness * 100.0f).toInt())
    }

    fun getBrightness(persisted: Boolean): Float {
      if (mCheckBox != null && !persisted) {
        return if (mCheckBox!!.isChecked) mDefaultBrightness else 0.0f
      } else if (mSeekBar != null && !persisted) {
        return (mSeekBar!!.progress / 100.0f)
      }
      return LineageSettings.Secure.getFloat(mResolver, mSetting, mDefaultBrightness)
    }

    fun applyBrightness() {
      LineageSettings.Secure.putFloat(mResolver, mSetting, getBrightness(false))
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
      handleBrightnessUpdate(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
      mActiveControl = this
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
      mActiveControl = this
      updateBrightnessPreview()
      updateTimeoutEnabledState()
    }

    fun setBrightness(value: Float) {
      if (mIsSingleValue) {
        mCheckBox?.isChecked = value != 0.0f
      } else {
        mSeekBar?.progress = (value * 100.0f).toInt()
      }
    }

    fun reset() {
      setBrightness(mDefaultBrightness)
    }

    private fun handleBrightnessUpdate(brightness: Int) {
      updateBrightnessPreview()
      mValue?.text = String.format("%d%%", brightness)
      updateTimeoutEnabledState()
    }
  }

  inner class ButtonBrightnessControl(
      brightnessSetting: String,
      private val mOnlyWhenPressedSetting: String,
      singleValue: Boolean,
      defaultBrightness: Float
  ) : BrightnessControl(brightnessSetting, singleValue, defaultBrightness) {

    private var mOnlyWhenPressedCheckBox: CheckBox? = null

    override fun init(container: ViewGroup) {
      super.init(container)

      mOnlyWhenPressedCheckBox = container.findViewById(R.id.backlight_only_when_pressed_switch)
      mOnlyWhenPressedCheckBox?.isChecked = isOnlyWhenPressedEnabled()
      mOnlyWhenPressedCheckBox?.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
      super.onCheckedChanged(buttonView, isChecked)
      setOnlyWhenPressedEnabled(mOnlyWhenPressedCheckBox?.isChecked == true)
    }

    fun isOnlyWhenPressedEnabled(): Boolean {
      return LineageSettings.System.getInt(mResolver, mOnlyWhenPressedSetting, 0) == 1
    }

    fun setOnlyWhenPressedEnabled(enabled: Boolean) {
      LineageSettings.System.putInt(mResolver, mOnlyWhenPressedSetting, if (enabled) 1 else 0)
    }
  }
}


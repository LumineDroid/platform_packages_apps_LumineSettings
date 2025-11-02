/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.gestures

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.provider.Settings.System.THREE_FINGER_GESTURE
import android.text.TextUtils

class SwipeToScreenshotPreferenceController(
    context: Context, key: String
) : GesturePreferenceController(context, key) {

    private val ON = 1
    private val OFF = 0

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE
    }

    override fun isSliceable(): Boolean {
        return TextUtils.equals(preferenceKey, "swipe_to_screenshot")
    }

    override fun getVideoPrefKey(): String {
        return PREF_KEY_VIDEO
    }

    override fun setChecked(isChecked: Boolean): Boolean {
        return Settings.System.putInt(
            mContext.contentResolver, THREE_FINGER_GESTURE,
            if (isChecked) ON else OFF
        )
    }

    override fun isChecked(): Boolean {
        return Settings.System.getInt(mContext.contentResolver, THREE_FINGER_GESTURE, 0) != 0
    }

    companion object {
        private const val PREF_KEY_VIDEO = "swipe_to_screenshot_video"
    }
}

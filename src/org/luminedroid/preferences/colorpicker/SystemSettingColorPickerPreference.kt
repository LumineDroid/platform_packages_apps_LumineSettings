/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.content.Context
import android.util.AttributeSet
import org.luminedroid.preferences.SystemSettingsStore

class SystemSettingColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ColorPickerPreference(context, attrs, defStyle) {

    init {
        setPreferenceDataStore(SystemSettingsStore(context.contentResolver))
    }
}


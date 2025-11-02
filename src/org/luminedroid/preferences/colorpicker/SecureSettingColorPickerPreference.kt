/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences.colorpicker

import android.content.Context
import android.util.AttributeSet
import org.luminedroid.preferences.SecureSettingsStore

class SecureSettingColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ColorPickerPreference(context, attrs, defStyle) {

    init {
        setPreferenceDataStore(SecureSettingsStore(context.contentResolver))
    }
}


/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.util.AttributeSet

class SecureSettingSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CustomSeekBarPreference(context, attrs, defStyle) {

    init {
        setPreferenceDataStore(SecureSettingsStore(context.contentResolver))
    }
}


/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.os.SystemProperties
import android.util.AttributeSet
import com.android.settingslib.development.SystemPropPoker
import lineageos.preference.SelfRemovingSwitchPreference

class SystemPropertySwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SelfRemovingSwitchPreference(context, attrs, defStyle) {

    override fun isPersisted(): Boolean {
        return SystemProperties.get(key, "").isNotEmpty()
    }

    override fun putBoolean(key: String, value: Boolean) {
        SystemProperties.set(key, value.toString())
        SystemPropPoker.getInstance().poke()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return SystemProperties.getBoolean(key, defaultValue)
    }
}


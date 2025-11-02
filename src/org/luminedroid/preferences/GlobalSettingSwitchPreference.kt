/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.provider.Settings
import android.util.AttributeSet
import lineageos.preference.SelfRemovingSwitchPreference

class GlobalSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SelfRemovingSwitchPreference(context, attrs, defStyle) {

    override fun isPersisted(): Boolean {
        return Settings.Global.getString(context.contentResolver, key) != null
    }

    override fun putBoolean(key: String, value: Boolean) {
        Settings.Global.putInt(context.contentResolver, key, if (value) 1 else 0)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val defValueInt = if (defaultValue) 1 else 0
        return Settings.Global.getInt(context.contentResolver, key, defValueInt) != 0
    }
}


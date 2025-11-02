/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import lineageos.preference.SelfRemovingSwitchPreference

class SystemSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SelfRemovingSwitchPreference(context, attrs, defStyle) {

    override fun isPersisted(): Boolean {
        return Settings.System.getString(context.contentResolver, key) != null
    }

    override fun putBoolean(key: String, value: Boolean) {
        Settings.System.putIntForUser(
            context.contentResolver,
            key,
            if (value) 1 else 0,
            UserHandle.USER_CURRENT
        )
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val defValueInt = if (defaultValue) 1 else 0
        return Settings.System.getIntForUser(
            context.contentResolver,
            key,
            defValueInt,
            UserHandle.USER_CURRENT
        ) != 0
    }
}


/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.ContentResolver
import android.provider.Settings
import androidx.preference.PreferenceDataStore

class GlobalSettingsStore(
    private val mContentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val defaultValueInt = if (defValue) 1 else 0
        return getInt(key, defaultValueInt) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float =
        Settings.Global.getFloat(mContentResolver, key, defValue)

    override fun getInt(key: String, defValue: Int): Int =
        Settings.Global.getInt(mContentResolver, key, defValue)

    override fun getLong(key: String, defValue: Long): Long =
        Settings.Global.getLong(mContentResolver, key, defValue)

    override fun getString(key: String, defValue: String?): String? {
        val result = Settings.Global.getString(mContentResolver, key)
        // Menggunakan Elvis operator (?:) untuk nilai default
        return result ?: defValue
    }


    override fun putBoolean(key: String, value: Boolean) {
        // Mengkonversi value (Boolean) ke Int (1/0)
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        Settings.Global.putFloat(mContentResolver, key, value)
    }

    override fun putInt(key: String, value: Int) {
        Settings.Global.putInt(mContentResolver, key, value)
    }

    override fun putLong(key: String, value: Long) {
        Settings.Global.putLong(mContentResolver, key, value)
    }

    override fun putString(key: String, value: String?) {
        Settings.Global.putString(mContentResolver, key, value)
    }
}


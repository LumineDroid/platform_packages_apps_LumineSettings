/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.ContentResolver
import android.os.UserHandle
import androidx.preference.PreferenceDataStore
import lineageos.providers.LineageSettings

class LineageSystemSettingsStore(
    private val mContentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val defaultValueInt = if (defValue) 1 else 0
        return LineageSettings.System.getIntForUser(
            mContentResolver,
            key,
            defaultValueInt,
            UserHandle.USER_CURRENT
        ) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float =
        LineageSettings.System.getFloatForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getInt(key: String, defValue: Int): Int =
        LineageSettings.System.getIntForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getLong(key: String, defValue: Long): Long =
        LineageSettings.System.getLongForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getString(key: String, defValue: String?): String? {
        val result = LineageSettings.System.getString(mContentResolver, key)
        return result ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        LineageSettings.System.putFloatForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putInt(key: String, value: Int) {
        LineageSettings.System.putIntForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putLong(key: String, value: Long) {
        LineageSettings.System.putLongForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putString(key: String, value: String?) {
        LineageSettings.System.putString(mContentResolver, key, value)
    }
}

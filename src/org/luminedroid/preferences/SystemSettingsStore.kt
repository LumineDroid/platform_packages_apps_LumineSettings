/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.ContentResolver
import android.os.UserHandle
import androidx.preference.PreferenceDataStore
import android.provider.Settings

class SystemSettingsStore(
    private val mContentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val defaultValueInt = if (defValue) 1 else 0
        return Settings.System.getIntForUser(
            mContentResolver,
            key,
            defaultValueInt,
            UserHandle.USER_CURRENT
        ) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float =
        Settings.System.getFloatForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getInt(key: String, defValue: Int): Int =
        Settings.System.getIntForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getLong(key: String, defValue: Long): Long =
        Settings.System.getLongForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getString(key: String, defValue: String?): String? {
        val result = Settings.System.getString(mContentResolver, key)
        return result ?: defValue
    }


    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        Settings.System.putFloatForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putInt(key: String, value: Int) {
        Settings.System.putIntForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putLong(key: String, value: Long) {
        Settings.System.putLongForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putString(key: String, value: String?) {
        Settings.System.putString(mContentResolver, key, value)
    }
}


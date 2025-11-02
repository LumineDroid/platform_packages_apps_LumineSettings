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

class LineageSecureSettingsStore(
    private val mContentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val defaultValueInt = if (defValue) 1 else 0
        return LineageSettings.Secure.getIntForUser(
            mContentResolver,
            key,
            defaultValueInt,
            UserHandle.USER_CURRENT
        ) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float =
        LineageSettings.Secure.getFloatForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getInt(key: String, defValue: Int): Int =
        LineageSettings.Secure.getIntForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getLong(key: String, defValue: Long): Long =
        LineageSettings.Secure.getLongForUser(
            mContentResolver,
            key,
            defValue,
            UserHandle.USER_CURRENT
        )

    override fun getString(key: String, defValue: String?): String? {
        val result = LineageSettings.Secure.getString(mContentResolver, key)
        return result ?: defValue
    }


    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        LineageSettings.Secure.putFloatForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putInt(key: String, value: Int) {
        LineageSettings.Secure.putIntForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putLong(key: String, value: Long) {
        LineageSettings.Secure.putLongForUser(
            mContentResolver,
            key,
            value,
            UserHandle.USER_CURRENT
        )
    }

    override fun putString(key: String, value: String?) {
        LineageSettings.Secure.putString(mContentResolver, key, value)
    }
}


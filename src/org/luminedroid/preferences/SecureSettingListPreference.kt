/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.ListPreference

class SecureSettingListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ListPreference(context, attrs, defStyle) {

    private var mAutoSummary: Boolean = false

    init {
        setPreferenceDataStore(SecureSettingsStore(context.contentResolver))
    }

    override fun setValue(value: String) {
        super.setValue(value)
        if (mAutoSummary || TextUtils.isEmpty(summary)) {
            setSummary(entry, true)
        }
    }

    override fun setSummary(summary: CharSequence) {
        setSummary(summary, false)
    }

    private fun setSummary(summary: CharSequence?, autoSummary: Boolean) {
        mAutoSummary = autoSummary
        super.setSummary(summary)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        val defaultValueString = defaultValue as String?

        val initialValue = if (restoreValue) {
            getPersistedString(defaultValueString)
        } else {
            defaultValueString
        }

        setValue(initialValue ?: "")
    }

    fun getIntValue(defValue: Int): Int {
        return value?.toIntOrNull() ?: defValue
    }
}


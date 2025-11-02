/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.lockscreen

import android.content.Context
import android.content.ContentResolver
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.SwitchPreferenceCompat

import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment

class PulseSettings : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.pulse_settings)
    }

    override fun getMetricsCategory(): Int {
        return MetricsProto.MetricsEvent.LUMINEDROID
    }

    companion object {
        @JvmStatic
        fun reset(context: Context) {
            val resolver: ContentResolver = context.contentResolver
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, 0, UserHandle.USER_CURRENT
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.PULSE_BAR_COUNT, 32, UserHandle.USER_CURRENT
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.PULSE_ROUNDED_BARS, 0, UserHandle.USER_CURRENT
            )
            Settings.Secure.putStringForUser(
                resolver,
                Settings.Secure.PULSE_COLOR, "lavalamp", UserHandle.USER_CURRENT
            )
        }
    }
}


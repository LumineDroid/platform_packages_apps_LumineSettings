/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.statusbar

import android.content.ContentResolver
import android.os.Bundle
import android.widget.Toast
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import lineageos.preference.LineageSecureSettingMainSwitchPreference
import lineageos.preference.LineageSecureSettingSwitchPreference
import lineageos.providers.LineageSettings

class NetworkTrafficSettings : SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener {

    private lateinit var mNetTraffic: LineageSecureSettingMainSwitchPreference
    private lateinit var mNetTrafficAutohide: LineageSecureSettingSwitchPreference
    private lateinit var mNetTrafficUnits: DropDownPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.network_traffic_settings)
        requireActivity().title = getString(R.string.network_traffic_settings_title)

        val resolver: ContentResolver = requireActivity().contentResolver

        mNetTraffic = findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_MODE)!!

        mNetTrafficAutohide = findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_AUTOHIDE)!!

        mNetTrafficUnits = findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_UNITS)!!
        mNetTrafficUnits.onPreferenceChangeListener = this
        val units =
            LineageSettings.Secure.getInt(
                resolver, LineageSettings.Secure.NETWORK_TRAFFIC_UNITS, /* Mbps */ 1
            )
        mNetTrafficUnits.value = units.toString()

        updateForClockConflicts()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val value = (newValue as String).toInt()
        val key = preference.key
        when (key) {
            LineageSettings.Secure.NETWORK_TRAFFIC_UNITS ->
                LineageSettings.Secure.putInt(
                    requireActivity().contentResolver,
                    LineageSettings.Secure.NETWORK_TRAFFIC_UNITS,
                    value
                )
        }
        return true
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    private fun updateEnabledStates(enabled: Boolean) {
        mNetTrafficAutohide.isEnabled = enabled
        mNetTrafficUnits.isEnabled = enabled
    }

    private fun updateForClockConflicts() {
        val clockPosition =
            LineageSettings.System.getInt(
                requireActivity().contentResolver, STATUS_BAR_CLOCK_STYLE, 2
            )

        if (clockPosition != 1) {
            return
        }

        mNetTraffic.isEnabled = false
        Toast.makeText(
            requireActivity(), R.string.network_traffic_disabled_clock, Toast.LENGTH_LONG
        ).show()
        updateEnabledStates(false)
    }

    companion object {
        private const val TAG = "NetworkTrafficSettings"
        private const val STATUS_BAR_CLOCK_STYLE = "status_bar_clock"
    }
}


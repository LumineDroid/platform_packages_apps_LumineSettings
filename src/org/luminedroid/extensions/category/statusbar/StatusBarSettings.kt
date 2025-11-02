/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.statusbar

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.fuelgauge.BatteryUtils
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import java.util.List
import lineageos.preference.LineageSystemSettingListPreference
import lineageos.providers.LineageSettings
import org.luminedroid.utils.DeviceUtils

@SearchIndexable
class StatusBarSettings : SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {

    private lateinit var mQuickPulldown: LineageSystemSettingListPreference
    private lateinit var mStatusBarClock: LineageSystemSettingListPreference
    private lateinit var mStatusBarAmPm: LineageSystemSettingListPreference
    private lateinit var mStatusBarBatteryShowPercent: LineageSystemSettingListPreference
    private lateinit var mStatusBarBatteryCategory: PreferenceCategory
    private lateinit var mStatusBarClockCategory: PreferenceCategory
    private lateinit var mNetworkTrafficPref: Preference

    private var mHasCenteredCutout: Boolean = false
    private var mBatteryPresent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_statusbar)

        // Ensure context is available
        val context = requireContext()

        mNetworkTrafficPref = findPreference(NETWORK_TRAFFIC_SETTINGS)!!

        mHasCenteredCutout = DeviceUtils.hasCenteredCutout(requireActivity())

        mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM)!!
        mStatusBarClock = findPreference(STATUS_BAR_CLOCK_STYLE)!!
        mStatusBarClock.onPreferenceChangeListener = this

        mStatusBarClockCategory = preferenceScreen.findPreference(CATEGORY_CLOCK)!!

        val statusBarBattery: LineageSystemSettingListPreference = findPreference(STATUS_BAR_BATTERY_STYLE)!!
        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT)!!
        statusBarBattery.onPreferenceChangeListener = this
        enableStatusBarBatteryDependents(statusBarBattery.getIntValue(2))

        val intent: Intent? = BatteryUtils.getBatteryIntent(context)
        if (intent != null) {
            mBatteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)
        }
        mStatusBarBatteryCategory = preferenceScreen.findPreference(CATEGORY_BATTERY)!!

        mQuickPulldown = findPreference(STATUS_BAR_QUICK_QS_PULLDOWN)!!
        mQuickPulldown.onPreferenceChangeListener = this
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0))
    }

    override fun onResume() {
        super.onResume()

        val curIconBlacklist =
            Settings.Secure.getString(requireContext().contentResolver, ICON_BLACKLIST)

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
            preferenceScreen.removePreference(mStatusBarClockCategory)
        } else {
            preferenceScreen.addPreference(mStatusBarClockCategory)
        }

        if (!mBatteryPresent || TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
            preferenceScreen.removePreference(mStatusBarBatteryCategory)
        } else {
            preferenceScreen.addPreference(mStatusBarBatteryCategory)
        }

        if (DateFormat.is24HourFormat(requireActivity())) {
            mStatusBarAmPm.isEnabled = false
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info)
        }

        val disallowCenteredClock = mHasCenteredCutout || getNetworkTrafficStatus() != 0

        // Adjust status bar preferences for RTL
        if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl)
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch)
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl)
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values)
            }
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl)
        } else {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch)
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch)
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries)
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values)
            }
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries)
        }

        // Disable network traffic preferences if clock is centered in the status bar
        updateNetworkTrafficStatus(getClockPosition())
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val value = (newValue as String).toInt()
        val key = preference.key
        when (key) {
            STATUS_BAR_QUICK_QS_PULLDOWN -> updateQuickPulldownSummary(value)
            STATUS_BAR_CLOCK_STYLE -> updateNetworkTrafficStatus(value)
            STATUS_BAR_BATTERY_STYLE -> enableStatusBarBatteryDependents(value)
        }
        return true
    }

    private fun enableStatusBarBatteryDependents(batteryIconStyle: Int) {
        mStatusBarBatteryShowPercent.isEnabled = batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT
    }

    private fun updateQuickPulldownSummary(value: Int) {
        val summary: String = when (value) {
            PULLDOWN_DIR_NONE -> resources.getString(R.string.status_bar_quick_qs_pulldown_off)

            PULLDOWN_DIR_LEFT, PULLDOWN_DIR_RIGHT -> {
                val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
                val isLeft = value == PULLDOWN_DIR_LEFT
                val directionStringId = if (isLeft xor isRtl) {
                    R.string.status_bar_quick_qs_pulldown_summary_left
                } else {
                    R.string.status_bar_quick_qs_pulldown_summary_right
                }
                resources.getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    resources.getString(directionStringId)
                )
            }
            else -> ""
        }
        mQuickPulldown.setSummary(summary)
    }

    private fun updateNetworkTrafficStatus(clockPosition: Int) {
        val isClockCentered = clockPosition == 1
        mNetworkTrafficPref.isEnabled = !isClockCentered
        mNetworkTrafficPref.setSummary(
            resources.getString(
                if (isClockCentered) R.string.network_traffic_disabled_clock else R.string.network_traffic_settings_summary
            )
        )
    }

    private fun getNetworkTrafficStatus(): Int {
        return LineageSettings.Secure.getInt(
            requireActivity().contentResolver, LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0
        )
    }

    private fun getClockPosition(): Int {
        return LineageSettings.System.getInt(
            requireActivity().contentResolver, STATUS_BAR_CLOCK_STYLE, 2
        )
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    companion object {
        private const val CATEGORY_BATTERY = "status_bar_battery_key"
        private const val CATEGORY_CLOCK = "status_bar_clock_key"

        private const val ICON_BLACKLIST = "icon_blacklist"

        private const val STATUS_BAR_CLOCK_STYLE = "status_bar_clock"
        private const val STATUS_BAR_AM_PM = "status_bar_am_pm"
        private const val STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style"
        private const val STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent"
        private const val STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown"

        private const val STATUS_BAR_BATTERY_STYLE_TEXT = 2

        private const val PULLDOWN_DIR_NONE = 0
        private const val PULLDOWN_DIR_RIGHT = 1
        private const val PULLDOWN_DIR_LEFT = 2

        private const val NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions_statusbar) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys: MutableList<String> = super.getNonIndexableKeys(context)
                    return keys
                }
            }
    }
}


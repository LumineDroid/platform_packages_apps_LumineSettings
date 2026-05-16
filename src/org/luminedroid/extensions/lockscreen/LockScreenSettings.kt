//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.lockscreen

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.internal.util.luminedroid.OmniJawsClient
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import org.luminedroid.utils.SystemUtils

@SearchIndexable
class LockScreenSettings :
    SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {

    private var weather: SwitchPreferenceCompat? = null
    private var smartspace: SwitchPreferenceCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_lockscreen)

        val prefScreen: PreferenceScreen = preferenceScreen

        smartspace = findPreference(KEY_SMARTSPACE)
        smartspace?.onPreferenceChangeListener = this

        weather = findPreference(KEY_WEATHER)
        weather?.onPreferenceChangeListener = this

        updateWeatherSettings()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference) {
            smartspace -> {
                smartspace?.isChecked = newValue as Boolean
                updateWeatherSettings()
                SystemUtils.showSystemUiRestartDialog(requireContext())
                return true
            }

            weather -> {
                weather?.isChecked = newValue as Boolean
                SystemUtils.showSystemUiRestartDialog(requireContext())
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        updateWeatherSettings()
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    private fun updateWeatherSettings() {
        val weatherPref = weather ?: return
        val smartspacePref = smartspace ?: return

        val weatherEnabled = OmniJawsClient.get().isOmniJawsEnabled(requireContext())

        weatherPref.isEnabled = !smartspacePref.isChecked && weatherEnabled
        weatherPref.setSummary(
            if (!smartspacePref.isChecked && weatherEnabled) R.string.lockscreen_weather_summary
            else R.string.lockscreen_weather_enabled_info
        )
    }

    companion object {

        private const val KEY_WEATHER = "lockscreen_weather_enabled"
        private const val KEY_SMARTSPACE = "lockscreen_smartspace_enabled"

        fun reset(context: Context) {
            val resolver: ContentResolver = context.contentResolver

            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_SMARTSPACE_ENABLED,
                1,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_WEATHER_ENABLED,
                0,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_WEATHER_LOCATION,
                0,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_WEATHER_TEXT,
                1,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_WEATHER_WIND_INFO,
                0,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_WEATHER_HUMIDITY_INFO,
                0,
                UserHandle.USER_CURRENT,
            )

            Settings.System.putIntForUser(
                resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO,
                1,
                UserHandle.USER_CURRENT,
            )
        }

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions_lockscreen) {
                override fun getNonIndexableKeys(context: Context): MutableList<String> {
                    return super.getNonIndexableKeys(context)
                }
            }
    }
}

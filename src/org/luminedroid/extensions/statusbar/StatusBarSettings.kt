/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.statusbar

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import org.luminedroid.preferences.SystemSettingSwitchPreference
import org.luminedroid.utils.SystemUtils

@SearchIndexable
class StatusBarSettings :
    SettingsPreferenceFragment(),
    Preference.OnPreferenceChangeListener,
    Indexable {
    private lateinit var coloredIcons: SystemSettingSwitchPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_statusbar)

        coloredIcons = findPreference(KEY_COLORED_ICONS)!!
        coloredIcons.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?,
    ): Boolean {
        val context = requireContext()

        when (preference) {
            coloredIcons -> {
                SystemUtils.showSystemUiRestartDialog(context)
                return true
            }
        }
        return false
    }

    override fun getMetricsCategory(): Int = MetricsEvent.LUMINEDROID

    companion object {
        private const val KEY_COLORED_ICONS = "statusbar_colored_icons"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.extensions_statusbar) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys = super.getNonIndexableKeys(context)
                    return keys
                }
            }
    }
}

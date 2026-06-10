//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.misc

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable

@SearchIndexable
class MiscSettings :
    SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_misc)

        val prefScreen = preferenceScreen
        val res = resources

        val pocketJudge = prefScreen.findPreference<Preference>(POCKET_JUDGE)
        val isPocketJudgeSupported = res.getBoolean(
            com.android.internal.R.bool.config_pocketModeSupported
        )
        if (!isPocketJudgeSupported) {
            pocketJudge?.let { prefScreen.removePreference(it) }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean = false

    override fun getMetricsCategory(): Int = MetricsEvent.LUMINEDROID

    companion object {
        private const val POCKET_JUDGE = "pocket_judge"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.extensions_misc) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys = super.getNonIndexableKeys(context).toMutableList()
                    val isPocketJudgeSupported = context.resources.getBoolean(
                        com.android.internal.R.bool.config_pocketModeSupported
                    )
                    if (!isPocketJudgeSupported) {
                        keys.add(POCKET_JUDGE)
                    }
                    return keys
                }
            }
    }
}

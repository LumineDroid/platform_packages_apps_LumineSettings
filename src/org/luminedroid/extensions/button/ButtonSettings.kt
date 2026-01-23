//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.button

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
class ButtonSettings :
    SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_button)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean = false

    override fun getMetricsCategory(): Int = MetricsEvent.LUMINEDROID

    companion object {
        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.extensions_button) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys = super.getNonIndexableKeys(context)
                    return keys
                }
            }
    }
}

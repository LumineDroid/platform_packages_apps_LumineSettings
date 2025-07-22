//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.button

import android.content.Context
import android.os.Bundle
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable

@SearchIndexable
class PowerOffVerify : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.power_off_verify)
    }

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.LUMINEDROID

    companion object {
        private const val TAG = "PowerOffVerify"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = BaseSearchIndexProvider(R.xml.power_off_verify)
    }
}

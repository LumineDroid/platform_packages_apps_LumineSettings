/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import com.android.internal.logging.nano.MetricsProto
import com.android.settings.dashboard.DashboardFragment
import com.android.settings.R
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable

@SearchIndexable
class Extensions : DashboardFragment() {

    override fun getPreferenceScreenResId(): Int {
        return R.xml.extensions
    }

    override fun getMetricsCategory(): Int {
        return MetricsProto.MetricsEvent.LUMINEDROID
    }

    override fun getLogTag(): String {
        return TAG
    }

    companion object {
        const val CATEGORY_KEY = "com.android.settings.category.ia.extensions"

        private const val TAG = "Extensions"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = BaseSearchIndexProvider(R.xml.extensions)
    }
}

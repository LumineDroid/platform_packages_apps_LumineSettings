/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.gestures

import android.content.Context
import android.content.SharedPreferences
import android.provider.SearchIndexableResource

import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import com.android.settings.dashboard.suggestions.SuggestionFeatureProvider
import com.android.settings.overlay.FeatureFactory
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable

import java.util.Arrays
import java.util.List

@SearchIndexable
class SwipeToScreenshotGestureSettings : DashboardFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun getMetricsCategory(): Int {
        return -1
    }

    override fun getLogTag(): String {
        return TAG
    }

    override fun getPreferenceScreenResId(): Int {
        return R.xml.swipe_to_screenshot_gesture_settings
    }

    companion object {
        private const val TAG = "SwipeToScreenshotGestureSettings"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider() {
                override fun getXmlResourcesToIndex(
                    context: Context, enabled: Boolean
                ): List<SearchIndexableResource> {
                    val sir = SearchIndexableResource(context)
                    sir.xmlResId = R.xml.swipe_to_screenshot_gesture_settings
                    return Arrays.asList(sir)
                }
            }
    }
}

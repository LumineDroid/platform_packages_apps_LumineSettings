/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import android.content.Context
import android.os.Bundle
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle
import com.android.settingslib.search.SearchIndexable

class Extensions : DashboardFragment() {
    companion object {
        const val CATEGORY_KEY = "com.android.settings.category.ia.extensions"
        private const val LOG_TAG = "Extensions"

        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions) {
                override fun createPreferenceControllers(context: Context): List<AbstractPreferenceController> =
                    buildPreferenceControllers(context, null, null)
            }

        private fun buildPreferenceControllers(
            context: Context,
            fragment: Extensions?,
            lifecycle: Lifecycle?,
        ): List<AbstractPreferenceController> {
            val controllers = mutableListOf<AbstractPreferenceController>()
            controllers.add(ExtensionsController(context))
            return controllers
        }
    }

    override fun getPreferenceScreenResId(): Int = R.xml.extensions

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.LUMINEDROID

    override fun onStart() {
        super.onStart()
    }

    override fun getLogTag(): String = LOG_TAG

    override fun createPreferenceControllers(context: Context): List<AbstractPreferenceController> =
        buildPreferenceControllers(context, this, getSettingsLifecycle())
}

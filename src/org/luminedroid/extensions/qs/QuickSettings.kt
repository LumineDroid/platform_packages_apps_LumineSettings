/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.qs

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import kotlin.math.roundToInt
import org.luminedroid.preferences.CustomSeekBarPreference

@SearchIndexable
class QuickSettings :
    SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {
    private lateinit var ShadeBlurRadiusPref: CustomSeekBarPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_quicksettings)

        val resolver: ContentResolver = requireActivity().contentResolver

        // Get max_shade_window_blur_radius from SystemUI
        val sysUiContext: Context =
            try {
                requireContext()
                    .createPackageContext(
                        SYSTEMUI_PKG,
                        Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE,
                    )
            } catch (e: NameNotFoundException) {
                requireContext()
            }

        val sysUiRes: Resources = sysUiContext.resources
        val resId = sysUiRes.getIdentifier(CONFIG, "dimen", SYSTEMUI_PKG)
        val defBlurRadiusPx = sysUiRes.getDimensionPixelSize(resId)
        val density = resources.displayMetrics.density
        val defBlurRadius = (defBlurRadiusPx / density).roundToInt()

        ShadeBlurRadiusPref = findPreference<CustomSeekBarPreference>(SHADE_BLUR_RADIUS)!!
        ShadeBlurRadiusPref.setDefaultValue(defBlurRadius)
        ShadeBlurRadiusPref.onPreferenceChangeListener = this

        val shadeBlurRadius =
            Settings.System.getIntForUser(
                resolver,
                SHADE_BLUR_RADIUS,
                defBlurRadius,
                UserHandle.USER_CURRENT,
            )
        ShadeBlurRadiusPref.value = shadeBlurRadius

        val blurEnabled =
            Settings.Global.getInt(resolver, Settings.Global.DISABLE_WINDOW_BLURS, 0) == 0

        ShadeBlurRadiusPref.isEnabled = blurEnabled
        if (!blurEnabled) {
            ShadeBlurRadiusPref.summary = "System blur is disabled"
        } else {
            ShadeBlurRadiusPref.setSummary(R.string.shade_blur_radius_summary)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver = requireActivity().contentResolver

        return when (preference) {
            ShadeBlurRadiusPref -> {
                val value = newValue as Int
                Settings.System.putIntForUser(
                    resolver,
                    SHADE_BLUR_RADIUS,
                    value,
                    UserHandle.USER_CURRENT,
                )
                true
            }

            else -> false
        }
    }

    override fun getMetricsCategory(): Int = MetricsEvent.LUMINEDROID

    companion object {
        private const val SYSTEMUI_PKG = "com.android.systemui"
        private const val CONFIG = "max_shade_window_blur_radius"
        private const val SHADE_BLUR_RADIUS = "shade_blur_radius"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.extensions_quicksettings) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys = super.getNonIndexableKeys(context)
                    return keys
                }
            }
    }
}

/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.lockscreen

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import java.util.List

@SearchIndexable
class LockScreenSettings : SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {

    private var mFingerprintCategory: PreferenceCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_lockscreen)

        val context = context
        val prefScreen: PreferenceScreen = preferenceScreen

        // Smart cast: findPreference returns a Preference, but we cast it to PreferenceCategory
        mFingerprintCategory = findPreference<PreferenceCategory>(KEY_FINGERPRINT_CATEGORY)

        val fingerprintManager =
            activity?.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected) {
            mFingerprintCategory?.let { prefScreen.removePreference(it) }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return false
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    companion object {
        private const val KEY_FINGERPRINT_CATEGORY = "lock_screen_fingerprint_category"
        private const val KEY_AUTHENTICATION_SUCCESS = "fp_success_vibrate"
        private const val KEY_AUTHENTICATION_ERROR = "fp_error_vibrate"
        private const val KEY_RIPPLE_EFFECT = "enable_ripple_effect"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions_lockscreen) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys: MutableList<String> = super.getNonIndexableKeys(context)

                    val fingerprintManager =
                        context.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager

                    if (fingerprintManager == null || !fingerprintManager.isHardwareDetected) {
                        keys.add(KEY_AUTHENTICATION_SUCCESS)
                        keys.add(KEY_AUTHENTICATION_ERROR)
                        keys.add(KEY_RIPPLE_EFFECT)
                    }
                    return keys
                }
            }
    }
}


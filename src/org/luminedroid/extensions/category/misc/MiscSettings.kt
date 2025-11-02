/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.misc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import java.util.List

@SearchIndexable
class MiscSettings : SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {

    private lateinit var mKeyboxFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPifFilePickerLauncher: ActivityResultLauncher<Intent>

    private var mKeyboxDataPreference: KeyboxDataPreference? = null
    private var mPifDataPreference: PifDataPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_misc)

        mKeyboxFilePickerLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val uri: Uri? = result.data?.data
                    val pref = findPreference<Preference>(KEYBOX_DATA_KEY)
                    if (pref is KeyboxDataPreference && uri != null) {
                        pref.handleFileSelected(uri)
                    }
                }
            }

        mPifFilePickerLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val uri: Uri? = result.data?.data
                    val pref = findPreference<Preference>(PIF_DATA_KEY)
                    if (pref is PifDataPreference && uri != null) {
                        pref.handleFileSelected(uri)
                    }
                }
            }

        // Find and set launchers for preferences
        mKeyboxDataPreference = findPreference(KEYBOX_DATA_KEY)
        mPifDataPreference = findPreference(PIF_DATA_KEY)

        mKeyboxDataPreference?.setFilePickerLauncher(mKeyboxFilePickerLauncher)
        mPifDataPreference?.setFilePickerLauncher(mPifFilePickerLauncher)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return false
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    companion object {
        private const val KEYBOX_DATA_KEY = "keybox_data_setting"
        private const val PIF_DATA_KEY = "pif_data_setting"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions_misc) {
                override fun getNonIndexableKeys(context: Context): List<String> {
                    val keys: MutableList<String> = super.getNonIndexableKeys(context)
                    return keys
                }
            }
    }
}


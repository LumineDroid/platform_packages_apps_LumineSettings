/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class ExtensionsController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        screen.findPreference<LayoutPreference>(KEY_EXTENSIONS_HOMEPAGE)?.let { extensionsPref ->
            setupExtensionsClickListeners(extensionsPref)
        }
    }

    private fun setupExtensionsClickListeners(preference: LayoutPreference) {
        val extensionsClickMap = mapOf(
            R.id.extensions_statusbar to "com.android.settings.Settings\$ExtensionsStatusbarActivity",
            R.id.extensions_quicksettings to "com.android.settings.Settings\$ExtensionsQuickSettingsActivity",
            R.id.extensions_button to "com.android.settings.Settings\$ExtensionsButtonActivity",
            R.id.extensions_lockscreen to "com.android.settings.Settings\$ExtensionsLockScreenActivity",
            R.id.extensions_about to "com.android.settings.Settings\$ExtensionsAboutActivity",
            R.id.extensions_misc to "com.android.settings.Settings\$ExtensionsMiscActivity"
        )
        extensionsClickMap.forEach { (viewId, activityName) ->
            preference.findViewById<View>(viewId)?.setOnClickListener {
                mContext.startActivity(createIntent(activityName))
            }
        }
    }

    private fun createIntent(activityName: String): Intent {
        return Intent().apply {
            component = ComponentName("com.android.settings", activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY_EXTENSIONS_HOMEPAGE

    companion object {
        private const val KEY_EXTENSIONS_HOMEPAGE = "extensions_homepage"
    }
}

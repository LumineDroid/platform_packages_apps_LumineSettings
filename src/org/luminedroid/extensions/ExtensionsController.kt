/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class ExtensionsController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        screen.findPreference<LayoutPreference>(KEY_EXTENSIONS_HOMEPAGE)?.let { pref ->
            pref.findViewById<ComposeView>(R.id.extensions_compose_view).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    LumineExtensionsTheme {
                        ExtensionsHomepage { itemId ->
                            mContext.startActivity(createIntentForItem(itemId))
                        }
                    }
                }
            }
        }
    }

    private fun createIntentForItem(itemId: String): Intent {
        val activityName =
            when (itemId) {
                "statusbar" -> "com.android.settings.Settings\$ExtensionsStatusbarActivity"
                "quicksettings" -> "com.android.settings.Settings\$ExtensionsQuickSettingsActivity"
                "button" -> "com.android.settings.Settings\$ExtensionsButtonActivity"
                "lockscreen" -> "com.android.settings.Settings\$ExtensionsLockScreenActivity"
                "about" -> "com.android.settings.Settings\$ExtensionsAboutActivity"
                "misc" -> "com.android.settings.Settings\$ExtensionsMiscActivity"
                else -> error("Unknown extension item: $itemId")
            }
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

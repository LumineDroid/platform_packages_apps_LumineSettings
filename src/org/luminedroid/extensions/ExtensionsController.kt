/*
 * Copyright (C) 2026 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference
import org.luminedroid.extensions.ui.ExtensionsHomepageView

/**
 * Preference controller for the Extensions homepage.
 */
class ExtensionsController(context: Context) : AbstractPreferenceController(context) {

    private val tileActivityMap = mapOf(
        "statusbar"     to "com.android.settings.Settings\$ExtensionsStatusbarActivity",
        "quicksettings" to "com.android.settings.Settings\$ExtensionsQuickSettingsActivity",
        "button"        to "com.android.settings.Settings\$ExtensionsButtonActivity",
        "lockscreen"    to "com.android.settings.Settings\$ExtensionsLockScreenActivity",
        "about"         to "com.android.settings.Settings\$ExtensionsAboutActivity",
        "misc"          to "com.android.settings.Settings\$ExtensionsMiscActivity",
    )

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val pref = screen.findPreference<LayoutPreference>(KEY_EXTENSIONS_HOMEPAGE) ?: return
        val homepageView = pref.findViewById<ExtensionsHomepageView>(
            R.id.extensions_homepage_compose_view
        ) ?: return

        homepageView.onTileClick = { tileId ->
            tileActivityMap[tileId]?.let { activityName ->
                mContext.startActivity(createIntent(activityName))
            }
        }

        homepageView.wallpaperBitmap = WallpaperUtils.getWallpaperBitmap(mContext)
    }

    private fun createIntent(activityName: String): Intent =
        Intent().apply {
            component = ComponentName("com.android.settings", activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY_EXTENSIONS_HOMEPAGE

    companion object {
        private const val KEY_EXTENSIONS_HOMEPAGE = "extensions_homepage"
    }
}

//
// SPDX-FileCopyrightText: 2025 The LibreMobileOS Foundation
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package com.android.settings.core.gateway

import com.android.settings.Settings
import org.luminedroid.extensions.Extensions
import org.luminedroid.extensions.about.AboutLumineDroidFragment
import org.luminedroid.extensions.button.ButtonSettings
import org.luminedroid.extensions.lockscreen.LockScreenSettings
import org.luminedroid.extensions.misc.MiscSettings
import org.luminedroid.extensions.qs.QuickSettings
import org.luminedroid.extensions.statusbar.StatusBarSettings

object LumineSettingsGateway {
    /**
     * A list of fragments that can be hosted by LumineSettingsActivity. SettingsActivity will throw
     * a security exception if the fragment it needs to display is not in this list.
     */
    val ENTRY_FRAGMENTS =
        arrayOf(
            Extensions::class.java.name,
            StatusBarSettings::class.java.name,
            QuickSettings::class.java.name,
            ButtonSettings::class.java.name,
            LockScreenSettings::class.java.name,
            MiscSettings::class.java.name,
            AboutLumineDroidFragment::class.java.name,
        )

    val SETTINGS_FOR_RESTRICTED =
        arrayOf(
            Settings.ExtensionsActivity::class.java.name,
            Settings.ExtensionsStatusbarActivity::class.java.name,
            Settings.ExtensionsQuickSettingsActivity::class.java.name,
            Settings.ExtensionsButtonActivity::class.java.name,
            Settings.ExtensionsLockScreenActivity::class.java.name,
            Settings.ExtensionsAboutActivity::class.java.name,
            Settings.ExtensionsMiscActivity::class.java.name,
        )
}

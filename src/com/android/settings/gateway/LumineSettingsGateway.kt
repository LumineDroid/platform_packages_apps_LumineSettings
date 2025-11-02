/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.core.gateway

import com.android.settings.Settings

import org.luminedroid.extensions.Extensions
import org.luminedroid.extensions.category.about.AboutLumineDroidFragment
import org.luminedroid.extensions.category.statusbar.StatusBarSettings
import org.luminedroid.extensions.category.qs.QuickSettings
import org.luminedroid.extensions.category.button.ButtonSettings
import org.luminedroid.extensions.category.lockscreen.LockScreenSettings
import org.luminedroid.extensions.category.misc.MiscSettings


object LumineSettingsGateway {

    /**
     * A list of fragment that can be hosted by LumineSettingsActivity. SettingsActivity will throw a
     * security exception if the fragment it needs to display is not in this list.
     */
    @JvmField
    val ENTRY_FRAGMENTS = arrayOf(
        Extensions::class.java.name,
        StatusBarSettings::class.java.name,
        QuickSettings::class.java.name,
        ButtonSettings::class.java.name,
        LockScreenSettings::class.java.name,
        MiscSettings::class.java.name,
        AboutLumineDroidFragment::class.java.name
    )

    @JvmField
    val SETTINGS_FOR_RESTRICTED = arrayOf(
        Settings.ExtensionsActivity::class.java.name,
        Settings.ExtensionsStatusbarActivity::class.java.name,
        Settings.ExtensionsQuickSettingsActivity::class.java.name,
        Settings.ExtensionsButtonActivity::class.java.name,
        Settings.ExtensionsLockScreenActivity::class.java.name,
        Settings.ExtensionsAboutActivity::class.java.name,
        Settings.ExtensionsMiscActivity::class.java.name
    )
}

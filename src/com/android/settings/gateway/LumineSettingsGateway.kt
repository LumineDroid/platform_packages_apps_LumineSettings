/*
 * Copyright (C) 2025 The LibreMobileOS Foundation
 * Copyright (C) 2025 LumineDroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

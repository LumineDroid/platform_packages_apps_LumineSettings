/*
 * Copyright (C) 2025 The LibreMobileOS Foundation
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

package com.android.settings.core.gateway;

import com.android.settings.Settings;
import com.android.settings.display.RefreshRateSettings;
import org.luminedroid.extensions.Extensions;
import org.luminedroid.extensions.category.about.AboutLumineDroidFragment;
import org.luminedroid.extensions.category.button.ButtonSettings;
import org.luminedroid.extensions.category.lockscreen.LockScreenSettings;
import org.luminedroid.extensions.category.misc.MiscSettings;
import org.luminedroid.extensions.category.qs.QuickSettings;
import org.luminedroid.extensions.category.statusbar.StatusBarSettings;

public class LumineSettingsGateway {

  /**
   * A list of fragment that can be hosted by LumineSettingsActivity. SettingsActivity will throw a
   * security exception if the fragment it needs to display is not in this list.
   */
  public static final String[] ENTRY_FRAGMENTS = {
    Extensions.class.getName(),
    RefreshRateSettings.class.getName(),
    StatusBarSettings.class.getName(),
    QuickSettings.class.getName(),
    ButtonSettings.class.getName(),
    LockScreenSettings.class.getName(),
    MiscSettings.class.getName(),
    AboutLumineDroidFragment.class.getName()
  };

  public static final String[] SETTINGS_FOR_RESTRICTED = {
    Settings.ExtensionsActivity.class.getName(),
    Settings.ExtensionsStatusbarActivity.class.getName(),
    Settings.ExtensionsQuickSettingsActivity.class.getName(),
    Settings.ExtensionsButtonActivity.class.getName(),
    Settings.ExtensionsLockScreenActivity.class.getName(),
    Settings.ExtensionsAboutActivity.class.getName(),
    Settings.ExtensionsMiscActivity.class.getName()
  };
}

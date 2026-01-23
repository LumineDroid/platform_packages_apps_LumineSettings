//
// SPDX-FileCopyrightText: 2016-2025 Android Project
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.lockscreen;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PulseSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pulse_settings);
    }

    public static void reset(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.PULSE_BAR_COUNT, 32, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.PULSE_ROUNDED_BARS, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putStringForUser(resolver,
                Settings.Secure.PULSE_COLOR, "lavalamp", UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.LUMINEDROID;
    }
}

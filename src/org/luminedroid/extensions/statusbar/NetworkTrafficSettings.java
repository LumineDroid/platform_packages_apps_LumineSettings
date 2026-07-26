//
// SPDX-FileCopyrightText: 2017-2021 crDroid Android Project
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import org.luminedroid.preferences.CustomSeekBarPreference;

public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "NetworkTrafficSettings";

    private CustomSeekBarPreference mNetTrafficAutohideThreshold;
    private CustomSeekBarPreference mNetTrafficRefreshInterval;
    private ListPreference mNetTrafficMode;
    private ListPreference mNetTrafficUnits;
    private SwitchPreferenceCompat mNetTrafficAutohide;
    private SwitchPreferenceCompat mNetTrafficHideArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficAutohideThreshold = (CustomSeekBarPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        mNetTrafficRefreshInterval = (CustomSeekBarPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL);
        mNetTrafficMode = (ListPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_MODE);
        mNetTrafficAutohide = (SwitchPreferenceCompat)
                findPreference(Settings.System.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficUnits = (ListPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_UNITS);
        mNetTrafficHideArrow = (SwitchPreferenceCompat)
                findPreference(Settings.System.NETWORK_TRAFFIC_HIDEARROW);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_MODE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_UNITS, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_HIDEARROW, 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.LUMINEDROID;
    }
}

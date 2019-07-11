/*
 * Copyright (C) 2025 LumineDroid
 * Copyright (C} 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.fuelgauge.BatteryUtils;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;
import org.luminedroid.utils.DeviceUtils;

@SearchIndexable
public class StatusBarSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

  private static final String CATEGORY_BATTERY = "status_bar_battery_key";
  private static final String CATEGORY_CLOCK = "status_bar_clock_key";

  private static final String ICON_BLACKLIST = "icon_blacklist";

  private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
  private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
  private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
  private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
  private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

  private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

  private static final int PULLDOWN_DIR_NONE = 0;
  private static final int PULLDOWN_DIR_RIGHT = 1;
  private static final int PULLDOWN_DIR_LEFT = 2;

  private static final String NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings";

  private LineageSystemSettingListPreference mQuickPulldown;
  private LineageSystemSettingListPreference mStatusBarClock;
  private LineageSystemSettingListPreference mStatusBarAmPm;
  private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

  private PreferenceCategory mStatusBarBatteryCategory;
  private PreferenceCategory mStatusBarClockCategory;
  private Preference mNetworkTrafficPref;

  private boolean mHasCenteredCutout;

  private boolean mBatteryPresent;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.extensions_statusbar);

    final Context context = getContext();

    mNetworkTrafficPref = findPreference(NETWORK_TRAFFIC_SETTINGS);

    mHasCenteredCutout = DeviceUtils.hasCenteredCutout(getActivity());

    mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM);
    mStatusBarClock = findPreference(STATUS_BAR_CLOCK_STYLE);
    mStatusBarClock.setOnPreferenceChangeListener(this);

    mStatusBarClockCategory = getPreferenceScreen().findPreference(CATEGORY_CLOCK);

    mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
    LineageSystemSettingListPreference statusBarBattery = findPreference(STATUS_BAR_BATTERY_STYLE);
    statusBarBattery.setOnPreferenceChangeListener(this);
    enableStatusBarBatteryDependents(statusBarBattery.getIntValue(2));

    Intent intent = BatteryUtils.getBatteryIntent(getContext());
    if (intent != null) {
      mBatteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
    }
    mStatusBarBatteryCategory = getPreferenceScreen().findPreference(CATEGORY_BATTERY);

    mQuickPulldown = findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
    mQuickPulldown.setOnPreferenceChangeListener(this);
    updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));
  }

  @Override
  public void onResume() {
    super.onResume();

    final String curIconBlacklist =
        Settings.Secure.getString(getContext().getContentResolver(), ICON_BLACKLIST);

    if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
      getPreferenceScreen().removePreference(mStatusBarClockCategory);
    } else {
      getPreferenceScreen().addPreference(mStatusBarClockCategory);
    }

    if (!mBatteryPresent || TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
      getPreferenceScreen().removePreference(mStatusBarBatteryCategory);
    } else {
      getPreferenceScreen().addPreference(mStatusBarBatteryCategory);
    }

    if (DateFormat.is24HourFormat(getActivity())) {
      mStatusBarAmPm.setEnabled(false);
      mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
    }

    final boolean disallowCenteredClock = mHasCenteredCutout || getNetworkTrafficStatus() != 0;

    // Adjust status bar preferences for RTL
    if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      if (disallowCenteredClock) {
        mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
        mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
      } else {
        mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
        mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
      }
      mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
    } else {
      if (disallowCenteredClock) {
        mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
        mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
      } else {
        mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
        mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
      }
      mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries);
    }

    // Disable network traffic preferences if clock is centered in the status bar
    updateNetworkTrafficStatus(getClockPosition());
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    int value = Integer.parseInt((String) newValue);
    String key = preference.getKey();
    switch (key) {
      case STATUS_BAR_QUICK_QS_PULLDOWN:
        updateQuickPulldownSummary(value);
        break;
      case STATUS_BAR_CLOCK_STYLE:
        updateNetworkTrafficStatus(value);
        break;
      case STATUS_BAR_BATTERY_STYLE:
        enableStatusBarBatteryDependents(value);
        break;
    }
    return true;
  }

  public static void reset(Context mContext) {
    ContentResolver resolver = mContext.getContentResolver();

    Settings.System.putIntForUser(
        resolver, Settings.System.STATUSBAR_COLORED_ICONS, 0, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.DATA_DISABLED_ICON, 1, UserHandle.USER_CURRENT);
  }

  private void enableStatusBarBatteryDependents(int batteryIconStyle) {
    mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
  }

  private void updateQuickPulldownSummary(int value) {
    String summary = "";
    switch (value) {
      case PULLDOWN_DIR_NONE:
        summary = getResources().getString(R.string.status_bar_quick_qs_pulldown_off);
        break;

      case PULLDOWN_DIR_LEFT:
      case PULLDOWN_DIR_RIGHT:
        summary =
            getResources()
                .getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources()
                        .getString(
                            (value == PULLDOWN_DIR_LEFT)
                                    ^ (getResources().getConfiguration().getLayoutDirection()
                                        == View.LAYOUT_DIRECTION_RTL)
                                ? R.string.status_bar_quick_qs_pulldown_summary_left
                                : R.string.status_bar_quick_qs_pulldown_summary_right));
        break;
    }
    mQuickPulldown.setSummary(summary);
  }

  private void updateNetworkTrafficStatus(int clockPosition) {
    boolean isClockCentered = clockPosition == 1;
    mNetworkTrafficPref.setEnabled(!isClockCentered);
    mNetworkTrafficPref.setSummary(
        getResources()
            .getString(
                isClockCentered
                    ? R.string.network_traffic_disabled_clock
                    : R.string.network_traffic_settings_summary));
  }

  private int getNetworkTrafficStatus() {
    return LineageSettings.Secure.getInt(
        getActivity().getContentResolver(), LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
  }

  private int getClockPosition() {
    return LineageSettings.System.getInt(
        getActivity().getContentResolver(), STATUS_BAR_CLOCK_STYLE, 2);
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.LUMINEDROID;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.extensions_statusbar) {
        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          return keys;
        }
      };
}

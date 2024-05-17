/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */
package org.luminedroid.extensions.category.qs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.providers.LineageSettings;
import org.luminedroid.preferences.SystemSettingListPreference;
import org.luminedroid.preferences.SystemSettingSwitchPreference;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

  private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
  private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
  private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
  private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
  private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
  private static final String KEY_BATTERY_PERCENT = "qs_show_battery_percent";
  private static final String KEY_BATTERY_STYLE = "qs_battery_style";

  private static final int BATTERY_STYLE_PORTRAIT = 0;
  private static final int BATTERY_STYLE_TEXT = 4;
  private static final int BATTERY_STYLE_HIDDEN = 5;

  private PreferenceCategory mInterfaceCategory;
  private ListPreference mShowBrightnessSlider;
  private ListPreference mBrightnessSliderPosition;
  private LineageSecureSettingSwitchPreference mShowAutoBrightness;
  private SystemSettingSwitchPreference mBrightnessSliderHaptic;
  private SystemSettingListPreference mBatteryStyle;
  private SystemSettingListPreference mBatteryPercent;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.extensions_quicksettings);

    final Context mContext = getContext();
    final ContentResolver resolver = mContext.getContentResolver();
    final PreferenceScreen prefScreen = getPreferenceScreen();
    final Resources resources = mContext.getResources();

    mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_BATTERY_STYLE);
    mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_BATTERY_PERCENT);

    int batterystyle =
        Settings.System.getIntForUser(
            resolver,
            Settings.System.QS_BATTERY_STYLE,
            BATTERY_STYLE_PORTRAIT,
            UserHandle.USER_CURRENT);

    mBatteryStyle.setOnPreferenceChangeListener(this);

    mBatteryPercent.setEnabled(
        batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);

    mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
    mShowBrightnessSlider.setOnPreferenceChangeListener(this);
    boolean showSlider =
        LineageSettings.Secure.getIntForUser(
                resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER,
                1,
                UserHandle.USER_CURRENT)
            > 0;

    mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
    mBrightnessSliderPosition.setEnabled(showSlider);

    mBrightnessSliderHaptic = findPreference(KEY_BRIGHTNESS_SLIDER_HAPTIC);
    mBrightnessSliderHaptic.setEnabled(showSlider);

    mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
    boolean automaticAvailable =
        mContext
            .getResources()
            .getBoolean(com.android.internal.R.bool.config_automatic_brightness_available);
    if (automaticAvailable) {
      mShowAutoBrightness.setEnabled(showSlider);
    } else {
      prefScreen.removePreference(mShowAutoBrightness);
    }
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference == mShowBrightnessSlider) {
      int value = Integer.parseInt((String) newValue);
      mBrightnessSliderPosition.setEnabled(value > 0);
      mBrightnessSliderHaptic.setEnabled(value > 0);
      if (mShowAutoBrightness != null) mShowAutoBrightness.setEnabled(value > 0);
      return true;
    } else if (preference == mBatteryStyle) {
      int value = Integer.parseInt((String) newValue);
      mBatteryPercent.setEnabled(value != BATTERY_STYLE_TEXT && value != BATTERY_STYLE_HIDDEN);
      return true;
    }
    return false;
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.LUMINEDROID;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.extensions_quicksettings) {
        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          Resources resources = context.getResources();
          boolean autoBrightnessAvailable =
              resources.getBoolean(
                  com.android.internal.R.bool.config_automatic_brightness_available);
          if (!autoBrightnessAvailable) {
            keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
          }
          return keys;
        }
      };
}

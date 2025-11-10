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
import androidx.preference.SwitchPreferenceCompat;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import lineageos.providers.LineageSettings;
import org.luminedroid.utils.DeviceUtils;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

  private static final String QS_BRIGHTNESS_CATEGORY = "qs_brightness_slider_category";
  private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
  private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
  private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
  private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";

  private ListPreference mShowBrightnessSlider;
  private ListPreference mBrightnessSliderPosition;
  private SwitchPreferenceCompat mBrightnessSliderHaptic;
  private SwitchPreferenceCompat mShowAutoBrightness;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.extensions_quicksettings);

    final Context context = getContext();
    final ContentResolver resolver = context.getContentResolver();

    PreferenceCategory brightnessCategory =
        (PreferenceCategory) findPreference(QS_BRIGHTNESS_CATEGORY);

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
    boolean hapticAvailable = DeviceUtils.hasVibrator(context);

    if (hapticAvailable) {
      mBrightnessSliderHaptic.setEnabled(showSlider);
    } else {
      brightnessCategory.removePreference(mBrightnessSliderHaptic);
    }

    mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
    boolean automaticAvailable =
        context
            .getResources()
            .getBoolean(com.android.internal.R.bool.config_automatic_brightness_available);

    if (automaticAvailable) {
      mShowAutoBrightness.setEnabled(showSlider);
    } else {
      brightnessCategory.removePreference(mShowAutoBrightness);
    }
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    ContentResolver resolver = getContext().getContentResolver();

    if (preference == mShowBrightnessSlider) {
      int value = Integer.parseInt((String) newValue);
      mBrightnessSliderPosition.setEnabled(value > 0);
      if (mBrightnessSliderHaptic != null) mBrightnessSliderHaptic.setEnabled(value > 0);
      if (mShowAutoBrightness != null) mShowAutoBrightness.setEnabled(value > 0);
      return true;
    }
    return false;
  }

  public static void reset(Context mContext) {
    ContentResolver resolver = mContext.getContentResolver();
    LineageSettings.Secure.putIntForUser(
        resolver, LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
    LineageSettings.Secure.putIntForUser(
        resolver, LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION, 0, UserHandle.USER_CURRENT);
    LineageSettings.Secure.putIntForUser(
        resolver, LineageSettings.Secure.QS_SHOW_AUTO_BRIGHTNESS, 1, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.QS_BRIGHTNESS_SLIDER_HAPTIC, 0, UserHandle.USER_CURRENT);
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
          final Resources res = context.getResources();

          boolean automaticAvailable =
              res.getBoolean(com.android.internal.R.bool.config_automatic_brightness_available);
          if (!automaticAvailable) {
            keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
          }

          boolean hapticAvailable = DeviceUtils.hasVibrator(context);
          if (!hapticAvailable) {
            keys.add(KEY_BRIGHTNESS_SLIDER_HAPTIC);
          }
          return keys;
        }
      };
}

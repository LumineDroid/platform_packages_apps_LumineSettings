/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.lockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.luminedroid.OmniJawsClient;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import org.luminedroid.utils.SystemUtils;

@SearchIndexable
public class LockScreenSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

  private static final String KEY_FINGERPRINT_CATEGORY = "lock_screen_fingerprint_category";
  private static final String KEY_AUTHENTICATION_SUCCESS = "fp_success_vibrate";
  private static final String KEY_AUTHENTICATION_ERROR = "fp_error_vibrate";
  private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";
  private static final String KEY_WEATHER = "lockscreen_weather_enabled";
  private static final String KEY_SMARTSPACE = "lockscreen_smartspace_enabled";

  private PreferenceCategory mFingerprintCategory;
  private SwitchPreferenceCompat mWeather;
  private SwitchPreferenceCompat mSmartspace;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.extensions_lockscreen);

    final Context context = getContext();
    final PreferenceScreen prefScreen = getPreferenceScreen();

    mFingerprintCategory = (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);

    FingerprintManager fingerprintManager =
        (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

    if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
      prefScreen.removePreference(mFingerprintCategory);
    }

    mSmartspace = (SwitchPreferenceCompat) findPreference(KEY_SMARTSPACE);
    mSmartspace.setOnPreferenceChangeListener(this);

    mWeather = (SwitchPreferenceCompat) findPreference(KEY_WEATHER);
    mWeather.setOnPreferenceChangeListener(this);
    updateWeatherSettings();
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference == mSmartspace) {
      mSmartspace.setChecked((Boolean) newValue);
      updateWeatherSettings();
      SystemUtils.showSystemUiRestartDialog(getContext());
      return true;
    } else if (preference == mWeather) {
      mWeather.setChecked((Boolean) newValue);
      SystemUtils.showSystemUiRestartDialog(getContext());
      return true;
    }

    return false;
  }

  public static void reset(Context mContext) {
    ContentResolver resolver = mContext.getContentResolver();
    Settings.Secure.putIntForUser(
        resolver, Settings.Secure.LOCKSCREEN_SMARTSPACE_ENABLED, 1, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.LOCKSCREEN_WEATHER_ENABLED, 0, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.LOCKSCREEN_WEATHER_LOCATION, 0, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.LOCKSCREEN_WEATHER_TEXT, 1, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.LOCKSCREEN_WEATHER_WIND_INFO, 0, UserHandle.USER_CURRENT);
    Settings.System.putIntForUser(
        resolver, Settings.System.LOCKSCREEN_WEATHER_HUMIDITY_INFO, 0, UserHandle.USER_CURRENT);
  }

  private void updateWeatherSettings() {
    if (mWeather == null || mSmartspace == null) return;

    boolean weatherEnabled = OmniJawsClient.get().isOmniJawsEnabled(getContext());
    mWeather.setEnabled(!mSmartspace.isChecked() && weatherEnabled);
    mWeather.setSummary(
        !mSmartspace.isChecked() && weatherEnabled
            ? R.string.lockscreen_weather_summary
            : R.string.lockscreen_weather_enabled_info);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateWeatherSettings();
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.LUMINEDROID;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.extensions_lockscreen) {
        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);

          FingerprintManager fingerprintManager =
              (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

          if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            keys.add(KEY_AUTHENTICATION_SUCCESS);
            keys.add(KEY_AUTHENTICATION_ERROR);
            keys.add(KEY_RIPPLE_EFFECT);
          }
          return keys;
        }
      };
}

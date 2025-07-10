/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;

@SearchIndexable
public class Extensions extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener {

  private static final String TAG = "Extensions";
  public static final String CATEGORY_EXTENSIONS = "com.android.settings.category.ia.extensions";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.extensions);

    final Context context = getContext();
    final ContentResolver resolver = context.getContentResolver();
    final PreferenceScreen prefScreen = getPreferenceScreen();
    final Resources resources = context.getResources();
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    final Context context = getContext();
    final ContentResolver resolver = context.getContentResolver();
    return false;
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.LUMINEDROID;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.extensions) {

        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          final Resources resources = context.getResources();
          return keys;
        }
      };
}

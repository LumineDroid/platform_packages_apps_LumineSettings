/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.about;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luminedroid.extensions.category.about.TeamMemberPreference;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AboutLumineDroid extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about_luminedroid, rootKey);

        try {
            JSONObject data = new JSONObject(loadJSONFromRaw());
            setupLinks(data.getJSONObject("links"));
            setupTeam(data.getJSONArray("team"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.LUMINEDROID;
    }

    private void setupLinks(JSONObject links) throws JSONException {
        setupLinkPreference("website", links.getString("website"));
        setupLinkPreference("telegram", links.getString("telegram"));
        setupLinkPreference("github", links.getString("github"));
    }

    private void setupLinkPreference(String key, String url) {
        Preference pref = findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            });
        }
    }

    private void setupTeam(JSONArray teamArray) throws JSONException {
        PreferenceCategory teamCategory = findPreference("team_category");
        if (teamCategory != null) {
            for (int i = 0; i < teamArray.length(); i++) {
                JSONObject member = teamArray.getJSONObject(i);
                String name = member.getString("name");
                String job = member.getString("job");
                String github = member.getString("github");
                String telegram = member.getString("telegram");

                TeamMemberPreference pref = new TeamMemberPreference(requireContext(), null);
                pref.setTitle(name);
                pref.setSummary(job);
                pref.setImageUrl("https://github.com/" + github + ".png");

                pref.setOnPreferenceClickListener(p -> {
                    showMemberLinks(name, github, telegram);
                    return true;
                });

                teamCategory.addPreference(pref);
            }
        }
    }

    private void showMemberLinks(String name, String githubUsername, String telegramUrl) {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.contact_dialog_title, name))
            .setItems(new CharSequence[]{
                getString(R.string.contact_option_github),
                getString(R.string.contact_option_telegram)
            }, (dialog, which) -> {
                String url = which == 0
                        ? "https://github.com/" + githubUsername
                        : telegramUrl;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private String loadJSONFromRaw() {
        try {
            InputStream is = getResources().openRawResource(R.raw.luminedroid);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}

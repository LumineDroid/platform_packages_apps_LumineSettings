/*
 * Copyright © 2018 Syberia Project
 * Date: 05.10.2018
 * Time: 21:21
 * Author: @alexxxdev <alexxxdev@ya.ru>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.luminedroid.extensions.category;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;

import org.luminedroid.extensions.category.AboutLumineAdapter;
import org.luminedroid.extensions.category.AboutLumineAdapter.About;
import org.luminedroid.extensions.category.AboutLumineAdapter.Dev;
import org.luminedroid.extensions.category.AboutLumineAdapter.TeamHeader;
import org.luminedroid.extensions.category.AboutLumineAdapter.Header;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AboutLumine extends SettingsPreferenceFragment {

    private List<AboutLumineAdapter.About> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_lumine, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.about_lumine_title);
        initList();

        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new AboutLumineAdapter(list, new AboutLumineAdapter.OnClickListener() {
            @Override
            public void OnClick(String url) {
                if (!url.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            }
        }));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYBERIA;
    }

    public String loadJSONFromRaw() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.luminedroid);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void initList() {
        try {
            String jsonString = loadJSONFromRaw();
            if (jsonString == null) return;

            JSONObject obj = new JSONObject(jsonString);
            String lumineGithub = "";
            String lumineTelegram = "";

            if (obj.has("lumine_github")) lumineGithub = obj.getString("lumine_github");
            if (obj.has("lumine_telegram")) lumineTelegram = obj.getString("lumine_telegram");

            list.add(new AboutLumineAdapter.TeamHeader(lumineGithub, lumineTelegram));

            if (obj.has("team")) {
                list.add(new AboutLumineAdapter.Header("Team"));
                JSONArray team = obj.getJSONArray("team");
                for (int i = 0; i < team.length(); i++) {
                    JSONObject dev = team.getJSONObject(i);
                    list.add(new AboutLumineAdapter.Dev(
                            dev.getString("name"),
                            dev.getString("role"),
                            dev.getString("avatar"),
                            "",
                            dev.getString("telegram_link")
                    ));
                }
            }
            if (obj.has("maintainers")) {
                list.add(new AboutLumineAdapter.Header("Maintainers"));
                JSONArray maintainers = obj.getJSONArray("maintainers");
                for (int i = 0; i < maintainers.length(); i++) {
                    JSONObject maintainer = maintainers.getJSONObject(i);
                    list.add(new AboutLumineAdapter.Maintainer(
                            maintainer.getString("device"),
                            new AboutLumineAdapter.Dev(
                                    maintainer.getString("name"),
                                    "",
                                    maintainer.getString("avatar"),
                                    maintainer.getString("github_link"),
                                    ""
                            )
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//
// SPDX-FileCopyrightText: 2019 The PixelExperience Project
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.misc;

import android.content.Context;
import android.content.pm.UserInfo;
import android.provider.Settings;
import android.os.UserManager;

import com.android.settings.core.BasePreferenceController;

import com.android.internal.util.luminedroid.HideDeveloperStatusUtils;

import java.util.List;

public class HideDeveloperStatusPreferenceController extends BasePreferenceController {

    private static final String PREF_KEY = "hide_developer_status_settings";
    private static HideDeveloperStatusUtils hideDeveloperStatusUtils = new HideDeveloperStatusUtils();

    private UserManager userManager;
    private List<UserInfo> userInfos;

    public HideDeveloperStatusPreferenceController(Context context) {
        super(context, PREF_KEY);
        userManager = UserManager.get(context);
        userInfos = userManager.getUsers();
        for (UserInfo info: userInfos) {
            hideDeveloperStatusUtils.setApps(context, info.id);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}

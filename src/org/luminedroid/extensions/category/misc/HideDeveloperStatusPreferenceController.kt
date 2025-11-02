/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.misc

import android.content.Context
import android.content.pm.UserInfo
import android.os.UserManager
import android.provider.Settings
import com.android.settings.core.BasePreferenceController
import com.android.internal.util.luminedroid.HideDeveloperStatusUtils
import java.util.List

class HideDeveloperStatusPreferenceController(context: Context) :
    BasePreferenceController(context, PREF_KEY) {

    private val userManager: UserManager
    private val userInfos: List<UserInfo>

    init {
        userManager = UserManager.get(context)
        userInfos = userManager.users
        for (info in userInfos) {
            hideDeveloperStatusUtils.setApps(context, info.id)
        }
    }

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE
    }

    companion object {
        private const val PREF_KEY = "hide_developer_status_settings"
        private val hideDeveloperStatusUtils = HideDeveloperStatusUtils()
    }
}


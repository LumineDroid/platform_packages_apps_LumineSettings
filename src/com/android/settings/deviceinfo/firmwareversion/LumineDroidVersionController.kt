/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import com.android.settings.core.BasePreferenceController

class LumineDroidVersionController(
    private val context: Context,
    key: String
) : BasePreferenceController(context, key) {

    companion object {
        private const val LUMINE_BUILD_VERSION_PROP = "org.lumine.build.version"
        private const val LUMINE_BUILD_TYPE_PROP = "org.lumine.build.type"
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE_UNSEARCHABLE

    override fun getSummary(): CharSequence {
        val lumineBuildVersion = SystemProperties.get(LUMINE_BUILD_VERSION_PROP, "Unknown")
        val lumineBuildType = SystemProperties.get(LUMINE_BUILD_TYPE_PROP, "Unknown")

        return "$lumineBuildVersion | $lumineBuildType"
    }
}

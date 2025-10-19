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
        private const val LUMINE_VERSION_PROP = "org.luminedroid.version"
        private const val LUMINE_DEVICE_PROP = "org.luminedroid.device"
        private const val LUMINE_BUILDTYPE_PROP = "org.luminedroid.build.type"
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE_UNSEARCHABLE

    override fun getSummary(): CharSequence {
        val lumineBuildVersion = SystemProperties.get(LUMINE_VERSION_PROP, "Unknown")
        val lumineDevice = SystemProperties.get(LUMINE_DEVICE_PROP, "Unknown")
        val lumineBuildType = SystemProperties.get(LUMINE_BUILDTYPE_PROP, "Unknown")

        return "$lumineBuildVersion | $lumineDevice | $lumineBuildType"
    }
}

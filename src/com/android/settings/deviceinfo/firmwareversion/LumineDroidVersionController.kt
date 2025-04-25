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
        private const val LUMINE_DISPLAY_VERSION_PROP = "ro.lineage.display.version"
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE_UNSEARCHABLE

    override fun getSummary(): CharSequence {
        val lumineBuildVersion = SystemProperties.get(LUMINE_DISPLAY_VERSION_PROP, "Unknown")

        return "$lumineBuildVersion"
    }
}

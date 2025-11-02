/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display

import android.content.Context
import android.provider.Settings
import android.os.UserHandle

import com.android.settings.core.BasePreferenceController

import com.android.internal.util.luminedroid.cutout.CutoutFullscreenController

class DisplayCutoutForceFullscreenPreferenceController(
    context: Context
) : BasePreferenceController(context, PREF_KEY) {

    private val mCutoutForceFullscreenSettings: CutoutFullscreenController

    init {
        mCutoutForceFullscreenSettings = CutoutFullscreenController(context)
    }

    override fun getAvailabilityStatus(): Int {
        return if (mCutoutForceFullscreenSettings.isSupported()) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
    }

    companion object {
        private const val PREF_KEY = "display_cutout_force_fullscreen_settings"
    }
}

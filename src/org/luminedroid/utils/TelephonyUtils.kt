/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.utils

import android.content.Context
import android.telephony.TelephonyManager
import androidx.annotation.NonNull

object TelephonyUtils {

    private val TAG: String = TelephonyUtils::class.java.simpleName

    @JvmStatic
    fun isVoiceCapable(@NonNull context: Context): Boolean {
        val telephony: TelephonyManager? = context.getSystemService(TelephonyManager::class.java)
        
        return telephony?.isVoiceCapable ?: false
    }
}


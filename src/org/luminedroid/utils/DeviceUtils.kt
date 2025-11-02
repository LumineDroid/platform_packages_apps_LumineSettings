/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.utils

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.SystemProperties
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.Display
import android.view.DisplayCutout
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.Surface
import androidx.annotation.NonNull
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL
import org.lineageos.internal.util.DeviceKeysConstants.*

object DeviceUtils {

    @JvmStatic
    fun hasCenteredCutout(context: Context): Boolean {
        val display: Display? = context.display
        val cutout: DisplayCutout? = display?.cutout
        
        if (cutout != null && display != null) {
            val realSize = Point()
            display.getRealSize(realSize)

            val rect: Rect? = when (display.rotation) {
                Surface.ROTATION_0 -> cutout.boundingRectTop
                Surface.ROTATION_90 -> cutout.boundingRectLeft
                Surface.ROTATION_180 -> cutout.boundingRectBottom
                Surface.ROTATION_270 -> cutout.boundingRectRight
                else -> null
            }

            return rect?.let {
                when (display.rotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> !(it.left <= 0 || it.right >= realSize.x)
                    Surface.ROTATION_90, Surface.ROTATION_270 -> !(it.top <= 0 || it.bottom >= realSize.y)
                    else -> false
                }
            } ?: false
        }
        return false
    }

    @JvmStatic
    fun getDeviceKeys(context: Context): Int {
        return context
            .resources
            .getInteger(org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys)
    }

    @JvmStatic
    fun getDeviceWakeKeys(context: Context): Int {
        return context
            .resources
            .getInteger(org.lineageos.platform.internal.R.integer.config_deviceHardwareWakeKeys)
    }

    @JvmStatic
    fun hasPowerKey(): Boolean {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
    }

    @JvmStatic
    fun hasHomeKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_HOME) != 0
    }

    @JvmStatic
    fun hasBackKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_BACK) != 0
    }

    @JvmStatic
    fun hasMenuKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_MENU) != 0
    }

    @JvmStatic
    fun hasAssistKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_ASSIST) != 0
    }

    @JvmStatic
    fun hasAppSwitchKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_APP_SWITCH) != 0
    }

    @JvmStatic
    fun hasCameraKey(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_CAMERA) != 0
    }

    @JvmStatic
    fun hasVolumeKeys(context: Context): Boolean {
        return (getDeviceKeys(context) and KEY_MASK_VOLUME) != 0
    }

    @JvmStatic
    fun canWakeUsingHomeKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_HOME) != 0
    }

    @JvmStatic
    fun canWakeUsingBackKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_BACK) != 0
    }

    @JvmStatic
    fun canWakeUsingMenuKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_MENU) != 0
    }

    @JvmStatic
    fun canWakeUsingAssistKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_ASSIST) != 0
    }

    @JvmStatic
    fun canWakeUsingAppSwitchKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_APP_SWITCH) != 0
    }

    @JvmStatic
    fun canWakeUsingCameraKey(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_CAMERA) != 0
    }

    @JvmStatic
    fun canWakeUsingVolumeKeys(context: Context): Boolean {
        return (getDeviceWakeKeys(context) and KEY_MASK_VOLUME) != 0
    }

    @JvmStatic
    fun hasButtonBacklightSupport(context: Context): Boolean {
        val buttonBrightnessControlSupported =
            context
                .resources
                .getInteger(
                    org.lineageos.platform.internal.R.integer
                        .config_deviceSupportsButtonBrightnessControl
                ) != 0

        return buttonBrightnessControlSupported && (
            hasHomeKey(context) ||
                hasBackKey(context) ||
                hasMenuKey(context) ||
                hasAssistKey(context) ||
                hasAppSwitchKey(context)
            )
    }

    @JvmStatic
    fun hasKeyboardBacklightSupport(context: Context): Boolean {
        return context
            .resources
            .getInteger(
                org.lineageos.platform.internal.R.integer
                    .config_deviceSupportsKeyboardBrightnessControl
            ) != 0
    }

    @JvmStatic
    fun isPackageInstalled(context: Context, pkg: String?, ignoreState: Boolean): Boolean {
        if (pkg != null) {
            try {
                val pi: PackageInfo =
                    context.packageManager.getPackageInfo(pkg, PackageManager.GET_META_DATA.toLong())
                if (!pi.applicationInfo.enabled && !ignoreState) {
                    return false
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun lockCurrentOrientation(activity: Activity) {
        val currentRotation = activity.display?.rotation ?: Surface.ROTATION_0
        val orientation = activity.resources.configuration.orientation
        val frozenRotation: Int
        
        frozenRotation = when (currentRotation) {
            Surface.ROTATION_0 -> {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            Surface.ROTATION_90 -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
            Surface.ROTATION_180 -> {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                }
            }
            Surface.ROTATION_270 -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                }
            }
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        
        if (frozenRotation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.requestedOrientation = frozenRotation
        }
    }

    @JvmStatic
    fun isDozeAvailable(context: Context): Boolean {
        var name: String? = if (Build.IS_DEBUGGABLE) {
            SystemProperties.get("debug.doze.component")
        } else {
            null
        }
        if (TextUtils.isEmpty(name)) {
            name = context.resources.getString(com.android.internal.R.string.config_dozeComponent)
        }
        return !TextUtils.isEmpty(name)
    }

    @JvmStatic
    fun deviceSupportsMobileData(ctx: Context): Boolean {
        val telephonyManager = ctx.getSystemService(TelephonyManager::class.java)
        return telephonyManager.isDataCapable
    }

    @JvmStatic
    fun deviceSupportsBluetooth(ctx: Context): Boolean {
        val bluetoothManager: BluetoothManager? =
            ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter != null
    }

    @JvmStatic
    fun deviceSupportsNfc(ctx: Context): Boolean {
        return NfcAdapter.getDefaultAdapter(ctx) != null
    }

    @JvmStatic
    fun deviceSupportsFlashLight(@NonNull context: Context): Boolean {
        val cameraManager = context.getSystemService(CameraManager::class.java)
        try {
            val ids = cameraManager.cameraIdList
            for (id in ids) {
                val c = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable != null && flashAvailable &&
                    lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK
                ) {
                    return true
                }
            }
        } catch (e: CameraAccessException) {
        } catch (e: AssertionError) {
        }
        return false
    }

    @JvmStatic
    fun isMobileDataEnabled(context: Context): Boolean {
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()
        return telephonyManager.createForSubscriptionId(subId).isDataEnabled
    }

    @JvmStatic
    fun isSwipeUpEnabled(context: Context): Boolean {
        if (isEdgeToEdgeEnabled(context)) {
            return false
        }
        return NAV_BAR_MODE_2BUTTON == context
            .resources
            .getInteger(com.android.internal.R.integer.config_navBarInteractionMode)
    }

    @JvmStatic
    fun isEdgeToEdgeEnabled(context: Context): Boolean {
        return NAV_BAR_MODE_GESTURAL == context
            .resources
            .getInteger(com.android.internal.R.integer.config_navBarInteractionMode)
    }
}


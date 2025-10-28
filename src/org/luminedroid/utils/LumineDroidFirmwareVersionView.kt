/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.text.BidiFormatter
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.android.settings.R
import com.android.settings.Utils
import com.android.settingslib.RestrictedLockUtils
import com.android.settingslib.RestrictedLockUtilsInternal
import com.android.settingslib.DeviceInfoUtils
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class LumineDroidFirmwareVersionView : AppCompatTextView {

    private val TAG = "LumineFirmwareView"
    private val DELAY_TIMER_MILLIS = 500
    private val ACTIVITY_TRIGGER_COUNT = 3
    private val mHits = LongArray(ACTIVITY_TRIGGER_COUNT)

    private var mFunDisallowedAdmin: RestrictedLockUtils.EnforcedAdmin? = null
    private var mFunDisallowedBySystem = false
    private var fullKernelVersion = false

    private val userManager by lazy { context.getSystemService(Context.USER_SERVICE) as UserManager }
    private val packageManager: PackageManager by lazy { context.packageManager }
    private val SECURITY_PATCH_URL = Uri.parse("https://source.android.com/docs/security/bulletin/")
    private val KEY_BUILD_DATE_PROP = "ro.build.date"
    private val LUMINE_VERSION_PROP = "org.luminedroid.version"
    private val LUMINE_DEVICE_PROP = "org.luminedroid.device"
    private val LUMINE_BUILDTYPE_PROP = "org.luminedroid.build.type"
    private val LUMINE_MAINTAINER_PROP = "org.luminedroid.maintainer"

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        initializeAdminPermissions()
        post { updateTextViews() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateTextViews()
    }

    private fun updateTextViews() {
        val parentView = rootView ?: return

        val androidVersionLayout = parentView.findViewById<LinearLayout>(R.id.lumine_android_version)
        val androidVersionSummary = parentView.findViewById<TextView>(R.id.lumine_android_version_summary)
        val lumineVersion = parentView.findViewById<TextView>(R.id.lumine_version)
        val maintainerLayout = parentView.findViewById<LinearLayout>(R.id.lumine_maintainer)
        val maintainerSummary = parentView.findViewById<TextView>(R.id.lumine_maintainer_summary)
        val securityPatchLayout = parentView.findViewById<LinearLayout>(R.id.lumine_security_patch)
        val securityPatchSummary = parentView.findViewById<TextView>(R.id.lumine_security_patch_summary)
        val basebandSummary = parentView.findViewById<TextView>(R.id.lumine_baseband_summary)
        val kernelLayout = parentView.findViewById<LinearLayout>(R.id.lumine_kernel)
        val kernelSummary = parentView.findViewById<TextView>(R.id.lumine_kernel_summary)
        val buildDateSummary = parentView.findViewById<TextView>(R.id.lumine_build_date_summary)
        val buildNumberSummary = parentView.findViewById<TextView>(R.id.lumine_build_number_summary)

        androidVersionSummary?.text = getAndroidVersion()
        lumineVersion?.text = getLumineVersion()
        maintainerSummary?.text = getMaintainerName()
        securityPatchSummary?.text = getSecurityPatch()
        basebandSummary?.text = getBasebandVersion()
        kernelSummary?.text = getFormattedKernelVersion()
        buildDateSummary?.text = getBuildDate()
        buildNumberSummary?.text = getBuildNumber()

        androidVersionLayout?.setOnClickListener { handleAndroidVersionClick() }
        maintainerLayout?.setOnClickListener { handleMaintainerClick() }
        securityPatchLayout?.setOnClickListener { handleSecurityPatchClick() }
        kernelLayout?.setOnClickListener { handleKernelClick(kernelSummary) }
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY

    fun getLumineVersion(): String {
        val lumineBuildVersion = SystemProperties.get(LUMINE_VERSION_PROP, "Unknown")
        val lumineDevice = SystemProperties.get(LUMINE_DEVICE_PROP, "Unknown")
        val lumineBuildType = SystemProperties.get(LUMINE_BUILDTYPE_PROP, "Unknown")

        return "$lumineBuildVersion | $lumineDevice | $lumineBuildType"
        }

    fun getMaintainerName(): String {
        val maintainer = SystemProperties.get(LUMINE_MAINTAINER_PROP, "Unknown")
        return if (maintainer.isNotEmpty()) maintainer else "Unknown"
    }

    fun getSecurityPatch(): String = DeviceInfoUtils.getSecurityPatch() ?: "Unknown"

    fun getBasebandVersion(): String = SystemProperties.get("gsm.version.baseband", "Unavailable")

    fun getFormattedKernelVersion(): String = DeviceInfoUtils.getFormattedKernelVersion(context)

    fun getFullKernelVersion(): String {
        val file = "/proc/version"
        return try {
            BufferedReader(FileReader(file), 256).use { it.readLine() ?: "Unavailable" }
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception when reading kernel version", e)
            "Unavailable"
        }
    }

    fun getBuildDate(): String {
        return SystemProperties.get(KEY_BUILD_DATE_PROP, context.getString(R.string.unknown))
    }

    fun getBuildNumber(): String {
        val display = Build.DISPLAY.replace("lineage_", "lumine_")
        return BidiFormatter.getInstance().unicodeWrap(display)
    }

    private fun handleAndroidVersionClick() {
        if (Utils.isMonkeyRunning()) return

        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()

        if (mHits[0] >= (SystemClock.uptimeMillis() - DELAY_TIMER_MILLIS)) {
            if (userManager.hasUserRestriction(UserManager.DISALLOW_FUN)) {
                if (mFunDisallowedAdmin != null && !mFunDisallowedBySystem) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, mFunDisallowedAdmin)
                }
                Log.d(TAG, "Sorry, no fun for you!")
                return
            }

            val intent = Intent(Intent.ACTION_MAIN)
                .setClassName("android", "com.android.internal.app.PlatLogoActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to start activity $intent", e)
            }
        }
    }

    private fun handleMaintainerClick() {
        val maintainer = SystemProperties.get(LUMINE_MAINTAINER_PROP, "")
        if (maintainer.isBlank() || maintainer == "Unknown") {
            Log.w(TAG, "No maintainer info available")
            return
        }

        val username = maintainer.trim()
            .replace("@", "")
        val telegramUri = Uri.parse("https://t.me/$username")

        val intent = Intent(Intent.ACTION_VIEW, telegramUri)
        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "No Telegram app found, opening in browser")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Telegram link for maintainer", e)
        }
    }

    private fun handleSecurityPatchClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply { data = SECURITY_PATCH_URL }

        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "No activity can handle security patch intent")
            return
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open security bulletin URL", e)
        }
    }

    private fun handleKernelClick(view: TextView?) {
        view ?: return
        if (fullKernelVersion) {
            view.text = getFormattedKernelVersion()
            fullKernelVersion = false
        } else {
            view.text = getFullKernelVersion()
            fullKernelVersion = true
        }
    }

    private fun initializeAdminPermissions() {
        mFunDisallowedAdmin = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
            context, UserManager.DISALLOW_FUN, UserHandle.myUserId()
        )
        mFunDisallowedBySystem = RestrictedLockUtilsInternal.hasBaseUserRestriction(
            context, UserManager.DISALLOW_FUN, UserHandle.myUserId()
        )
    }
}

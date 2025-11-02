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

class LumineDroidFirmwareVersionView : AppCompatTextView {

    private val TAG = "LumineFirmwareView"
    private val DELAY_TIMER_MILLIS = 500
    private val ACTIVITY_TRIGGER_COUNT = 3
    private val mHits = LongArray(ACTIVITY_TRIGGER_COUNT)

    private var mFunDisallowedAdmin: RestrictedLockUtils.EnforcedAdmin? = null
    private var mFunDisallowedBySystem = false

    private val userManager by lazy { context.getSystemService(Context.USER_SERVICE) as UserManager }
    private val packageManager: PackageManager by lazy { context.packageManager }

    private val LUMINE_VERSION_PROP = "org.luminedroid.version"
    private val LUMINE_DEVICE_PROP = "org.luminedroid.device"
    private val LUMINE_BUILDTYPE_PROP = "org.luminedroid.build.type"
    private val LUMINE_MAINTAINER_PROP = "org.luminedroid.maintainer"
    private val LUMINE_MAINTAINER_LINK_PROP = "org.luminedroid.maintainer.link"
    private val DEFAULT_MAINTAINER_LINK = "https://github.com/LumineDroid"

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

        androidVersionSummary?.text = getAndroidVersion()
        lumineVersion?.text = getLumineVersion()
        maintainerSummary?.text = getMaintainerName()

        androidVersionLayout?.setOnClickListener { handleAndroidVersionClick() }
        maintainerLayout?.setOnClickListener { handleMaintainerClick() }
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
        val maintainerLink = SystemProperties.get(LUMINE_MAINTAINER_LINK_PROP, DEFAULT_MAINTAINER_LINK)
        val finalLink = if (maintainerLink.isBlank()) DEFAULT_MAINTAINER_LINK else maintainerLink.trim()

        val uri = try {
            Uri.parse(finalLink)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid maintainer link: $finalLink", e)
            Uri.parse(DEFAULT_MAINTAINER_LINK)
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "No activity found to handle maintainer link, fallback to browser")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open maintainer link: $finalLink", e)
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

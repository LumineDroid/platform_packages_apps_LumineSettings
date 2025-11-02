/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.button

import android.Manifest
import android.content.Context
import android.content.pm.UserInfo
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.service.controls.ControlsProviderService
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.internal.util.EmergencyAffordanceManager
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settingslib.applications.ServiceListing
import java.util.List
import lineageos.app.LineageGlobalActions
import org.lineageos.internal.util.PowerMenuConstants
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_AIRPLANE // Import static
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_BUGREPORT // Import static
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_DEVICECONTROLS // Import static
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_EMERGENCY // Import static
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_SCREENSHOT // Import static
import org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_USERS // Import static
import org.luminedroid.utils.TelephonyUtils

class PowerMenuActions : SettingsPreferenceFragment() {

    private lateinit var mPowerMenuItemsCategory: PreferenceCategory

    private var mScreenshotPref: CheckBoxPreference? = null
    private var mAirplanePref: CheckBoxPreference? = null
    private var mUsersPref: CheckBoxPreference? = null
    private var mBugReportPref: CheckBoxPreference? = null
    private var mEmergencyPref: CheckBoxPreference? = null
    private var mDeviceControlsPref: CheckBoxPreference? = null

    private lateinit var mLineageGlobalActions: LineageGlobalActions

    private lateinit var mEmergencyAffordanceManager: EmergencyAffordanceManager
    private var mForceEmergCheck = false

    private lateinit var mContext: Context
    private lateinit var mUserManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.power_menu_settings)
        requireActivity().title = getString(R.string.power_menu_title)
        mContext = requireActivity().applicationContext
        mUserManager = UserManager.get(mContext)
        mLineageGlobalActions = LineageGlobalActions.getInstance(mContext)
        mEmergencyAffordanceManager = EmergencyAffordanceManager(mContext)

        mPowerMenuItemsCategory = findPreference(CATEGORY_POWER_MENU_ITEMS)!!

        for (action in PowerMenuConstants.getAllActions()) {
            if (action == GLOBAL_ACTION_KEY_SCREENSHOT) {
                mScreenshotPref = findPreference(GLOBAL_ACTION_KEY_SCREENSHOT)
            } else if (action == GLOBAL_ACTION_KEY_AIRPLANE) {
                mAirplanePref = findPreference(GLOBAL_ACTION_KEY_AIRPLANE)
            } else if (action == GLOBAL_ACTION_KEY_USERS) {
                mUsersPref = findPreference(GLOBAL_ACTION_KEY_USERS)
            } else if (action == GLOBAL_ACTION_KEY_BUGREPORT) {
                mBugReportPref = findPreference(GLOBAL_ACTION_KEY_BUGREPORT)
            } else if (action == GLOBAL_ACTION_KEY_EMERGENCY) {
                mEmergencyPref = findPreference(GLOBAL_ACTION_KEY_EMERGENCY)
            } else if (action == GLOBAL_ACTION_KEY_DEVICECONTROLS) {
                mDeviceControlsPref = findPreference(GLOBAL_ACTION_KEY_DEVICECONTROLS)
            }
        }

        if (!TelephonyUtils.isVoiceCapable(requireActivity())) {
            mPowerMenuItemsCategory.removePreference(mEmergencyPref)
            mEmergencyPref = null
        }
    }

    override fun onStart() {
        super.onStart()

        if (mScreenshotPref != null) {
            mScreenshotPref!!.isChecked =
                mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_SCREENSHOT)
        }

        if (mAirplanePref != null) {
            mAirplanePref!!.isChecked =
                mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_AIRPLANE)
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                mPowerMenuItemsCategory.removePreference(mUsersPref)
                mUsersPref = null
            } else {
                val users: List<UserInfo> = mUserManager.users
                val enabled = users.size > 1
                mUsersPref!!.isChecked =
                    mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_USERS) && enabled
                mUsersPref!!.isEnabled = enabled
            }
        }

        if (mBugReportPref != null) {
            mBugReportPref!!.isChecked =
                mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_BUGREPORT)
        }

        if (mEmergencyPref != null) {
            mForceEmergCheck = mEmergencyAffordanceManager.needsEmergencyAffordance()
            mEmergencyPref!!.isChecked =
                mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_EMERGENCY) || mForceEmergCheck
            mEmergencyPref!!.isEnabled = !mForceEmergCheck
        }

        if (mDeviceControlsPref != null) {
            mDeviceControlsPref!!.isChecked =
                mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_DEVICECONTROLS)

            // Enable preference if any device control app is installed
            val serviceListing =
                ServiceListing.Builder(mContext)
                    .setIntentAction(ControlsProviderService.SERVICE_CONTROLS)
                    .setPermission(Manifest.permission.BIND_CONTROLS)
                    .setNoun("Controls Provider")
                    .setSetting("controls_providers")
                    .setTag("controls_providers")
                    .build()
            serviceListing.addCallback { services -> mDeviceControlsPref!!.isEnabled = services.isNotEmpty() }
            serviceListing.reload()
        }

        updatePreferences()
    }

    override fun onResume() {
        super.onResume()
        updatePreferences()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val value: Boolean

        if (preference == mScreenshotPref) {
            value = mScreenshotPref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT)

        } else if (preference == mAirplanePref) {
            value = mAirplanePref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE)

        } else if (preference == mUsersPref) {
            value = mUsersPref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_USERS)

        } else if (preference == mBugReportPref) {
            value = mBugReportPref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_BUGREPORT)
            Settings.Global.putInt(
                contentResolver, Settings.Global.BUGREPORT_IN_POWER_MENU, if (value) 1 else 0
            )

        } else if (preference == mEmergencyPref) {
            value = mEmergencyPref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_EMERGENCY)

        } else if (preference == mDeviceControlsPref) {
            value = mDeviceControlsPref!!.isChecked
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_DEVICECONTROLS)

        } else {
            return super.onPreferenceTreeClick(preference)
        }
        return true
    }

    private fun updatePreferences() {
        val currentUser = mUserManager.getUserInfo(UserHandle.myUserId())
        val developmentSettings =
            Settings.Global.getInt(
                contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) == 1
        val bugReport =
            Settings.Global.getInt(contentResolver, Settings.Global.BUGREPORT_IN_POWER_MENU, 0) == 1
        val isPrimaryUser = currentUser == null || currentUser.isPrimary

        if (mBugReportPref != null) {
            mBugReportPref!!.isEnabled = developmentSettings && isPrimaryUser
            if (!developmentSettings) {
                mBugReportPref!!.isChecked = false
                mBugReportPref!!.setSummary(R.string.power_menu_bug_report_devoptions_unavailable)
            } else if (!isPrimaryUser) {
                mBugReportPref!!.isChecked = false
                mBugReportPref!!.setSummary(R.string.power_menu_bug_report_unavailable_for_user)
            } else {
                mBugReportPref!!.isChecked = bugReport
                mBugReportPref!!.setSummary(null)
            }
        }
        if (mEmergencyPref != null) {
            if (mForceEmergCheck) {
                mEmergencyPref!!.setSummary(R.string.power_menu_emergency_affordance_enabled)
            } else {
                mEmergencyPref!!.setSummary(null)
            }
        }
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    companion object {
        internal const val TAG = "PowerMenuActions"

        private const val CATEGORY_POWER_MENU_ITEMS = "power_menu_items"
    }
}


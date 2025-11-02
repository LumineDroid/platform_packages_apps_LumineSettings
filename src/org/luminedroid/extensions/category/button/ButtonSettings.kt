/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.button

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.om.IOverlayManager
import android.content.res.Resources
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.os.ServiceManager
import android.os.UserHandle
import android.provider.Settings
import android.util.ArraySet
import android.util.Log
import android.view.Display
import android.view.IWindowManager
import android.view.WindowManagerGlobal
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.Indexable
import com.android.settingslib.search.SearchIndexable
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.Set
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings
import org.luminedroid.extensions.category.button.ButtonBacklightBrightness
import org.luminedroid.utils.DeviceUtils
import org.luminedroid.utils.TelephonyUtils
import org.lineageos.internal.util.DeviceKeysConstants.*
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY
import com.android.systemui.shared.recents.utilities.Utilities.isLargeScreen

@SearchIndexable
class ButtonSettings : SettingsPreferenceFragment(), Preference.OnPreferenceChangeListener, Indexable {

    private lateinit var mBackLongPressAction: ListPreference
    private lateinit var mHomeLongPressAction: ListPreference
    private lateinit var mHomeDoubleTapAction: ListPreference
    private lateinit var mMenuPressAction: ListPreference
    private lateinit var mMenuLongPressAction: ListPreference
    private lateinit var mAssistPressAction: ListPreference
    private lateinit var mAssistLongPressAction: ListPreference
    private lateinit var mAppSwitchPressAction: ListPreference
    private lateinit var mAppSwitchLongPressAction: ListPreference
    private lateinit var mCameraWakeScreen: SwitchPreferenceCompat
    private lateinit var mCameraSleepOnRelease: SwitchPreferenceCompat
    private lateinit var mVolumeKeyCursorControl: ListPreference
    private lateinit var mSwapVolumeButtons: SwitchPreferenceCompat
    private lateinit var mVolumePanelOnLeft: SwitchPreferenceCompat
    private lateinit var mDisableNavigationKeys: SwitchPreferenceCompat
    private lateinit var mNavigationBackLongPressAction: ListPreference
    private lateinit var mNavigationHomeLongPressAction: ListPreference
    private lateinit var mNavigationHomeDoubleTapAction: ListPreference
    private lateinit var mNavigationAppSwitchLongPressAction: ListPreference
    private lateinit var mEdgeLongSwipeAction: ListPreference
    private lateinit var mPowerEndCall: SwitchPreferenceCompat
    private lateinit var mHomeAnswerCall: SwitchPreferenceCompat
    private lateinit var mTorchLongPressPowerTimeout: ListPreference
    private lateinit var mSwapCapacitiveKeys: SwitchPreferenceCompat
    // private lateinit var mNavBarInverse: SwitchPreferenceCompat

    private var mEnableTaskbar: SwitchPreferenceCompat? = null

    private lateinit var mNavigationPreferencesCat: PreferenceCategory

    private lateinit var mHandler: Handler

    private lateinit var mHardware: LineageHardwareManager

    private var mBatteryPresent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.extensions_button)

        mHardware = LineageHardwareManager.getInstance(requireActivity())
        val res: Resources = resources
        val resolver: ContentResolver = requireActivity().contentResolver
        val prefScreen: PreferenceScreen = preferenceScreen

        val hasPowerKey = DeviceUtils.hasPowerKey()
        val hasHomeKey = DeviceUtils.hasHomeKey(requireActivity())
        val hasBackKey = DeviceUtils.hasBackKey(requireActivity())
        val hasMenuKey = DeviceUtils.hasMenuKey(requireActivity())
        val hasAssistKey = DeviceUtils.hasAssistKey(requireActivity())
        val hasAppSwitchKey = DeviceUtils.hasAppSwitchKey(requireActivity())
        val hasCameraKey = DeviceUtils.hasCameraKey(requireActivity())
        val hasVolumeKeys = DeviceUtils.hasVolumeKeys(requireActivity())

        val showHomeWake = DeviceUtils.canWakeUsingHomeKey(requireActivity())
        val showBackWake = DeviceUtils.canWakeUsingBackKey(requireActivity())
        val showMenuWake = DeviceUtils.canWakeUsingMenuKey(requireActivity())
        val showAssistWake = DeviceUtils.canWakeUsingAssistKey(requireActivity())
        val showAppSwitchWake = DeviceUtils.canWakeUsingAppSwitchKey(requireActivity())
        val showCameraWake = DeviceUtils.canWakeUsingCameraKey(requireActivity())
        val showVolumeWake = DeviceUtils.canWakeUsingVolumeKeys(requireActivity())

        val powerCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_POWER)
        val homeCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_HOME)
        val backCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_BACK)
        val menuCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_MENU)
        val assistCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_ASSIST)
        val appSwitchCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_APPSWITCH)
        val volumeCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_VOLUME)
        val cameraCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_CAMERA)
        val extrasCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_EXTRAS)

        // Power button ends calls.
        mPowerEndCall = findPreference(KEY_POWER_END_CALL)!!

        // Long press power while display is off to activate torchlight
        val torchLongPressPowerGesture: SwitchPreferenceCompat? =
            findPreference(KEY_TORCH_LONG_PRESS_POWER_GESTURE)
        val torchLongPressPowerTimeout =
            LineageSettings.System.getInt(
                resolver, LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0
            )
        mTorchLongPressPowerTimeout =
            initList(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT, torchLongPressPowerTimeout)!!

        // Home button answers calls.
        mHomeAnswerCall = findPreference(KEY_HOME_ANSWER_CALL)!!

        mHandler = Handler(Looper.getMainLooper())

        // Force Navigation bar related options
        mDisableNavigationKeys = findPreference(KEY_DISABLE_NAV_KEYS)!!

        mNavigationPreferencesCat = findPreference(CATEGORY_NAVBAR)!!

        val defaultBackLongPressAction =
            Action.fromIntSafe(
                res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior
                )
            )
        val defaultHomeLongPressAction =
            Action.fromIntSafe(
                res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior
                )
            )
        val defaultHomeDoubleTapAction =
            Action.fromIntSafe(
                res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior
                )
            )
        val defaultAppSwitchLongPressAction =
            Action.fromIntSafe(
                res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior
                )
            )
        val backLongPressAction =
            Action.fromSettings(
                resolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction
            )
        val homeLongPressAction =
            Action.fromSettings(
                resolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction
            )
        val homeDoubleTapAction =
            Action.fromSettings(
                resolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction
            )
        val appSwitchLongPressAction =
            Action.fromSettings(
                resolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction
            )
        val edgeLongSwipeAction =
            Action.fromSettings(
                resolver, LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION, Action.NOTHING
            )

        // Navigation bar back long press
        mNavigationBackLongPressAction = initList(KEY_NAVIGATION_BACK_LONG_PRESS, backLongPressAction)!!

        // Navigation bar home long press
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS, homeLongPressAction)!!

        // Navigation bar home double tap
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP, homeDoubleTapAction)!!

        // Navigation bar app switch long press
        mNavigationAppSwitchLongPressAction =
            initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS, appSwitchLongPressAction)!!

        // Edge long swipe gesture
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE, edgeLongSwipeAction)!!

        // Hardware key disabler
        if (isKeyDisablerSupported(requireActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption()
            mNavigationPreferencesCat.isEnabled = mDisableNavigationKeys.isChecked
            mDisableNavigationKeys.setDisableDependentsState(true)
        } else {
            prefScreen.removePreference(mDisableNavigationKeys)
        }
        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked, /* force */ true)

        if (hasPowerKey) {
            if (!TelephonyUtils.isVoiceCapable(requireActivity())) {
                powerCategory?.removePreference(mPowerEndCall)
                // mPowerEndCall is already initialized as lateinit, so we remove the null assignment
            }
            if (!DeviceUtils.deviceSupportsFlashLight(requireActivity())) {
                powerCategory?.removePreference(torchLongPressPowerGesture)
                powerCategory?.removePreference(mTorchLongPressPowerTimeout)
            }
        }
        if (powerCategory == null || powerCategory.preferenceCount == 0) {
            powerCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory?.removePreference(findPreference(KEY_HOME_WAKE_SCREEN))
            }

            if (!TelephonyUtils.isVoiceCapable(requireActivity())) {
                homeCategory?.removePreference(mHomeAnswerCall)
                // mHomeAnswerCall is already initialized as lateinit
            }

            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction)!!
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction)!!
            if (mDisableNavigationKeys.isChecked) {
                mHomeLongPressAction.isEnabled = false
                mHomeDoubleTapAction.isEnabled = false
            }
        }
        if (homeCategory == null || homeCategory.preferenceCount == 0) {
            homeCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasBackKey) {
            if (!showBackWake) {
                backCategory?.removePreference(findPreference(KEY_BACK_WAKE_SCREEN))
            }

            mBackLongPressAction = initList(KEY_BACK_LONG_PRESS, backLongPressAction)!!
            if (mDisableNavigationKeys.isChecked) {
                mBackLongPressAction.isEnabled = false
            }
        }
        if (backCategory == null || backCategory.preferenceCount == 0) {
            backCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory?.removePreference(findPreference(KEY_MENU_WAKE_SCREEN))
            }

            val pressAction =
                Action.fromSettings(resolver, LineageSettings.System.KEY_MENU_ACTION, Action.MENU)
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction)!!

            val longPressAction =
                Action.fromSettings(
                    resolver,
                    LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                    if (hasAssistKey) Action.NOTHING else Action.APP_SWITCH
                )
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction)!!
        }
        if (menuCategory == null || menuCategory.preferenceCount == 0) {
            menuCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory?.removePreference(findPreference(KEY_ASSIST_WAKE_SCREEN))
            }

            val pressAction =
                Action.fromSettings(resolver, LineageSettings.System.KEY_ASSIST_ACTION, Action.SEARCH)
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction)!!

            val longPressAction =
                Action.fromSettings(
                    resolver, LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, Action.VOICE_SEARCH
                )
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction)!!
        }
        if (assistCategory == null || assistCategory.preferenceCount == 0) {
            assistCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory?.removePreference(findPreference(KEY_APP_SWITCH_WAKE_SCREEN))
            }

            val pressAction =
                Action.fromSettings(
                    resolver, LineageSettings.System.KEY_APP_SWITCH_ACTION, Action.APP_SWITCH
                )
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction)!!

            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, appSwitchLongPressAction)!!
        }
        if (appSwitchCategory == null || appSwitchCategory.preferenceCount == 0) {
            appSwitchCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasCameraKey) {
            mCameraWakeScreen = findPreference(KEY_CAMERA_WAKE_SCREEN)!!
            mCameraSleepOnRelease = findPreference(KEY_CAMERA_SLEEP_ON_RELEASE)!!

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen)
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease)
            }
        }
        if (cameraCategory == null || cameraCategory.preferenceCount == 0) {
            cameraCategory?.let { prefScreen.removePreference(it) }
        }

        if (hasVolumeKeys) {
            if (!showVolumeWake) {
                volumeCategory?.removePreference(findPreference(KEY_VOLUME_WAKE_SCREEN))
            }

            if (!TelephonyUtils.isVoiceCapable(requireActivity())) {
                volumeCategory?.removePreference(findPreference(KEY_VOLUME_ANSWER_CALL))
            }

            val cursorControlAction =
                Settings.System.getInt(resolver, Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0)
            mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL, cursorControlAction)!!

            val swapVolumeKeys =
                LineageSettings.System.getInt(
                    resolver, LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0
                )
            mSwapVolumeButtons = prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS)!!
            mSwapVolumeButtons.isChecked = swapVolumeKeys > 0

            val volumePanelOnLeft =
                LineageSettings.Secure.getIntForUser(
                    resolver, LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, 0, UserHandle.USER_CURRENT
                ) != 0
            mVolumePanelOnLeft = prefScreen.findPreference(KEY_VOLUME_PANEL_ON_LEFT)!!
            mVolumePanelOnLeft.isChecked = volumePanelOnLeft
        } else {
            extrasCategory?.removePreference(findPreference(KEY_CLICK_PARTIAL_SCREENSHOT))
        }
        if (volumeCategory == null || volumeCategory.preferenceCount == 0) {
            volumeCategory?.let { prefScreen.removePreference(it) }
        }

        // Only show the navigation bar category on devices that have a navigation bar
        // or support disabling the hardware keys
        if (!hasNavigationBar() && !isKeyDisablerSupported(requireActivity())) {
            prefScreen.removePreference(mNavigationPreferencesCat)
        }

        val backlight: ButtonBacklightBrightness? = findPreference(KEY_BUTTON_BACKLIGHT)
        if (!DeviceUtils.hasButtonBacklightSupport(requireActivity()) &&
            !DeviceUtils.hasKeyboardBacklightSupport(requireActivity())) {
            backlight?.let { prefScreen.removePreference(it) }
        }

        if (hasCameraKey) {
            if (res.getBoolean(org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                // If single-stage key, mCameraSleepOnRelease was already removed, so we check the key's presence
            } else if (mCameraSleepOnRelease != null) {
                mCameraSleepOnRelease.setDependency(KEY_CAMERA_WAKE_SCREEN)
            }
        }

        val volumeWakeScreen: SwitchPreferenceCompat? = findPreference(KEY_VOLUME_WAKE_SCREEN)
        val volumeMusicControls: SwitchPreferenceCompat? = findPreference(KEY_VOLUME_MUSIC_CONTROLS)

        if (volumeWakeScreen != null && volumeMusicControls != null) {
            volumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN)
            volumeWakeScreen.setDisableDependentsState(true)
        }

        mSwapCapacitiveKeys = findPreference(KEY_SWAP_CAPACITIVE_KEYS)!!
        if (!isKeySwapperSupported(requireActivity())) {
            prefScreen.removePreference(mSwapCapacitiveKeys)
        } else {
            mSwapCapacitiveKeys.onPreferenceChangeListener = this
            mSwapCapacitiveKeys.setDependency(KEY_DISABLE_NAV_KEYS)
        }

        // mNavBarInverse = findPreference(KEY_NAV_BAR_INVERSE)

        mEnableTaskbar = findPreference(KEY_ENABLE_TASKBAR)
        if (mEnableTaskbar != null) {
            if (!isLargeScreen(requireContext()) || !hasNavigationBar()) {
                mNavigationPreferencesCat.removePreference(mEnableTaskbar!!)
            } else {
                mEnableTaskbar!!.onPreferenceChangeListener = this
                mEnableTaskbar!!.isChecked =
                    LineageSettings.System.getInt(
                        resolver,
                        LineageSettings.System.ENABLE_TASKBAR,
                        if (isLargeScreen(requireContext())) 1 else 0
                    ) == 1
            }
        }

        val unsupportedValues = ArrayList<Int>()
        val entries =
            ArrayList(Arrays.asList(*res.getStringArray(R.array.hardware_keys_action_entries)))
        val values =
            ArrayList(Arrays.asList(*res.getStringArray(R.array.hardware_keys_action_values)))

        // hide split screen option unconditionally - it doesn't work at the moment
        // once someone gets it working again: hide it only for low-ram devices
        // (check ActivityManager.isLowRamDeviceStatic())
        unsupportedValues.add(Action.SPLIT_SCREEN.ordinal)

        for (unsupportedValue in unsupportedValues) {
            entries.removeAt(unsupportedValue)
            values.removeAt(unsupportedValue)
        }

        val actionEntries = entries.toTypedArray()
        val actionValues = values.toTypedArray()

        if (hasBackKey) {
            mBackLongPressAction.entries = actionEntries
            mBackLongPressAction.entryValues = actionValues
        }

        if (hasHomeKey) {
            mHomeLongPressAction.entries = actionEntries
            mHomeLongPressAction.entryValues = actionValues

            mHomeDoubleTapAction.entries = actionEntries
            mHomeDoubleTapAction.entryValues = actionValues
        }

        if (hasMenuKey) {
            mMenuPressAction.entries = actionEntries
            mMenuPressAction.entryValues = actionValues

            mMenuLongPressAction.entries = actionEntries
            mMenuLongPressAction.entryValues = actionValues
        }

        if (hasAssistKey) {
            mAssistPressAction.entries = actionEntries
            mAssistPressAction.entryValues = actionValues

            mAssistLongPressAction.entries = actionEntries
            mAssistLongPressAction.entryValues = actionValues
        }

        if (hasAppSwitchKey) {
            mAppSwitchPressAction.entries = actionEntries
            mAppSwitchPressAction.entryValues = actionValues

            mAppSwitchLongPressAction.entries = actionEntries
            mAppSwitchLongPressAction.entryValues = actionValues
        }

        mNavigationBackLongPressAction.entries = actionEntries
        mNavigationBackLongPressAction.entryValues = actionValues

        mNavigationHomeLongPressAction.entries = actionEntries
        mNavigationHomeLongPressAction.entryValues = actionValues

        mNavigationHomeDoubleTapAction.entries = actionEntries
        mNavigationHomeDoubleTapAction.entryValues = actionValues

        mNavigationAppSwitchLongPressAction.entries = actionEntries
        mNavigationAppSwitchLongPressAction.entryValues = actionValues

        mEdgeLongSwipeAction.entries = actionEntries
        mEdgeLongSwipeAction.entryValues = actionValues
    }

    override fun onResume() {
        super.onResume()

        // Power button ends calls.
        if (mPowerEndCall != null) {
            val incallPowerBehavior =
                Settings.Secure.getInt(
                    contentResolver,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT
                )
            val powerButtonEndsCall =
                (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP)
            mPowerEndCall.isChecked = powerButtonEndsCall
        }

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            val incallHomeBehavior =
                LineageSettings.Secure.getInt(
                    contentResolver,
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT
                )
            val homeButtonAnswersCall =
                (incallHomeBehavior == LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER)
            mHomeAnswerCall.isChecked = homeButtonAnswersCall
        }
    }

    private fun initList(key: String, value: Action): ListPreference? {
        return initList(key, value.ordinal)
    }

    private fun initList(key: String, value: Int): ListPreference? {
        val list: ListPreference? = preferenceScreen.findPreference(key)
        if (list == null) return null
        list.value = value.toString()
        list.summary = list.entry
        list.onPreferenceChangeListener = this
        return list
    }

    private fun handleListChange(pref: ListPreference, newValue: Any, setting: String) {
        val value = newValue as String
        val index = pref.findIndexOfValue(value)
        pref.summary = pref.entries[index]
        LineageSettings.System.putInt(contentResolver, setting, Integer.parseInt(value))
    }

    private fun handleSystemListChange(pref: ListPreference, newValue: Any, setting: String) {
        val value = newValue as String
        val index = pref.findIndexOfValue(value)
        pref.summary = pref.entries[index]
        Settings.System.putInt(contentResolver, setting, Integer.parseInt(value))
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return when (preference) {
            mBackLongPressAction, mNavigationBackLongPressAction -> {
                handleListChange(
                    preference as ListPreference,
                    newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION
                )
                true
            }
            mHomeLongPressAction, mNavigationHomeLongPressAction -> {
                handleListChange(
                    preference as ListPreference,
                    newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION
                )
                true
            }
            mHomeDoubleTapAction, mNavigationHomeDoubleTapAction -> {
                handleListChange(
                    preference as ListPreference,
                    newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION
                )
                true
            }
            mMenuPressAction -> {
                handleListChange(mMenuPressAction, newValue, LineageSettings.System.KEY_MENU_ACTION)
                true
            }
            mMenuLongPressAction -> {
                handleListChange(
                    mMenuLongPressAction, newValue, LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION
                )
                true
            }
            mAssistPressAction -> {
                handleListChange(mAssistPressAction, newValue, LineageSettings.System.KEY_ASSIST_ACTION)
                true
            }
            mAssistLongPressAction -> {
                handleListChange(
                    mAssistLongPressAction, newValue, LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION
                )
                true
            }
            mAppSwitchPressAction -> {
                handleListChange(
                    mAppSwitchPressAction, newValue, LineageSettings.System.KEY_APP_SWITCH_ACTION
                )
                true
            }
            mAppSwitchLongPressAction, mNavigationAppSwitchLongPressAction -> {
                handleListChange(
                    preference as ListPreference,
                    newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION
                )
                true
            }
            mVolumeKeyCursorControl -> {
                handleSystemListChange(
                    mVolumeKeyCursorControl, newValue, Settings.System.VOLUME_KEY_CURSOR_CONTROL
                )
                true
            }
            mTorchLongPressPowerTimeout -> {
                handleListChange(
                    mTorchLongPressPowerTimeout,
                    newValue,
                    LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT
                )
                true
            }
            mEdgeLongSwipeAction -> {
                handleListChange(
                    mEdgeLongSwipeAction, newValue, LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION
                )
                true
            }
            mSwapCapacitiveKeys -> {
                mHardware.set(LineageHardwareManager.FEATURE_KEY_SWAP, newValue as Boolean)
                true
            }
            mEnableTaskbar -> {
                LineageSettings.System.putInt(
                    contentResolver,
                    LineageSettings.System.ENABLE_TASKBAR,
                    if (newValue as Boolean) 1 else 0
                )
                true
            }
            else -> false
        }
    }

    private fun setButtonNavigationMode(overlayPackage: String) {
        val overlayManager =
            IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
        try {
            overlayManager.setEnabledExclusiveInCategory(overlayPackage, UserHandle.USER_CURRENT)
        } catch (e: RemoteException) {
            throw e.rethrowFromSystemServer()
        }
    }

    private fun enablePreference(pref: Preference?, enabled: Boolean) {
        pref?.isEnabled = enabled
    }

    private fun writeDisableNavkeysOption(enabled: Boolean) {
        LineageSettings.System.putIntForUser(
            requireActivity().contentResolver,
            LineageSettings.System.FORCE_SHOW_NAVBAR,
            if (enabled) 1 else 0,
            UserHandle.USER_CURRENT
        )
        mHardware.set(LineageHardwareManager.FEATURE_KEY_DISABLE, enabled)
    }

    private fun updateDisableNavkeysOption() {
        val enabled =
            LineageSettings.System.getIntForUser(
                requireActivity().contentResolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR,
                0,
                UserHandle.USER_CURRENT
            ) != 0

        mDisableNavigationKeys.isChecked = enabled
    }

    private fun updateDisableNavkeysCategories(navbarEnabled: Boolean, force: Boolean) {
        val prefScreen: PreferenceScreen = preferenceScreen

        /* Disable hw-key options if they're disabled */
        val homeCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_HOME)
        val backCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_BACK)
        val menuCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_MENU)
        val assistCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_ASSIST)
        val appSwitchCategory: PreferenceCategory? = prefScreen.findPreference(CATEGORY_APPSWITCH)
        val backlight: ButtonBacklightBrightness? = prefScreen.findPreference(KEY_BUTTON_BACKLIGHT)

        /* Toggle backlight control depending on navbar state, force it to
        off if enabling */
        if (backlight != null) {
            backlight.isEnabled = !navbarEnabled
            backlight.updateSummary()
        }

        /* Toggle hardkey control availability depending on navbar state */
        if (mNavigationPreferencesCat != null) {
            if (force || navbarEnabled) {
                if (DeviceUtils.isEdgeToEdgeEnabled(requireContext())) {
                    mNavigationPreferencesCat.addPreference(mEdgeLongSwipeAction)

                    mNavigationPreferencesCat.removePreference(mNavigationBackLongPressAction)
                    mNavigationPreferencesCat.removePreference(mNavigationHomeLongPressAction)
                    mNavigationPreferencesCat.removePreference(mNavigationHomeDoubleTapAction)
                    mNavigationPreferencesCat.removePreference(mNavigationAppSwitchLongPressAction)
                } else {
                    mNavigationPreferencesCat.addPreference(mNavigationBackLongPressAction)
                    mNavigationPreferencesCat.addPreference(mNavigationHomeLongPressAction)
                    mNavigationPreferencesCat.addPreference(mNavigationHomeDoubleTapAction)
                    mNavigationPreferencesCat.addPreference(mNavigationAppSwitchLongPressAction)

                    mNavigationPreferencesCat.removePreference(mEdgeLongSwipeAction)
                }
            }
        }
        if (backCategory != null) {
            enablePreference(mBackLongPressAction, !navbarEnabled)
        }
        if (homeCategory != null) {
            enablePreference(mHomeAnswerCall, !navbarEnabled)
            enablePreference(mHomeLongPressAction, !navbarEnabled)
            enablePreference(mHomeDoubleTapAction, !navbarEnabled)
        }
        if (menuCategory != null) {
            enablePreference(mMenuPressAction, !navbarEnabled)
            enablePreference(mMenuLongPressAction, !navbarEnabled)
        }
        if (assistCategory != null) {
            enablePreference(mAssistPressAction, !navbarEnabled)
            enablePreference(mAssistLongPressAction, !navbarEnabled)
        }
        if (appSwitchCategory != null) {
            enablePreference(mAppSwitchPressAction, !navbarEnabled)
            enablePreference(mAppSwitchLongPressAction, !navbarEnabled)
        }
    }

    private fun hasNavigationBar(): Boolean {
        var hasNavigationBar = false
        try {
            val windowManager = WindowManagerGlobal.getWindowManagerService()
            hasNavigationBar = windowManager.hasNavigationBar(Display.DEFAULT_DISPLAY)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting navigation bar status")
        }
        return hasNavigationBar
    }

    private fun isKeyDisablerSupported(context: Context): Boolean {
        val hardware = LineageHardwareManager.getInstance(context)
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE)
    }

    private fun isKeySwapperSupported(context: Context): Boolean {
        val hardware = LineageHardwareManager.getInstance(context)
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference) {
            mSwapVolumeButtons -> {
                val value: Int = if (mSwapVolumeButtons.isChecked) {
                    /* The native inputflinger service uses the same logic of:
                     * 1 - the volume rocker is on one the sides, relative to the natural
                     * orientation of the display (true for all phones and most tablets)
                     * 2 - the volume rocker is on the top or bottom, relative to the
                     * natural orientation of the display (true for some tablets)
                     */
                    resources.getInteger(R.integer.config_volumeRockerVsDisplayOrientation)
                } else {
                    /* Disable the re-orient functionality */
                    0
                }
                LineageSettings.System.putInt(
                    requireActivity().contentResolver,
                    LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION,
                    value
                )
            }
            mVolumePanelOnLeft -> {
                LineageSettings.Secure.putIntForUser(
                    requireActivity().contentResolver,
                    LineageSettings.Secure.VOLUME_PANEL_ON_LEFT,
                    if (mVolumePanelOnLeft.isChecked) 1 else 0,
                    UserHandle.USER_CURRENT
                )
                return true
            }
            mDisableNavigationKeys -> {
                mDisableNavigationKeys.isEnabled = false
                mNavigationPreferencesCat.isEnabled = false
                if (!mDisableNavigationKeys.isChecked) {
                    setButtonNavigationMode(NAV_BAR_MODE_3BUTTON_OVERLAY)
                }
                writeDisableNavkeysOption(mDisableNavigationKeys.isChecked)
                updateDisableNavkeysOption()
                updateDisableNavkeysCategories(true, false)
                mHandler.postDelayed(
                    {
                        mDisableNavigationKeys.isEnabled = true
                        mNavigationPreferencesCat.isEnabled = mDisableNavigationKeys.isChecked
                        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked, false)
                    },
                    1000
                )
            }
            mPowerEndCall -> {
                handleTogglePowerButtonEndsCallPreferenceClick()
                return true
            }
            mHomeAnswerCall -> {
                handleToggleHomeButtonAnswersCallPreferenceClick()
                return true
            }
            else -> return super.onPreferenceTreeClick(preference)
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(
            contentResolver,
            Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
            if (mPowerEndCall.isChecked)
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
            else
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF
        )
    }

    private fun handleToggleHomeButtonAnswersCallPreferenceClick() {
        LineageSettings.Secure.putInt(
            contentResolver,
            LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
            if (mHomeAnswerCall.isChecked)
                LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
            else
                LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING
        )
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.LUMINEDROID
    }

    companion object {
        private const val TAG = "SystemSettings"

        private const val KEY_BUTTON_BACKLIGHT = "button_backlight"
        private const val KEY_BACK_WAKE_SCREEN = "back_wake_screen"
        private const val KEY_CAMERA_LAUNCH = "camera_launch"
        private const val KEY_CAMERA_SLEEP_ON_RELEASE = "camera_sleep_on_release"
        private const val KEY_CAMERA_WAKE_SCREEN = "camera_wake_screen"
        private const val KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press"
        private const val KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press"
        private const val KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap"
        private const val KEY_HOME_WAKE_SCREEN = "home_wake_screen"
        private const val KEY_MENU_PRESS = "hardware_keys_menu_press"
        private const val KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press"
        private const val KEY_MENU_WAKE_SCREEN = "menu_wake_screen"
        private const val KEY_ASSIST_PRESS = "hardware_keys_assist_press"
        private const val KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press"
        private const val KEY_ASSIST_WAKE_SCREEN = "assist_wake_screen"
        private const val KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press"
        private const val KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press"
        private const val KEY_APP_SWITCH_WAKE_SCREEN = "app_switch_wake_screen"
        private const val KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control"
        private const val KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons"
        private const val KEY_VOLUME_PANEL_ON_LEFT = "volume_panel_on_left"
        private const val KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen"
        private const val KEY_VOLUME_ANSWER_CALL = "volume_answer_call"
        private const val KEY_DISABLE_NAV_KEYS = "disable_nav_keys"
        private const val KEY_NAVIGATION_BACK_LONG_PRESS = "navigation_back_long_press"
        private const val KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press"
        private const val KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap"
        private const val KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press"
        private const val KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe"
        private const val KEY_POWER_END_CALL = "power_end_call"
        private const val KEY_HOME_ANSWER_CALL = "home_answer_call"
        private const val KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls"
        private const val KEY_TORCH_LONG_PRESS_POWER_GESTURE = "torch_long_press_power_gesture"
        private const val KEY_TORCH_LONG_PRESS_POWER_TIMEOUT = "torch_long_press_power_timeout"
        private const val KEY_CLICK_PARTIAL_SCREENSHOT = "click_partial_screenshot"
        private const val KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys"
        private const val KEY_NAV_BAR_INVERSE = "sysui_nav_bar_inverse"
        private const val KEY_ENABLE_TASKBAR = "enable_taskbar"

        private const val CATEGORY_POWER = "power_key"
        private const val CATEGORY_HOME = "home_key"
        private const val CATEGORY_BACK = "back_key"
        private const val CATEGORY_MENU = "menu_key"
        private const val CATEGORY_ASSIST = "assist_key"
        private const val CATEGORY_APPSWITCH = "app_switch_key"
        private const val CATEGORY_CAMERA = "camera_key"
        private const val CATEGORY_VOLUME = "volume_keys"
        private const val CATEGORY_NAVBAR = "navigation_bar_category"
        private const val CATEGORY_EXTRAS = "extras_category"

        @JvmStatic
        fun restoreKeyDisabler(context: Context) {
            if (!isKeyDisablerSupported(context)) {
                return
            }

            val enabled =
                LineageSettings.System.getIntForUser(
                    context.contentResolver,
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    0,
                    UserHandle.USER_CURRENT
                ) != 0

            val hardware = LineageHardwareManager.getInstance(context)
            hardware.set(LineageHardwareManager.FEATURE_KEY_DISABLE, enabled)
        }

        @JvmStatic
        fun restoreKeySwapper(context: Context) {
            if (!isKeySwapperSupported(context)) {
                return
            }

            val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val hardware = LineageHardwareManager.getInstance(context)
            hardware.set(
                LineageHardwareManager.FEATURE_KEY_SWAP,
                preferences.getBoolean(KEY_SWAP_CAPACITIVE_KEYS, false)
            )
        }

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.extensions_button) {

                override fun getNonIndexableKeys(context: Context): List<String> {
                    val result: MutableSet<String> = ArraySet()
                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        result.add(KEY_POWER_END_CALL)
                        result.add(KEY_HOME_ANSWER_CALL)
                        result.add(KEY_VOLUME_ANSWER_CALL)
                    }

                    if (!DeviceUtils.hasBackKey(context)) {
                        result.add(CATEGORY_BACK)
                        result.add(KEY_BACK_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                        result.add(KEY_BACK_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasHomeKey(context)) {
                        result.add(CATEGORY_HOME)
                        result.add(KEY_HOME_LONG_PRESS)
                        result.add(KEY_HOME_DOUBLE_TAP)
                        result.add(KEY_HOME_ANSWER_CALL)
                        result.add(KEY_HOME_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                        result.add(KEY_HOME_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasMenuKey(context)) {
                        result.add(CATEGORY_MENU)
                        result.add(KEY_MENU_PRESS)
                        result.add(KEY_MENU_LONG_PRESS)
                        result.add(KEY_MENU_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingMenuKey(context)) {
                        result.add(KEY_MENU_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasAssistKey(context)) {
                        result.add(CATEGORY_ASSIST)
                        result.add(KEY_ASSIST_PRESS)
                        result.add(KEY_ASSIST_LONG_PRESS)
                        result.add(KEY_ASSIST_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingAssistKey(context)) {
                        result.add(KEY_ASSIST_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasAppSwitchKey(context)) {
                        result.add(CATEGORY_APPSWITCH)
                        result.add(KEY_APP_SWITCH_PRESS)
                        result.add(KEY_APP_SWITCH_LONG_PRESS)
                        result.add(KEY_APP_SWITCH_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingAppSwitchKey(context)) {
                        result.add(KEY_APP_SWITCH_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasCameraKey(context)) {
                        result.add(CATEGORY_CAMERA)
                        result.add(KEY_CAMERA_LAUNCH)
                        result.add(KEY_CAMERA_SLEEP_ON_RELEASE)
                        result.add(KEY_CAMERA_WAKE_SCREEN)
                    } else if (!DeviceUtils.canWakeUsingCameraKey(context)) {
                        result.add(KEY_CAMERA_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.hasVolumeKeys(context)) {
                        result.add(CATEGORY_VOLUME)
                        result.add(KEY_SWAP_VOLUME_BUTTONS)
                        result.add(KEY_VOLUME_ANSWER_CALL)
                        result.add(KEY_VOLUME_KEY_CURSOR_CONTROL)
                        result.add(KEY_VOLUME_MUSIC_CONTROLS)
                        result.add(KEY_VOLUME_PANEL_ON_LEFT)
                        result.add(KEY_VOLUME_WAKE_SCREEN)
                        result.add(KEY_CLICK_PARTIAL_SCREENSHOT)
                    } else if (!DeviceUtils.canWakeUsingVolumeKeys(context)) {
                        result.add(KEY_VOLUME_WAKE_SCREEN)
                    }

                    if (!DeviceUtils.deviceSupportsFlashLight(context)) {
                        result.add(KEY_TORCH_LONG_PRESS_POWER_GESTURE)
                        result.add(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT)
                    }

                    if (!isKeyDisablerSupported(context)) {
                        result.add(KEY_DISABLE_NAV_KEYS)
                    }

                    if (!isKeySwapperSupported(context)) {
                        result.add(KEY_SWAP_CAPACITIVE_KEYS)
                    }

                    if (!DeviceUtils.hasButtonBacklightSupport(context) &&
                        !DeviceUtils.hasKeyboardBacklightSupport(context)) {
                        result.add(KEY_BUTTON_BACKLIGHT)
                    }

                    if (!isLargeScreen(context) || !hasNavigationBar()) {
                        result.add(KEY_ENABLE_TASKBAR)
                    }

                    if (hasNavigationBar()) {
                        if (DeviceUtils.isEdgeToEdgeEnabled(context)) {
                            result.add(KEY_NAVIGATION_HOME_LONG_PRESS)
                            result.add(KEY_NAVIGATION_HOME_DOUBLE_TAP)
                            result.add(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS)
                        } else {
                            result.add(KEY_EDGE_LONG_SWIPE)
                        }
                    }
                    return ArrayList(result)
                }

                private fun isKeyDisablerSupported(context: Context): Boolean {
                    val hardware = LineageHardwareManager.getInstance(context)
                    return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE)
                }

                private fun isKeySwapperSupported(context: Context): Boolean {
                    val hardware = LineageHardwareManager.getInstance(context)
                    return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP)
                }

                private fun hasNavigationBar(): Boolean {
                    var hasNavigationBar = false
                    try {
                        val windowManager = WindowManagerGlobal.getWindowManagerService()
                        hasNavigationBar = windowManager.hasNavigationBar(Display.DEFAULT_DISPLAY)
                    } catch (e: RemoteException) {
                        Log.e(TAG, "Error getting navigation bar status")
                    }
                    return hasNavigationBar
                }
            }
    }
}

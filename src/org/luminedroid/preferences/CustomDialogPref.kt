/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.preferences

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat

class CustomDialogPref<T : DialogInterface> : DialogPreference {

    private var mFragment: CustomPreferenceDialogFragment? = null

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun isDialogOpen(): Boolean {
        val dialog = getDialog()
        return dialog != null && dialog is Dialog && dialog.isShowing
    }

    @Suppress("UNCHECKED_CAST")
    fun getDialog(): T? {
        return (mFragment?.dialog) as T?
    }

    protected fun onPrepareDialogBuilder(
        builder: AlertDialog.Builder,
        listener: DialogInterface.OnClickListener
    ) {
    }

    protected fun onDialogClosed(positiveResult: Boolean) {}

    protected fun onClick(dialog: T?, which: Int) {}

    protected fun onBindDialogView(view: View) {}

    protected fun onStart() {}

    protected fun onStop() {}

    protected fun onPause() {}

    protected fun onResume() {}

    fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        return null
    }

    protected fun onCreateDialogView(context: Context): View? {
        return null
    }

    private fun setFragment(fragment: CustomPreferenceDialogFragment) {
        mFragment = fragment
    }

    protected fun onDismissDialog(dialog: T?, which: Int): Boolean {
        return true
    }

    class CustomPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

        companion object {
            @JvmStatic
            fun newInstance(key: String): CustomPreferenceDialogFragment {
                val fragment = CustomPreferenceDialogFragment()
                val b = Bundle(1)
                b.putString(ARG_KEY, key)
                fragment.arguments = b
                return fragment
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun getCustomizablePreference(): CustomDialogPref<DialogInterface> {
            return preference as CustomDialogPref<DialogInterface>
        }

        private inner class OnDismissListener(
            private val mDialog: DialogInterface,
            private val mWhich: Int
        ) : View.OnClickListener {

            override fun onClick(view: View) {
                this@CustomPreferenceDialogFragment.onClick(mDialog, mWhich)
                if (getCustomizablePreference().onDismissDialog(mDialog, mWhich)) {
                    mDialog.dismiss()
                }
            }
        }

        override fun onStart() {
            super.onStart()
            if (dialog is AlertDialog) {
                val a = dialog as AlertDialog

                a.getButton(Dialog.BUTTON_NEUTRAL)?.setOnClickListener(
                    OnDismissListener(a, Dialog.BUTTON_NEUTRAL)
                )
                a.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener(
                    OnDismissListener(a, Dialog.BUTTON_POSITIVE)
                )
                a.getButton(Dialog.BUTTON_NEGATIVE)?.setOnClickListener(
                    OnDismissListener(a, Dialog.BUTTON_NEGATIVE)
                )
            }
            getCustomizablePreference().onStart()
        }

        override fun onStop() {
            super.onStop()
            getCustomizablePreference().onStop()
        }

        override fun onPause() {
            super.onPause()
            getCustomizablePreference().onPause()
        }

        override fun onResume() {
            super.onResume()
            getCustomizablePreference().onResume()
        }

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
            super.onPrepareDialogBuilder(builder)
            getCustomizablePreference().setFragment(this)
            getCustomizablePreference().onPrepareDialogBuilder(builder, this)
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            getCustomizablePreference().onDialogClosed(positiveResult)
        }

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            getCustomizablePreference().onBindDialogView(view)
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            super.onClick(dialog, which)
            getCustomizablePreference().onClick(dialog, which)
        }

        @NonNull
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            getCustomizablePreference().setFragment(this)
            val sub = getCustomizablePreference().onCreateDialog(savedInstanceState)
            return sub ?: super.onCreateDialog(savedInstanceState)
        }

        override fun onCreateDialogView(context: Context): View? {
            val v = getCustomizablePreference().onCreateDialogView(context)
            return v ?: super.onCreateDialogView(context)
        }
    }
}


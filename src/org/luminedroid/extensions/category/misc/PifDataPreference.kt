/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.misc

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class PifDataPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    private var mFilePickerLauncher: ActivityResultLauncher<Intent>? = null

    init {
        setLayoutResource(R.layout.pref_with_delete)
    }

    fun setFilePickerLauncher(launcher: ActivityResultLauncher<Intent>) {
        this.mFilePickerLauncher = launcher
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val title = holder.findViewById(R.id.title) as TextView
        val summary = holder.findViewById(R.id.summary) as TextView
        val deleteButton = holder.findViewById(R.id.delete_button) as ImageButton

        title.text = this.title
        summary.text = this.summary

        holder.itemView.setOnClickListener {
            if (mFilePickerLauncher != null) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "application/json"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                mFilePickerLauncher?.launch(intent)
            }
        }

        deleteButton.setOnClickListener {
            Settings.Secure.putString(
                context.contentResolver, Settings.Secure.PIF_DATA, null
            )
            Toast.makeText(context, "User PIF data cleared", Toast.LENGTH_SHORT).show()
            callChangeListener(null)

            killPackages()
        }
    }

    fun handleFileSelected(uri: Uri?) {
        if (uri == null || (!uri.toString().endsWith(".json") &&
            "application/json" != context.contentResolver.getType(uri))) {
            Toast.makeText(context, "Invalid file selected", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Kotlin's 'use' replaces Java's try-with-resources
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->

                    val jsonContent = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonContent.append(line).append('\n')
                    }

                    val json = jsonContent.toString()

                    Settings.Secure.putString(context.contentResolver, Settings.Secure.PIF_DATA, json)
                    Toast.makeText(context, "JSON file loaded", Toast.LENGTH_SHORT).show()
                    callChangeListener(json)

                    killPackages()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read JSON file", e)
            Toast.makeText(context, "Failed to read JSON", Toast.LENGTH_SHORT).show()
        }
    }

    private fun killPackages() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val packages = arrayOf("com.google.android.gms", "com.android.vending")
            if (am != null) {
                for (pkg in packages) {
                    am.javaClass.getMethod("forceStopPackage", String::class.java).invoke(am, pkg)
                    Log.i(TAG, "$pkg process killed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill packages", e)
        }
    }

    companion object {
        private const val TAG = "PifDataPref"
    }
}


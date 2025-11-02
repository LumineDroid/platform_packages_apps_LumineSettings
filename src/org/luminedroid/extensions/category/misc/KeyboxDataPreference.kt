/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.misc

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
import java.io.StringReader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class KeyboxDataPreference(context: Context, attrs: AttributeSet) :
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
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "text/xml"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                mFilePickerLauncher?.launch(intent)
            }
        }

        deleteButton.setOnClickListener {
            Settings.Secure.putString(
                context.contentResolver, Settings.Secure.KEYBOX_DATA, null
            )
            Toast.makeText(context, "XML data cleared", Toast.LENGTH_SHORT).show()
            callChangeListener(null)
        }
    }

    fun handleFileSelected(uri: Uri?) {
        if (uri == null || (!uri.toString().endsWith(".xml") &&
            "text/xml" != context.contentResolver.getType(uri))) {
            Toast.makeText(context, "Invalid file selected", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Kotlin's 'use' function is the equivalent of Java's try-with-resources
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->

                    val xmlContent = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        xmlContent.append(line).append('\n')
                    }

                    val xml = xmlContent.toString()
                    if (!validateXml(xml)) {
                        Toast.makeText(context, "Invalid XML: missing required data", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    Settings.Secure.putString(
                        context.contentResolver, Settings.Secure.KEYBOX_DATA, xml
                    )
                    Toast.makeText(context, "XML file loaded", Toast.LENGTH_SHORT).show()
                    callChangeListener(xml)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read XML file", e)
            Toast.makeText(context, "Failed to read XML", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateXml(xml: String): Boolean {
        var hasEcdsaKey = false
        var hasRsaKey = false
        var hasEcdsaPrivKey = false
        var hasRsaPrivKey = false
        var ecdsaCertCount = 0
        var rsaCertCount = 0
        var numberOfKeyboxes = -1

        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))

            var currentAlg: String? = null

            var eventType = parser.next()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    val name = parser.name
                    when (name) {
                        "NumberOfKeyboxes" -> {
                            parser.next() // move to TEXT event
                            if (parser.eventType == XmlPullParser.TEXT) {
                                numberOfKeyboxes = try {
                                    parser.text.trim().toInt()
                                } catch (e: NumberFormatException) {
                                    -1
                                }
                            }
                        }

                        "Key" -> {
                            currentAlg = parser.getAttributeValue(null, "algorithm")
                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                hasEcdsaKey = true
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                hasRsaKey = true
                            } else {
                                currentAlg = null // unsupported key
                            }
                        }

                        "PrivateKey" -> {
                            val format = parser.getAttributeValue(null, "format")
                            if (!"pem".equals(format, ignoreCase = true)) {
                                Log.w(TAG, "Invalid or missing format for PrivateKey")
                                return false
                            }
                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                hasEcdsaPrivKey = true
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                hasRsaPrivKey = true
                            }
                        }

                        "Certificate" -> {
                            val format = parser.getAttributeValue(null, "format")
                            if (!"pem".equals(format, ignoreCase = true)) {
                                Log.w(TAG, "Invalid or missing format for Certificate")
                                return false
                            }

                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                ecdsaCertCount++
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                rsaCertCount++
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && "Key" == parser.name) {
                    currentAlg = null
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML validation failed", e)
            return false
        }

        return numberOfKeyboxes == 1 &&
            hasEcdsaKey && hasEcdsaPrivKey && ecdsaCertCount >= 1 &&
            hasRsaKey && hasRsaPrivKey && rsaCertCount >= 1
    }

    companion object {
        private const val TAG = "KeyboxDataPref"
    }
}


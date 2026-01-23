//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.about

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import org.json.JSONObject

class AboutLumineDroidFragment : SettingsPreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about_luminedroid)
        loadTeamData()
    }

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.LUMINEDROID

    private fun loadTeamData() {
        val context = requireContext()
        val json =
            context.resources.openRawResource(R.raw.luminedroid).bufferedReader().use {
                it.readText()
            }

        val jsonObj = JSONObject(json)
        val devCat = findPreference<PreferenceCategory>("luminedroid_dev_category")
        val contribCat = findPreference<PreferenceCategory>("luminedroid_contrib_category")

        jsonObj.getJSONArray("developers").let { devs ->
            for (i in 0 until devs.length()) {
                val d = devs.getJSONObject(i)
                devCat?.addPreference(
                    createPersonPref(
                        context,
                        d.getString("name"),
                        d.getString("role"),
                        d.getString("username"),
                        d.getString("link"),
                    )
                )
            }
        }

        jsonObj.getJSONArray("contributors").let { cons ->
            for (i in 0 until cons.length()) {
                val c = cons.getJSONObject(i)
                contribCat?.addPreference(
                    createPersonPref(
                        context,
                        c.getString("name"),
                        c.getString("role"),
                        c.getString("username"),
                        c.getString("link"),
                    )
                )
            }
        }
    }

    private fun createPersonPref(
        context: Context,
        name: String,
        role: String,
        username: String,
        link: String,
    ): Preference {
        val pref = Preference(context)
        pref.title = name
        pref.summary = role
        pref.icon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_person, null)
        pref.intent =
            android.content.Intent(android.content.Intent.ACTION_VIEW).setData(Uri.parse(link))

        lifecycleScope.launch(Dispatchers.IO) {
            val avatar = fetchGithubAvatar(username)
            withContext(Dispatchers.Main) { if (avatar != null) pref.icon = avatar }
        }

        return pref
    }

    private fun fetchGithubAvatar(username: String): Drawable? =
        try {
            val url = URL("https://github.com/$username.png?size=64")
            (url.openConnection() as HttpURLConnection).run {
                connectTimeout = 1200
                readTimeout = 1200
                doInput = true
                connect()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                BitmapDrawable(resources, bitmap.toRoundedBitmap())
            }
        } catch (_: Exception) {
            null
        }

    private fun Bitmap.toRoundedBitmap(): Bitmap {
        val size = minOf(width, height)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, null, rect, paint)
        return out
    }
}

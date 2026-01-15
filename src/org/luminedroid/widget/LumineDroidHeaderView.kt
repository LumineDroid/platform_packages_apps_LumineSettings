/*
 * Copyright (C) 2025 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemProperties
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.android.settings.R

class LumineDroidHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val BUILD_TYPE_PROP = "org.luminedroid.build.type"
        private const val VERSION_PROP = "org.luminedroid.build.version"
        private const val DEVICE_PROP = "org.luminedroid.device"

        private const val MAINTAINER_PROP = "org.luminedroid.maintainer"
        private const val MAINTAINER_LINK_PROP = "org.luminedroid.maintainer.link"

        private const val BUILD_OFFICIAL = "OFFICIAL"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { bindViews() }
    }

    private fun bindViews() {
        val root = rootView ?: return

        root.findViewById<TextView>(R.id.luminedroid_version)?.text =
            getLumineVersion()

        val maintainerIcon = root.findViewById<ImageView>(R.id.maintainer_icon)
        val maintainerTitle = root.findViewById<TextView>(R.id.maintainer_title)
        val maintainerSummary = root.findViewById<TextView>(R.id.maintainer_summary)
        val maintainerCard = root.findViewById<View>(R.id.maintainer_card)

        val isOfficial = SystemProperties
            .get(BUILD_TYPE_PROP, "")
            .equals(BUILD_OFFICIAL, true)

        if (isOfficial) {
            maintainerIcon?.setImageResource(R.drawable.ic_maintainer_verified)
            maintainerTitle?.setText(R.string.maintainer_official)
        } else {
            maintainerIcon?.setImageResource(R.drawable.ic_maintainer_unverified)
            maintainerTitle?.setText(R.string.maintainer_unofficial)
        }

        val maintainer = SystemProperties.get(MAINTAINER_PROP, "")
        maintainerSummary?.text =
            if (maintainer.isNotBlank()) {
                context.getString(R.string.maintainer_by, maintainer)
            } else {
                context.getString(R.string.maintainer_unknown)
            }

        val link = SystemProperties.get(MAINTAINER_LINK_PROP, "")
        if (link.isNotBlank() && maintainerCard != null) {
            maintainerCard.isClickable = true
            maintainerCard.isFocusable = true
            maintainerCard.setOnClickListener {
                openLink(link)
            }
        }
    }

    private fun getLumineVersion(): String {
        val version = SystemProperties.get(VERSION_PROP, "Unknown")
        val device = SystemProperties.get(DEVICE_PROP, "Unknown")
        val type = SystemProperties.get(BUILD_TYPE_PROP, "Unknown")
        return "$version | $device | $type"
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

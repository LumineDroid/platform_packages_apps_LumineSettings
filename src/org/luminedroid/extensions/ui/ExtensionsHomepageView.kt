/*
 * Copyright (C) 2026 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AbstractComposeView

class ExtensionsHomepageView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AbstractComposeView(context, attrs, defStyle) {

    var wallpaperBitmap: Bitmap? by mutableStateOf(null)
    var onTileClick: ((String) -> Unit)? = null

    @Composable
    override fun Content() {
        val colorScheme =
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                if (isInDarkMode()) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                MaterialTheme.colorScheme
            }

        MaterialTheme(colorScheme = colorScheme) {
            ExtensionsUI(
                wallpaperBitmap = wallpaperBitmap,
                onTileClick = { id -> onTileClick?.invoke(id) },
            )
        }
    }

    private fun isInDarkMode(): Boolean {
        val uiMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}

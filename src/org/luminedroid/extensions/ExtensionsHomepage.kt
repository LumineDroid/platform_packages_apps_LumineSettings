/*
 * Copyright (C) 2026 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R

private data class ExtensionItem(
    val id: String,
    val titleRes: Int,
    val summaryRes: Int,
    val icon: ImageVector,
)

private val extensionItems =
    listOf(
        ExtensionItem(
            "statusbar",
            R.string.extensions_statusbar_title,
            R.string.extensions_statusbar_summary,
            Icons.Rounded.Wifi,
        ),
        ExtensionItem(
            "quicksettings",
            R.string.extensions_quicksettings_title,
            R.string.extensions_quicksettings_summary,
            Icons.Rounded.GridView,
        ),
        ExtensionItem(
            "button",
            R.string.extensions_button_title,
            R.string.extensions_button_summary,
            Icons.Rounded.TouchApp,
        ),
        ExtensionItem(
            "lockscreen",
            R.string.extensions_lockscreen_title,
            R.string.extensions_lockscreen_summary,
            Icons.Rounded.Lock,
        ),
        ExtensionItem(
            "about",
            R.string.extensions_about_title,
            R.string.extensions_about_summary,
            Icons.Rounded.Info,
        ),
        ExtensionItem(
            "misc",
            R.string.extensions_misc_title,
            R.string.extensions_misc_summary,
            Icons.Rounded.Tune,
        ),
    )

@Composable
fun LumineExtensionsTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val colorScheme =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (dark) darkColorScheme() else lightColorScheme()
        }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
private fun rememberWallpaperBitmap(): Bitmap? {
    val context = LocalContext.current
    return remember {
        runCatching {
                val drawable: Drawable =
                    WallpaperManager.getInstance(context).drawable ?: return@remember null
                val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1080
                val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1920
                val scale = 800f / maxOf(w, h).toFloat()
                val bw = (w * scale).toInt().coerceAtLeast(1)
                val bh = (h * scale).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888).also { bmp ->
                    drawable.setBounds(0, 0, bw, bh)
                    drawable.draw(Canvas(bmp))
                }
            }
            .getOrNull()
    }
}

@Composable
fun ExtensionsHomepage(onItemClick: (String) -> Unit) {
    val wallpaperBmp = rememberWallpaperBitmap()

    Column(
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 6.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderCard(
            wallpaperBmp = wallpaperBmp,
            modifier = Modifier.fillMaxWidth().height(140.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            extensionItems.take(2).forEach { item ->
                VerticalExtensionCard(
                    item = item,
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = { onItemClick(item.id) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                extensionItems.drop(2).take(2).forEach { item ->
                    VerticalExtensionCard(
                        item = item,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        onClick = { onItemClick(item.id) },
                    )
                }
            }

            val about = extensionItems[4]
            AboutCard(
                item = about,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onItemClick(about.id) },
            )
        }

        val misc = extensionItems[5]
        MiscCard(
            item = misc,
            onClick = { onItemClick(misc.id) },
        )
    }
}

@Composable
private fun HeaderCard(wallpaperBmp: Bitmap?, modifier: Modifier) {
    val headerBg = colorResource(R.color.extensions_header_bg)
    val headerText = colorResource(R.color.extensions_text_color)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = headerBg),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (wallpaperBmp != null) {
                Image(
                    bitmap = wallpaperBmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().blur(radiusX = 12.dp, radiusY = 12.dp),
                )
            }

            Box(modifier = Modifier.matchParentSize().background(headerBg.copy(alpha = 0.45f)))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.extensions_title),
                    color = headerText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.extensions_homepage_summary),
                    color = headerText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    modifier = Modifier.alpha(0.90f),
                )
            }
        }
    }
}

@Composable
private fun VerticalExtensionCard(
    item: ExtensionItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                IconPill(icon = item.icon, size = 36.dp)
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp).padding(top = 2.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(item.titleRes),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(item.summaryRes),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    modifier = Modifier.alpha(0.70f),
                )
            }
        }
    }
}

@Composable
private fun AboutCard(
    item: ExtensionItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconPill(icon = item.icon, size = 56.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(item.titleRes),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(item.summaryRes),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 4,
                modifier = Modifier.alpha(0.70f),
            )
        }
    }
}

@Composable
private fun MiscCard(
    item: ExtensionItem,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                    text = stringResource(item.titleRes),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(item.summaryRes),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    modifier = Modifier.alpha(0.70f),
                )
            }
            IconPill(icon = item.icon, size = 38.dp)
        }
    }
}

@Composable
private fun IconPill(icon: ImageVector, size: Dp) {
    Box(
        modifier =
            Modifier.size(size)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                )
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/*
 * Copyright (C) 2026 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R

data class ExtensionTile(val id: String, val iconRes: Int, val titleRes: Int, val summaryRes: Int)

val extensionTiles =
    listOf(
        ExtensionTile(
            "statusbar",
            R.drawable.ic_statusbar,
            R.string.extensions_statusbar_title,
            R.string.extensions_statusbar_summary,
        ),
        ExtensionTile(
            "quicksettings",
            R.drawable.ic_quicksettings,
            R.string.extensions_quicksettings_title,
            R.string.extensions_quicksettings_summary,
        ),
        ExtensionTile(
            "button",
            R.drawable.ic_button,
            R.string.extensions_button_title,
            R.string.extensions_button_summary,
        ),
        ExtensionTile(
            "lockscreen",
            R.drawable.ic_lockscreen,
            R.string.extensions_lockscreen_title,
            R.string.extensions_lockscreen_summary,
        ),
        ExtensionTile(
            "misc",
            R.drawable.ic_misc,
            R.string.extensions_misc_title,
            R.string.extensions_misc_summary,
        ),
        ExtensionTile(
            "about",
            R.drawable.ic_about,
            R.string.extensions_about_title,
            R.string.extensions_about_summary,
        ),
    )

@Composable
fun ExtensionsUI(wallpaperBitmap: android.graphics.Bitmap? = null, onTileClick: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HeroCard(wallpaperBitmap = wallpaperBitmap)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            extensionTiles
                .filter { it.id == "statusbar" || it.id == "quicksettings" }
                .forEach { tile ->
                    SmallTileCard(
                        tile = tile,
                        modifier = Modifier.weight(1f),
                        onTileClick = onTileClick,
                    )
                }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                extensionTiles
                    .filter { it.id == "button" || it.id == "lockscreen" }
                    .forEach { tile ->
                        SmallTileCard(
                            tile = tile,
                            modifier = Modifier.fillMaxWidth(),
                            onTileClick = onTileClick,
                        )
                    }
            }

            extensionTiles
                .find { it.id == "about" }
                ?.let { tile ->
                    AboutTallCard(
                        tile = tile,
                        modifier = Modifier.weight(1f),
                        onTileClick = onTileClick,
                    )
                }
        }

        extensionTiles
            .find { it.id == "misc" }
            ?.let { tile -> MiscBannerCard(tile = tile, onTileClick = onTileClick) }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun HeroCard(wallpaperBitmap: android.graphics.Bitmap?) {
    val shape = RoundedCornerShape(28.dp)
    val accentColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = Modifier.fillMaxWidth().clip(shape)) {
        if (wallpaperBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = androidx.compose.ui.graphics.asImageBitmap(wallpaperBitmap),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Box(modifier = Modifier.matchParentSize().background(accentColor))
        }

        Box(modifier = Modifier.matchParentSize().background(accentColor.copy(alpha = 0.45f)))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.extensions_title),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.extensions_homepage_summary),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun SmallTileCard(
    tile: ExtensionTile,
    modifier: Modifier = Modifier,
    onTileClick: (String) -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Card(
        modifier = modifier.clip(shape).clickable { onTileClick(tile.id) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            TileIcon(tile.iconRes, size = 40.dp)
            Spacer(Modifier.height(10.dp))
            TileTitle(tile.titleRes, fontSize = 15.sp)
            Spacer(Modifier.height(3.dp))
            TileSummary(tile.summaryRes, maxLines = 3)
        }
    }
}

@Composable
private fun AboutTallCard(
    tile: ExtensionTile,
    modifier: Modifier = Modifier,
    onTileClick: (String) -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Card(
        modifier = modifier.clip(shape).clickable { onTileClick(tile.id) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TileIcon(tile.iconRes, size = 72.dp)
            Spacer(Modifier.height(12.dp))
            TileTitle(tile.titleRes, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            TileSummary(tile.summaryRes, maxLines = 5, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MiscBannerCard(tile: ExtensionTile, onTileClick: (String) -> Unit) {
    val shape = RoundedCornerShape(24.dp)

    Card(
        modifier = Modifier.fillMaxWidth().clip(shape).clickable { onTileClick(tile.id) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TileTitle(tile.titleRes, fontSize = 17.sp)
                Spacer(Modifier.height(3.dp))
                TileSummary(tile.summaryRes, maxLines = 2)
            }
            Spacer(Modifier.width(12.dp))
            TileIcon(tile.iconRes, size = 44.dp)
        }
    }
}

@Composable
private fun TileIcon(iconRes: Int, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier =
            Modifier.size(size)
                .clip(RoundedCornerShape(size / 4))
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun TileTitle(
    titleRes: Int,
    fontSize: androidx.compose.ui.unit.TextUnit,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = stringResource(titleRes),
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TileSummary(summaryRes: Int, maxLines: Int, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = stringResource(summaryRes),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().alpha(0.65f),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
    )
}

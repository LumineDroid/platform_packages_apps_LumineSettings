/*
 * SPDX-FileCopyrightText: 2026 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.about

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.luminedroid.extensions.LumineExtensionsTheme

private data class PersonUi(
    val name: String,
    val role: String,
    val username: String,
    val githubUrl: String,
    val telegramUrl: String?,
    val badgeLabel: String,
    val badgeStyle: BadgeStyle,
)

@Composable
fun AboutLumineDroidScreen(
    developers: List<TeamItem.Person>,
    contributors: List<TeamItem.Person>,
) {
    LumineExtensionsTheme {
        AboutContent(developers = developers, contributors = contributors)
    }
}

@Composable
private fun AboutContent(
    developers: List<TeamItem.Person>,
    contributors: List<TeamItem.Person>,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(Tab.DEVS) }

    val devItems =
        remember(developers) {
            developers.map {
                it.toUi(BadgeStyle.PINK, context.getString(R.string.luminedroid_badge_core_dev))
            }
        }
    val contribItems =
        remember(contributors) {
            contributors.map {
                it.toUi(BadgeStyle.BLUE, context.getString(R.string.luminedroid_badge_contributor))
            }
        }
    val currentList = if (selectedTab == Tab.DEVS) devItems else contribItems

    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp)
                .padding(top = 6.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeroCard(
            onSocialClick = { url ->
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )

        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.luminedroid_meet_the_team),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = stringResource(R.string.luminedroid_page_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        TeamTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        SectionHeader(
            label =
                stringResource(
                    if (selectedTab == Tab.DEVS) R.string.luminedroid_dev_section_title
                    else R.string.luminedroid_contrib_section_title
                ),
            count = currentList.size,
        )

        TeamGrid(people = currentList, context = context)
    }
}

@Composable
private fun HeroCard(onSocialClick: (String) -> Unit) {
    val urlGithub = stringResource(R.string.luminedroid_url_github)
    val urlTelegram = stringResource(R.string.luminedroid_url_telegram)
    val urlWebsite = stringResource(R.string.luminedroid_url_website)

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val containerColor = MaterialTheme.colorScheme.secondaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.08f),
                                containerColor.copy(alpha = 0f),
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Icon with enhanced styling
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    primaryColor,
                                    secondaryColor,
                                )
                            ),
                            shape = RoundedCornerShape(50),
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_luminedroid_logo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.luminedroid_project_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp,
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.luminedroid_project_tagline),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.75f),
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.luminedroid_project_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.alpha(0.65f).padding(horizontal = 8.dp),
                )

                Spacer(Modifier.height(24.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
                    thickness = 1.dp,
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SocialButton(
                        labelRes = R.string.luminedroid_social_github,
                        icon = painterResource(R.drawable.ic_github),
                        modifier = Modifier.weight(1f),
                        onClick = { onSocialClick(urlGithub) },
                    )
                    SocialButton(
                        labelRes = R.string.luminedroid_social_telegram,
                        icon = rememberVectorPainter(Icons.Rounded.Send),
                        modifier = Modifier.weight(1f),
                        onClick = { onSocialClick(urlTelegram) },
                    )
                    SocialButton(
                        labelRes = R.string.luminedroid_social_website,
                        icon = rememberVectorPainter(Icons.Rounded.Language),
                        modifier = Modifier.weight(1f),
                        onClick = { onSocialClick(urlWebsite) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialButton(
    labelRes: Int,
    icon: androidx.compose.ui.graphics.painter.Painter,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TeamTabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TeamTabChip(
            label = stringResource(R.string.luminedroid_tab_devs),
            icon = painterResource(R.drawable.ic_github),
            selected = selectedTab == Tab.DEVS,
            modifier = Modifier.padding(end = 8.dp),
            onClick = { onTabSelected(Tab.DEVS) },
        )
        TeamTabChip(
            label = stringResource(R.string.luminedroid_tab_contribs),
            icon = rememberVectorPainter(Icons.Rounded.Group),
            selected = selectedTab == Tab.CONTRIBS,
            onClick = { onTabSelected(Tab.CONTRIBS) },
        )
    }
}

@Composable
private fun TeamTabChip(
    label: String,
    icon: androidx.compose.ui.graphics.painter.Painter,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val alpha by animateFloatAsState(if (selected) 1f else 0.75f, label = "tabAlpha")

    val containerColor =
        if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    val contentColor =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier =
            modifier
                .height(44.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .alpha(alpha),
        shape = RoundedCornerShape(50),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun TeamGrid(people: List<PersonUi>, context: android.content.Context) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        people.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { person ->
                    PersonCard(
                        person = person,
                        modifier = Modifier.weight(1f),
                        onGithub = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(person.githubUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        onTelegram =
                            person.telegramUrl?.let { url ->
                                {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PersonCard(
    person: PersonUi,
    modifier: Modifier,
    onGithub: () -> Unit,
    onTelegram: (() -> Unit)?,
) {
    val badgeContainerColor =
        when (person.badgeStyle) {
            BadgeStyle.PINK -> MaterialTheme.colorScheme.primary
            BadgeStyle.BLUE -> MaterialTheme.colorScheme.tertiary
        }
    val badgeContentColor =
        when (person.badgeStyle) {
            BadgeStyle.PINK -> MaterialTheme.colorScheme.onPrimary
            BadgeStyle.BLUE -> MaterialTheme.colorScheme.onTertiary
        }

    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onGithub),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape,
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                GithubAvatar(
                    username = person.username,
                    modifier = Modifier.size(76.dp).clip(CircleShape),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = person.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 2,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.70f),
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "@${person.username}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.45f),
            )

            Spacer(Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
                thickness = 1.dp,
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SocialIconButton(
                    icon = painterResource(R.drawable.ic_github),
                    contentDescRes = R.string.luminedroid_github_desc,
                    onClick = onGithub,
                )
                if (onTelegram != null) {
                    SocialIconButton(
                        icon = rememberVectorPainter(Icons.Rounded.Send),
                        contentDescRes = R.string.luminedroid_telegram_desc,
                        onClick = onTelegram,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeContainerColor,
            ) {
                Text(
                    text = person.badgeLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = badgeContentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SocialIconButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    contentDescRes: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                contentDescription = stringResource(contentDescRes),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private sealed interface AvatarState {
    data object Loading : AvatarState

    data class Success(val bitmap: ImageBitmap) : AvatarState

    data object Error : AvatarState
}

@Composable
private fun GithubAvatar(username: String, modifier: Modifier = Modifier) {
    val state by
        produceState<AvatarState>(AvatarState.Loading, username) {
            value =
                withContext(Dispatchers.IO) {
                    runCatching {
                            val url = URL("https://github.com/$username.png?size=96")
                            val conn =
                                (url.openConnection() as HttpURLConnection).apply {
                                    connectTimeout = 5_000
                                    readTimeout = 5_000
                                    instanceFollowRedirects = true
                                }
                            conn.inputStream.use { stream ->
                                BitmapFactory.decodeStream(stream)?.asImageBitmap()?.let {
                                    AvatarState.Success(it)
                                } ?: AvatarState.Error
                            }
                        }
                        .getOrElse { AvatarState.Error }
                }
        }

    Box(
        modifier =
            modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is AvatarState.Loading ->
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp).alpha(0.3f),
                )
            is AvatarState.Success ->
                Image(
                    bitmap = s.bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            is AvatarState.Error ->
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp),
                )
        }
    }
}

@Composable
private fun IconPill(
    painter: androidx.compose.ui.graphics.painter.Painter,
    size: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier =
            Modifier.size(size)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                )
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun TeamItem.Person.toUi(style: BadgeStyle, label: String) =
    PersonUi(
        name = name,
        role = role,
        username = username,
        githubUrl = if (link.startsWith("http")) link else "https://github.com/$username",
        telegramUrl =
            telegram
                .takeIf { it.isNotBlank() }
                ?.let {
                    if (it.startsWith("http")) it else "https://t.me/$it"
                },
        badgeLabel = label,
        badgeStyle = style,
    )

typealias Tab = AboutLumineDroidFragment.Tab

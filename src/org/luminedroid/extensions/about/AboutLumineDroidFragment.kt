//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.about

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import org.json.JSONObject

class AboutLumineDroidFragment : DashboardFragment() {

    enum class Tab { DEVS, CONTRIBS }

    override fun getPreferenceScreenResId(): Int = 0
    override fun getLogTag(): String = "AboutLumineDroid"
    override fun getMetricsCategory(): Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val (devItems, contribItems) = loadTeamData()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AboutLumineDroidScreen(
                    developers = devItems,
                    contributors = contribItems,
                )
            }
        }
    }

    private fun loadTeamData(): Pair<List<TeamItem.Person>, List<TeamItem.Person>> {
        val ctx = requireContext()
        val json = ctx.resources.openRawResource(R.raw.luminedroid)
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        fun parseList(key: String, badgeLabel: String, style: BadgeStyle) =
            buildList {
                val arr = root.getJSONArray(key)
                for (i in 0 until arr.length()) {
                    val d = arr.getJSONObject(i)
                    add(
                        TeamItem.Person(
                            name       = d.getString("name"),
                            role       = d.getString("role"),
                            username   = d.getString("username"),
                            link       = d.getString("link"),
                            telegram   = d.optString("telegram", ""),
                            badgeLabel = badgeLabel,
                            badgeStyle = style,
                        )
                    )
                }
            }

        return Pair(
            parseList("developers",  ctx.getString(R.string.luminedroid_badge_core_dev),  BadgeStyle.PINK),
            parseList("contributors", ctx.getString(R.string.luminedroid_badge_contributor), BadgeStyle.BLUE),
        )
    }
}

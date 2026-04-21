//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import kotlinx.coroutines.*
import org.json.JSONObject

class AboutLumineDroidFragment : DashboardFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TeamAdapter
    private lateinit var tabDevs: LinearLayout
    private lateinit var tabContribs: LinearLayout
    private lateinit var iconDevs: ImageView
    private lateinit var iconContribs: ImageView
    private lateinit var textDevs: TextView
    private lateinit var textContribs: TextView
    private lateinit var sectionLabel: TextView
    private lateinit var sectionCount: TextView
    private lateinit var btnSocialGithub: LinearLayout
    private lateinit var btnSocialTelegram: LinearLayout
    private lateinit var btnSocialWebsite: LinearLayout

    enum class Tab {
        DEVS,
        CONTRIBS,
    }

    override fun getPreferenceScreenResId(): Int = 0

    override fun getLogTag(): String = "AboutLumineDroid"

    override fun getMetricsCategory(): Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.about_luminedroid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super<DashboardFragment>.onViewCreated(view, savedInstanceState)

        btnSocialGithub = view.findViewById(R.id.btn_social_github)
        btnSocialTelegram = view.findViewById(R.id.btn_social_telegram)
        btnSocialWebsite = view.findViewById(R.id.btn_social_website)

        tabDevs = view.findViewById(R.id.tab_devs)
        tabContribs = view.findViewById(R.id.tab_contribs)

        iconDevs = tabDevs.getChildAt(0) as ImageView
        textDevs = tabDevs.getChildAt(1) as TextView
        iconContribs = tabContribs.getChildAt(0) as ImageView
        textContribs = tabContribs.getChildAt(1) as TextView

        sectionLabel = view.findViewById(R.id.section_label)
        sectionCount = view.findViewById(R.id.section_count)
        recyclerView = view.findViewById(R.id.team_recycler)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled = false

        adapter = TeamAdapter(requireContext(), lifecycleScope)
        recyclerView.adapter = adapter

        btnSocialGithub.setOnClickListener { openUrl(getString(R.string.luminedroid_url_github)) }
        btnSocialTelegram.setOnClickListener {
            openUrl(getString(R.string.luminedroid_url_telegram))
        }
        btnSocialWebsite.setOnClickListener { openUrl(getString(R.string.luminedroid_url_website)) }

        tabDevs.setOnClickListener { switchTab(Tab.DEVS) }
        tabContribs.setOnClickListener { switchTab(Tab.CONTRIBS) }

        loadTeamData()
    }

    private fun loadTeamData() {
        val ctx = requireContext()
        val json =
            ctx.resources.openRawResource(R.raw.luminedroid).bufferedReader().use { it.readText() }

        val root = JSONObject(json)

        val devItems = mutableListOf<TeamItem.Person>()
        root.getJSONArray("developers").let { arr ->
            for (i in 0 until arr.length()) {
                val d = arr.getJSONObject(i)
                devItems +=
                    TeamItem.Person(
                        name = d.getString("name"),
                        role = d.getString("role"),
                        username = d.getString("username"),
                        link = d.getString("link"),
                        telegram = d.optString("telegram", ""),
                        badgeLabel = ctx.getString(R.string.luminedroid_badge_core_dev),
                        badgeStyle = BadgeStyle.PINK,
                    )
            }
        }

        val contribItems = mutableListOf<TeamItem.Person>()
        root.getJSONArray("contributors").let { arr ->
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                contribItems +=
                    TeamItem.Person(
                        name = c.getString("name"),
                        role = c.getString("role"),
                        username = c.getString("username"),
                        link = c.getString("link"),
                        telegram = c.optString("telegram", ""),
                        badgeLabel = ctx.getString(R.string.luminedroid_badge_contributor),
                        badgeStyle = BadgeStyle.BLUE,
                    )
            }
        }

        adapter.setData(devItems, contribItems)
        switchTab(Tab.DEVS)
    }

    private fun switchTab(tab: Tab) {
        val ctx = requireContext()
        val activeColor = ContextCompat.getColor(ctx, R.color.luminedroid_tab_active_text)
        val defaultColor = ContextCompat.getColor(ctx, R.color.luminedroid_tab_default_text)

        tabDevs.isSelected = (tab == Tab.DEVS)
        tabContribs.isSelected = (tab == Tab.CONTRIBS)

        iconDevs.setColorFilter(if (tab == Tab.DEVS) activeColor else defaultColor)
        textDevs.setTextColor(if (tab == Tab.DEVS) activeColor else defaultColor)
        iconContribs.setColorFilter(if (tab == Tab.CONTRIBS) activeColor else defaultColor)
        textContribs.setTextColor(if (tab == Tab.CONTRIBS) activeColor else defaultColor)

        when (tab) {
            Tab.DEVS -> {
                sectionLabel.setText(R.string.luminedroid_dev_section_title)
                sectionCount.text = adapter.devCount.toString()
            }
            Tab.CONTRIBS -> {
                sectionLabel.setText(R.string.luminedroid_contrib_section_title)
                sectionCount.text = adapter.contribCount.toString()
            }
        }

        adapter.showTab(tab)
        recyclerView.scrollToPosition(0)
    }

    private fun openUrl(url: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

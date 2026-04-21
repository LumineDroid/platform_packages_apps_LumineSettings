//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.about

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.R
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*

class TeamAdapter(private val context: Context, private val scope: CoroutineScope) :
    ListAdapter<TeamItem.Person, TeamAdapter.PersonVH>(DIFF) {

    private var devList: List<TeamItem.Person> = emptyList()
    private var contribList: List<TeamItem.Person> = emptyList()

    val devCount
        get() = devList.size

    val contribCount
        get() = contribList.size

    companion object {
        private val DIFF =
            object : DiffUtil.ItemCallback<TeamItem.Person>() {
                override fun areItemsTheSame(a: TeamItem.Person, b: TeamItem.Person) =
                    a.username == b.username

                override fun areContentsTheSame(a: TeamItem.Person, b: TeamItem.Person) = a == b
            }
    }

    fun setData(devs: List<TeamItem.Person>, contribs: List<TeamItem.Person>) {
        devList = devs
        contribList = contribs
    }

    fun showTab(tab: AboutLumineDroidFragment.Tab) {
        submitList(if (tab == AboutLumineDroidFragment.Tab.DEVS) devList else contribList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonVH =
        PersonVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_team_person, parent, false)
        )

    override fun onBindViewHolder(holder: PersonVH, position: Int) = holder.bind(getItem(position))

    inner class PersonVH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivAvatar: ImageView = v.findViewById(R.id.person_avatar)
        private val tvName: TextView = v.findViewById(R.id.person_name)
        private val tvRole: TextView = v.findViewById(R.id.person_role)
        private val tvHandle: TextView = v.findViewById(R.id.person_handle)
        private val btnGithub: ImageView = v.findViewById(R.id.btn_github)
        private val btnTelegram: ImageView = v.findViewById(R.id.btn_telegram)
        private val tvBadge: TextView = v.findViewById(R.id.person_badge)

        fun bind(item: TeamItem.Person) {
            tvName.text = item.name
            tvRole.text = item.role
            tvHandle.text = "@${item.username}"

            val (bgRes, colorRes) =
                when (item.badgeStyle) {
                    BadgeStyle.PINK ->
                        R.drawable.luminedroid_badge_pink to R.color.luminedroid_badge_pink_text
                    BadgeStyle.BLUE ->
                        R.drawable.luminedroid_badge_blue to R.color.luminedroid_badge_blue_text
                }
            tvBadge.text = item.badgeLabel
            tvBadge.background = ContextCompat.getDrawable(context, bgRes)
            tvBadge.setTextColor(ContextCompat.getColor(context, colorRes))

            val githubUrl =
                if (item.link.startsWith("http")) item.link
                else "https://github.com/${item.username}"
            btnGithub.setOnClickListener { openUrl(githubUrl) }

            if (item.telegram.isNotBlank()) {
                btnTelegram.visibility = View.VISIBLE
                val tgUrl =
                    if (item.telegram.startsWith("http")) item.telegram
                    else "https://t.me/${item.telegram}"
                btnTelegram.setOnClickListener { openUrl(tgUrl) }
            } else {
                btnTelegram.visibility = View.GONE
            }

            itemView.setOnClickListener { openUrl(githubUrl) }

            ivAvatar.setImageResource(R.drawable.ic_luminedroid_person)
            scope.launch(Dispatchers.IO) {
                val bmp = fetchRoundAvatar(item.username)
                withContext(Dispatchers.Main) { if (bmp != null) ivAvatar.setImageDrawable(bmp) }
            }
        }

        private fun openUrl(url: String) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun fetchRoundAvatar(username: String): Drawable? =
        try {
            val url = URL("https://github.com/$username.png?size=96")
            (url.openConnection() as HttpURLConnection).run {
                connectTimeout = 3000
                readTimeout = 3000
                doInput = true
                connect()
                val src = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                disconnect()
                BitmapDrawable(context.resources, src.toCircle())
            }
        } catch (_: Exception) {
            null
        }

    private fun Bitmap.toCircle(): Bitmap {
        val size = minOf(width, height)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, Rect(0, 0, width, height), rectF.toRect(), paint)
        return out
    }

    private fun RectF.toRect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}

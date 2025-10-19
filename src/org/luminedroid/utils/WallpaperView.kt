package org.luminedroid.utils

import android.app.WallpaperManager
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class WallpaperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private val contextM: Context = context

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val wallpaperManager = WallpaperManager.getInstance(contextM)
        setImageDrawable(wallpaperManager.drawable)
    }
}


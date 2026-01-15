package org.luminedroid.widget

import android.app.WallpaperManager
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.ImageView

class WallpaperBlurView @JvmOverloads constructor(
    private val contextM: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(contextM, attrs, defStyleAttr) {

    init {
        setRenderEffect(RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val wallpaperManager = WallpaperManager.getInstance(contextM)
        setImageDrawable(wallpaperManager.drawable)
    }
}

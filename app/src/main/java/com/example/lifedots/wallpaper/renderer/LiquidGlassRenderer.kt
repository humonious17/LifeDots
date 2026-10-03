package com.example.lifedots.wallpaper.renderer

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader

/** Static optical layers: no blur buffers or continuous animation needed. */
class LiquidGlassRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val panel = RectF()
    private var width = 0
    private var height = 0
    private lateinit var glow: RadialGradient
    private lateinit var reflection: LinearGradient

    fun draw(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        if (width != canvas.width || height != canvas.height) {
            width = canvas.width
            height = canvas.height
            glow = RadialGradient(w * .15f, h * .3f, h * .8f,
                intArrayOf(0x7042BFC7, 0x303F408A, 0x000B182C), null, Shader.TileMode.CLAMP)
            reflection = LinearGradient(0f, 0f, w, h,
                intArrayOf(0xA0FFFFFF.toInt(), 0x10FFFFFF, 0x6083F4DC), null, Shader.TileMode.CLAMP)
            panel.set(w * .04f, h * .025f, w * .96f, h * .975f)
        }
        paint.style = Paint.Style.FILL
        paint.shader = glow
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.color = 0x182B4969
        val radius = w * .07f
        canvas.drawRoundRect(panel, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (w / 400f).coerceAtLeast(1f)
        paint.shader = reflection
        canvas.drawRoundRect(panel, radius, radius, paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
    }
}

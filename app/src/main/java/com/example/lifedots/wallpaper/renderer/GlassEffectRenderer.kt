package com.example.lifedots.wallpaper.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.lifedots.preferences.GlassEffectSettings
import com.example.lifedots.preferences.GlassStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GlassEffectRenderer {

    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawGlassBackground(canvas: Canvas, glassSettings: GlassEffectSettings, colors: ThemeColors) {
        if (glassSettings.style == GlassStyle.NONE) return

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        glassPaint.reset()
        glassPaint.isAntiAlias = true

        when (glassSettings.style) {
            GlassStyle.LIGHT_FROST -> {
                // Light frosted glass effect
                glassPaint.color = Color.argb(
                    (glassSettings.opacity * 255).toInt(),
                    255, 255, 255
                )
                glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                canvas.drawRect(0f, 0f, width, height, glassPaint)

                // Add subtle gradient overlay
                val gradient = LinearGradient(
                    0f, 0f, 0f, height,
                    Color.argb(40, 255, 255, 255),
                    Color.argb(10, 255, 255, 255),
                    Shader.TileMode.CLAMP
                )
                glassPaint.shader = gradient
                glassPaint.maskFilter = null
                canvas.drawRect(0f, 0f, width, height, glassPaint)
                glassPaint.shader = null
            }

            GlassStyle.HEAVY_FROST -> {
                // Heavy frosted glass with multiple layers
                for (i in 3 downTo 1) {
                    glassPaint.color = Color.argb(
                        (glassSettings.opacity * 80 / i).toInt(),
                        255, 255, 255
                    )
                    glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur * i, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawRect(0f, 0f, width, height, glassPaint)
                }
            }

            GlassStyle.ACRYLIC -> {
                // Windows 11 Acrylic-style effect
                // Tinted blur layer
                val tintR = Color.red(glassSettings.tint)
                val tintG = Color.green(glassSettings.tint)
                val tintB = Color.blue(glassSettings.tint)

                glassPaint.color = Color.argb(
                    (glassSettings.opacity * 200).toInt(),
                    tintR, tintG, tintB
                )
                glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                canvas.drawRect(0f, 0f, width, height, glassPaint)

                // Noise texture simulation with dots
                glassPaint.maskFilter = null
                glassPaint.color = Color.argb(15, 255, 255, 255)
                val noiseRandom = Random(System.currentTimeMillis() / 1000)
                for (i in 0 until 200) {
                    val x = noiseRandom.nextFloat() * width
                    val y = noiseRandom.nextFloat() * height
                    canvas.drawCircle(x, y, 1f, glassPaint)
                }
            }

            GlassStyle.CRYSTAL -> {
                // Crystal clear glass with refraction-like effect
                val gradient = RadialGradient(
                    centerX, centerY,
                    maxOf(width, height) / 2,
                    intArrayOf(
                        Color.argb((glassSettings.opacity * 100).toInt(), 255, 255, 255),
                        Color.argb((glassSettings.opacity * 50).toInt(), 200, 220, 255),
                        Color.argb((glassSettings.opacity * 30).toInt(), 180, 200, 255)
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                glassPaint.shader = gradient
                canvas.drawRect(0f, 0f, width, height, glassPaint)
                glassPaint.shader = null

                // Add light streaks
                glassPaint.color = Color.argb(30, 255, 255, 255)
                glassPaint.strokeWidth = 2f
                glassPaint.style = Paint.Style.STROKE
                for (i in 0 until 5) {
                    val startX = width * (0.2f + i * 0.15f)
                    canvas.drawLine(startX, 0f, startX - 50, height, glassPaint)
                }
                glassPaint.style = Paint.Style.FILL
            }

            GlassStyle.ICE -> {
                // Ice effect with blue tint and crystalline patterns
                glassPaint.color = Color.argb(
                    (glassSettings.opacity * 150).toInt(),
                    200, 230, 255
                )
                glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                canvas.drawRect(0f, 0f, width, height, glassPaint)
                glassPaint.maskFilter = null

                // Draw ice crystal patterns
                glassPaint.color = Color.argb(40, 255, 255, 255)
                glassPaint.strokeWidth = 1.5f
                glassPaint.style = Paint.Style.STROKE
                val iceRandom = Random(42)
                for (i in 0 until 20) {
                    val x = iceRandom.nextFloat() * width
                    val y = iceRandom.nextFloat() * height
                    drawIceCrystal(canvas, x, y, 30f + iceRandom.nextFloat() * 40f, glassPaint)
                }
                glassPaint.style = Paint.Style.FILL
            }

            GlassStyle.NONE -> { /* No effect */ }
        }
    }

    private fun drawIceCrystal(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        // Draw a 6-pointed ice crystal
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val endX = cx + (size * cos(angle)).toFloat()
            val endY = cy + (size * sin(angle)).toFloat()
            canvas.drawLine(cx, cy, endX, endY, paint)

            // Add small branches
            val midX = cx + (size * 0.6f * cos(angle)).toFloat()
            val midY = cy + (size * 0.6f * sin(angle)).toFloat()
            val branchAngle1 = angle + Math.PI / 6
            val branchAngle2 = angle - Math.PI / 6
            val branchLen = size * 0.3f
            canvas.drawLine(
                midX, midY,
                midX + (branchLen * cos(branchAngle1)).toFloat(),
                midY + (branchLen * sin(branchAngle1)).toFloat(),
                paint
            )
            canvas.drawLine(
                midX, midY,
                midX + (branchLen * cos(branchAngle2)).toFloat(),
                midY + (branchLen * sin(branchAngle2)).toFloat(),
                paint
            )
        }
    }
}

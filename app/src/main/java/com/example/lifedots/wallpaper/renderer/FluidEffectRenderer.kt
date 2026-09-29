package com.example.lifedots.wallpaper.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.lifedots.preferences.FluidEffectSettings
import com.example.lifedots.preferences.FluidStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class FluidEffectRenderer {

    private val fluidPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawFluidBackground(
        canvas: Canvas,
        fluidSettings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        if (fluidSettings.style == FluidStyle.NONE) return

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        fluidPaint.reset()
        fluidPaint.isAntiAlias = true

        when (fluidSettings.style) {
            FluidStyle.WATER -> {
                drawWaterEffect(canvas, width, height, fluidSettings, colors, fluidPhase)
            }
            FluidStyle.LAVA -> {
                drawLavaEffect(canvas, width, height, fluidSettings, colors, fluidPhase)
            }
            FluidStyle.MERCURY -> {
                drawMercuryEffect(canvas, width, height, fluidSettings, colors, fluidPhase)
            }
            FluidStyle.PLASMA -> {
                drawPlasmaEffect(canvas, width, height, fluidSettings, colors, fluidPhase)
            }
            FluidStyle.AURORA -> {
                drawAuroraEffect(canvas, width, height, fluidSettings, colors, fluidPhase)
            }
            FluidStyle.NONE -> { /* No effect */ }
        }
    }

    private fun drawWaterEffect(
        canvas: Canvas,
        width: Float,
        height: Float,
        settings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        // Animated water waves
        val waveCount = 5
        val baseAlpha = (settings.colorIntensity * 60).toInt()

        for (i in 0 until waveCount) {
            val phase = fluidPhase + i * 0.5f
            val waveHeight = height * 0.05f * settings.turbulence

            fluidPaint.color = Color.argb(
                baseAlpha - i * 10,
                100, 150 + i * 20, 255
            )

            val path = Path()
            path.moveTo(0f, height)

            for (x in 0..width.toInt() step 10) {
                val y = height * (0.6f + i * 0.08f) +
                        sin(x * 0.02 + phase.toDouble()).toFloat() * waveHeight +
                        sin(x * 0.01 + phase * 0.5).toFloat() * waveHeight * 0.5f

                if (x == 0) path.moveTo(x.toFloat(), y)
                else path.lineTo(x.toFloat(), y)
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

            canvas.drawPath(path, fluidPaint)
        }
    }

    private fun drawLavaEffect(
        canvas: Canvas,
        width: Float,
        height: Float,
        settings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        // Animated lava bubbles and flow
        val baseAlpha = (settings.colorIntensity * 100).toInt()

        // Background lava glow
        val gradient = LinearGradient(
            0f, height, 0f, 0f,
            Color.argb(baseAlpha, 255, 100, 0),
            Color.argb(baseAlpha / 3, 255, 50, 0),
            Shader.TileMode.CLAMP
        )
        fluidPaint.shader = gradient
        canvas.drawRect(0f, height * 0.5f, width, height, fluidPaint)
        fluidPaint.shader = null

        // Lava bubbles
        fluidPaint.color = Color.argb(baseAlpha, 255, 150, 50)
        val bubbleRandom = Random((fluidPhase * 10).toLong())
        for (i in 0 until 15) {
            val x = bubbleRandom.nextFloat() * width
            val baseY = height * 0.7f + bubbleRandom.nextFloat() * height * 0.25f
            val y = baseY - (sin(fluidPhase + i.toFloat()).toFloat() + 1) * 30f * settings.turbulence
            val radius = 10f + bubbleRandom.nextFloat() * 20f

            fluidPaint.maskFilter = BlurMaskFilter(radius * 0.5f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(x, y, radius, fluidPaint)
        }
        fluidPaint.maskFilter = null
    }

    private fun drawMercuryEffect(
        canvas: Canvas,
        width: Float,
        height: Float,
        settings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        // Metallic liquid mercury effect
        val baseAlpha = (settings.colorIntensity * 150).toInt()

        // Mercury pools
        fluidPaint.color = Color.argb(baseAlpha, 180, 180, 200)

        val poolRandom = Random(42)
        for (i in 0 until 8) {
            val cx = poolRandom.nextFloat() * width
            val cy = height * 0.5f + poolRandom.nextFloat() * height * 0.4f
            val rx = 30f + poolRandom.nextFloat() * 60f
            val ry = 15f + poolRandom.nextFloat() * 30f

            // Animate position slightly
            val animCx = cx + sin(fluidPhase + i.toDouble()).toFloat() * 10f * settings.turbulence
            val animCy = cy + cos(fluidPhase * 0.7 + i.toDouble()).toFloat() * 5f * settings.turbulence

            // Metallic gradient
            val gradient = RadialGradient(
                animCx - rx * 0.3f, animCy - ry * 0.3f, rx,
                Color.argb(baseAlpha, 240, 240, 255),
                Color.argb(baseAlpha, 120, 120, 140),
                Shader.TileMode.CLAMP
            )
            fluidPaint.shader = gradient

            val rect = RectF(animCx - rx, animCy - ry, animCx + rx, animCy + ry)
            canvas.drawOval(rect, fluidPaint)
        }
        fluidPaint.shader = null
    }

    private fun drawPlasmaEffect(
        canvas: Canvas,
        width: Float,
        height: Float,
        settings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        // Colorful plasma effect
        val baseAlpha = (settings.colorIntensity * 100).toInt()

        // Create plasma-like color bands
        for (y in 0..height.toInt() step 20) {
            for (x in 0..width.toInt() step 20) {
                val value = sin(x * 0.01 + fluidPhase.toDouble()) +
                        sin(y * 0.01 + fluidPhase * 0.5) +
                        sin((x + y) * 0.01 + fluidPhase * 0.3) +
                        sin(sqrt((x * x + y * y).toDouble()) * 0.01)

                val normalizedValue = ((value + 4) / 8).toFloat()

                val r = (sin(normalizedValue * Math.PI * 2).toFloat() * 127 + 128).toInt()
                val g = (sin(normalizedValue * Math.PI * 2 + 2).toFloat() * 127 + 128).toInt()
                val b = (sin(normalizedValue * Math.PI * 2 + 4).toFloat() * 127 + 128).toInt()

                fluidPaint.color = Color.argb(baseAlpha / 2, r, g, b)
                canvas.drawRect(x.toFloat(), y.toFloat(), x + 20f, y + 20f, fluidPaint)
            }
        }
    }

    private fun drawAuroraEffect(
        canvas: Canvas,
        width: Float,
        height: Float,
        settings: FluidEffectSettings,
        colors: ThemeColors,
        fluidPhase: Float
    ) {
        // Northern lights aurora effect
        val baseAlpha = (settings.colorIntensity * 80).toInt()

        val auroraColors = intArrayOf(
            Color.argb(baseAlpha, 0, 255, 100),
            Color.argb(baseAlpha, 0, 200, 255),
            Color.argb(baseAlpha, 150, 0, 255),
            Color.argb(baseAlpha, 255, 0, 150)
        )

        for (band in 0 until 4) {
            val path = Path()
            val baseY = height * (0.1f + band * 0.15f)

            path.moveTo(0f, baseY)

            for (x in 0..width.toInt() step 5) {
                val wave1 = sin(x * 0.005 + fluidPhase + band).toFloat() * 50f * settings.turbulence
                val wave2 = sin(x * 0.01 + fluidPhase * 1.5 + band * 0.5).toFloat() * 30f * settings.turbulence
                val y = baseY + wave1 + wave2

                path.lineTo(x.toFloat(), y)
            }

            path.lineTo(width, baseY + 100f)
            path.lineTo(0f, baseY + 100f)
            path.close()

            fluidPaint.color = auroraColors[band]
            fluidPaint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawPath(path, fluidPaint)
        }
        fluidPaint.maskFilter = null
    }
}

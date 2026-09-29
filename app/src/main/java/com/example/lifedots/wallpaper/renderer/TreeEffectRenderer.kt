package com.example.lifedots.wallpaper.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.lifedots.preferences.TreeEffectSettings
import com.example.lifedots.preferences.TreeStyle
import com.example.lifedots.preferences.WallpaperSettings
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TreeEffectRenderer {

    private val treePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val treePath = Path()

    fun drawTreeEffect(
        canvas: Canvas,
        settings: WallpaperSettings,
        colors: ThemeColors,
        dayOfYear: Int,
        totalDays: Int,
        topOffset: Float,
        bottomOffset: Float
    ) {
        val treeSettings = settings.treeEffectSettings
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val availableHeight = height - topOffset - bottomOffset

        // Progress through the year (0 to 1)
        val progress = dayOfYear.toFloat() / totalDays.toFloat()

        // Draw ground if enabled
        if (treeSettings.showGround) {
            treePaint.color = Color.argb(255, 60, 40, 20)
            val groundHeight = 50f
            canvas.drawRect(0f, height - bottomOffset - groundHeight, width, height - bottomOffset, treePaint)

            // Grass on top
            treePaint.color = Color.argb(200, 50, 120, 50)
            canvas.drawRect(0f, height - bottomOffset - groundHeight, width, height - bottomOffset - groundHeight + 10f, treePaint)
        }

        val treeCenterX = width / 2
        val treeBaseY = height - bottomOffset - 50f
        val maxTreeHeight = availableHeight * 0.8f

        when (treeSettings.style) {
            TreeStyle.SIMPLE -> drawSimpleTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
            TreeStyle.DETAILED -> drawDetailedTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
            TreeStyle.BONSAI -> drawBonsaiTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
            TreeStyle.SAKURA -> drawSakuraTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
            TreeStyle.WILLOW -> drawWillowTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
        }
    }

    private fun drawSimpleTree(
        canvas: Canvas,
        centerX: Float,
        baseY: Float,
        maxHeight: Float,
        progress: Float,
        settings: TreeEffectSettings,
        colors: ThemeColors,
        dayOfYear: Int
    ) {
        val trunkHeight = maxHeight * 0.3f * progress
        val trunkWidth = 20f + progress * 15f

        // Draw trunk
        treePaint.color = settings.trunkColor
        val trunkRect = RectF(
            centerX - trunkWidth / 2,
            baseY - trunkHeight,
            centerX + trunkWidth / 2,
            baseY
        )
        canvas.drawRoundRect(trunkRect, 5f, 5f, treePaint)

        // Draw foliage layers (triangular)
        if (progress > 0.2f) {
            val foliageProgress = (progress - 0.2f) / 0.8f
            treePaint.color = settings.leafColor

            val layers = 3
            for (i in 0 until layers) {
                val layerProgress = minOf(1f, foliageProgress * layers - i)
                if (layerProgress <= 0) continue

                val layerTop = baseY - trunkHeight - (maxHeight * 0.6f) * ((layers - i).toFloat() / layers) * layerProgress
                val layerBottom = baseY - trunkHeight * 0.5f - (maxHeight * 0.2f * i)
                val layerWidth = (80f + i * 40f) * layerProgress

                treePath.reset()
                treePath.moveTo(centerX, layerTop)
                treePath.lineTo(centerX - layerWidth, layerBottom)
                treePath.lineTo(centerX + layerWidth, layerBottom)
                treePath.close()

                canvas.drawPath(treePath, treePaint)
            }

            // Add dots/fruits as day indicators
            drawTreeDots(canvas, centerX, baseY - trunkHeight, 100f * foliageProgress, dayOfYear, settings, colors)
        }
    }

    private fun drawDetailedTree(
        canvas: Canvas,
        centerX: Float,
        baseY: Float,
        maxHeight: Float,
        progress: Float,
        settings: TreeEffectSettings,
        colors: ThemeColors,
        dayOfYear: Int
    ) {
        // Draw trunk with branches
        treePaint.color = settings.trunkColor
        treePaint.strokeWidth = 8f + progress * 10f
        treePaint.strokeCap = Paint.Cap.ROUND
        treePaint.style = Paint.Style.STROKE

        val trunkHeight = maxHeight * 0.4f * progress

        // Main trunk
        canvas.drawLine(centerX, baseY, centerX, baseY - trunkHeight, treePaint)

        // Branches
        if (progress > 0.3f) {
            val branchProgress = (progress - 0.3f) / 0.7f
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.4f, -45f, trunkHeight * 0.3f * branchProgress, treePaint, 2)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.4f, 45f, trunkHeight * 0.3f * branchProgress, treePaint, 2)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.6f, -35f, trunkHeight * 0.35f * branchProgress, treePaint, 2)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.6f, 35f, trunkHeight * 0.35f * branchProgress, treePaint, 2)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.8f, -25f, trunkHeight * 0.25f * branchProgress, treePaint, 1)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.8f, 25f, trunkHeight * 0.25f * branchProgress, treePaint, 1)
        }

        treePaint.style = Paint.Style.FILL

        // Leaf clusters
        if (progress > 0.4f) {
            val leafProgress = (progress - 0.4f) / 0.6f
            treePaint.color = settings.leafColor

            drawLeafCluster(canvas, centerX, baseY - trunkHeight, 60f * leafProgress, treePaint)
            drawLeafCluster(canvas, centerX - 40f, baseY - trunkHeight * 0.7f, 45f * leafProgress, treePaint)
            drawLeafCluster(canvas, centerX + 40f, baseY - trunkHeight * 0.7f, 45f * leafProgress, treePaint)
            drawLeafCluster(canvas, centerX - 60f, baseY - trunkHeight * 0.5f, 35f * leafProgress, treePaint)
            drawLeafCluster(canvas, centerX + 60f, baseY - trunkHeight * 0.5f, 35f * leafProgress, treePaint)

            // Day indicator dots
            drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.6f, 80f * leafProgress, dayOfYear, settings, colors)
        }
    }

    private fun drawBranch(canvas: Canvas, startX: Float, startY: Float, angle: Float, length: Float, paint: Paint, depth: Int) {
        val radAngle = Math.toRadians(angle.toDouble() - 90)
        val endX = startX + (length * cos(radAngle)).toFloat()
        val endY = startY + (length * sin(radAngle)).toFloat()

        paint.strokeWidth = (depth * 3f + 2f)
        canvas.drawLine(startX, startY, endX, endY, paint)

        if (depth > 0) {
            drawBranch(canvas, endX, endY, angle - 25f, length * 0.6f, paint, depth - 1)
            drawBranch(canvas, endX, endY, angle + 25f, length * 0.6f, paint, depth - 1)
        }
    }

    private fun drawLeafCluster(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        paint.maskFilter = BlurMaskFilter(radius * 0.3f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.maskFilter = null
    }

    private fun drawBonsaiTree(
        canvas: Canvas,
        centerX: Float,
        baseY: Float,
        maxHeight: Float,
        progress: Float,
        settings: TreeEffectSettings,
        colors: ThemeColors,
        dayOfYear: Int
    ) {
        // Compact bonsai style
        val trunkHeight = maxHeight * 0.25f * progress
        val trunkWidth = 15f + progress * 20f

        // Curved trunk
        treePaint.color = settings.trunkColor
        treePaint.strokeWidth = trunkWidth
        treePaint.strokeCap = Paint.Cap.ROUND
        treePaint.style = Paint.Style.STROKE

        treePath.reset()
        treePath.moveTo(centerX, baseY)
        treePath.quadTo(
            centerX - 30f * progress, baseY - trunkHeight * 0.5f,
            centerX - 20f * progress, baseY - trunkHeight
        )
        canvas.drawPath(treePath, treePaint)

        treePaint.style = Paint.Style.FILL

        // Compact foliage pads
        if (progress > 0.3f) {
            val foliageProgress = (progress - 0.3f) / 0.7f
            treePaint.color = settings.leafColor

            // Multiple foliage pads
            val padPositions = listOf(
                Pair(centerX - 20f * progress, baseY - trunkHeight),
                Pair(centerX - 50f * progress, baseY - trunkHeight * 0.7f),
                Pair(centerX + 10f * progress, baseY - trunkHeight * 0.8f)
            )

            for ((x, y) in padPositions) {
                treePaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawOval(
                    RectF(x - 40f * foliageProgress, y - 20f * foliageProgress,
                        x + 40f * foliageProgress, y + 15f * foliageProgress),
                    treePaint
                )
            }
            treePaint.maskFilter = null

            drawTreeDots(canvas, centerX - 20f, baseY - trunkHeight * 0.8f, 50f * foliageProgress, dayOfYear, settings, colors)
        }

        // Draw pot
        treePaint.color = Color.argb(255, 120, 60, 30)
        val potWidth = 80f
        val potHeight = 30f
        canvas.drawRoundRect(
            RectF(centerX - potWidth / 2, baseY, centerX + potWidth / 2, baseY + potHeight),
            10f, 10f, treePaint
        )
    }

    private fun drawSakuraTree(
        canvas: Canvas,
        centerX: Float,
        baseY: Float,
        maxHeight: Float,
        progress: Float,
        settings: TreeEffectSettings,
        colors: ThemeColors,
        dayOfYear: Int
    ) {
        // Japanese cherry blossom tree
        val trunkHeight = maxHeight * 0.35f * progress
        val trunkWidth = 12f + progress * 8f

        // Curved trunk
        treePaint.color = settings.trunkColor
        treePaint.strokeWidth = trunkWidth
        treePaint.strokeCap = Paint.Cap.ROUND
        treePaint.style = Paint.Style.STROKE

        treePath.reset()
        treePath.moveTo(centerX, baseY)
        treePath.cubicTo(
            centerX + 20f, baseY - trunkHeight * 0.3f,
            centerX - 10f, baseY - trunkHeight * 0.6f,
            centerX, baseY - trunkHeight
        )
        canvas.drawPath(treePath, treePaint)

        // Branches
        if (progress > 0.2f) {
            treePaint.strokeWidth = trunkWidth * 0.5f
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.5f, -60f, trunkHeight * 0.4f, treePaint, 1)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.5f, 50f, trunkHeight * 0.35f, treePaint, 1)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.7f, -40f, trunkHeight * 0.3f, treePaint, 1)
            drawBranch(canvas, centerX, baseY - trunkHeight * 0.7f, 45f, trunkHeight * 0.35f, treePaint, 1)
        }

        treePaint.style = Paint.Style.FILL

        // Cherry blossoms
        if (progress > 0.3f) {
            val bloomProgress = (progress - 0.3f) / 0.7f
            treePaint.color = settings.bloomColor

            // Blossom clusters
            val blossomRandom = Random(dayOfYear)
            for (i in 0 until (50 * bloomProgress).toInt()) {
                val angle = blossomRandom.nextFloat() * 360f
                val distance = blossomRandom.nextFloat() * 100f * bloomProgress
                val bx = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
                val by = (baseY - trunkHeight * 0.7f) + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.6f

                val size = 3f + blossomRandom.nextFloat() * 5f
                treePaint.alpha = 180 + blossomRandom.nextInt(75)
                canvas.drawCircle(bx, by, size, treePaint)
            }

            // Falling petals
            treePaint.alpha = 150
            val petalCount = (20 * bloomProgress).toInt()
            val time = System.currentTimeMillis() / 50f
            for (i in 0 until petalCount) {
                val px = (centerX - 80f + blossomRandom.nextFloat() * 160f + sin(time * 0.01 + i).toFloat() * 20f)
                val py = (baseY - trunkHeight + ((time + i * 50) % (trunkHeight + 100)).toFloat())
                canvas.drawCircle(px, py, 3f, treePaint)
            }

            treePaint.alpha = 255
            drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.6f, 70f * bloomProgress, dayOfYear, settings, colors)
        }
    }

    private fun drawWillowTree(
        canvas: Canvas,
        centerX: Float,
        baseY: Float,
        maxHeight: Float,
        progress: Float,
        settings: TreeEffectSettings,
        colors: ThemeColors,
        dayOfYear: Int
    ) {
        // Weeping willow tree
        val trunkHeight = maxHeight * 0.3f * progress
        val trunkWidth = 18f + progress * 12f

        // Main trunk
        treePaint.color = settings.trunkColor
        treePaint.strokeWidth = trunkWidth
        treePaint.strokeCap = Paint.Cap.ROUND
        treePaint.style = Paint.Style.STROKE
        canvas.drawLine(centerX, baseY, centerX, baseY - trunkHeight, treePaint)

        treePaint.style = Paint.Style.FILL

        // Drooping branches with leaves
        if (progress > 0.25f) {
            val branchProgress = (progress - 0.25f) / 0.75f
            treePaint.color = settings.leafColor
            treePaint.strokeWidth = 2f
            treePaint.style = Paint.Style.STROKE

            val branchCount = (30 * branchProgress).toInt()
            val time = System.currentTimeMillis() / 1000f

            for (i in 0 until branchCount) {
                val startAngle = -150f + (i.toFloat() / branchCount) * 120f
                val startX = centerX + cos(Math.toRadians(startAngle.toDouble())).toFloat() * 20f
                val startY = baseY - trunkHeight + sin(Math.toRadians(startAngle.toDouble())).toFloat() * 10f

                val branchLength = 80f + (i % 5) * 30f * branchProgress
                val swayAmount = sin(time + i * 0.5).toFloat() * 10f

                treePath.reset()
                treePath.moveTo(startX, startY)
                treePath.cubicTo(
                    startX + swayAmount, startY + branchLength * 0.3f,
                    startX + swayAmount * 1.5f, startY + branchLength * 0.6f,
                    startX + swayAmount * 2f, startY + branchLength
                )

                canvas.drawPath(treePath, treePaint)
            }

            treePaint.style = Paint.Style.FILL
            drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.5f, 60f * branchProgress, dayOfYear, settings, colors)
        }
    }

    private fun drawTreeDots(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        dayOfYear: Int,
        settings: TreeEffectSettings,
        colors: ThemeColors
    ) {
        // Draw small dots representing days passed as fruits/leaves
        val dotRandom = Random(42)
        val dotsToShow = minOf(dayOfYear, 50)

        for (i in 0 until dotsToShow) {
            val angle = dotRandom.nextFloat() * 360f
            val distance = dotRandom.nextFloat() * radius
            val dx = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val dy = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.6f

            treePaint.color = if (i == dayOfYear - 1) colors.todayDot else colors.filledDot
            treePaint.alpha = if (i == dayOfYear - 1) 255 else 180
            canvas.drawCircle(dx, dy, 4f, treePaint)
        }
        treePaint.alpha = 255
    }
}

package com.example.lifedots.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import kotlin.math.min

object ImageUtils {

    /**
     * Load and scale a bitmap from URI to fit target dimensions
     */
    fun loadScaledBitmap(context: Context, uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
        if (targetWidth <= 0 || targetHeight <= 0) return null
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Decode bounds first
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream.use { BitmapFactory.decodeStream(it, null, options) }
            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false

            // Decode actual bitmap
            val inputStream2 = context.contentResolver.openInputStream(uri)
                ?: return null
            val bitmap = inputStream2.use { BitmapFactory.decodeStream(it, null, options) }

            if (bitmap == null) return null

            // Scale to fit screen
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }

            scaledBitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate optimal sample size for bitmap loading
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Apply blur effect to a bitmap using RenderScript
     */
    @Suppress("DEPRECATION")
    fun applyBlur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val clampedRadius = min(25f, radius)
        if (clampedRadius <= 0) return bitmap

        return try {
            val rs = RenderScript.create(context)
            try {
                val input = Allocation.createFromBitmap(rs, bitmap)
                val output = Allocation.createTyped(rs, input.type)
                val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                script.setRadius(clampedRadius)
                script.setInput(input)
                script.forEach(output)
                val blurredBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    bitmap.config ?: Bitmap.Config.ARGB_8888
                )
                output.copyTo(blurredBitmap)
                blurredBitmap
            } finally {
                rs.destroy()
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Get thumbnail bitmap for preview
     */
    fun getThumbnail(context: Context, uri: Uri, size: Int): Bitmap? {
        return loadScaledBitmap(context, uri, size, size)
    }
}

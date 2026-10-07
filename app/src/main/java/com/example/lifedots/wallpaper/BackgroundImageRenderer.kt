package com.example.lifedots.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import com.example.lifedots.preferences.BackgroundSettings
import com.example.lifedots.util.ImageUtils

/** Owns the processed image; opacity changes do not require decoding or blurring again. */
internal class BackgroundImageRenderer(private val context: Context) {
    private data class CacheKey(val uri: String, val width: Int, val height: Int, val blur: Float)
    private var key: CacheKey? = null
    private var bitmap: Bitmap? = null
    private val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val overlayPaint = Paint()

    fun draw(canvas: Canvas, settings: BackgroundSettings, fallbackColor: Int) {
        val uri = settings.imageUri
        if (!settings.enabled || uri == null) {
            clear()
            return
        }
        val requested = CacheKey(uri, canvas.width, canvas.height, settings.blurRadius)
        if (key != requested) {
            clear()
            key = requested
            val source = ImageUtils.loadScaledBitmap(context, Uri.parse(uri), canvas.width, canvas.height)
            if (source != null) {
                bitmap = ImageUtils.applyBlur(context, source, settings.blurRadius)
                if (bitmap !== source) source.recycle()
            }
        }
        val image = bitmap ?: return
        val opacity = settings.opacity.coerceIn(0f, 1f)
        imagePaint.alpha = (opacity * 255).toInt()
        canvas.drawBitmap(image, 0f, 0f, imagePaint)
        overlayPaint.color = fallbackColor
        overlayPaint.alpha = ((1f - opacity) * 200).toInt()
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), overlayPaint)
    }

    fun clear() {
        bitmap?.recycle()
        bitmap = null
        key = null
    }
}

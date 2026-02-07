package com.assgui.gourmandine.ui.screens.addreview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

data class EmojiSticker(
    val emoji: String,
    var x: Float,
    var y: Float,
    var size: Float = 80f
)

class PhotoEditorCanvasView(context: Context) : View(context) {

    private var sourceBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapRect = RectF()

    private var sepiaIntensity = 0f
    private var brightnessIntensity = 0.5f

    private val stickers = mutableListOf<EmojiSticker>()
    private var draggedStickerIndex = -1
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    fun setImage(bitmap: Bitmap) {
        sourceBitmap = bitmap
        scaledBitmap = null
        invalidate()
    }

    fun setFilterIntensity(sepia: Float, brightness: Float) {
        sepiaIntensity = sepia
        brightnessIntensity = brightness
        invalidate()
    }

    fun addEmoji(emoji: String) {
        val cx = width / 2f
        val cy = height / 2f
        stickers.add(EmojiSticker(emoji, cx, cy))
        invalidate()
    }

    fun exportBitmap(): Bitmap {
        val w = width
        val h = height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        drawContent(canvas)
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawContent(canvas)
    }

    private fun drawContent(canvas: Canvas) {
        val bmp = sourceBitmap ?: return

        if (scaledBitmap == null || scaledBitmap?.width != width) {
            val scale = minOf(width.toFloat() / bmp.width, height.toFloat() / bmp.height)
            val w = (bmp.width * scale).toInt()
            val h = (bmp.height * scale).toInt()
            scaledBitmap = Bitmap.createScaledBitmap(bmp, w, h, true)
            val left = (width - w) / 2f
            val top = (height - h) / 2f
            bitmapRect.set(left, top, left + w, top + h)
        }

        val colorMatrix = buildFilterMatrix()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        scaledBitmap?.let {
            canvas.drawBitmap(it, bitmapRect.left, bitmapRect.top, paint)
        }

        // Draw emoji stickers
        for (sticker in stickers) {
            emojiPaint.textSize = sticker.size
            canvas.drawText(sticker.emoji, sticker.x, sticker.y, emojiPaint)
        }
    }

    private fun buildFilterMatrix(): ColorMatrix {
        val matrix = ColorMatrix()

        if (sepiaIntensity > 0f) {
            val s = sepiaIntensity
            val sepiaMatrix = ColorMatrix(
                floatArrayOf(
                    1f - 0.607f * s, 0.769f * s, 0.189f * s, 0f, 0f,
                    0.349f * s, 1f - 0.314f * s, 0.168f * s, 0f, 0f,
                    0.272f * s, 0.534f * s, 1f - 0.869f * s, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(sepiaMatrix)
        }

        val brightnessValue = (brightnessIntensity - 0.5f) * 200f
        if (brightnessValue != 0f) {
            val bMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessValue,
                    0f, 1f, 0f, 0f, brightnessValue,
                    0f, 0f, 1f, 0f, brightnessValue,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(bMatrix)
        }

        return matrix
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i in stickers.indices.reversed()) {
                    val s = stickers[i]
                    val halfSize = s.size / 2f
                    if (event.x in (s.x - halfSize)..(s.x + halfSize) &&
                        event.y in (s.y - s.size)..(s.y + halfSize / 2f)
                    ) {
                        draggedStickerIndex = i
                        dragOffsetX = event.x - s.x
                        dragOffsetY = event.y - s.y
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggedStickerIndex >= 0) {
                    stickers[draggedStickerIndex].x = event.x - dragOffsetX
                    stickers[draggedStickerIndex].y = event.y - dragOffsetY
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                draggedStickerIndex = -1
            }
        }
        return super.onTouchEvent(event)
    }
}

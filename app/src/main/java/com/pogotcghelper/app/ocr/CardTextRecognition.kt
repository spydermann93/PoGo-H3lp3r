package com.pogotcghelper.app.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Runs on-device ML Kit text recognition on a single captured camera frame and returns
 * its text as a list of lines (roughly top-to-bottom, matching ML Kit's block/line order).
 * There's no free image-recognition API that identifies a Pokémon card by photo alone, so
 * this is the practical alternative: read the printed name/number off the card, then let
 * the caller search for it normally. Always closes [imageProxy].
 *
 * [ImageCapture]'s in-memory capture callback hands back a JPEG-encoded frame -- a single
 * plane of compressed bytes, not raw YUV pixels -- so this decodes it with [BitmapFactory]
 * rather than going through ML Kit's `InputImage.fromMediaImage`, which expects raw
 * YUV_420_888/NV21 data. Feeding it compressed JPEG bytes instead doesn't crash, it just
 * silently misreads them as pixels, producing garbled OCR output.
 */
suspend fun recognizeCardText(imageProxy: ImageProxy): List<String> {
    val bitmap = decodeUprightBitmap(imageProxy)
    imageProxy.close()
    if (bitmap == null) return emptyList()

    val inputImage = InputImage.fromBitmap(bitmap, 0)
    return suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val lines = visionText.textBlocks.flatMap { block -> block.lines.map { it.text.trim() } }
                    .filter { it.isNotBlank() }
                continuation.resume(lines)
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }
}

/** Decodes the captured JPEG frame and bakes CameraX's reported rotation into the bitmap. */
private fun decodeUprightBitmap(imageProxy: ImageProxy): Bitmap? {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    val rotation = imageProxy.imageInfo.rotationDegrees
    if (rotation == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

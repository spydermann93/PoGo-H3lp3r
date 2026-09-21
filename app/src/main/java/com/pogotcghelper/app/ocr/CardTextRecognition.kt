package com.pogotcghelper.app.ocr

import androidx.camera.core.ExperimentalGetImage
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
 * the caller search for it normally. Always closes [imageProxy], even on failure.
 *
 * Uses [ImageProxy.getImage], which CameraX marks [ExperimentalGetImage]. Opting in here
 * absorbs that requirement so callers don't need to -- this function's own signature has
 * nothing experimental about it. The [ImageProxy.getImage] access is deliberately a plain
 * statement here rather than inside the [suspendCancellableCoroutine] lambda below: Android
 * Lint's opt-in check doesn't reliably see an enclosing @OptIn through a lambda argument
 * (even to an inline function like this one), so it has to sit directly in this function's
 * own body to be recognized as covered.
 */
@OptIn(ExperimentalGetImage::class)
suspend fun recognizeCardText(imageProxy: ImageProxy): List<String> {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return emptyList()
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    return suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val lines = visionText.textBlocks.flatMap { block -> block.lines.map { it.text.trim() } }
                    .filter { it.isNotBlank() }
                imageProxy.close()
                continuation.resume(lines)
            }
            .addOnFailureListener {
                imageProxy.close()
                continuation.resume(emptyList())
            }
    }
}

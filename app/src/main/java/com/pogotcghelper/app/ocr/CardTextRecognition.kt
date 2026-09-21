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
 */
@ExperimentalGetImage
suspend fun recognizeCardText(imageProxy: ImageProxy): List<String> = suspendCancellableCoroutine { continuation ->
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        continuation.resume(emptyList())
        return@suspendCancellableCoroutine
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
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

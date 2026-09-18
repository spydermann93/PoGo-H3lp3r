package com.pogotcghelper.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import coil.compose.AsyncImage

/**
 * Card art, or a clear "no image available" placeholder when the source has none.
 * A handful of older/niche cards (e.g. very old sets, promo kits) genuinely have no
 * artwork in the data source, so this reads as an intentional state rather than a
 * silently broken image.
 *
 * [grayscale] desaturates the art -- used to mark a card as not-yet-owned. This is
 * meant as one signal among several (an icon and a text label carry the same state
 * too), not the only one, since color/saturation alone isn't a reliable signal for
 * every viewer.
 */
@Composable
fun CardArtwork(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    grayscale: Boolean = false,
) {
    if (imageUrl.isBlank()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ImageNotSupported,
                contentDescription = "No image available",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
        )
    }
}

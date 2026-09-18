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
import coil.compose.AsyncImage

/**
 * Card art, or a clear "no image available" placeholder when the source has none.
 * A handful of older/niche cards (e.g. very old sets, promo kits) genuinely have no
 * artwork in the data source, so this reads as an intentional state rather than a
 * silently broken image.
 */
@Composable
fun CardArtwork(imageUrl: String, contentDescription: String?, modifier: Modifier = Modifier) {
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
        )
    }
}

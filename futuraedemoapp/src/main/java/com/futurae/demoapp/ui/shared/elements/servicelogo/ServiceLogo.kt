package com.futurae.demoapp.ui.shared.elements.servicelogo

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.futurae.demoapp.ui.theme.DisableColor

@Composable
fun ServiceLogo(
    modifier: Modifier,
    url: String?
) {
    var isLoadedSuccessfully by remember {
        mutableStateOf(false)
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "Service Logo",
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
        placeholder =  rememberVectorPainter(Icons.Default.AccountBox),
        error =  rememberVectorPainter(Icons.Default.AccountBox),
        fallback =  rememberVectorPainter(Icons.Default.AccountBox),
        onSuccess = {
            isLoadedSuccessfully = true
        },
        contentScale = ContentScale.Crop,
        colorFilter = if (isLoadedSuccessfully) {
            null
        } else {
            ColorFilter.tint(DisableColor)
        }
    )
}
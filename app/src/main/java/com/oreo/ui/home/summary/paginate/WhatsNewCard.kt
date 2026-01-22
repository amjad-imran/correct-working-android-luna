package com.oreo.ui.home.summary.paginate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oreo.data.model.BannerItem

@Composable
fun WhatsNewCardsList(
    banners: List<BannerItem>,
    onBannerClick: (BannerItem) -> Unit,
    onCloseClick: (BannerItem) -> Unit
) {
    LazyRow(
        Modifier.padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = banners
        ) { banner ->
            BannerCard(
                banner = banner,
                onClick = { onBannerClick(banner) },
                onCloseClick = { onCloseClick(banner) }
            )
        }
    }
}

@Composable
fun BannerCard(
    banner: BannerItem,
    onClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(343.dp)
            .height(172.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            AsyncImage(
                model = banner.imageUrl["en"],
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            if (banner.showCrossButton) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable { onCloseClick() }
                        .padding(4.dp),
                    tint = Color.White
                )
            }
        }
    }
}

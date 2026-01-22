package com.oreo.ui.home.summary.paginate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.noisefit.luna.R
import com.oreo.data.model.BannerItem
@Composable
fun WhatsNewCardsList(
    banners: List<BannerItem>,
    onBannerClick: (BannerItem) -> Unit,
    onCloseClick: (BannerItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth - 32.dp

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardWidth / 2)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
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
                    painter = painterResource(id = R.drawable.ic_whats_new_cross),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clickable { onCloseClick() }
                )
            }
        }
    }
}

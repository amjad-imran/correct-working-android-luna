package com.oreo.ui.home.summary.paginate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.noisefit.luna.R
import com.oreo.data.model.BannerItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
@Composable
fun WhatsNewCardsList(
    banners: List<BannerItem>,
    userSelectedLanguage: String,
    onBannerClick: (BannerItem) -> Unit,
    onCloseClick: (BannerItem) -> Unit
) {
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidthPx = with(LocalDensity.current) {
        (screenWidth - 32.dp).toPx()
    }

    val gilroy = FontFamily(
        Font(com.noisefit_commans.R.font.gilroy_medium)
    )

    val currentIndex by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset

            if (offset > cardWidthPx / 2) {
                index + 1
            } else {
                index
            }
        }
    }

    Column {
        val safeSelectedIndex =
            if (banners.isNotEmpty()) {
                currentIndex.coerceIn(0, banners.size - 1)
            } else {
                0
            }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if(banners.isEmpty()) return
            Text(
                text = stringResource(R.string.whats_new),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = gilroy,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )

            DotsIndicator(
                totalDots = banners.size,
                selectedIndex = currentIndex.coerceIn(0, safeSelectedIndex),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
            )
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = banners,
                key = { it.id }
            ) { banner ->
                BannerCard(
                    banner = banner,
                    userSelectedLanguage = userSelectedLanguage,
                    onClick = { onBannerClick(banner) },
                    onCloseClick = { onCloseClick(banner) }
                )
            }
        }
    }
}

@Composable
fun BannerCard(
    banner: BannerItem,
    userSelectedLanguage: String,
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
                model = banner.imageUrl[userSelectedLanguage],
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

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    spacing: Dp = 6.dp,
    selectedColor: Color = Color.White,
    unSelectedColor: Color = Color.White.copy(alpha = 0.4f)
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        if (index == selectedIndex) selectedColor else unSelectedColor
                    )
            )
        }
    }
}

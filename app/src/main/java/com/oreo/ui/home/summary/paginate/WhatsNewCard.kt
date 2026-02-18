package com.oreo.ui.home.summary.paginate

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.IconButton
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.viewpager2.widget.ViewPager2
import coil.request.ImageRequest

@Composable
fun WhatsNewCardsList(
    banners: List<BannerItem>,
    userSelectedLanguage: String,
    onBannerClick: (BannerItem) -> Unit,
    onCloseClick: (BannerItem) -> Unit
) {
    if (banners.isEmpty()) return

    val composeView = LocalView.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { banners.size }
    )

    val font = FontFamily(
        Font(com.noisefit_commans.R.font.google_sans_flex_medium)
    )

    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.whats_new),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = font,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )

            DotsIndicator(
                totalDots = banners.size,
                selectedIndex = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInteropFilter { event ->
                when (event.actionMasked) {

                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        var p = composeView.parent
                        while (p != null) {
                            p.requestDisallowInterceptTouchEvent(true)
                            if(p is ViewPager2) break
                            p = p.parent
                        }
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        var p = composeView.parent
                        while (p != null) {
                            p.requestDisallowInterceptTouchEvent(false)
                            if(p is ViewPager2) break
                            p = p.parent
                        }
                    }
                }
                false // let Compose pager handle the event
            }
        ) { page ->
            val banner = banners[page]
            BannerCard(
                banner = banner,
                userSelectedLanguage = userSelectedLanguage,
                onClick = { onBannerClick(banner) },
                onCloseClick = { onCloseClick(banner) }
            )
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banner.imageUrl[userSelectedLanguage])
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            if (banner.showCrossButton) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whats_new_cross),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
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

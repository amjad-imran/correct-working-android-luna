package com.oreo.ui.compose.element.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noisefit.luna.R
import com.oreo.ui.compose.styles.FontStyle


@Preview
@Composable
fun CircularBackButtonPreview() {
    CircularBackButton(onClick = {})
}

@Composable
fun CircularBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color = Color(0x0affffff))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_back_arrow_new),
            contentDescription = "Back"
        )
    }
}


@Preview
@Composable
fun CircularHistoryButtonPreview() {
    CircularHistoryButton(onClick = {})
}

@Composable
fun CircularHistoryButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color = Color(0x0affffff))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_history),
            contentDescription = "Back"
        )
    }
}

@Preview
@Composable
fun ElipsizeTextButtonPreview() {
    ElipsizeTextButton("My referals", onClick = {})
}


@Composable
fun ElipsizeTextButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x0fffffff))
            .border(1.dp, Color(0x1effffff), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),

        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, // Text inside the button
            color = Color.White, // Text color
            style = FontStyle.SIZE_16
        )
    }
}



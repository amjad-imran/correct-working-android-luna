package com.oreo.ui.compose.element.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oreo.ui.compose.styles.FontStyle


@Preview
@Composable
fun ButtonPrimaryPreview() {
    ButtonPrimary(text = "Android", onClick = {})
}

@Composable
fun ButtonPrimary(text: String, onClick: () -> Unit) {
    val buttonHeight = 52.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
    ) {
        val buttonWidthPx = constraints.maxWidth.toFloat()
        val buttonHeightPx = constraints.maxHeight.toFloat()
        Button(
            onClick = { onClick() },
            shape = RoundedCornerShape(buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF222e44),  // Light cyan-like start color
                            Color(0xFF344566),  // Center color
                            Color(0xFF304164)   // Dark blue end color
                        ),
                        center = Offset(
                            buttonWidthPx / 2,
                            buttonHeightPx + buttonHeightPx
                        ),
                        radius = 600f  // Gradient radius
                    ),
                    shape = RoundedCornerShape(buttonHeight)
                )
                .border(1.dp, Color(0x1effffff), RoundedCornerShape(50))
        ) {
            Text(
                text = text,
                style = FontStyle.SIZE_16,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Preview
@Composable
fun ButtonSecondaryPreview() {
    ButtonSecondary(Modifier,text = "Android", onClick = {})
}

@Composable
fun ButtonSecondary(modifier: Modifier,
                    buttonHeight: Dp = 52.dp,
                    text: String, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick() },
        shape = RoundedCornerShape(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .height(buttonHeight)
            .background(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(buttonHeight)
            )
            .border(1.dp, Color(0x1effffff), RoundedCornerShape(50))
    ) {
        Text(
            text = text,
            style = FontStyle.SIZE_16,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview
@Composable
fun ButtonBluePreview() {
    ButtonBlue(text = "Android", onClick = {})
}


@Composable
fun ButtonBlue(text: String, onClick: () -> Unit) {
    val buttonHeight = 52.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
    ) {
        val buttonWidthPx = constraints.maxWidth.toFloat()
        val buttonHeightPx = constraints.maxHeight.toFloat()
        Button(
            onClick = { onClick() },
            shape = RoundedCornerShape(buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFB4FFFA),  // Light cyan-like start color
                            Color(0xFF3988FF),  // Center color
                            Color(0xFF0051CB)   // Dark blue end color
                        ),
                        center = Offset(
                            buttonWidthPx / 2,
                            buttonHeightPx + buttonHeightPx
                        ),
                        radius = 600f  // Gradient radius
                    ),
                    shape = RoundedCornerShape(buttonHeight)
                )
        ) {
            Text(
                text = text,
                style = FontStyle.SIZE_16,
                color = Color.White,
                fontFamily = FontFamily(
                    Font(
                        com.noisefit_commans.R.font.google_sans_flex_medium,
                        FontWeight.Normal
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


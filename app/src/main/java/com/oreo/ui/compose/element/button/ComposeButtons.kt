package com.oreo.ui.compose.element.button

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noisefit.luna.R
import com.oreo.ui.compose.styles.FontStyle

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
                style = FontStyle.FontSize16,
                color = Color.White,
                fontFamily = FontFamily(
                    Font(
                        com.noisefit_commans.R.font.gilroy_medium,
                        FontWeight.Normal
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


package com.oreo.ui.compose.styles

import android.graphics.fonts.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object FontStyle {

    val FontSize16 = TextStyle(
        fontFamily = FontFamily(
            Font(
                com.noisefit_commans.R.font.gilroy_medium,
                FontWeight.Normal
            )
        ),
        fontSize = 16.sp
    )


}
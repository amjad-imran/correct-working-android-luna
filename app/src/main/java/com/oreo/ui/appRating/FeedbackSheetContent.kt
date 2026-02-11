package com.oreo.ui.appRating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noisefit_commans.R.font.gilroy_medium

@Composable
fun FeedbackSheetContent(
    uiState: AppRatingDislikeFeedbackBSViewModel.FeedbackSheetUiState,
    onAction: (AppRatingDislikeFeedbackBSViewModel.FeedbackSheetAction) -> Unit,
    onSubmitBtnClicked: () -> Unit,
    submitBtnText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(Modifier.height(4.dp))

        uiState.reasons.forEach { item ->
            val reason = item.reason
            ReasonRow(
                text = item.label,
                checked = uiState.selectedReasons.contains(reason),
                onClick = {
                    onAction(
                        AppRatingDislikeFeedbackBSViewModel.FeedbackSheetAction.ToggleReason(reason)
                    )
                }
            )

            // ✅ TextField appears right under "Something else" option
            if (reason == AppRatingDislikeFeedbackBSViewModel.FeedbackReason.SOMETHING_ELSE &&
                uiState.isSomethingElseSelected
            ) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.details,
                    onValueChange = {
                        onAction(
                            AppRatingDislikeFeedbackBSViewModel.FeedbackSheetAction.DetailsChanged(it)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tell us a bit more?") },
                    minLines = 4,
                    maxLines = 4,
                    singleLine = false,
                )
            }

            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(80.dp))

        Button(
            onClick = {
                onSubmitBtnClicked()
                onAction(AppRatingDislikeFeedbackBSViewModel.FeedbackSheetAction.Submit)
            },
            enabled = uiState.isSubmitEnabled && !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,          // selected/enabled look
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF8A8A8A), // grey bg when disabled (only bg changes)
                disabledContentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            )
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                    color = Color.Black
                )
                Spacer(Modifier.width(10.dp))
            }

            Text(
                text = submitBtnText,
                style = TextStyle(
                    fontFamily = FontFamily(Font(gilroy_medium, weight = FontWeight.Medium)),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    fontSize = 14.sp,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))
    }

}

@Composable
private fun ReasonRow(
    text: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox, onClick = onClick),
        shape = shape,
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppRatingSquareCheckbox(
                checked = checked,
                onClick = onClick
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily(Font(gilroy_medium, weight = FontWeight.Medium)),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    fontSize = 14.sp,
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AppRatingSquareCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    val boxShape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(boxShape)
            .background(if (checked) Color.White else Color.Transparent)
            .border(1.dp, Color.White, boxShape)
            .clickable(role = Role.Checkbox, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
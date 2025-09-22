package com.noisefit.luna.widget.water

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.ImageProvider
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.action.clickable
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.ResourceColorProvider
import com.noisefit.luna.R

class WaterIntakeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> =
       PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    fun Content() {
        val prefs = currentState<Preferences>()
        val currentMl = prefs[PrefKeys.CurrentMl] ?: 0
        val goalMl = (prefs[PrefKeys.GoalMl] ?: DEFAULT_GOAL_ML).coerceAtLeast(250)

        val progress = (currentMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)

        Box(
            modifier = GlanceModifier
                .height(180.dp)
                .width(180.dp)
                .cornerRadius(24.dp)
                .background(ImageProvider(R.drawable.water_widget_bg))
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(R.color.water_bg_base)
            ) {
                val waterHeight = 180.dp * progress
                val spacerHeight = 180.dp * (1f - progress)
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(GlanceModifier.height(spacerHeight))
                    Box(
                        modifier = GlanceModifier
                            .height(waterHeight)
                            .fillMaxWidth()
                            .background(R.color.water_bg_fill)
                    ) { }
                }
            }
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    text = "Water Intake",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Spacer(GlanceModifier.height(6.dp))

                Text(
                    text = formatValue(currentMl, goalMl),
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.Bottom,
            ) {
                // Minus button
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(actionRunCallback<AdjustWaterCallback>(
                            parameters = actionParametersOf(
                                AdjustWaterCallback.DeltaKey to (-STEP_ML).toString()
                            )
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_water_minus),
                        contentDescription = "Minus",
                        modifier = GlanceModifier.size(32.dp)
                    )
                    /*Text(
                        text = "−",
                        style = TextStyle(
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    )*/
                }
                Spacer(GlanceModifier.defaultWeight())
                // Plus button
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(
                            actionRunCallback<AdjustWaterCallback>(
                                parameters = actionParametersOf(
                                    AdjustWaterCallback.DeltaKey to STEP_ML.toString()
                                )
                            )),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_water_plus),
                        contentDescription = "Plus",
                        modifier = GlanceModifier.size(32.dp)
                    )
                    /* Text(
                         text = "+",
                         style = TextStyle(
                             fontSize = 24.sp,
                             textAlign = TextAlign.Center
                         )
                     )*/
                }
            }
        }
    }

    companion object {
        const val STEP_ML = 250
        const val DEFAULT_GOAL_ML = 4000

        fun formatValue(currentMl: Int, goalMl: Int): String {
            val currentL = currentMl / 1000f
            val goalL = goalMl / 1000f
            return String.format("%.2f/%.0fL", currentL, goalL)
        }
    }
}

object PrefKeys {
    val CurrentMl = intPreferencesKey("water_current_ml")
    val GoalMl = intPreferencesKey("water_goal_ml")
}

class AdjustWaterCallback : ActionCallback{
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val deltaStr = parameters[DeltaKey] ?: return
        val delta = deltaStr.toIntOrNull() ?: return

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[PrefKeys.CurrentMl] ?: 0
            val goal = (prefs[PrefKeys.GoalMl] ?: WaterIntakeWidget.DEFAULT_GOAL_ML)

            // TODO: Replace with your API call and assign the returned values.
            // Example: val result = repository.updateWater(delta)
            val newCurrent = (current + delta).coerceIn(0, goal)

            prefs.toMutablePreferences().apply {
                this[PrefKeys.CurrentMl] = newCurrent
                this[PrefKeys.GoalMl] = goal
            }
        }

        WaterIntakeWidget().update(context, glanceId)
    }

    companion object {
        val DeltaKey: ActionParameters.Key<String> = ActionParameters.Key("delta")
    }
}

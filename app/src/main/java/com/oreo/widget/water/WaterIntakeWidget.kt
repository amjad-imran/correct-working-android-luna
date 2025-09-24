package com.oreo.widget.water

import android.content.Context
import com.google.gson.JsonObject
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.ImageProvider
import androidx.glance.currentState
import androidx.glance.action.clickable
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import kotlin.math.roundToInt

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
        val isMetric = prefs[PrefKeys.IsMetric] ?: true
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
                        .padding(top = 32.dp, start = 20.dp, end = 20.dp)
                )

                Spacer(GlanceModifier.height(6.dp))

                Text(
                    text = formatValue(currentMl, goalMl, isMetric),
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth().fillMaxHeight()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Vertical.Bottom,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(
                            actionRunCallback<AdjustWaterCallback>(
                                parameters = actionParametersOf(
                                    AdjustWaterCallback.IncrementKey to false
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_water_minus),
                        contentDescription = "Minus",
                        modifier = GlanceModifier.size(32.dp)
                    )
                }
                Spacer(GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(
                            actionRunCallback<AdjustWaterCallback>(
                                parameters = actionParametersOf(
                                    AdjustWaterCallback.IncrementKey to true
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_water_plus),
                        contentDescription = "Plus",
                        modifier = GlanceModifier.size(32.dp)
                    )
                }
            }
        }
    }

    companion object {
        const val STEP_ML = 250
        const val DEFAULT_GOAL_ML = 3000

        fun formatValue(currentMl: Int, goalMl: Int, isMetric: Boolean): String {
            val hydrationText = StringBuilder()
            if (isMetric) {
                hydrationText.append((currentMl.toFloat() / 1000))
                hydrationText.append("/")
                hydrationText.append((goalMl.toFloat() / 1000))
                hydrationText.append("L")
            } else {
                val convertedHydrate = currentMl.toFloat() * 0.033814
                hydrationText.append(String.format("%.1f", convertedHydrate))
                hydrationText.append("/")

                val convertedHydrateGoal =
                    convertMlToOuncesRounded(goalMl.toDouble())

                hydrationText.append("$convertedHydrateGoal")
                hydrationText.append("oz")
            }

            return hydrationText.toString()
        }

        fun convertMlToOuncesRounded(milliliters: Double): Int {
            val ounces = milliliters / 29.5735
            val roundedOunces = (ounces / 10).roundToInt() * 10
            return roundedOunces
        }
    }
}

object PrefKeys {
    val CurrentMl = intPreferencesKey("water_current_ml")
    val IsMetric = booleanPreferencesKey("is_metric")
    val GoalMl = intPreferencesKey("water_goal_ml")
}

class AdjustWaterCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val incrementKey = parameters[IncrementKey] ?: return

        var current = 0
        var goal = WaterIntakeWidget.DEFAULT_GOAL_ML
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        current = prefs[PrefKeys.CurrentMl] ?: 0
        goal = (prefs[PrefKeys.GoalMl] ?: WaterIntakeWidget.DEFAULT_GOAL_ML)

        if (!incrementKey && current == 0) {
            return
        }

        val updated = (current + if (incrementKey) WaterIntakeWidget.STEP_ML else -WaterIntakeWidget.STEP_ML)

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[PrefKeys.CurrentMl] = updated
                this[PrefKeys.GoalMl] = goal
            }
        }

        WaterIntakeWidget().update(context, glanceId)

        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WaterWidgetEntryPoint::class.java)
        val repository: OreoUserActivityRepository = entryPoint.userActivityRepository()

        val reqObj = JsonObject().apply {
            addProperty("hydration_amount", updated)
            addProperty("date", LocalDate.now().toString())
        }

        repository.updateHydration(reqObj).collect { resource ->
            when (resource) {
                is Resource.Success -> {

                }
                else -> { }
            }
        }
    }

    companion object {
        val IncrementKey: ActionParameters.Key<Boolean> = ActionParameters.Key("increment")
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WaterWidgetEntryPoint {
    fun userActivityRepository(): OreoUserActivityRepository
}

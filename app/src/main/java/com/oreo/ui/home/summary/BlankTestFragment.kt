package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BlankTestFragment : Fragment() {

    @Inject
    lateinit var watchDataStore: WatchDataStore

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_blank_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            while (true) {
                val remainingTime     = watchDataStore.getBatteryRemainingTime()
                val fullyChargedTime  = watchDataStore.getBatteryFullyChargeTime()
                val fitnessAge        = watchDataStore.getFitnessAge()
                val workout           = watchDataStore.getWorkout()

                withContext(Dispatchers.Main) {

                    // ── BATTERY ────────────────────────────────────────────
                    view.findViewById<TextView>(R.id.tvRemainingBatteryTime).text =
                        "$remainingTime%"
                    view.findViewById<ProgressBar>(R.id.batteryProgressBar).progress =
                        remainingTime.coerceIn(0, 100)
                    view.findViewById<TextView>(R.id.tvSleepDuration).text =
                        "${remainingTime / 2} min"
                    view.findViewById<TextView>(R.id.tvFullyChargedTime).text =
                        "Fully Charged Time: $fullyChargedTime"

                    // ── HRV / FITNESS AGE ──────────────────────────────────
                    view.findViewById<TextView>(R.id.tvExtra2).text = "$fitnessAge"

                    // ── WORKOUT CHIPS — every field in RecordedWorkoutData ─
                    fun tv(id: Int) = view.findViewById<TextView>(id)

                    tv(R.id.tvWorkoutId).text            = workout?.id?.toString()           ?: "--"
                    tv(R.id.tvWorkoutIsSynced).text       = workout?.isSynced?.toString()     ?: "--"
                    tv(R.id.tvWorkoutIsAccepted).text     = workout?.isAccepted?.toString()   ?: "--"
                    tv(R.id.tvWorkoutDuration).text       = workout?.duration?.toString()     ?: "--"
                    tv(R.id.tvWorkoutDurationSeconds).text= workout?.durationSeconds?.toString() ?: "--"
                    tv(R.id.tvWorkoutType).text           = workout?.type?.toString()         ?: "--"
                    tv(R.id.tvWorkoutType2).text          = workout?.type?.toString()         ?: "--"
                    tv(R.id.tvWorkoutIntensity).text      = workout?.intensity?.toString()    ?: "--"
                    tv(R.id.tvWorkoutCalories).text       = workout?.calories?.toString()     ?: "--"
                    tv(R.id.tvWorkoutSteps).text          = workout?.steps?.toString()        ?: "--"
                    tv(R.id.tvWorkoutStartTime).text      = workout?.startTime
                        ?.let { timeFormat.format(Date(it)) } ?: "--"
                    tv(R.id.tvWorkoutEndTime).text        = workout?.endTime
                        ?.let { timeFormat.format(Date(it)) } ?: "--"
                    tv(R.id.tvWorkoutCadence).text        = workout?.cadence?.toString()      ?: "--"
                    tv(R.id.tvWorkoutDistance).text       = workout?.distance?.toString()     ?: "--"
                    tv(R.id.tvWorkoutRecovery).text       = workout?.recoveryTime?.toString() ?: "--"
                    tv(R.id.tvWorkoutFitnessAge).text     = workout?.fitnessAge?.toString()   ?: "--"
                    tv(R.id.tvWorkoutEnergyConsumption).text = workout?.energyConsumption?.toString() ?: "--"
                    tv(R.id.tvWorkoutDate).text           = workout?.date                     ?: "--"
                    tv(R.id.tvWorkoutHrData).text         = workout?.hrData                   ?: "--"
                    tv(R.id.tvWorkoutIntensityList).text  = workout?.intensityList            ?: "--"
                }

                delay(2000L)
            }
        }
    }
}
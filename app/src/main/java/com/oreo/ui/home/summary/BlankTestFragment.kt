package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.delay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BlankTestFragment : Fragment() {

    @Inject
    lateinit var watchDataStore: WatchDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_blank_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            while (true) {
                val remainingTime = watchDataStore.getBatteryRemainingTime()
                val fullyChargedTime = watchDataStore.getBatteryFullyChargeTime()
                val fitnessAge = watchDataStore.getFitnessAge()
                val energyConsumption = watchDataStore.getEnergyConsumption()
                withContext(Dispatchers.Main){
                    view.findViewById<TextView>(R.id.tvRemainingBatteryTime).text =
                        "Remaining Battery Time: $remainingTime"

                    view.findViewById<TextView>(R.id.tvSleepDuration).text =
                        "Remaining Days: ${remainingTime/2}"

                    view.findViewById<TextView>(R.id.tvFullyChargedTime).text =
                        "Fully Charged Time: $fullyChargedTime"

                    view.findViewById<TextView>(R.id.tvExtra1).text =
                        "Fitness Age: $fitnessAge"

                    view.findViewById<TextView>(R.id.tvExtra2).text =
                        "Enery Expenditure: $energyConsumption"

                    view.findViewById<TextView>(R.id.tvExtra3).text =
                        "WorkOut:"

                    view.findViewById<TextView>(R.id.tvExtra3Desc).text =
                        "${watchDataStore.getWorkout()}"
                }
                delay(2000L)
            }
        }
    }
}
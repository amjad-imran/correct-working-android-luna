package com.oreo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowTestStepsDataBinding
import com.noisefit.luna.databinding.TestOreoStepsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TestDataActivityFragment :
    BaseFragment<TestOreoStepsBinding>(TestOreoStepsBinding::inflate) {

    @Inject
    lateinit var oreoDataRepository: OreoUserActivityRepository

    private val userDataLive = MutableLiveData<List<Any>>()
    private val args: TestDataActivityFragmentArgs by navArgs()
    private val adapter: TestStepsDataAdapter by lazy {
        TestStepsDataAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = args.data.type.name

        binding.rvData.layoutManager = LinearLayoutManager(requireContext())
        binding.rvData.adapter = adapter

        getData(args.data)
    }


    fun getData(data: TestUserData) {
        GlobalScope.launch(Dispatchers.IO) {
            val userActivities = oreoDataRepository.getTestDataListByType(data)
            userDataLive.postValue(userActivities)
        }
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        userDataLive.observe(this) {

            if (args.data.type == DataType.SLEEP) {
                val data = (it as List<OreoSleepData>).firstOrNull()
                data?.let { steps ->
                    setUI(steps)
                }
            } else if (args.data.type == DataType.ACTIVITY) {
                val data = (it as List<OreoStepsData>).firstOrNull()
                data?.let { steps ->
                    setUI(steps)
                }
            }


        }

    }

    private fun setUI(dayData: OreoSleepData) {

        val (hourTotal, minuteTotal) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
            dayData.total
        )
        val (hourTB, minuteTb) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
            dayData.timeInBedTime
        )

        val data = "Total Sleep : $hourTotal h $minuteTotal m\n" +
                "Time in bed : $hourTB h $minuteTb m\n" +
                "Efficiency ${dayData.sleepEfficiency}\n" +
                "SleepLatency ${dayData.sleepLatency}\n" +
                "Sleep Start Time ${dayData.startTime}\n" +
                "Sleep End Time ${dayData.endTime}\n" +
                "total_deep ${dayData.deep}\n" +
                "total_light ${dayData.light}\n" +
                "total_awake ${dayData.awake}\n" +
                "rem_count ${dayData.remCount}\n"

        binding.tvData.text = data


        //adapter.setDataSet(generateData(steps ?: ArrayList()))

    }

    private fun setUI(steps: OreoStepsData) {

        val data = "Total steps : ${steps.totalSteps}\n" +
                "Total calories : ${steps.totalCalories}\n" +
                "Total distance : ${steps.totalDistance}\n"

        binding.tvData.text = data




        adapter.setDataSet(generateData(steps.stepArray ?: ArrayList()))

    }

    fun generateData(oreoStepDataBreakups: java.util.ArrayList<OreoStepsData.OreoStepDataBreakup>): List<Pair<String, String>> {

        val response = ArrayList<Pair<String, String>>()
        oreoStepDataBreakups.forEach {
            response.add(
                Pair(
                    "${it.hourOfTheDay} - ${((it.hourOfTheDay ?: 0) + 1)}",
                    "Steps : ${it.steps}\nDistance : ${it.distance}\nCalories : ${it.calories}"
                )
            )
        }
        return response
    }


}

class TestStepsDataAdapter() :
    RecyclerView.Adapter<TestStepsDataAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<Pair<String, String>>()

    inner class ViewHolder(val binding: RowTestStepsDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(testUserData: Pair<String, String>) {

            binding.tvTime.text =
                testUserData.first
            binding.tvData.text =
                testUserData.second

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowTestStepsDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])

    }

    fun setDataSet(dataSet: List<Pair<String, String>>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

}
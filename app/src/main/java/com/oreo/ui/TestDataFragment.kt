package com.oreo.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTestDataBinding
import com.noisefit.luna.databinding.RowTabsBinding
import com.noisefit.luna.databinding.TestUserDataBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.TestUserData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class TestDataFragment : BaseFragment<FragmentTestDataBinding>(FragmentTestDataBinding::inflate) {

    @Inject
    lateinit var oreoDataRepository: OreoUserActivityRepository

    @Inject
    lateinit var sessionManager: SessionManager

    private val userDataLive = MutableLiveData<List<TestUserData>>()

    private val adapter: TestDataAdapter by lazy {
        TestDataAdapter {

            if (it.type == DataType.ACTIVITY || it.type == DataType.SLEEP) {
                navigate(R.id.testDataActivityFragment, Bundle().apply {
                    this.putParcelable("data", it)
                })
            } else {
                navigate(R.id.testDataListFragment, Bundle().apply {
                    this.putParcelable("data", it)
                })
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvData.layoutManager = LinearLayoutManager(requireContext())
        binding.rvData.adapter = adapter

        binding.toolbar.tvTitle.text = "Ring Data"

        binding.tvLastSync.text = "last sync ${
            sessionManager.getLastSyncTime()?.let { DateFormats.getRelativeTime(it) }
        }"


        getData()
    }


    fun getData() {
        GlobalScope.launch(Dispatchers.IO) {
            val userActivities = oreoDataRepository.getTestData()
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
            adapter.setDataSet(it)
        }

    }


}



enum class DataType {
    HEART_RATE, HRV, BLOOD_OXYGEN, RESPIRATORY, TEMP, SLEEP, ACTIVITY
}

class TestDataAdapter(val listener: (data: TestUserData) -> Unit) :
    RecyclerView.Adapter<TestDataAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<TestUserData>()

    inner class ViewHolder(val binding: TestUserDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(testUserData: TestUserData) {
            binding.tvData1.text = if (testUserData.time.isNullOrEmpty()) {
                testUserData.type.name
            } else {
                testUserData.time
            }
            binding.tvData2.text = testUserData.data

            binding.root.setOnClickListener {
                listener.invoke(testUserData)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TestUserDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])

    }

    fun setDataSet(dataSet: List<TestUserData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

}
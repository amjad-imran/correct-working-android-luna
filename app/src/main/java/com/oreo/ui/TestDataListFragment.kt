package com.oreo.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentTestDataBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.tryCatch
import com.oreo.data.model.TestUserData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TestDataListFragment :
    BaseFragment<FragmentTestDataBinding>(FragmentTestDataBinding::inflate) {

    @Inject
    lateinit var oreoDataRepository: OreoUserActivityRepository

    private val userDataLive = MutableLiveData<List<Any>>()
    private val args: TestDataListFragmentArgs by navArgs()

    private val adapter: TestDataAdapter by lazy {
        TestDataAdapter {

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.rvData.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvData.adapter = adapter


        binding.toolbar.tvTitle.text = args.data.type.name


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
            tryCatch {
                adapter.setDataSet(it as List<TestUserData>)
            }
        }

    }


}
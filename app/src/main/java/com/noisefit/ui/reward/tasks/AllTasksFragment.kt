package com.noisefit.ui.reward.tasks

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.R
import com.noisefit_commans.data.model.TaskList
import com.noisefit.databinding.FragmentAllTasksBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.TaskEnums
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllTasksFragment : BaseFragment<FragmentAllTasksBinding>(FragmentAllTasksBinding::inflate) {
    private val mViewModel: AllTaskViewModel by viewModels()
    private val mainActivityViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var watchesSDK: WatchesSDK

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private val mAdapter: AllTasksAdapter by lazy {
        AllTasksAdapter(object : OnCollectClickListener {
            override fun onCollectClick(taskData: TaskList, position: Int) {
                if (taskData.prediction_id==null) {
                    taskData.transactionId?.let {
                        mViewModel.collectCoupon(it) { coins ->
                            mViewModel.updateCoins(coins)
                            mAdapter.removeItem(position)
                            updateListUI()
                            showCollectAnimation(taskData, position)
                        }
                    }
                } else {
                    taskData.prediction_id?.let {
                        mViewModel.collectNplCoupon(it) { coins ->
                            mViewModel.updateCoins(coins)
                            mAdapter.removeItem(position)
                            updateListUI()
                            showCollectAnimation(taskData, position)
                        }
                    }
                }
            }

            override fun onItemClicked(taskData: TaskList?) {
                if (taskData == null) {
                    navigate(R.id.stepStreakFragment)
                    return
                }
                handleNavigation(taskData)
            }
        })
    }

    private fun updateListUI() {
        val adapterSize = mAdapter.itemCount
        if (adapterSize == 0) {
            mAdapter.setDataSet(
                arrayListOf(
                    TaskList(
                        transactionId = null,
                        points = null,
                        title = null,
                        status = null,
                        taskEnum = null
                    )
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.ALL_TASK_LANDING_PAGE_VISIT)
        mViewModel.getTaskListData()
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvAllTask) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    override fun initListener() {
        binding.vTouchBlock.setOnClickListener {

        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.vCoinsBack.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        mViewModel.taskList.observe(this) {
            mAdapter.setDataSet(it)
            updateListUI()
        }

        mViewModel.userCoins.observe(this) {
            binding.tvCoins.text = it.toString()
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    fun handleNavigation(taskData: TaskList) {
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.ALL_TASKS_ITEM_CLICK,
            HashMap<String, Any>().apply {
                this["task_name"] = taskData.title ?: ""
            })
        when (taskData.taskEnum) {
            TaskEnums.WATCH_PAIR.type -> {
                if (localDataStore.getConnectedDevice() == null) {
                    startActivity(PairDeviceActivity.getStartIntent(requireContext(),true))
                }
            }
            TaskEnums.PROFILE.type -> {
                startActivity(GuestProfileSetupActivity.getStartIntent(requireContext()))
            }

            TaskEnums.CUSTOM_WATCHFACE.type -> {
                if (localDataStore.getConnectedDevice() == null) {
                    context.showShortToast(getString(R.string.text_no_device_paired))
                    return
                }

                if (watchesSDK.hasCategoryWatchFace()) {
                    navigate(R.id.watchface2CategoryFragment)
                } else {
                    navigate(R.id.watchFaceListingFragment)
                }
            }
            TaskEnums.CHALLENGE_PARTICIPATION.type -> {
                mainActivityViewModel.navigateTo(BottomNavOption.EXPLORE)
            }
            TaskEnums.FRIEND_ADDED.type -> {
                navigate(R.id.addFriendsFragment)
            }
            TaskEnums.SHARE.type -> {
                navigate(R.id.navigation_activity)
            }
            else -> {
                taskData.message?.let {
                    if (it.isEmpty()) return@let
                    navigate(
                        R.id.singleActionBottomSheet,
                        bundle = bundleOf("title" to (taskData.title ?: ""), "description" to it)
                    )
                }
            }
        }

    }

    private fun showCollectAnimation(taskData: TaskList, position: Int) {

        binding.vAnimCollect.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.ALL_TASKS_COLLECT_COIN_CLICK,
                    HashMap<String, Any>().apply {
                        this["task_name"] = taskData.title ?: ""
                    })


                binding.vTouchBlock.gone()
                binding.vAnimCollect.gone()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        binding.vTouchBlock.visible()
        binding.vAnimCollect.visible()
        binding.vAnimCollect.setAnimation(R.raw.anim_coins)
        binding.vAnimCollect.playAnimation()
        binding.vAnimCollect.repeatCount = 0
    }


}
package com.noisefit.ui.reward.coin

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CouponList
import com.noisefit_commans.data.model.RewardProfileData
import com.noisefit_commans.data.model.TaskList
import com.noisefit.luna.databinding.FragmentCoinBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.ui.reward.tasks.AllTasksAdapter
import com.noisefit.ui.reward.tasks.OnCollectClickListener
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.TaskEnums
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CoinFragment : BaseFragment<FragmentCoinBinding>(FragmentCoinBinding::inflate) {
    private val mViewModel: CoinViewModel by viewModels()

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var watchesSDK: WatchesSDK

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private val mAdapterTask: AllTasksAdapter by lazy {
        AllTasksAdapter(object : OnCollectClickListener {
            override fun onCollectClick(taskData: TaskList, position: Int) {
                if (taskData.prediction_id==null) {
                    taskData.transactionId?.let {
                        mViewModel.collectCoupon(it) { coins ->
                            mViewModel.updateCoins(coins)
                            mAdapterTask.removeItem(position)
                            showCollectAnimation(taskData, position)
                        }
                    }
                } else {
                    taskData.prediction_id?.let {
                        mViewModel.collectNplCoupon(it) { coins ->
                            mViewModel.updateCoins(coins)
                            mAdapterTask.removeItem(position)
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
        val adapterSize = mAdapterTask.itemCount
        if (adapterSize == 0) {
            mAdapterTask.setDataSet(
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

    private fun showCollectAnimation(taskData: TaskList, position: Int) {
        binding.vAnimCollect.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {

                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.REWARD_COLLECT_COIN_CLICK,
                    HashMap<String, Any>().apply {
                        this["task_name"] = taskData.title ?: ""
                    })
                binding.vTouchBlock.gone()
                binding.vAnimCollect.gone()
                if (mAdapterTask.itemCount == 0) {
                    mViewModel.getRewardProfileData()
                }

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

    private val mAdapterEarning: CoinEarningAdapter by lazy {
        CoinEarningAdapter(object : CoinEarningAdapter.OnBannerClickListener {
            override fun onBannerClick(resultData: CouponList) {
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.REWARD_ITEM_DEAL_CLICK,
                    HashMap<String, Any>().apply {
                        this["title"] = resultData.title ?: ""
//                        this["brand"] = resultData.brand ?: "" //todo this will add later
                    })
                navigate(R.id.voucherDetailsFragment, Bundle().apply {
                    putString("comeFrom", "coupon")
                    putString("title", resultData.title)
                    putInt("id", resultData.id ?: -1)
                })
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_LANDING_PAGE_VISIT)
        setRecycler()
        mViewModel.getRewardProfileData()

        if (!mViewModel.localDataStore.isCoinsWalkAroundShown()) {
            navigate(CoinFragmentDirections.actionCoinFragmentToCoinsWalkAroundBottomDialogFragment())
        }
    }

    private fun setRecycler() {
        with(binding.rvTasks) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapterTask
        }
        with(binding.rvEarning) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapterEarning
        }

    }

    override fun initListener() {
        binding.vTouchBlock.setOnClickListener {

        }
        binding.lytToolbar.tvTitle.text = getString(R.string.text_coins_page_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvTransHistory.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_TRANSACTION_HISTORY_CLICK)
            navigate(CoinFragmentDirections.actionCoinFragmentToTransactionHistoryFragment())
        }
        binding.tvAboutCoins.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARDS_ABOUT_COIN_CLICK)
            navigate(R.id.aboutRewardsFragment)
        }
        binding.ivTaskMore.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_ALL_TASKS_CLICK)
            navigate(CoinFragmentDirections.actionCoinFragmentToTransactionTaskFragment())
        }
        binding.ivEarnMore.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_ALL_DEALS_CLICK)
            navigate(CoinFragmentDirections.actionCoinFragmentToAllDealsFragment())
        }
        binding.lytToolbar.root.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_VOUCHER_PURCHASED_ICON_CLICK)
            navigate(CoinFragmentDirections.actionCoinFragmentToMyVoucherFragment())
        }

    }

    override fun subscribeObservers() {
        mViewModel.rewardProfileData.observe(this) {
            if (it != null) {
                updateUI(it)
            }
        }

        mViewModel.userCoins.observe(this) {
            binding.tvEarnedCoins.text = it.toString()
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }


    private fun updateUI(it: RewardProfileData) {
        binding.lytContainer.visible()
        mAdapterTask.setDataSet(it.taskList ?: ArrayList())
        updateListUI()
        binding.lytToolbar.tvCoins.text = (it.voucherCount ?: 0).toString()
        if (it.couponList.isNullOrEmpty()) {
            mAdapterEarning.setDataSet(mViewModel.getEmptyCoinData())
        } else {
            mAdapterEarning.setDataSet(it.couponList!!)
        }

    }

    fun handleNavigation(taskData: TaskList) {
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.REWARD_TASK_ITEM_CLICK,
            HashMap<String, Any>().apply {
                this["task_name"] = taskData.title ?: ""
            })
        when (taskData.taskEnum) {
            TaskEnums.WATCH_PAIR.type -> {
                if (localDataStore.getConnectedDevice() == null) {
                    startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
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


}
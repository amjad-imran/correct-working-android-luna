package com.noisefit.ui.friends

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.R

import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.FriendProgress
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.databinding.FragmentFriendsListBinding
import com.noisefit.databinding.LayoutPopUpEmojiBinding
import com.noisefit.databinding.LayoutReactionPopUpEmojiBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.ui.friends.compete.FriendSharedViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.common.upTo2Decimal
import com.noisefit_commans.models.DurationRange
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.utils.prettyCountDecimal
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter
import com.noisefit_commans.ui.BaseFragment


@AndroidEntryPoint
class FriendsListFragment :
    BaseFragment<FragmentFriendsListBinding>(FragmentFriendsListBinding::inflate) {

    private var isDimViewShown = false

    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null

    private var reactionPopupWindow: PopupWindow? = null

    private val viewModel: FriendListViewModel by viewModels()
    private val sharedViewModel: FriendSharedViewModel by activityViewModels()


    private val mAdapter: FriendListAdapter by lazy {
        FriendListAdapter(object : OnFriendsItemClickListener {

            override fun onItemEmojiClick() {
                viewModel.getUserFriendEmojiData()
            }

            override fun onItemClick(friend: FriendProgress) {
                LOGS.d("FriendListFragment", "open_item_click")

                if (isDimViewShown) {
                    return
                }
                if (friend.firstName.equals("me", true)) {
                    navigate(
                        FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment()
                            .apply {
                                this.friendId = -1
                            })
                } else {
                    navigate(
                        FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment()
                            .apply {
                                this.friendId = friend.user_id
                            })
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onItemLongClick(
                view: View,
                pos: Int,
                context: Context,
                friend: FriendProgress
            ) {
                nullableBinding?.rvFriendList?.setOnTouchListener { view, event ->
                    return@setOnTouchListener true
                }
                showPopup(view, friend, pos, false)
            }

        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startDate.value = DateFormats.getCurrentDateYMDFormat()
        viewModel.endDate.value = DateFormats.getCurrentDateYMDFormat()
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)


        viewModel.getFriendListData(false)

        setRecycler()


    }

    private fun setRecycler() {
        with(binding.rvFriendList) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(
        anchor: View,
        friendProgress: FriendProgress,
        position: Int,
        isDemoView: Boolean
    ) {
        popupView = anchor
        var offset = 168
        if (isDemoView) {
            offset = 128
        }
        nullableBinding?.let {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth: Int = displayMetrics.widthPixels

            val binding =
                LayoutPopUpEmojiBinding.inflate(
                    LayoutInflater.from(anchor.context),
                    null,
                    false
                )

            if (isDemoView) {
                binding.emojiContainer.invisible()
                binding.tvHoldText.visible()
            } else {
                binding.tvHoldText.invisible()
                binding.emojiContainer.visible()
            }
            this.popupWindow = PopupWindow(anchor.context).apply {
                isOutsideTouchable = true
                width = screenWidth

                contentView = binding.root.apply {
                    measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                }
                setBackgroundDrawable(BitmapDrawable())
            }.also { popupWindow ->
                val location = IntArray(2).apply {
                    anchor.getLocationOnScreen(this)
                }

                LOGS.d("showPopup loc ${location[0]} ${location[1]}")
                popupWindow.setOnDismissListener {
                    delay(100L) {
                        isDimViewShown = false
                    }
                    popupView?.alpha = 1f
                    nullableBinding?.rvFriendList?.setOnTouchListener(null)
                }


                popupWindow.showAtLocation(
                    anchor,
                    Gravity.TOP or Gravity.START,
                    (location[0]),
                    location[1] - offset
                )
                popupView?.alpha = 0f
                isDimViewShown = true
                if (isDemoView) {
                    popupWindow.dimBehind80()
                } else {
                    popupWindow.dimBehind()
                }

                setPopupWindowData(anchor, binding, friendProgress, position)
                handleEmojiView(position, binding)
//56//210
                //       this.binding.scrollViewMain.isNestedScrollingEnabled = false
                LOGS.d("showPopup__POP_WINDOW ${popupWindow.height}")
                popupWindow.contentView.setOnLongClickListener {
                    binding.tvHoldText.invisible()
                    binding.emojiContainer.visible()
                    popupWindow.dimBehind()

                    true
                }
            }
        }

    }

    private fun showReactionPopup(
        anchor: View,
        lottie: Int
    ) {

        nullableBinding?.let {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth: Int = displayMetrics.widthPixels
            val screenHeight: Int = displayMetrics.heightPixels

            //56//210
            LOGS.d("showPopup $screenWidth $screenHeight")

            val binding =
                LayoutReactionPopUpEmojiBinding.inflate(
                    LayoutInflater.from(anchor.context),
                    null,
                    false
                )


            this.reactionPopupWindow = PopupWindow(anchor.context).apply {
                isOutsideTouchable = true
                width = screenWidth

                contentView = binding.root.apply {
                    measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                }
                setBackgroundDrawable(BitmapDrawable())
            }.also { popupWindow ->
                val location = IntArray(2).apply {
                    anchor.getLocationOnScreen(this)
                }

                LOGS.d("showPopup_____ loc ${location[0]} ${location[1]}")
                popupWindow.setOnDismissListener {
                    reactionPopupWindow?.dismiss()
                }


                popupWindow.showAtLocation(
                    anchor,
                    Gravity.TOP or Gravity.START,
                    (location[0]),
                    location[1] - 260
                )

                playReaction(binding, lottie)


            }
        }

    }

    private fun playReaction(binding: LayoutReactionPopUpEmojiBinding, lottie: Int) {

        binding.lottieBackAnim.apply {
            repeatCount = 0
            setAnimation(lottie)
            playAnimation()
            addAnimatorListener(object :
                Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    reactionPopupWindow?.dismiss()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {

                }

            })
        }


    }

    private fun dismissPopupWindow() {
        popupWindow?.dismiss()
    }


    private fun handleEmojiView(
        position: Int,
        binding: LayoutPopUpEmojiBinding
    ) {

        val reaction = viewModel.friendList.value?.get(position)?.userEmoji ?: return


        when (reaction) {
            Emoji.EmojiHeart.emoji -> {

                binding.ivGiveEmojiHeart.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmoji100.background = null

            }
            Emoji.EmojiHand.emoji -> {
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmoji100.background = null
                binding.ivGiveEmojiStrong.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }
            Emoji.EmojiFire.emoji -> {
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmoji100.background = null
                binding.ivGiveEmojiFire.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }
            Emoji.Emoji100.emoji -> {
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmoji100.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }


        }

    }


    private fun setPopupWindowData(
        anchor: View,
        binding: LayoutPopUpEmojiBinding,
        friendProgress: FriendProgress,
        position: Int
    ) {
        var caloriesProgress = friendProgress.calories.toFloat()
        val caloriesGoal = friendProgress.caloriesGoal.toFloat()
        caloriesProgress = (if (caloriesProgress > caloriesGoal) 100f else
            caloriesProgress.calculatePercentage(
                caloriesGoal
            ))
        var stepsProgress = friendProgress.steps.toFloat()
        val stepsGoal = friendProgress.stepGoal.toFloat()
        stepsProgress = (if (stepsProgress > stepsGoal) 100f else
            stepsProgress.calculatePercentage(
                stepsGoal
            ))

        var distanceProgress = friendProgress.distance.toFloat()
        val distanceGoal = friendProgress.distanceGoal.toFloat()
        distanceProgress = (if (distanceProgress > distanceGoal) 100f else
            distanceProgress.calculatePercentage(
                distanceGoal
            ))

        binding.lytFriendList.apply {
            textViewTitle.text = friendProgress.firstName
            tvCaloriesValue.text = friendProgress.calories.prettyCount()
            tvStepsValue.text = friendProgress.steps.prettyCount()

            tvDistanceValue.text = friendProgress.distance.upTo2Decimal().prettyCountDecimal()

            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.distance_arc_bg, 40f, 16f
                )
            )
            val distanceIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 0f, 100f, R.color.distance_arc, 40f, 16f
                )
            )

            dynamicArcView.addEvent(
                DecoEvent.Builder(distanceProgress)
                    .setDuration(RING_ANIMATION)
                    .setIndex(distanceIndex).build()
            )




            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.steps_arc_bg, 20f, 16f
                )
            )
            val stepIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 0f, 100f, R.color.steps_arc, 20f, 16f
                )
            )
            dynamicArcView.addEvent(
                DecoEvent.Builder(stepsProgress).setIndex(stepIndex).setDuration(RING_ANIMATION)
                    .build()
            )



            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    requireActivity(), 100f, 100f, R.color.calories_arc_bg, 16f
                )
            )
            val caloriesIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    requireActivity(),
                    0f,
                    100f,
                    R.color.calories_arc,
                    16f
                )
            )
            dynamicArcView.addEvent(
                DecoEvent.Builder(caloriesProgress).setDuration(RING_ANIMATION)
                    .setIndex(caloriesIndex).build()
            )

//            caloriesRing.animateProgress(caloriesProgress)
//            stepRing.animateProgress(stepsProgress)
//            distanceRing.animateProgress(distanceProgress)
            friendProgress.imageUrl?.let {
                imageViewProfile.loadImage(
                    requireActivity(),
                    friendProgress.imageUrl,
                    R.drawable.ic_default_profile_image
                )
            }

        }

        binding.ivGiveEmojiFire.setOnClickListener {

            if (binding.ivGiveEmojiFire.background == null) {
                LOGS.d("EMOJI_TYPE fire")
                showReactionPopup(anchor, R.raw.emoji_fire_lottie)
                viewModel.updateFriendList(friendProgress.firstName, position, Emoji.EmojiFire)

            } else {
                viewModel.updateFriendList(friendProgress.firstName, position, null)
            }

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDSPROFILE_EMOJI_CLICK)
            handleEmojiView(position, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmojiStrong.setOnClickListener {
            if (binding.ivGiveEmojiStrong.background == null) {
                showReactionPopup(anchor, R.raw.emoji_strong_lottie)
                viewModel.updateFriendList(friendProgress.firstName, position, Emoji.EmojiHand)
            } else {
                viewModel.updateFriendList(friendProgress.firstName, position, null)
            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDSPROFILE_EMOJI_CLICK)
            handleEmojiView(position, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmoji100.setOnClickListener {
            if (binding.ivGiveEmoji100.background == null) {
                showReactionPopup(anchor, R.raw.emoji_100_lottie)
                viewModel.updateFriendList(friendProgress.firstName, position, Emoji.Emoji100)
            } else {
                viewModel.updateFriendList(friendProgress.firstName, position, null)
            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDSPROFILE_EMOJI_CLICK)
            handleEmojiView(position, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmojiHeart.setOnClickListener {
            if (binding.ivGiveEmojiHeart.background == null) {
                showReactionPopup(anchor, R.raw.emoji_heart_lottie)
                viewModel.updateFriendList(friendProgress.firstName, position, Emoji.EmojiHeart)
            } else {
                viewModel.updateFriendList(friendProgress.firstName, position, null)
            }

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDSPROFILE_EMOJI_CLICK)
            handleEmojiView(position, binding)
            dismissPopupWindow()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        popupView?.alpha = 1f
        reactionPopupWindow?.dismiss()
        isDimViewShown = false
        dismissPopupWindow()
    }

    private fun updateUI(tempListData: ArrayList<FriendProgress>) {
        mAdapter.setDataSet(tempListData)
        binding.lytEmptyList.tvMessage.text =
            getString(R.string.text_friends_that_sweat_together_stay_together)
        binding.lytEmptyList.vExercise.setAnimation(R.raw.anim_add_friends)
        binding.lytEmptyList.vExercise.playAnimation()
        binding.lytEmptyList.vExercise.repeatCount = LottieDrawable.INFINITE

        /*binding.lytEmptyList.vJoin.setAnimation(R.raw.anim_join_challenge)
        binding.lytEmptyList.vJoin.playAnimation()
        binding.lytEmptyList.vJoin.repeatCount = LottieDrawable.INFINITE*/
        sharedViewModel.friendsListSize = tempListData.size
        if (tempListData.isNotEmpty()) {
            binding.view1.visible()
            binding.ivCalender.visible()
            binding.tvToday.visible()
            binding.ivNext.visible()
            binding.include32.root.visible()
            if (viewModel.insightValue.value.isNullOrEmpty()) {
                binding.view2.gone()
                binding.tvDoingBetter.gone()
            } else {
                binding.view2.visible()
                binding.tvDoingBetter.visible()
            }
            binding.rvFriendList.visible()
            binding.lytEmptyList.root.gone()

            nullableBinding?.rvFriendList?.post {
                try {

                    if ((viewModel.friendList.value?.size ?: 0) > 0) {
                        if (!viewModel.localDataStore.isHoldPressPopEmojiViewShown()) {
                            val friendProgress = viewModel.friendList.value?.get(1)
                            val demoPopupView1 = binding.rvFriendList.getChildAt(1)
                            showPopup(demoPopupView1, friendProgress!!, 1, true)
                            viewModel.localDataStore.setHoldPressPopEmojiViewShown(true)
                        }


//                        if (!sharedViewModel.dasda) {
//
//                            sharedViewModel.dasda = true
//                        }
                    }


                } catch (_: Exception) {

                }


            }

        } else {
            binding.view1.gone()
            binding.ivCalender.gone()
            binding.tvToday.gone()
            binding.ivNext.gone()
            binding.view2.gone()
            binding.include32.root.gone()
            binding.rvFriendList.gone()
            binding.lytEmptyList.root.visible()
            binding.tvDoingBetter.gone()
        }
    }

    override fun initListener() {
        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.textSyncingData.visible()
                viewModel.getFriendListData(true)
            }
        })
        binding.lytEmptyList.bAddFriends.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACTIVITY_ADDFRIENDS_CLICK)
            navigate(FriendsFragmentDirections.actionNavigationFriendsFragmentToAddFriendsFragment())
        }
        binding.view1.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACTIVITY_DURATIONFILTER_CLICK)
            requireActivity().supportFragmentManager.setFragmentResultListener(
                VALUE_REQUEST_KEY,
                this@FriendsListFragment
            ) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.setDuration(it1)
                    setDurationValue()
                }

            }
            navigate(
                FriendsFragmentDirections.actionNavigationFriendsFragmentToDurationBottomSheet(
                    viewModel.getDurationValue(),
                    AppStaticData.getDurationValues(),
                    getString(R.string.text_duration)
                )
            )
        }


    }

    private fun setDurationValue() {

        when {
            viewModel.getDurationValue() == DurationRange.PREVIOUS_WEEK.type -> {
                viewModel.startDate.value = DateFormats.getPreviousWeek(-1)[0]
                viewModel.endDate.value = DateFormats.getPreviousWeek(-1)[6]
            }
            viewModel.getDurationValue() == DurationRange.MONTHLY.type -> {
                viewModel.startDate.value = DateFormats.getFirstDayOfMonth()
                viewModel.endDate.value = DateFormats.getLastDayOfMonth()
            }
            viewModel.getDurationValue() == DurationRange.TODAY.type -> {
                viewModel.startDate.value = DateFormats.getCurrentDateYMDFormat()
                viewModel.endDate.value = DateFormats.getCurrentDateYMDFormat()
            }
            viewModel.getDurationValue() == DurationRange.YESTERDAY.type -> {
                viewModel.startDate.value = DateFormats.getPreviousDateYMDFormat()
                viewModel.endDate.value = DateFormats.getPreviousDateYMDFormat()
            }
            else -> {
                viewModel.startDate.value = DateFormats.getPreviousWeek(0)[0]
                viewModel.endDate.value = DateFormats.getPreviousWeek(0)[6]
            }
        }
//        binding.tvToday.text = viewModel.getDurationValue()

        val shouldRefresh = viewModel.getDurationValue() != DurationRange.TODAY.type

        viewModel.getFriendListData(shouldRefresh)
    }

    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            FriendsFragmentDirections.actionNavigationFriendsFragToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    override fun subscribeObservers() {
        viewModel.duration.observe(this) {
            it?.let {
                binding.tvToday.text = viewModel.getDurationValue()
            }
        }


        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                stopRefresh()
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
            }
        }

        viewModel.friendList.observe(this) {
            it?.let {
                stopRefresh()
                binding.tvDoingBetter.text = HtmlCompat.fromHtml(viewModel.insightValue.value ?: "", 0)
                showHideView()
                updateUI(it as ArrayList<FriendProgress>)

                if (sharedViewModel.showReactionSheet) {
                    sharedViewModel.showReactionSheet = false
                    viewModel.getUserFriendEmojiData()
                }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
                stopRefresh()
            }
        }

    }

    fun stopRefresh(){
        binding.swipeToRefresh.refreshComplete()
        binding.textSyncingData.gone()
    }

    private fun showHideView() {
        if (viewModel.insightValue.value.toString().isEmpty()) {
            binding.view2.gone()
            binding.tvDoingBetter.gone()
        } else {
            binding.view2.visible()
            binding.tvDoingBetter.visible()
        }
    }

}
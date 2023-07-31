package com.noisefit.ui.friends.profile

import android.animation.Animator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayout
import com.noisefit.R
import com.noisefit_commans.data.model.CommonFriends
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.FriendProfile
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.databinding.FragmentFriendProfileBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.location.search.CLOSED_SEARCH_STATE_KEY
import com.noisefit.ui.friends.profile.activity.FriendActivityFragment
import com.noisefit.ui.friends.profile.challenges.FriendChallengeFragment
import com.noisefit.ui.friends.profile.timeline.TimelineFragment
import com.noisefit.ui.friends.request.received.*
import com.noisefit.ui.profile.ProfilePicActivity
import com.noisefit.ui.profile.UserType
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter


@AndroidEntryPoint
class FriendProfileFragment :
    BaseFragment<FragmentFriendProfileBinding>(FragmentFriendProfileBinding::inflate) {

    private val viewModel: FriendProfileViewModel by viewModels()
    private val sharedViewModel: FriendProfileSharedViewModel by activityViewModels()
    private val args: FriendProfileFragmentArgs by navArgs()
    private val interestsAdapter: InterestsAdapter by lazy {
        InterestsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            try {
                //https://noisefit.page.link/FriendsProfile?id=59

                val deepLinkKey = it.keySet()?.firstOrNull()
                val uri = ((it.get(deepLinkKey) as Intent).data as Uri)
                val challengeId = uri.getQueryParameter("id")

                if (challengeId == null) navigateUpSafe()

                val parsedId = challengeId?.toIntOrNull()
                if (parsedId == null) {
                    navigateUpSafe()
                }

                viewModel.localDataStore.getUser()?.id?.let { id ->
                    viewModel.isMyProfile = id == parsedId
                }

                viewModel.profileId = parsedId!!.toLong()
                sharedViewModel.userId = parsedId.toLong()


            } catch (exp: Exception) {
                exp.printStackTrace()
                var id = args.friendId

                if (id == viewModel.localUserId) {
                    id = -1
                }

                viewModel.isMyProfile = id == -1
                viewModel.profileId = id.toLong()
                sharedViewModel.userId = id.toLong()
            }
        }
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)



        setTabs()
        setInterestsRecycler()

        viewModel.getFriendProfile(false)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MYPROFILE_TIMELINE_CLICK)
                        loadTimeLine()
                    }

                    1 -> {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_ACTIVITY_CLICK)
                        loadFragment(FriendActivityFragment())
                    }

                    else -> {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_CHALLENGES_CLICK)
                        loadFragment(FriendChallengeFragment())
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }

    private fun setInterestsRecycler() {
        binding.lytFriendHeader.rvInterests.apply {
            layoutManager = FlexboxLayoutManager(context)
            adapter = interestsAdapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        sharedViewModel.clearOldData(null, null)
        sharedViewModel.setActivityData(null, null)
    }

    private fun setTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Timeline"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Activity"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Challenges"))

    }

    private fun loadTimeLine() {

        var friendId: Long = -1L
        if (!viewModel.isMyProfile) {
            friendId = viewModel.profileId

        }
        loadFragment(TimelineFragment.newInstance(friendId))
    }

    private fun initUi(profile: FriendProfile) {
        binding.layoutMain.visible()
        binding.content.visible()
        binding.tvTitle.text = profile.name
        sharedViewModel.name = profile.name
        sharedViewModel.canCompete = profile.canCompete == true

        when (profile.user_type) {
            UserType.Influencer.type -> {
                binding.lytFriendHeader.ivVerified.visible()
            }

            else -> {
                binding.lytFriendHeader.ivVerified.gone()
            }
        }



        context?.let {
            binding.lytFriendHeader.ivProfile.loadImage(
                it, profile.getThumbnail(), R.drawable.ic_default_profile_image
            )
        }

        binding.lytFriendHeader.apply {
            tvFriendsCount.text = "${profile.friends ?: 0}"
            tvBadgesCount.text = "${profile.badges ?: 0}"
            tvPostCount.text = "${profile.post_count ?: 0}"
        }


        if (profile.hasLocationData()) {
            binding.lytFriendHeader.tvCity.text = profile.location?.city ?: ""
        } else {
            binding.lytFriendHeader.tvCity.gone()
        }

        var commonInterests = 0
        profile.interests?.forEach {
            if (it.isCommon == true) {
                commonInterests++
            }
        }

        if (viewModel.isMyProfile) {
            loadTimeLine()
            binding.tabLayout.getTabAt(0)?.select()
            binding.lytFriendHeader.tvUserName.text = viewModel.localDataStore.getUser()?.firstName
            binding.lytFriendHeader.tvCommonInterestsCount.gone()
            binding.lytFriendHeader.lytCommonFriends.root.gone()
            hideProfileAction()
            if (profile.hasLocationData()) {
                binding.lytFriendHeader.tvCity.visible()
            } else {
                binding.lytFriendHeader.tvCity.gone()
            }

            if (profile.interests.isNullOrEmpty()) {

                binding.lytFriendHeader.tvAddInterest.visible()
                binding.lytFriendHeader.tvEmptyInterestMsg.visible()
                binding.lytFriendHeader.rvInterests.gone()
            } else {
                binding.lytFriendHeader.tvAddInterest.gone()
                binding.lytFriendHeader.tvEmptyInterestMsg.gone()
                binding.lytFriendHeader.rvInterests.visible()
            }


        } else {

            binding.lytFriendHeader.tvUserName.text = profile.name
            binding.flFragment.gone()

            if (profile.interests.isNullOrEmpty()) {
                binding.lytFriendHeader.textView54.gone()
                binding.lytFriendHeader.tvAddInterest.gone()
                binding.lytFriendHeader.tvEmptyInterestMsg.gone()
                binding.lytFriendHeader.rvInterests.gone()
                binding.lytFriendHeader.divider.root.gone()
            } else {
                binding.lytFriendHeader.textView54.visible()
                binding.lytFriendHeader.tvAddInterest.gone()
                binding.lytFriendHeader.tvEmptyInterestMsg.gone()
                binding.lytFriendHeader.rvInterests.visible()
                binding.lytFriendHeader.divider.root.visible()
            }

            if (commonInterests != 0) {
                binding.lytFriendHeader.tvCommonInterestsCount.visible()
                binding.lytFriendHeader.tvCommonInterestsCount.text =
                    "($commonInterests Common interests)"
            }
            setCommonFriends(profile.commonFriends)

            if (profile.request_status == FRIEND_STATUS_NONE.toInt() ||
                profile.request_status == FRIEND_STATUS_REJECTED.toInt() ||
                profile.request_status == FRIEND_STATUS_REMOVE.toInt()
            ) {
                showProfileAction()

                binding.lytProfileActions.btnNegative.gone()
                binding.lytProfileActions.btnPositive.visible()
                binding.lytProfileActions.btnPositive.text = "Add Friend"
                updateCountTextColor(ContextCompat.getColor(requireContext(), R.color.white_64))
                makeUiClickable(false)
                binding.ivMenu.gone()
            } else if (profile.request_status == FRIEND_STATUS_ADD.toInt()) {
                showProfileAction()
                makeUiClickable(false)
                binding.ivMenu.gone()
                updateCountTextColor(ContextCompat.getColor(requireContext(), R.color.white_64))
                if (profile.isRequestReceived == true) {
                    binding.lytProfileActions.btnNegative.visible()
                    binding.lytProfileActions.btnPositive.visible()
                    binding.lytProfileActions.btnNegative.text = "Decline"
                    binding.lytProfileActions.btnPositive.text = "Accept"
                } else {
                    binding.lytProfileActions.btnNegative.visible()
                    binding.lytProfileActions.btnPositive.gone()
                    binding.lytProfileActions.btnNegative.text = "Cancel Request"
                }
            } else if (profile.request_status == FRIEND_STATUS_ACCEPT.toInt()) {
                binding.tabLayout.getTabAt(0)?.select()
                hideProfileAction()
                loadTimeLine()
                makeUiClickable(true)
                binding.ivMenu.visible()
                updateCountTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.accent_color_purple
                    )
                )
            }
        }

        profile.interests?.sortedByDescending { it.isCommon }
            ?.let { interestsAdapter.setDataSet(it) }


    }

    private fun hideProfileAction() {
        binding.collapseToolbar.setScrollBehavior(true)
        binding.lytProfileActions.root.gone()
    }

    private fun showProfileAction() {
        binding.collapseToolbar.setScrollBehavior(false)
        binding.lytProfileActions.root.visible()
    }


    private fun makeUiClickable(isClickable: Boolean) {
        binding.lytFriendHeader.viewFriends.isClickable = isClickable
        binding.lytFriendHeader.viewBadges.isClickable = isClickable
        binding.lytFriendHeader.viewPosts.isClickable = isClickable
    }

    private fun updateCountTextColor(color: Int) {
        binding.lytFriendHeader.tvFriendsCount.setTextColor(color)
        binding.lytFriendHeader.tvBadgesCount.setTextColor(color)
        binding.lytFriendHeader.tvPostCount.setTextColor(color)
    }

    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            FriendProfileFragmentDirections.actionFriendProfileFragmentFragToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    override fun initListener() {

        binding.lytFriendHeader.viewFriends.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MYFRIENDLIST_CLICK)
            if (viewModel.isMyProfile)
                navigate(R.id.myFriendList, Bundle().apply {
                    putString("friendId", viewModel.profileId.toString())
                })
            else
                navigate(R.id.friendsFriendFragment, Bundle().apply {
                    putInt("friendId", viewModel.profileId.toInt())
                    putString("name", sharedViewModel.name)
                })
        }
        binding.lytFriendHeader.viewBadges.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MYBADGESLIST_CLICK)
            navigate(R.id.friendBadgeFragment)
        }
        binding.lytFriendHeader.viewPosts.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MYPOSTLIST_CLICK)
            if ((viewModel.friendProfile.value?.post_count
                    ?: 0) > 0
            ) {
                if (binding.tabLayout.selectedTabPosition != 0) {
                    binding.tabLayout.getTabAt(0)?.select()
                }
                binding.layoutMain.setExpanded(false)
            }
        }

        binding.lytFriendHeader.lytCommonFriends.root.setOnClickListener {
            navigate(
                FriendProfileFragmentDirections.actionFriendProfileFragmentToCommonFriendFragment()
                    .apply {
                        this.friendId = viewModel.profileId.toInt()
                    })

        }



        binding.ivMenu.setOnClickListener {
            if (viewModel.isMyProfile) {
                requireActivity().supportFragmentManager.setFragmentResultListener(
                    EDIT_PROFILE,
                    this
                ) { key, bundle ->
                    val isSelected = bundle.getBoolean("allow")
                    val viewGuidelines = bundle.getBoolean("guidelines")
                    if (isSelected) {
                        viewModel.showEditProfile.postValue(Event(true))
                    }
                    if (viewGuidelines) {
                        viewModel.showGuidelines.postValue(Event(true))
                    }
                }

                navigate(
                    FriendProfileFragmentDirections.actionFriendProfileFragmentToBottomProfileOptions(
                        true, sharedViewModel.canCompete
                    )
                )
            } else {

                requireActivity().supportFragmentManager.setFragmentResultListener(
                    COMPETE_NOW,
                    this
                ) { key, bundle ->
                    val isSelected = bundle.getBoolean("allow")
                    if (isSelected) {
                        viewModel.callComputeNow.postValue(Event(true))
                    }
                }
                requireActivity().supportFragmentManager.setFragmentResultListener(
                    REMOVE_FRIEND,
                    this
                ) { key, bundle ->
                    val isSelected = bundle.getBoolean("allow")
                    if (isSelected) {
                        viewModel.callRemoveFriend.postValue(Event(true))

                    }
                }
                navigate(
                    FriendProfileFragmentDirections.actionFriendProfileFragmentToBottomProfileOptions(
                        false, sharedViewModel.canCompete
                    )
                )
            }
        }


        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.swipeToRefresh.refreshComplete()
                viewModel.getFriendProfile(true)
            }
        })


        requireActivity().supportFragmentManager.setFragmentResultListener(
            CLOSED_SEARCH_STATE_KEY, this
        ) { _, bundle ->
            val isClosed = bundle.getBoolean("closed")
            if (isClosed) {
                navigate(FriendProfileFragmentDirections.actionFriendProfileFragmentToLocationBottomSheet())
            }

        }

        binding.lytFriendHeader.tvAddInterest.setOnClickListener {
            setFragmentResultListener(INTEREST_UPDATE_KEY) { _, bundle ->
                viewModel.getFriendProfile(true)
            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_INTERESTEDIT_CLICK)
            navigate(FriendProfileFragmentDirections.actionFriendProfileFragmentToBottomSheetInterestSelector())
        }


        binding.lytFriendHeader.ivProfile.setOnClickListener {
            val imageViewPair =
                androidx.core.util.Pair.create(
                    binding.lytFriendHeader.ivProfile as View,
                    "profilePic"
                )

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), imageViewPair
            )
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACTIVITY_FRIENDSPROFILE_CLICK)
            viewModel.friendProfile.value?.image_url?.let { imageUrl ->
                startActivity(Intent(requireContext(), ProfilePicActivity::class.java).apply {
                    this.putExtra("imageUrl", imageUrl)
                }, options.toBundle())
            }

        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }


        binding.lytProfileActions.btnPositive.setOnClickListener {
            viewModel.friendProfile.value?.let { profile ->
                when (profile.request_status) {
                    FRIEND_STATUS_NONE.toInt(), FRIEND_STATUS_REJECTED.toInt(), FRIEND_STATUS_REMOVE.toInt() -> {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_ADDFRIENDS_CLICK)
                        viewModel.updateFriendRequestStatus(
                            profile.user_id.toInt(), FRIEND_STATUS_ADD_STRING
                        )
                    }

                    FRIEND_STATUS_ADD.toInt() -> {
                        if (profile.isRequestReceived == true) {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_ACCEPTFRIENDS_CLICK)
                            viewModel.updateFriendRequestStatus(
                                profile.user_id.toInt(), FRIEND_STATUS_ACCEPT_STRING
                            )
                        } else {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_REMOVEFRIENDS_CLICK)
                            viewModel.updateFriendRequestStatus(
                                profile.user_id.toInt(), FRIEND_STATUS_REMOVE_STRING
                            )
                        }
                    }
                }
            }

        }

        binding.lytProfileActions.btnNegative.setOnClickListener {
            viewModel.friendProfile.value?.let { profile ->
                if (profile.request_status == FRIEND_STATUS_ADD.toInt()) {
                    if (profile.isRequestReceived == true) {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_DECLINEFRIENDS_CLICK)
                        viewModel.updateFriendRequestStatus(
                            profile.user_id.toInt(), FRIEND_STATUS_REJECTED_STRING
                        )
                    } else if (profile.isRequestReceived == false) {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_REMOVEFRIENDS_CLICK)
                        viewModel.updateFriendRequestStatus(
                            profile.user_id.toInt(), FRIEND_STATUS_REMOVE_STRING
                        )
                    }
                }
            }
        }

    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.navigateUp.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        viewModel.showEditProfile.observe(this) {
            it.getContent()?.let {
                navigate(
                    FriendProfileFragmentDirections.actionFriendProfileFragmentToProfileEditFragment(
                    )
                )
            }
        }
        viewModel.showGuidelines.observe(this) {
            it.getContent()?.let {
                activity?.let { act ->
                    startActivity(WebViewActivity.getStartIntent(
                        act,
                        getString(R.string.text_community_guidelines),
                        AppConstants.URL_FEEDS_GUIDELINES
                    ))
                }
            }
        }
        viewModel.callComputeNow.observe(this) {
            it.getContent()?.let {
                competeConfirmation()
            }
        }
        viewModel.callRemoveFriend.observe(this) {
            it.getContent()?.let {
                removeConfirmation()
            }
        }

        viewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
            }
        }


        viewModel.friendProfile.observe(this) {
            sharedViewModel.userId = it.user_id


            if (viewModel.isAccepted) {
                binding.lytProfileActions.lvAnimUnlock.visible()
                binding.lytProfileActions.imageView3.invisible()
                binding.lytProfileActions.lvAnimUnlock.setAnimation(R.raw.anim_friends_unlock)
                binding.lytProfileActions.lvAnimUnlock.repeatCount = 0
                binding.lytProfileActions.lvAnimUnlock.playAnimation()



                binding.lytProfileActions.lvAnimUnlock.addAnimatorListener(object :
                    Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        hideProfileAction()
                        binding.lytProfileActions.lvAnimUnlock.pauseAnimation()
                        initUi(it)
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                })
            } else {
                initUi(it)
                binding.lytProfileActions.lvAnimUnlock.invisible()
                binding.lytProfileActions.imageView3.visible()
            }


            if (it.activity != null) {
                binding.tabLayout.visible()
                binding.divider11.root.visible()
                sharedViewModel.setActivityData(it.activity.apply {
                    this?.userName = it.name
                }, it.activityMessage)
            } else {
                binding.tabLayout.gone()
                binding.divider11.root.gone()
                sharedViewModel.setActivityData(null, null)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    fun setCommonFriends(commonFriends: CommonFriends?) {
        if (!commonFriends?.image_url.isNullOrEmpty()) {
//            binding.lytFriendHeader.include25.root.visible()
            with(binding.lytFriendHeader.lytCommonFriends) {
                root.visible()
                if (commonFriends?.total == 0) {

                    tvParticipantValue.text =
                        Html.fromHtml("Common friends <b>${commonFriends.first_name}</b>")
                } else {
                    var othersText = "other"
                    if ((commonFriends?.total ?: 1) > 1) {
                        othersText = "others"
                    }

                    tvParticipantValue.text =
                        Html.fromHtml("Common friends <b>${commonFriends?.first_name}</b> and <b>${commonFriends?.total.toString()} $othersText</b>")


                }
                if (commonFriends?.image_url?.size == 1) {
                    imageViewCircle1.visible()
                    imageViewCircle2.gone()
                    imageViewCircle3.gone()
                    imageViewCircle1.loadImage(
                        imageViewCircle1.context,
                        commonFriends.image_url[0],
                        R.drawable.ic_default_profile_image
                    )
                } else if (commonFriends?.image_url?.size == 2) {
                    imageViewCircle1.visible()
                    imageViewCircle2.visible()
                    imageViewCircle3.gone()
                    imageViewCircle1.loadImage(
                        imageViewCircle1.context,
                        commonFriends.image_url[0],
                        R.drawable.ic_default_profile_image
                    )
                    imageViewCircle2.loadImage(
                        imageViewCircle2.context,
                        commonFriends.image_url[1],
                        R.drawable.ic_default_profile_image
                    )
                } else if (commonFriends?.image_url!!.size >= 3) {
                    imageViewCircle1.visible()
                    imageViewCircle2.visible()
                    imageViewCircle3.visible()
                    imageViewCircle1.loadImage(
                        imageViewCircle1.context,
                        commonFriends.image_url[0],
                        R.drawable.ic_default_profile_image
                    )
                    imageViewCircle2.loadImage(
                        imageViewCircle2.context,
                        commonFriends.image_url[1],
                        R.drawable.ic_default_profile_image
                    )
                    imageViewCircle3.loadImage(
                        imageViewCircle3.context,
                        commonFriends.image_url[2],
                        R.drawable.ic_default_profile_image
                    )
                }
            }
        } else {
//            binding.lytFriendHeader.include25.root.gone()
            binding.lytFriendHeader.lytCommonFriends.root.gone()
        }
    }

    private fun competeConfirmation() {
        setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_COMPETENOW_CLICK)
                viewModel.updateCompetitionRequestStatus(viewModel.profileId.toInt() ?: -1, "sent")
            }
        }

        val challengeDescription = viewModel.friendProfile.value?.description

        navigate(
            FriendProfileFragmentDirections.actionFriendProfileFragmentToAlertTextBottomSheet(
                "", challengeDescription ?: "","",""
            )
        )
    }

    private fun removeConfirmation() {
        setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                viewModel.friendProfile.value?.let {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_REMOVEFRIENDS_CLICK)
                    viewModel.updateFriendRequestStatus(
                        it.user_id.toInt(),
                        FRIEND_STATUS_REMOVE_STRING
                    )
                }
            }
        }
        val userName = viewModel.friendProfile.value?.name
        val descriptionText =
            "Are you sure you want to remove\n" + "${if (userName.isNullOrEmpty()) "" else userName} from your friends?"
        navigate(
            FriendProfileFragmentDirections.actionFriendProfileFragmentToAlertTextBottomSheet(
                getString(R.string.text_remove_friend_ques), descriptionText,"",""
            )
        )
    }


    fun loadFragment(fragment: Fragment) {
        binding.flFragment.visible()
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, fragment)
        fragmentTransaction.commit()

    }


}
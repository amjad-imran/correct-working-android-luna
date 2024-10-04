package com.oreo.ui.referral

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.data.model.referral.ReferralInfoResponse
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentReferralBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notify


@AndroidEntryPoint
class ReferralFragment : BaseFragment<FragmentReferralBinding>(FragmentReferralBinding::inflate) {

    private val viewModel: ReferralViewModel by viewModels()
    private val args: ReferralFragmentArgs by navArgs()
    private val indicators = ArrayList<View>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.referralInfo.postValue(args.referralInfo)
    }

    private fun setViewPager(referralInfoResponse: ReferralInfoResponse) {
        val pagerAdapter = ScreenSlidePagerAdapter(childFragmentManager, lifecycle)

        binding.vpMain.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(20))
            })
            adapter = pagerAdapter
        }

        val dataList = viewModel.getCards(referralInfoResponse)
        binding.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(dataList.size, position)
            }
        })
        pagerAdapter.setDataSet(dataList)
        updateIndicators(dataList.size, 0)


        /*binding.vpMain.apply {
            // Reduce the page size to show a partial view of the next item
            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)

            setPageTransformer { page, position ->
                val offset = position * -(2 * offsetPx + pageMarginPx)
                if (position <= 1) {
                    page.translationX = offset
                }
            }

            // Reduce the side padding of the viewpager2 to show partial next view
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.setPadding(offsetPx, 0, offsetPx, 0)
            recyclerView.clipToPadding = false
        }*/
    }

    private fun setupIndicators(count: Int) {
        indicators.clear()
        binding.indicatorLayout.removeAllViews()
        for (i in 0 until count) {
            val indicator = View(this@ReferralFragment.context)
            val params = LinearLayout.LayoutParams(
                R.dimen.indicator_width_active,
                R.dimen.indicator_height
            )
            params.setMargins(8, 0, 8, 0)
            indicator.layoutParams = params
            indicator.setBackgroundResource(R.drawable.indicator_inactive)
            indicators.add(indicator)
            binding.indicatorLayout.addView(indicator)
        }

        if (indicators.isNotEmpty()) {
            indicators[0].setBackgroundResource(R.drawable.indicator_active)
        }
    }

    private fun updateIndicators(count: Int, position: Int) {
        LOGS.d("sdfjhsdkfjhksd $count -  $position")
        indicators.clear()
        binding.indicatorLayout.removeAllViews()
        for (i in 0 until count) {
            val indicator = View(this@ReferralFragment.context)
            val params = LinearLayout.LayoutParams(
                if (position == i) {
                    46f.dpToPixel().toInt()
                } else {
                    16f.dpToPixel().toInt()
                },
                2f.dpToPixel().toInt()
            )
            params.setMargins(4, 0, 4, 0)
            indicator.layoutParams = params
            if (position == i) {
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
            indicators.add(indicator)
            binding.indicatorLayout.addView(indicator)
        }
        binding.indicatorLayout.requestLayout()
    }

    override fun initListener() {
        binding.bClaimCode.setOnClickListener {
            if (viewModel.referralCode.value?.referralCode.isNullOrEmpty()) {
                viewModel.getReferCode()
            } else {
                context?.let { ctx ->
                    viewModel.referralCode.value?.shareMessage?.let {
                        ShareUtil.shareText(ctx, it)
                    }
                }
            }
        }

        binding.tvReferrals.setOnClickListener {
            navigate(R.id.myReferralsFragment)
        }

    }

    override fun subscribeObservers() {
        viewModel.referralInfo.observe(this) {
            setViewPager(it)
            binding.tvReferralTitle.text = it.referralTitle
            binding.tvName.text = "Hello ${viewModel.getUserName()}"

            val remainingDays = it.remainingDays ?: 1

            binding.tvDaysLeft.text = if (remainingDays == 1) {
                "$remainingDays day left"
            } else {
                "$remainingDays days left"
            }
        }


        viewModel.referralCode.observe(this) {
            if (it?.referralCode.isNullOrEmpty()) {
                binding.bClaimCode.text = getString(R.string.text_claim_code)
            } else {
                binding.bClaimCode.text = "Share code \"${it.referralCode}\""
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }


    private inner class ScreenSlidePagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        val fragments = ArrayList<Fragment>()
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]

        fun setDataSet(fragments: List<Fragment>) {
            this.fragments.clear()
            this.fragments.addAll(fragments)
            notifyDataSetChanged()
        }

    }
}
/*

@Composable
fun ReferralFragmentScreen() {
    val viewModel: ReferralViewModel = hiltViewModel()
    val loading by viewModel.getLoading().collectAsState()

    ReferralFragmentMain(
        loading,
        "Claim code"
    )

}

@Composable
fun ReferralFragmentMain(
    loading: Boolean,
    buttonText: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ReferralToolbar(true,
            onBackClicked = {},
            onReferralClicked = {})

        // Column for Scrollable content in the middle and Spacer to take remaining space
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 88.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = "Hello Amit",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0x63f2f1e9),
                style = FontStyle.SIZE_20
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Referral month",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xfff4eded),
                style = FontStyle.SIZE_36
            )

            val pagerState = rememberPagerState(pageCount = {
                10
            })
            HorizontalPager(state = pagerState) { page ->

                Text(
                    text = "Page: $page",
                    modifier = Modifier
                        .background(Color.Cyan)
                        .fillMaxWidth()
                )
            }

        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
        ) {
            ButtonPrimary(buttonText, onClick = {

            })
            Spacer(modifier = Modifier.height(30.dp))
        }


        if (loading) {
            Loading()
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReferralFragmentPreview() {
    ReferralFragmentScreen()
}

@Composable
fun ReferralToolbar(
    showMyReferrals: Boolean,
    onBackClicked: () -> Unit, onReferralClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularBackButton(onClick = onBackClicked)

        if (showMyReferrals) {
            ElipsizeTextButton("My referals", onClick = onReferralClicked)
        } else {
            Spacer(modifier = Modifier.width(38.dp))//width to be same as icon
        }
    }

}

*/

package com.oreo.ui.referral

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.luna.databinding.FragmentReferralBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.referral.type.ReferralPrizeFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReferralFragment : BaseFragment<FragmentReferralBinding>(FragmentReferralBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewPager()
    }

    private fun setViewPager() {
        val pagerAdapter = ScreenSlidePagerAdapter(childFragmentManager, lifecycle)
        binding.vpMain.adapter = pagerAdapter
        pagerAdapter.setDataSet()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


    private inner class ScreenSlidePagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        val fragments = ArrayList<Fragment>()
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]

        fun setDataSet() {
            this.fragments.add(ReferralPrizeFragment())
            this.fragments.add(ReferralPrizeFragment())
            this.fragments.add(ReferralPrizeFragment())
            this.fragments.add(ReferralPrizeFragment())
            this.fragments.add(ReferralPrizeFragment())
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

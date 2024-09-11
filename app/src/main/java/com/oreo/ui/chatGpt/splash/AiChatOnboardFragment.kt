package com.oreo.ui.chatGpt.splash

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiChatOnboardBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.compose.element.button.ButtonBlue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiChatOnboardFragment :
    BaseFragment<FragmentAiChatOnboardBinding>(FragmentAiChatOnboardBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ScreenAiChatOnboard(onButtonClick = {
                    localDataStore.setAiChatSplashShown()
                    navigate(
                        AiChatOnboardFragmentDirections.actionAiChatOnboardFragmentToAiTopQuestionsFragment(
                        )
                    )
                })
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}

@Composable
fun ScreenAiChatOnboard(onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentScale = ContentScale.Crop,
            contentDescription = "",
            painter = painterResource(R.drawable.luna_ai_splash_back)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(164.dp)
            )
            Image(
                modifier = Modifier
                    .height(162.dp)
                    .width(162.dp),
                contentDescription = "",
                painter = painterResource(R.drawable.image_luna_circle)
            )
            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.height(28.dp),
                    contentDescription = "",
                    painter = painterResource(R.drawable.image_luna_ai_logo)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    modifier = Modifier.height(22.dp),
                    contentDescription = "",
                    painter = painterResource(R.drawable.image_luna_ai_version)
                )

            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                contentDescription = "",
                painter = painterResource(R.drawable.image_ai_tag_line)
            )

            Spacer(
                modifier = Modifier.height(54.dp)
            )

            ButtonBlue(
                text = "Ask Luna AI anything",
                onClick = {
                    onButtonClick()
                }
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }
    }


}

@Preview
@Composable
fun AiChatOnboardScreenPreview() {
    ScreenAiChatOnboard(onButtonClick = {

    })
}






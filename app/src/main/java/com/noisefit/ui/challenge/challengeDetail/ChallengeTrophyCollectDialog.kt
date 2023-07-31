package com.noisefit.ui.challenge.challengeDetail

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.HandlerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit.luna.databinding.DialogTrophyCollectBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

const val CHALLENGE_TROPHY_COLLECTED = "CHALLENGE_TROPHY_COLLECTED"

@AndroidEntryPoint
class ChallengeTrophyCollectDialog : DialogFragment() {

    lateinit var binding: DialogTrophyCollectBinding
    var shareUri: Uri? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: ChallengeTrophyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTrophyCollectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        viewModel.trophyTitle = arguments?.let {
            ChallengeTrophyCollectDialogArgs.fromBundle(it).trophyTitle
        }
        viewModel.trophyUrl = arguments?.let {
            ChallengeTrophyCollectDialogArgs.fromBundle(it).trophyUrl
        }
        viewModel.activityId = arguments?.let {
            ChallengeTrophyCollectDialogArgs.fromBundle(it).activityId
        }
        viewModel.isTrophyCollected = arguments?.let {
            ChallengeTrophyCollectDialogArgs.fromBundle(it).isTrophyCollected
        }
        setObservers()
        loadTrophyData()

        binding.ivClose.setOnClickListener {
            setFragmentResult(
                CHALLENGE_TROPHY_COLLECTED,
                bundleOf("isCollected" to viewModel.isTrophyCollected)
            )
            findNavController().navigateUp()
        }

        binding.ivFacebook.setOnClickListener {

            shareImageSocial(1)
        }
        binding.ivInsta.setOnClickListener {

            shareImageSocial(2)
        }
        binding.ivWhatsapp.setOnClickListener {

            shareImageSocial(3)
        }
        binding.ivOthers.setOnClickListener {

            shareImageSocial(0)
        }
        binding.bCollect.setOnClickListener {
            if (binding.bCollect.alpha == 1f) {
                viewModel.activityId?.let {
                    viewModel.collectChallengeTrophy(it)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.badgeCollected.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                viewModel.isTrophyCollected = true
                loadTrophyData()
            }

        }
        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun loadTrophyData() {

        binding.textHeader.text = "SPECIAL TROPHY"
        binding.badgeLayout.textView49.text = getString(R.string.text_challenge_completed)

        Glide.with(requireContext())
            .load(viewModel.trophyUrl ?: "")
            .into(binding.badgeLayout.imageView23)

        if (viewModel.isTrophyCollected == true) {
            binding.bCollect.text = getString(R.string.text_collected)
            binding.bCollect.alpha = 0.6f
            showShareOptions()
            binding.badgeLayout.textView50.text =
                "You have successfully completed ${viewModel.trophyTitle ?: ""}"
        } else {
            binding.bCollect.text = getString(R.string.text_collect)
            binding.badgeLayout.textView50.text =
                "You have successfully completed ${viewModel.trophyTitle ?: ""} Please collect your trophy"
        }
    }

    private fun showShareOptions() {
        binding.view11.visible()
        binding.textView31.visible()
        binding.ivFacebook.visible()
        binding.ivInsta.visible()
        binding.ivWhatsapp.visible()
        binding.ivOthers.visible()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCancelable(false)
    }

    /**
     * @param shareOn 0->Default 1->Facebook 2->Insta 3->Whatsapp
     */
    fun shareImageSocial(shareOn: Int) {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())

        val view = binding.badgeLayout.root

        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            val bitmap = ImageUtil.getBitmapFromView(view)
            bitmap?.let {
                if (shareUri == null) {
                    shareUri = ImageUtil.getTempImageUri(it, requireContext())
                }
                mainThreadHandler.post {
                    binding.progressBar.root.gone()

                    when (shareOn) {
                        0 -> {
                            ShareUtil.shareOthers(requireContext(), shareUri)
                        }
                        1 -> {
                            ShareUtil.shareOnFacebook(requireContext(), shareUri)
                        }
                        2 -> {
                            ShareUtil.shareOnInsta(requireContext(), shareUri)
                        }
                        3 -> {
                            ShareUtil.shareOnWhatsapp(requireContext(), shareUri)
                        }
                    }
                }
            }
            mainThreadHandler.post {
                binding.progressBar.root.gone()
            }
        }
    }

}
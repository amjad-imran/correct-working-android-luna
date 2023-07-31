package com.noisefit.ui.reward.voucher.voucherdetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit_commans.data.model.VoucherDetailsData
import com.noisefit.luna.databinding.FragmentVoucherDetailBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.reward.voucher.VOUCHER_SOLD_KEY
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VoucherDetailFragment :
    BaseFragment<FragmentVoucherDetailBinding>(FragmentVoucherDetailBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: VoucherDetailViewModel by viewModels()
    private val args: VoucherDetailFragmentArgs by navArgs()

    private val mHTRedeemAdapter: HTRedeemAdapter by lazy {
        HTRedeemAdapter()
    }
    private val mTCRedeemAdapter: TCRedeemAdapter by lazy {
        TCRedeemAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            try {
                //https://noisefit.page.link/voucherDetails?id=59

                val deepLinkKey = it.keySet()?.firstOrNull()
                val uri = ((it.get(deepLinkKey) as Intent).data as Uri)
                val voucherId = uri.getQueryParameter("id")

                if (voucherId == null) navigateUpSafe()

                val parsedId = voucherId?.toIntOrNull()
                if (parsedId == null) {
                    navigateUpSafe()
                }

                mViewModel.id = parsedId
                mViewModel.comeFrom = ""


            } catch (exp: Exception) {
                exp.printStackTrace()
                mViewModel.id = args.id
                mViewModel.comeFrom = args.comeFrom
            }
        }





        setRecycler()
        if (mViewModel.comeFrom.equals("coupon", ignoreCase = false)) {
            mViewModel.getCouponDetailsData()
        } else {
            mViewModel.getUserVoucherDetailsData()
        }

    }

    private fun setRecycler() {
        with(binding.rvHowToRedeem) {
            adapter = mHTRedeemAdapter
        }
        with(binding.rvTerms) {
            adapter = mTCRedeemAdapter
        }
    }

    override fun initListener() {

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnRedeem.setOnClickListener {
            if (mViewModel.couponCode == null) {
                mViewModel.availCoupon()
            } else {
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.VOUCHER_DETAIL_PAGE_REDEEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["title"] = mViewModel.title ?: ""
                        this["brand"] = mViewModel.brand ?: ""
                    })
                redirectToRedeemUrl()
            }
        }
        binding.lytCoupon.ivShare.setOnClickListener {
            copyToClipboard()
        }

    }

    private fun copyToClipboard() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", mViewModel.couponCode)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun redirectToRedeemUrl() {
        copyToClipboard()
        if (mViewModel.redeemUrl != null) {
            val url: String = if (mViewModel.redeemUrl?.startsWith("http://") != true &&
                mViewModel.redeemUrl?.startsWith(
                    "https://"
                ) != true
            ) {
                "http://" + mViewModel.redeemUrl
            } else {
                mViewModel.redeemUrl ?: ""
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }


    override fun subscribeObservers() {
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mViewModel.voucherDetailsData.observe(this) {
            updateUi(it)
        }
        mViewModel.couponDetailsData.observe(this) {
            updateUi(it)
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
        mViewModel.availCouponData.observe(this) {
            if (it.code == null) {
                setFragmentResultListener(VOUCHER_SOLD_KEY) { _, bundle ->
                    if (bundle.getBoolean("redirectToDeals")) {
                        navigateUpSafe()
                    }
                }
                navigate(
                    VoucherDetailFragmentDirections.actionVoucherDetailsToVoucherSoldBottomSheet(
                        it.title, it.message
                    )
                )
            } else {
                mViewModel.couponCode = it.code
                updateCouponView()
            }
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.VOUCHER_DETAIL_PAGE_AVAIL_CLICK,
                HashMap<String, Any>().apply {
                    this["title"] = mViewModel.title ?: ""
                    this["brand"] = mViewModel.brand ?: ""
                })
        }

    }

    private fun updateUi(value: VoucherDetailsData?) {
        binding.rootView.visible()
        mViewModel.couponCode = value?.couponCode
        mViewModel.points = value?.points
        mViewModel.redeemUrl = value?.redirectionLink
        mViewModel.title = value?.title
        mViewModel.brand = value?.brand
        binding.btnRedeem.visible()

        if (value?.is_eligible == true) {
            binding.btnRedeem.enable()
        } else {
            binding.btnRedeem.disable()
        }

        binding.ivBanner.loadImage(
            requireContext(),
            value?.imageUrl, R.drawable.image_placeholder_voucher_banner
        )

        binding.lytToolbar.tvTitle.text = value?.brand
        binding.tvVoucherTitle.text = value?.title
        binding.tvVoucherSubTitle.text = value?.subTitle

        if (value?.validTill.isNullOrEmpty()) {
            binding.tvValidTill.invisible()
            binding.tvValidTillDate.invisible()
        } else {
            binding.tvValidTill.visible()
            binding.tvValidTillDate.visible()
            tryCatch {
                binding.tvValidTillDate.text =
                    DateFormats.formatServerTimeToMDYearFormat(value?.validTill)
            }
        }


        binding.ivBrandLogo.loadImage(requireContext(), value?.logoUrl)
        mHTRedeemAdapter.setDataSet(value?.howToAvail ?: ArrayList())
        mTCRedeemAdapter.setDataSet(value?.termsAndConditions ?: ArrayList())
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.VOUCHER_DETAIL_LANDING_PAGE_VISIT,
            HashMap<String, Any>().apply {
                this["title"] = value?.title ?: ""
                this["brand"] = value?.brand ?: ""
            })
        updateCouponView()


    }

    private fun updateCouponView() {
        if (mViewModel.couponCode == null) {
            binding.btnRedeem.text = "Avail for ${mViewModel.points} coins"
            binding.lytCoupon.root.gone()
        } else {
            binding.btnRedeem.enable()
            binding.btnRedeem.text = getString(R.string.text_redeem)
            binding.lytCoupon.root.visible()
            binding.lytCoupon.tvCouponCode.text = mViewModel.couponCode
        }
    }


}
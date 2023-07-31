package com.noisefit.ui.settings.helpAndSupport.details

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHelpAndSupportDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.response.HelpAndSupportDetailResponse
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.makeLinks
import com.noisefit_commans.ui.openAppSystemSettings
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpAndSupportDetailsFragment : BaseFragment<FragmentHelpAndSupportDetailsBinding>(
    FragmentHelpAndSupportDetailsBinding::inflate
), HelpAndSupportContentInteractionListener {
    companion object {
        const val FORMAT_BREAK_LINE = -1
        const val OPEN_WATCH_FIRMWARE_ARGS = 1
        const val OPEN_PLAYSTORE_ARGS = 2
        const val OPEN_APP_SETTING_ARGS = 3
        const val OPEN_IGNORE_BATTERY_OPTIMISATION_ARGS = 4
        const val OPEN_PHONE_SETTING_ARGS = 5
        const val OPEN_NOTIFICATION_SETTING_ARGS = 6
        const val OPEN_APP_COMPATIBILITY_ARGS = 7
        const val OPEN_BATTERY_SAVER_ARGS = 8
        const val OPEN_TASK_LIST_ARGS = 9
        const val OPEN_VOUCHER_ARGS = 10
        const val OPEN_CHECK_EXPIRATION_DATE_ARGS = 12
        const val OPEN_GET_THE_VOUCHER_DETAILS_ARGS = 11
    }


    private val viewModel: HelpAndSupportDetailsViewModel by viewModels()
    private val helpAndSupportContentAdapter by lazy {
        HelpAndSupportContentAdapter(this)
    }
    private val args: HelpAndSupportDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        viewModel.setHealthAndSupportQuestionId(args.questionId)

    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = helpAndSupportContentAdapter

        }
    }

    override fun initListener() {
        binding.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytIssueNotResolved.tvClickHere.makeLinks(
            false,
            Pair("Click Here", View.OnClickListener {
                activity?.let {
                    navigate(R.id.feedbackFragment)
                }

            })
        )

        binding.lytHelpFul.btnAnyOtherQuery.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytHelpFul.ivThumbsUp.setOnClickListener {
            viewModel.thumbLastState = 1
            binding.lytHelpFul.ivThumbsUp.alpha = 1.0f
            binding.lytHelpFul.ivThumbsDown.alpha = 0.5f
        }

        binding.lytHelpFul.ivThumbsDown.setOnClickListener {
            viewModel.thumbLastState = 0
            binding.lytHelpFul.ivThumbsUp.alpha = 0.5f
            binding.lytHelpFul.ivThumbsDown.alpha = 1.0f

        }

        //TODO asif
        binding.ivVideoThumbnail.setOnClickListener {
            navigate(
                HelpAndSupportDetailsFragmentDirections.actionHsDetailsFragmentToHsVideoPlayerFragment(
                    viewModel.helpAndSupportDetails.value?.videoList?.toTypedArray()
                )
            )
        }


    }


    private fun setTitle(title: String) {
        binding.apply {
            tvTitle.text = title
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        tryCatch {
            if (viewModel.thumbLastState == -1) return@tryCatch

            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.HELP_AND_SUPPORT,
                HashMap<String, Any>().apply {
                    this["ques_id"] = viewModel.questionId
                    this["like"] = viewModel.thumbLastState == 1
                }
            )
        }


    }


    private fun setHelpAndSupportContent(helpAndSupportDetailResponse: HelpAndSupportDetailResponse) {
        if (helpAndSupportDetailResponse.shortAnswer.isNullOrEmpty()) {
            binding.tvShortAns.gone()
            binding.include2.root.gone()
        } else {
            binding.include2.root.visible()
            binding.tvShortAns.text = helpAndSupportDetailResponse.shortAnswer
        }


        helpAndSupportDetailResponse.content?.let { helpAndSupportContentAdapter.setDataSet(it) }
        //handle video thumbnail url
        if (helpAndSupportDetailResponse.thumbnail != null) {
            binding.tvGuide.visible()
            binding.ivVideoThumbnail.visible()
            binding.ivPlay.visible()
            binding.include44.root.visible()
            binding.ivVideoThumbnail.loadImage(
                binding.ivVideoThumbnail.context,
                helpAndSupportDetailResponse.thumbnail
            )
        } else {
            binding.tvGuide.gone()
            binding.ivVideoThumbnail.gone()
            binding.ivPlay.gone()
            binding.include44.root.gone()
        }

        binding.include1.root.visible()
        binding.lytHelpFul.root.visible()
        binding.lytIssueNotResolved.root.visible()


    }


    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.helpAndSupportDetails.observe(this) {
            it?.let {
                setTitle(it.question ?: "")
                setHelpAndSupportContent(it)
            }
        }
        viewModel.questionId.observe(this) {
            viewModel.fetchHelpAndSupportData(it)
        }
    }


    override fun onHelpAndSupportClick(actionId: Int) {
        when (actionId) {
            OPEN_WATCH_FIRMWARE_ARGS -> {
                if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_DEVICE_CHECK_FOR_UPDATES_CLICK)
                    navigate(R.id.checkForUpdatesFragment)
                } else {
                    context.showShortToast(getString(R.string.text_device_not_connected))
                    //navigateUpSafe()
                }
            }

            OPEN_PLAYSTORE_ARGS -> {
                ShareUtil.openPlayStore(requireContext(), "com.noisefit")
            }

            OPEN_APP_SETTING_ARGS -> {
                requireContext().openAppSystemSettings()
            }

            OPEN_IGNORE_BATTERY_OPTIMISATION_ARGS -> {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }

            OPEN_PHONE_SETTING_ARGS -> {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }

            OPEN_BATTERY_SAVER_ARGS -> {
                viewModel.module = "1"
                goToSetting()
            }

            OPEN_NOTIFICATION_SETTING_ARGS -> {
                ApplicationUtils.requestNotificationAccess(requireActivity())
            }

            OPEN_APP_COMPATIBILITY_ARGS -> {
                navigate(R.id.appCompatibilityFragment)
            }

            OPEN_TASK_LIST_ARGS, OPEN_GET_THE_VOUCHER_DETAILS_ARGS -> {
                navigate(R.id.coinFragment)
            }

            OPEN_VOUCHER_ARGS, OPEN_CHECK_EXPIRATION_DATE_ARGS -> {
                navigate(R.id.myVoucherFragment)
            }

        }
    }


    /**
     * 点击去设置
     * */
    private fun goToSetting() {
        // phoneType: 0 xiaomi   1 huawei   2 oppo   3 vivo   4 其它（other）
        // module   : 1 关闭省电模式 2 选择后台“无限制” 3 允许应用自启动 4 后台进程锁定  5电源管理 6关闭耗电保护
        // module   : 1 Turn off the power saving mode 2 Select the background "Unlimited" 3 Allow the application to start automatically 4 Lock the background process 5 Power management 6 Turn off the power consumption protection
        val mtype = Build.BRAND // 手机品牌 Mobile phone brands
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        var componentName: ComponentName? = null
        try {
            when (viewModel.module!!) {
                "1" -> {
                    when (viewModel.phoneType) {
                        0 -> { //xiaomi
                            componentName = ComponentName(
                                "com.miui.securitycenter",
                                "com.miui.powercenter.PowerMainActivity"
                            )
                            intent.component = componentName
                            startActivity(intent)
                        }

                        else -> defSetting()
                    }
                }

                "2" -> {
                    when (viewModel.phoneType) {
                        0 -> { //xiaomi
                            componentName = ComponentName(
                                "com.miui.powerkeeper",
                                "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                            )
                            intent.putExtra(
                                "package_label",
                                requireActivity().packageManager.getApplicationLabel(
                                    requireActivity().applicationInfo
                                ).toString()
                            )
                            intent.putExtra("package_name", requireActivity().packageName)
                            intent.component = componentName
                            startActivity(intent)
                        }

                        else -> defSetting()
                    }
                }

                "3" -> {
                    when (viewModel.phoneType) {
                        0 -> { //xiaomi
                            componentName = ComponentName(
                                "com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity"
                            )
                        }

                        1 -> { //huawei
                            componentName = ComponentName(
                                "com.huawei.systemmanager",
                                "com.huawei.systemmanager.mainscreen.MainScreenActivity"
                            )
                        }

                        2 -> { //oppo
                            componentName = ComponentName(
                                "com.coloros.safecenter",
                                "com.coloros.privacypermissionsentry.PermissionTopActivity"
                            )
                        }

                        3 -> { //vivo
                            componentName =
                                ComponentName(
                                    "com.vivo.permissionmanager",
                                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                                )
                        }
                    }
                    if (mtype.startsWith("ZTE")) {
                        componentName = ComponentName(
                            "com.zte.heartyservice",
                            "com.zte.heartyservice.autorun.AppAutoRunManager"
                        )
                    } else if (mtype.startsWith("F")) {
                        componentName = ComponentName(
                            "com.gionee.softmanager",
                            "com.gionee.softmanager.oneclean.AutoStartMrgActivity"
                        )
                    }
                    intent.component = componentName
                    startActivity(intent)
                }
                /*"4" -> {
                    when (phoneType) {
                        0 -> { //xiaomi
                        }
                        1 -> { //huawei
                        }
                        2 -> { //oppo
                        }
                        3 -> { //vivo
                        }
                        else-> defSetting()
                    }
                }*/
                "5" -> {
                    when (viewModel.phoneType) {
                        1 -> { //huawei
                            componentName = ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings\$HighPowerApplicationsActivity"
                            )
                        }
                    }
                    intent.component = componentName
                    startActivity(intent)

                }

                "6" -> {
                    when (viewModel.phoneType) {
                        2 -> { //oppo
                            componentName = ComponentName(
                                "com.coloros.oppoguardelf",
                                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
                            )
                        }

                        3 -> { //vivo
                            componentName = ComponentName(
                                "com.iqoo.powersaving",
                                "com.iqoo.powersaving.PowerSavingManagerActivity"
                            )
                        }
                    }
                    LOGS.d("HERE_IM ${viewModel.module} ${viewModel.phoneType}")
                    intent.component = componentName
                    startActivity(intent)
                }

                else -> defSetting()
            }
        } catch (e: Exception) { //抛出异常就直接打开设置页面 Open the settings page directly if an exception is thrown
            e.printStackTrace()
            defSetting()
        }
    }

    /**
     * 鸿蒙os应用自启用 Hongmeng os application is automatically enabled
     */
    private fun goTiSetting2() {
        val mtype = Build.BRAND // 手机品牌  Mobile phone brands
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        var componentName: ComponentName? = null
        try {
            if (TextUtils.equals(viewModel.module, "3") && viewModel.phoneType == 1) {
                componentName = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                intent.component = componentName
                startActivity(intent)
            }
        } catch (e: Exception) { //抛出异常就直接打开设置页面 Open the settings page directly if an exception is thrown
            e.printStackTrace()
            defSetting()
        }
    }

    private fun defSetting() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
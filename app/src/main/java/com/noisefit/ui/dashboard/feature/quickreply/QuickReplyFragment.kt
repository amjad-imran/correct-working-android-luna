package com.noisefit.ui.dashboard.feature.quickreply

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentWorldClockBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val QUICK_REPLY_KEY = "QUICK_REPLY_KEY"

@AndroidEntryPoint
class QuickReplyFragment :
    BaseFragment<FragmentWorldClockBinding>(FragmentWorldClockBinding::inflate),
    QuickReplyRowAction {

    private val viewModel: QuickReplyViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val adapter by lazy { QuickReplyAdapter(this) }

    @Inject
    lateinit var localDataStore: DataStoredInterface

    var isEditAllowed = true


    private val maxQuickReplies: Int by lazy {
        AppStaticData.getMaxQuickRepliesCount(localDataStore.getConnectedDevice())
    }

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.textAddClock.text = getString(R.string.text_add_to_add_reply, maxQuickReplies)
        binding.textView8.text = getString(R.string.text_quick_reply)
        binding.textView18.text = getString(R.string.text_quick_replies)
        binding.textNoClock.text = getString(R.string.text_no_quick_reply_added)

        binding.btnAddClockBottom.text = getString(R.string.text_add_replies)

        binding.ivNoClock.setImageResource(R.drawable.ic_placeholder_quick_reply)

        setRecycler()
        if (viewModel.replies == null) {
            binding.progressBar.root.visible()
            sessionManager.sendQueryAction(QueryAction.GetCustomReplies)
        }


        val connectedDevice = localDataStore.getConnectedDevice()!!
        when (connectedDevice.deviceType) {
            DeviceType.COLORFIT_VISION.deviceType,
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.QUBE_2.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_MACRO.deviceType-> {
                isEditAllowed = false
                binding.btnAddClockBottom.gone()
//                binding.tvEdit.gone()
                binding.btnAdd.gone()
                adapter.setDisableDeleteMode(true)
            }
        }

    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAddClockBottom.setOnClickListener {
            invokeAddReply()
        }
        binding.tvEdit.setOnClickListener {
            viewModel.setEditMode(true)
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.QUICK_REPLY_EDIT_CLICK)
        }
        binding.btnAdd.setOnClickListener {
            invokeAddReply()
        }

        binding.btnSaveChanges.setOnClickListener {
            binding.progressBar.root.visible()
            viewModel.setEditMode(false)


            val customReply = CustomReplyData(
                customReplies = adapter.getCurrentDataSet() as ArrayList<CustomReplyData.CustomReply>
            )
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateCustomReply(
                    customReply
                )
            )
        }

    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.CustomReplyObtained -> {
                    binding.progressBar.root.gone()
                    it.customReplyData.customReplies.forEach { reply ->
                        LOGS.d("Custom Reply : ${reply.content} ${reply.crc} ${reply.index}")
                    }

                    if (it.customReplyData.customReplies.isEmpty()) {
                        binding.textView17.text = getString(R.string.text_sms_about_empty_data)
                        setStateNoData()
                    } else {
                        binding.textView17.text = getString(R.string.text_sms_about)
                        setStateHasData()
                    }
                    it.customReplyData.customReplies.let { it1 -> setCustomReplies(it1) }
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                when (it) {
                    is UpdateDeviceDataCallback.CustomizeReplyUpdated -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            context.showShortToast(getString(R.string.text_updated_successfully))
                            sessionManager.sendQueryAction(QueryAction.GetCustomReplies)
                        } else {
                            context.showShortToast(getString(R.string.text_updated_failed))
                            sessionManager.sendQueryAction(QueryAction.GetCustomReplies)
                        }
                    }
                    else -> {
                        LOGS.d("Reply in else")
                    }
                }
            }

        }

        viewModel.editMode.observe(viewLifecycleOwner) {
            if (it) {
                setStateEdit()
            } else {
                setStateDefault()
            }
        }

    }

    private fun setRecycler() {
        binding.rvClocks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClocks.adapter = adapter

        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.rvClocks)

    }

    fun invokeAddReply() {
        if (viewModel.replies == null) return
        if (viewModel.replies!!.size >= maxQuickReplies) {
            val alertMessage = getString(R.string.text_max_reply_message, maxQuickReplies)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_reply_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
            return
        }
        viewModel.setEditMode(true)
        viewModel.updatePosition = -1

        setFragmentResultListener(QUICK_REPLY_KEY) { key, bundle ->
            val reply = bundle.getParcelable<CustomReplyData.CustomReply>("reply")
            reply?.let {
                if (viewModel.replies == null) {
                    viewModel.replies = ArrayList()
                }
                viewModel.replies!!.add(reply)
            }
            viewModel.replies?.let {
                adapter.setDataSet(it)
                updateCount()
            }
        }
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.QUICK_REPLY_ADD_CLICK)
        navigate(
            QuickReplyFragmentDirections.actionQuickReplyFragmentToQuickReplyEditBottomSheet(null)
        )
    }

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int) {
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.QUICK_REPLY_DELETE_CLICK)
        updateCount()
    }

    override fun onItemClicked(position: Int, reply: CustomReplyData.CustomReply) {
        viewModel.updatePosition = position
        setFragmentResultListener(QUICK_REPLY_KEY) { key, bundle ->
            val replyReceived = bundle.getParcelable<CustomReplyData.CustomReply>("reply")
            replyReceived?.let {
                if (viewModel.replies == null) return@setFragmentResultListener
                viewModel.replies!!.removeAt(viewModel.updatePosition)
                viewModel.replies!!.add(viewModel.updatePosition, reply)
            }
            viewModel.replies?.let {
                adapter.setDataSet(it)
                updateCount()
            }
        }
        navigate(
            QuickReplyFragmentDirections.actionQuickReplyFragmentToQuickReplyEditBottomSheet(reply)
        )
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun setStateEdit() {
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        binding.btnAddClockBottom.gone()
        if (isEditAllowed) {
            binding.btnAdd.visible()
        }

        adapter.setEditMode(true)
    }

    private fun setStateDefault() {
        if (isEditAllowed) {
            if (adapter.itemCount != 0) {
                binding.tvEdit.visible()
            }
        }

        binding.btnSaveChanges.gone()

        adapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.ivNoClock.visible()
        binding.textNoClock.visible()
        binding.textAddClock.visible()
        if (viewModel.editMode.value != true) {
            binding.btnAddClockBottom.visible()
        }
        binding.btnAdd.gone()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.ivNoClock.gone()
        binding.textNoClock.gone()
        binding.textAddClock.gone()

        binding.btnAddClockBottom.gone()
        if (isEditAllowed) {
            binding.btnAdd.visible()
        }
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }

    }


    fun updateCount() {
        val dataCount = adapter.itemCount
        binding.tvCount.text = "($dataCount/$maxQuickReplies)"
        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }

    fun setCustomReplies(reply: List<CustomReplyData.CustomReply>) {
        viewModel.replies = reply as ArrayList<CustomReplyData.CustomReply>
        adapter.setDataSet(reply)
        updateCount()
    }
}
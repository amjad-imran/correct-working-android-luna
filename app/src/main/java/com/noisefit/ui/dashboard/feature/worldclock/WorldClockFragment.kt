package com.noisefit.ui.dashboard.feature.worldclock

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentWorldClockBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.WorldClockList
import com.noisefit_commans.models.WorldClocksPushData
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.truncate
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible


const val MAX_CLOCK = 5
const val ADD_WORLD_CLOCK_KEY = "ADD_WORLD_CLOCK_KEY"

@AndroidEntryPoint
class WorldClockFragment :
    BaseFragment<FragmentWorldClockBinding>(FragmentWorldClockBinding::inflate),
    WorldClockRowAction {

    private val viewModel: WorldClockViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val adapter by lazy { WorldClockAdapter(this) }

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView17.text = getString(R.string.text_about_world_clock)
        binding.textAddClock.text = getString(R.string.text_add_to_add_clock, MAX_CLOCK)

        setRecycler()
        if (viewModel.clocks == null) {
            sessionManager.sendQueryAction(QueryAction.GetWorldClock)
            binding.progressBar.root.visible()
        }
    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAddClockBottom.setOnClickListener {
            invokeAddClock()
        }
        binding.tvEdit.setOnClickListener {
            viewModel.setEditMode(true)
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.CLOCK_EDIT_CLICK)
        }
        binding.btnAdd.setOnClickListener {
            invokeAddClock()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)

            //val offset = Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.ZONE_OFFSET)
            val offset = Date().timezoneOffset
            val timeZone = (truncate(offset / 15f) * -1).toInt()

            LOGS.d("TimeZone $timeZone")

            val clock = WorldClocksPushData(
                worldClocks = adapter.getCurrentDataSet() as ArrayList<WorldClockList.WClock>,
                localTimeZone = timeZone
            )
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetWorldClock(
                    clock
                )
            )
        }

    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.WorldClockDataObtained -> {
                    binding.progressBar.root.gone()

//                    it.worldClocks.worldClocks.forEach { clock ->
//                        LOGS.d("Clock : ${clock.content} ${clock.timeZone}")
//                    }
                    it.worldClocks.worldClocks.let { it1 -> setCustomReplies(it1) }
                }
                else -> {}
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

    private fun invokeAddClock() {
        LOGS.d("${viewModel.clocks}")
        if (viewModel.clocks == null) return
        if (viewModel.clocks!!.size >= 5) {
            val alertMessage = getString(R.string.text_max_clock_message, 5)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_clock_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
            return
        }

        viewModel.updatePosition = -1

        setFragmentResultListener(ADD_WORLD_CLOCK_KEY) { key, bundle ->
            val clock = bundle.getParcelable<WorldClockList.WClock>("clock")
            clock?.let {
                viewModel.setEditMode(true)
                if (viewModel.clocks == null) {
                    viewModel.clocks = ArrayList()
                }

                if (!viewModel.hasClock(it)) {
                    viewModel.clocks!!.add(clock)
                } else {
                    context.showShortToast("${it.content} is already present")
                }
            }
            viewModel.clocks?.let {
                adapter.setDataSet(it)
                updateClockCount()
            }
        }
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.CLOCK_ADD_CLICK)
        navigate(
            WorldClockFragmentDirections.actionWorldClockFragmentToWorldClockBottomSheet()
        )
    }

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int) {
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.CLOCK_DELETE_CLICK)
        adapter.removeItem(position)
        updateClockCount()
        if (adapter.itemCount == 0) {
            binding.btnSaveChanges.performClick()
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun setStateEdit() {
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        adapter.setEditMode(true)
    }

    private fun setStateDefault() {
        if (adapter.itemCount != 0) {
            binding.tvEdit.visible()
        }
        binding.btnSaveChanges.gone()

        adapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.btnAdd.gone()
        binding.btnSaveChanges.gone()
        binding.ivNoClock.visible()
        binding.textNoClock.visible()
        binding.textAddClock.visible()
        binding.btnAddClockBottom.visible()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.btnAdd.visible()
        binding.ivNoClock.gone()
        binding.textNoClock.gone()
        binding.textAddClock.gone()
        binding.btnAddClockBottom.gone()
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }
    }


    fun updateClockCount() {
        val dataCount = adapter.itemCount
        binding.tvCount.text = "($dataCount/$MAX_CLOCK)"
        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }

    private fun setCustomReplies(reply: ArrayList<WorldClockList.WClock>) {
        viewModel.clocks = reply
        adapter.setDataSet(reply)
        updateClockCount()

        /*if(reply.isEmpty()){
            binding.bEdit.gone()
        }else{
            binding.bEdit.visible()
        }*/
    }
}
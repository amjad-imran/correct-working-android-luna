package com.noisefit.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.EndGame
import com.noisefit.databinding.BottomSheetUpdateEndgameBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.onboarding.onboardProfile.endgame.EndGameSelectionAdapter
import com.noisefit_commans.common.MarginSideItemDecoration
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val END_GAME_VALUE = "END_GAME_VALUE"

@AndroidEntryPoint
class EndGameUpdateBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetUpdateEndgameBinding>(BottomSheetUpdateEndgameBinding::inflate) {

    private var endGameList = ArrayList<EndGame>()
    private val endGameAdapter by lazy {
        EndGameSelectionAdapter()
    }

    @Inject
    lateinit var localDataStore: DataStoredInterface
    var selectedEndGameId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            selectedEndGameId = EndGameUpdateBottomSheetArgs.fromBundle(it).endGame
        }

        endGameList.addAll(localDataStore.getEndGameList())
        setGoalsRecycler()
        initListener()

    }

    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {

            if (selectedEndGameId == -1) {
                context.showShortToast(getString(R.string.text_please_select_end_game))
                return@setOnClickListener
            }
            setFragmentResult(
                END_GAME_VALUE,
                bundleOf("endGame" to selectedEndGameId)
            )
            navigateUpSafe()
        }
    }

    private fun setGoalsRecycler() {

        binding.rv.apply {
            addItemDecoration(MarginSideItemDecoration(16))
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = endGameAdapter
        }
        endGameAdapter.setOnEndGameInteractionListener(object :
            EndGameSelectionAdapter.OnEndGameInteractionListener {
            override fun onEndGameSelected(endGame: EndGame, position: Int) {
                LOGS.d("onItemSelected: $position ")
                selectedEndGameId = endGame.id
                endGameAdapter.notifyDataSetChanged()

            }

        })
        if (selectedEndGameId != -1) {
            endGameAdapter.setSelectedPosition(selectedEndGameId - 1)

        }
        endGameAdapter.setDataSet(endGameList)

    }

    override fun subscribeObservers() {

    }


}
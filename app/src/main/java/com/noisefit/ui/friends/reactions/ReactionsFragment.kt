package com.noisefit.ui.friends.reactions

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit.databinding.FragmentReactionsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.tryCatch
import dagger.hilt.android.AndroidEntryPoint

private const val EMOJI_LIST_KEY = "EMOJI_LIST_KEY"

@AndroidEntryPoint
class ReactionsFragment :
    BaseFragment<FragmentReactionsBinding>(FragmentReactionsBinding::inflate) {

    private var reactionsWrapper: ReactionsWrapper? = null

    private val mAdapter: ReactionsAdapter by lazy {
        ReactionsAdapter(object : ReactionActions {
            override fun onUserClicked(userId: Long) {
                tryCatch {
                    val stringUri = "https://noisefit.page.link/FriendsProfile?id=$userId"
                    val request = NavDeepLinkRequest.Builder
                        .fromUri(stringUri.toUri())
                        .build()
                    navigate(request)
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(reactionsWrapper: ReactionsWrapper) =
            ReactionsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EMOJI_LIST_KEY, reactionsWrapper)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reactionsWrapper = it.getParcelable(EMOJI_LIST_KEY)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mAdapter.setDataSet(reactionsWrapper?.userFriendData!!)
    }

    private fun setRecycler() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}


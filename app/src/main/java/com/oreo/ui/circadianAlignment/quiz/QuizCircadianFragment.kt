package com.oreo.ui.circadianAlignment.quiz

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentQuizCircadianBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.QuizQuestionCircadianDataModel

class QuizCircadianFragment : BaseFragment<FragmentQuizCircadianBinding>(FragmentQuizCircadianBinding::inflate) {

    private val questionAdapter: QuizQuestionAdapter by lazy {
        QuizQuestionAdapter(){
            handleQuizClick(it)
        }
    }

    private fun handleQuizClick(ques: QuizQuestionCircadianDataModel) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
    }

    private fun setAdapter() {

       /*val layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
            override fun smoothScrollToPosition(
                recyclerView: RecyclerView,
                state: RecyclerView.State,
                position: Int
            ) {

                val smoothScroller = object : LinearSmoothScroller(recyclerView.context){
                    override fun getVerticalSnapPreference(): Int {
                        return SNAP_TO_START
                    }
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return 100f / displayMetrics.densityDpi
                    }
                }

                smoothScroller.targetPosition = position
                startSmoothScroll(smoothScroller)

            }
        }*/

        val layoutManager = SingleScrollLinearLayoutManager(requireContext())
        binding.rvQuestions.layoutManager = layoutManager

        // Calculate padding: (RecyclerView height / 2) - (approx item height / 2)
        val screenHeight = resources.displayMetrics.heightPixels
        val estimatedItemHeight = 300 // Adjust this if your item layout is taller/shorter
        val padding = (screenHeight / 2) - (estimatedItemHeight / 2)

        binding.rvQuestions.addItemDecoration(CenterPaddingItemDecoration(padding))

        binding.rvQuestions.adapter = questionAdapter

        questionAdapter.updateDataSet(getQuesData())

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvQuestions)

        binding.rvQuestions.post {
            val snapView = snapHelper.findSnapView(layoutManager)
            snapView?.let {
                val position = layoutManager.getPosition(it)
                questionAdapter.setFocusedIndex(position)
            }
        }

        binding.rvQuestions.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    val snapView = snapHelper.findSnapView(layoutManager)
                    val centerPos = layoutManager.getPosition(snapView!!)
                    questionAdapter.setFocusedIndex(centerPos)
                }
            }
        })
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    private fun getQuesData(): List<QuizQuestionCircadianDataModel>{
        return listOf(
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            )
        )
    }

}

// ----

/*
here is the circadian screen code: " 'class QuizCircadianFragment : BaseFragment<FragmentQuizCircadianBinding>(FragmentQuizCircadianBinding::inflate) {

    private val questionAdapter: QuizQuestionAdapter by lazy {
        QuizQuestionAdapter(){
            handleQuizClick(it)
        }
    }

    private fun handleQuizClick(ques: QuizQuestionCircadianDataModel) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvQuestions.layoutManager = layoutManager
        binding.rvQuestions.adapter = questionAdapter

        questionAdapter.updateDataSet(getQuesData())

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvQuestions)

        binding.rvQuestions.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    val snapView = snapHelper.findSnapView(layoutManager)
                    val centerPos = layoutManager.getPosition(snapView!!)
                    questionAdapter.setFocusedIndex(centerPos)
                }
            }
        })
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    private fun getQuesData(): List<QuizQuestionCircadianDataModel>{
        return listOf(
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            ),
            QuizQuestionCircadianDataModel(
                text = "One hears about \"morning\" and \"evening\" types of people. Which one of these types do you consider yourself to be?",
                options = listOf(
                    "Definitely a \"morning\" type",
                    "Rather more a \"morning\" than \"evening\" tуре",
                    "Rather more an \"evening\" than \"morning\" type",
                    "Definitely an \"evening\" type"
                )
            )
        )
    }

}' and '<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/questions_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.oreo.ui.circadianAlignment.QuizCircadianFragment">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvQuestions"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:paddingTop="100dp"
        android:paddingBottom="100dp" />

</FrameLayout>' ,,,, adapter code: 'class QuizQuestionAdapter(
    val quesClickListener: (ques: QuizQuestionCircadianDataModel) -> Unit
):
    RecyclerView.Adapter<QuizQuestionAdapter.QuizQuestionViewHolder>() {

    private val mList: ArrayList<QuizQuestionCircadianDataModel> = ArrayList()

    private var focusedIndex = 0

    fun setFocusedIndex(index: Int) {
        val old = focusedIndex
        focusedIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    inner class QuizQuestionViewHolder(val binding: ItemQuestionQuizCircadianBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestionCircadianDataModel, isFocused: Boolean) {
            binding.tvQuestion.text = question.text

            binding.optionsContainer.removeAllViews()
            if (isFocused) {
                binding.blurOverlay.gone()
                question.options.forEach { option ->
                    val btn = Button(itemView.context).apply {
                        text = option
                        setOnClickListener {
                            quesClickListener(question)
                        }
                    }
                    binding.optionsContainer.addView(btn)
                }
            }else{
                binding.blurOverlay.visible()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizQuestionViewHolder {
        val binding = ItemQuestionQuizCircadianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizQuestionViewHolder(binding)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: QuizQuestionViewHolder, position: Int) {
        holder.bind(mList[position], position == focusedIndex)
    }

    fun updateDataSet(list: List<QuizQuestionCircadianDataModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}' ,,, item code : '<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">

    <!-- Main container -->
    <LinearLayout
        android:id="@+id/questionCard"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:elevation="4dp">

        <TextView
            android:id="@+id/tvQuestion"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Question here"
            android:textColor="@android:color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="12dp" />

        <LinearLayout
            android:id="@+id/optionsContainer"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </LinearLayout>

    <!-- Blur overlay for non-focused items -->
    <View
        android:id="@+id/blurOverlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#14FFFFFF"
        android:visibility="gone"/>
</FrameLayout>' " ...now what I want is when the screen initially gets rendered , then the first item is rendered in middle and then scroll then next item at center , 1st item on its top andf 3rd item blow 2nd one.scroll is relly sensitive , make it less
 */
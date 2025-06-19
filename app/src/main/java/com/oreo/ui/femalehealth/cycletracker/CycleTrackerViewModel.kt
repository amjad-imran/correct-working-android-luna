package com.oreo.ui.femalehealth.cycletracker

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.dataConverter.FemaleHealthDataConvertor
import com.oreo.data.dataConverter.FemaleHealthGeneratorResult
import com.oreo.data.model.NotificationToggleModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.TempPeriodData
import com.oreo.data.model.femaleh.TempPrediction
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.custom.ItemTemp
import com.oreo.ui.custom.Section
import com.oreo.ui.custom.TempPeriodCombineModel
import com.oreo.ui.home.summary.paginate.NotificationGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.SortedMap
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CycleTrackerViewModel @Inject constructor(
    private val femaleHealthRepository: FemaleHealthRepository,
    val femaleHealthDataConvertor: FemaleHealthDataConvertor,
    val userRepositoryOld: UserRepository,
    val localDataStore: DataStoredInterface,
    val resourcesProvider: ResourcesProvider,
    val vibrationUtils: VibrationUtils,
    val sessionManager: SessionManager,
    private val deviceRepository: OreoDeviceRepository,
) : BaseViewModel() {

    var notificationToggleModel = MutableLiveData<NotificationToggleModel>()
    var isCalendarSetupDone: Boolean = false
    var todayDate = LocalDate.now()
    var firstPeriodDate: LocalDate = LocalDate.now().minusMonths(2)
    var lastPeriodDate: LocalDate? = null

    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    var notifyDateChange = MutableLiveData<Event<LocalDate>>()

    private val _femaleHealthData = MutableLiveData<FemaleHealthUserInfoModel?>()
    val femaleHealthData: LiveData<FemaleHealthUserInfoModel?> get() = _femaleHealthData

    private val _cycleHistoryData = MutableLiveData<PeriodCycleHistory?>()
    val cycleHistoryData: LiveData<PeriodCycleHistory?> get() = _cycleHistoryData

    private val _avgInsightData = MutableLiveData<Pair<Int, Int>?>()
    val avgInsightData: LiveData<Pair<Int, Int>?> get() = _avgInsightData

    private val _cyclePredictionData = MutableLiveData<TempPrediction?>()
    val cyclePredictionData: LiveData<TempPrediction?> get() = _cyclePredictionData

    private val _symptomList = MutableLiveData<ArrayList<Pair<String, String>>>()
    val symptomList: LiveData<ArrayList<Pair<String, String>>> get() = _symptomList

    private val _navigateToBack = MutableLiveData<Event<Boolean>>()
    val navigateToBack: LiveData<Event<Boolean>> get() = _navigateToBack

    var currentSelectedPhase: CyclePhase? = null

    var healthDataDateList = HashMap<LocalDate, DayState>()

    val planState = MutableLiveData<Event<Triple<Boolean, Boolean, Boolean>>>()

    var lastDataLoadedFor: String? = null

    private var dateJob: Job? = null

    fun getDataForDate(date: String) {

        if (date.equals(lastDataLoadedFor)) return

        lastDataLoadedFor = date

        dateJob?.cancel()

        dateJob = viewModelScope.launch {
            femaleHealthRepository.getFemaleHealthUserInfo(date).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        lastDataLoadedFor = null
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        lastDataLoadedFor = null
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getDataForDate(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            lastDataLoadedFor = null
                            _femaleHealthData.postValue(it)

                            val symList = ArrayList<Pair<String, String>>()
                            it?.symptom?.flow?.let {
                                val title = resourcesProvider.getString(
                                    R.string.text_flow_value,
                                    it.symptomName ?: ""
                                )
                                symList.add(Pair(it.icon ?: "", title))
                            }
                            it?.symptom?.symptoms?.forEach {
                                symList.add(Pair(it.icon ?: "", it.symptomName ?: ""))
                            }

                            _symptomList.postValue(symList)

                            handleTempGraph(it)


                        }
                    }
                }
            }

        }
    }

    private fun handleTempGraph(data: FemaleHealthUserInfoModel?) {
        if (selectedDate.value != null && selectedDate.value!! > todayDate) {
            _cyclePredictionData.postValue(
                null
            )
            return
        }

        data?.temp?.let { list ->
            val tempVariance = calculateTempVariance(list)
            if (hasNonNullData(list)) {
                _cyclePredictionData.postValue(
                    TempPrediction(
                        tempVariation = tempVariance,
                        message = data.tempNudge,
                        tempData = list,
                        pendingNights = data.pendingNights
                    )
                )
            } else {
                _cyclePredictionData.postValue(
                    null
                )
            }
        } ?: run {
            _cyclePredictionData.postValue(
                null
            )
        }
    }

    private fun hasNonNullData(list: List<TempPeriodData>): Boolean {
        var isNonNull = false
        list.forEach {
            if (it.temperature != null) {
                isNonNull = true
            }
        }
        return isNonNull
    }

    private fun calculateTempVariance(list: List<TempPeriodData>): Float? {
//        if (list == null) {
//            return null
//        }

        return list.getOrNull(0)?.temperature ?: null
    }

    fun getCycleHistoryData() {
        viewModelScope.launch {
            femaleHealthRepository.getPeriodCycleHistory().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getCycleHistoryData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.cycleHistory.isNullOrEmpty()) {

                                _navigateToBack.postValue(Event(true))
                                return@collect
                            }
                            generateHealthData(it)
                        }
                    }
                }
            }
        }
    }

    private fun generateHealthData(cycleData: PeriodCycleHistory) {
        viewModelScope.launch {

            femaleHealthDataConvertor.convertHealthData(cycleData, true)
                .collect { resource ->

                    when (resource) {
                        is FemaleHealthGeneratorResult.Loading -> {
                            setLoading(resource.loading)
                        }

                        is FemaleHealthGeneratorResult.Success -> {
                            healthDataDateList.clear()
                            healthDataDateList = resource.value
                            _cycleHistoryData.postValue(cycleData)


                            if (cycleData.avg == null) {
                                _avgInsightData.postValue(null)
                            } else {
                                _avgInsightData.postValue(
                                    Pair(
                                        cycleData.avg.periodLengthAvg ?: 0,
                                        cycleData.avg.cycleLengthAvg ?: 0
                                    )
                                )
                            }
                        }
                    }

                }
        }
    }


    fun isCycleLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 21..35
    }

    fun isPeriodLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 2..7
    }

    fun getPregnancyText(text: String?): String {
        return if (text.equals("high", true)) {
            resourcesProvider.getString(R.string.text_high_chance_of_pregnancy)
        } else if (text.equals("low", true)) {
            resourcesProvider.getString(R.string.text_low_chance_of_pregnancy)
        } else if (text.equals("fertile", true)) {
            resourcesProvider.getString(R.string.text_your_body_is_at_it_s_most_fertile_today)
        } else if (text.equals("incr", true)) {
            resourcesProvider.getString(R.string.text_increasing_chance_of_pregnancy)
        } else if (text.equals("decr", true)) {
            resourcesProvider.getString(R.string.text_decreasing_chance_of_pregnancy)
        } else {
            ""
        }
    }

    fun getComfortDietWorkoutData(isComfortWorkout: Boolean) {
        viewModelScope.launch {
            deviceRepository.getLunaZoneData().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getComfortDietWorkoutData(isComfortWorkout)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            planState.postValue(
                                Event(
                                    Triple(
                                        it.workoutPlan ?: false,
                                        it.nutritionalPlan ?: false,
                                        isComfortWorkout
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns Pair(Phase string, phase color)
     */
    fun getCurrentPhaseText(
        ovulationDate: String?, periodDate: String?, currentDate: String
    ): Pair<String, Int>? {
        if (ovulationDate == null) return Pair(
            resourcesProvider.getString(R.string.text_follicular_phase),
            R.color.color_follicular
        )
        if (periodDate.isNullOrEmpty()) return null

        val localCurrentDate = LocalDate.parse(currentDate)
        val ovDateLocal = LocalDate.parse(ovulationDate)

        return if (localCurrentDate.isBefore(ovDateLocal) || localCurrentDate == ovDateLocal) {
            Pair(
                resourcesProvider.getString(R.string.text_follicular_phase),
                R.color.color_follicular
            )
        } else {
            Pair(resourcesProvider.getString(R.string.text_luteal_phase), R.color.color_luteal)
        }
    }

    fun calculateDaysLeft(dateString: String, selectedDate: String): Long {
        val targetDate = LocalDate.parse(dateString)
        val today = LocalDate.parse(selectedDate)
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    fun updateSelectedDate(date: LocalDate) {
        val old = selectedDate.value

        selectedDate.value = date
        notifyDateChange.value = Event(old)
    }

    private fun getCurrentCycleDay(
        periodDate: LocalDate, cycleLength: Int, currentDate: LocalDate
    ): Int {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(periodDate, currentDate).toInt()
        return (daysSinceLastPeriod % cycleLength) + 1
    }


    /**
     * return Pair(DayState, isDateSelected)
     */
    fun getCurrentState(date: LocalDate): Pair<DayState, Boolean> {
        val isDateSelected = date == selectedDate.value

        val history = cycleHistoryData.value?.cycleHistory
        if (history.isNullOrEmpty()) {
            return Pair(DayState.Default, isDateSelected)
        }

        val returnVal = healthDataDateList[date]

        return if (returnVal == null) {
            Pair(DayState.Default, isDateSelected)
        } else {
            Pair(returnVal, isDateSelected)
        }
    }

    fun onWeekScrolled(date: LocalDate) {

        if (date > selectedDate.value) {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = if (diff == 0L) {
                date
            } else {
                date.plusDays(7 - diff)
            }
            updateSelectedDate(newDate)
        } else {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = date.plusDays(diff)
            updateSelectedDate(newDate)

        }


    }


    fun combineTempData(tempData: List<TempPeriodData>?): TempPeriodCombineModel {

        val sections: MutableList<Section> = ArrayList()

        val items: MutableList<ItemTemp> = ArrayList()
        if (tempData.isNullOrEmpty()) {
            return TempPeriodCombineModel(
                sections = sections, items = items, 0f
            )
        }

        getPeriodSection(tempData.last().date, tempData.first().date)?.forEach {
            sections.add(it)
        }

        var minValue = 2.5f
        var maxValue = -2.5f

        items.add(ItemTemp(null, phase = CyclePhase.FOLLECULAR, 0, ""))

        tempData.forEachIndexed { index, i ->
            val phase = getCurrentPhase(i.date)

            val convertedTemp = if (sessionManager.isMetric() && i.temperature != null) {
                AppConversionUtils.fahrenheitToCelsius(32 + i.temperature)
            } else {
                i.temperature
            }

            items.add(ItemTemp(convertedTemp, phase, index + 1, i.date))

            convertedTemp?.let { temp ->
                if (temp < minValue) {
                    minValue = temp
                }

                if (temp > maxValue) {
                    maxValue = temp
                }
            }

        }

        if (abs(minValue) > maxValue) {
            maxValue = abs(minValue)
        }

        return TempPeriodCombineModel(
            sections = sections, items = items, maxValue
        )
    }

    private fun getCurrentPhase(date: String): CyclePhase {
        val localDate = LocalDate.parse(date)

        var phase: CyclePhase? = null

        cycleHistoryData.value?.cycleHistory?.forEach {
            val periodStart = LocalDate.parse(it.periodDate)
            val periodEnd = periodStart.plusDays(it.cycleLength?.toLong() ?: 1).minusDays(1)
            val ovDate = if (it.ovulationStartDate != null) {
                LocalDate.parse(it.ovulationStartDate)
            } else {
                null
            }


            if (localDate in periodStart..periodEnd) {
                phase = if (ovDate != null && localDate > ovDate) {
                    CyclePhase.LUTEAL
                } else {
                    CyclePhase.FOLLECULAR
                }
            }

            if (phase != null) {
                return@forEach
            }
        }
        return phase ?: CyclePhase.LUTEAL
    }

    private fun getPeriodSection(startDate: String, endDate: String): List<Section>? {

        val history = cycleHistoryData.value?.cycleHistory
        if (history.isNullOrEmpty()) return null
        val sections = ArrayList<Section>()

        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateLocal = LocalDate.parse(startDate, pattern)
        val endDateLocal = LocalDate.parse(endDate, pattern)


        val sortedPeriodData =
            healthDataDateList.filter { (it.value is DayState.Period) && (it.key <= endDateLocal) && (it.key >= startDateLocal) }
                .toSortedMap()

        val sortedOvData =
            healthDataDateList.filter {
                (it.value is DayState.Fertile || it.value is DayState.OvulationDay) && (
                        it.key <= endDateLocal
                        ) && (it.key >= startDateLocal)
            }.toSortedMap()

        createSections(sections, sortedPeriodData, startDateLocal, 1)
        createSections(sections, sortedOvData, startDateLocal, 2)

        return sections
    }

    /**
     * 1->period, 2->Fertile
     */
    private fun createSections(
        sections: ArrayList<Section>,
        sortedPeriodData: SortedMap<LocalDate, DayState>,
        startDateLocal: LocalDate,
        type: Int
    ) {
        var lastValue: Map.Entry<LocalDate, DayState>? = null
        val tempDatesArray = ArrayList<LocalDate>()
        sortedPeriodData.forEach {
            val currentKey = it.key

            if (lastValue == null || ChronoUnit.DAYS.between(
                    lastValue!!.key,
                    currentKey
                ) != 1L
            ) {
                lastValue = null
                if (tempDatesArray.isNotEmpty()) {

                    var daysStart =
                        abs(ChronoUnit.DAYS.between(startDateLocal, tempDatesArray.first()))
                    val daysEnd =
                        abs(ChronoUnit.DAYS.between(startDateLocal, tempDatesArray.last()))

                    sections.add(
                        Section(
                            if (type == 1) "period" else "",
                            daysStart.toInt(),
                            daysEnd.toInt(),
                            if (type == 1) Color.parseColor("#80ff7fc4") else Color.parseColor("#801ec9ff"),
                            imageRes = if (type == 1) R.drawable.ic_fertile_graph else R.drawable.ic_period_graph
                        )
                    )
                }
                tempDatesArray.clear()
            }

            tempDatesArray.add(currentKey)
            lastValue = it
        }
        if (tempDatesArray.isNotEmpty()) {
            var daysStart = abs(ChronoUnit.DAYS.between(startDateLocal, tempDatesArray.first()))
            val daysEnd = abs(ChronoUnit.DAYS.between(startDateLocal, tempDatesArray.last()))
            sections.add(
                Section(
                    if (type == 1) "period" else "",
                    daysStart.toInt(),
                    daysEnd.toInt(),
                    if (type == 1) Color.parseColor("#80ff7fc4") else Color.parseColor("#801ec9ff"),
                    imageRes = if (type == 1) R.drawable.ic_fertile_graph else R.drawable.ic_period_graph
                )
            )
        }
    }

    fun isPastCycle(selectedDateLocal: LocalDate): Boolean {
        val periodDate =
            cycleHistoryData.value?.cycleHistory?.firstOrNull()?.periodDate ?: return false
        val periodDateLocal = LocalDate.parse(periodDate)
        return selectedDateLocal < periodDateLocal
    }

    fun isPastCycleLogic2(selectedDateLocal: LocalDate): Boolean {
        val periodDateLocal = lastPeriodDate ?: return false
        return selectedDateLocal < periodDateLocal
    }

    fun getCalendarStart(): LocalDate {
        val periodDate = cycleHistoryData.value?.userDefault?.calendarStart ?: run {
            cycleHistoryData.value?.userDefault?.firstPeriodDate ?: "2024-03-01"
        }
        return LocalDate.parse(periodDate)
    }

    fun toggleNotification() {

    }

    fun getNotificationToggle() {
        viewModelScope.launch {
            userRepositoryOld.getNotificationToggle().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            notificationToggleModel.postValue(it)
                            localDataStore.setShouldShowSleepNotification(it.sleep_notification?:false)
                        }
                    }

                    else -> {}
                }
            }
        }

    }

    fun updateNotificationToggle() {
        viewModelScope.launch {

            vibrationUtils.vibrate(HAPTIC_VIBRATION)

            val master = notificationToggleModel.value?.hydrate_notification ?: false == true ||
                    notificationToggleModel?.value?.steps_notification ?: false == true ||
                    notificationToggleModel?.value?.female_health ?: false == true ||
                    notificationToggleModel?.value?.sleep_notification ?: false == true ||
                    notificationToggleModel?.value?.caffeine ?: false == true

            val request = JsonObject().apply {
                this.addProperty("master_notification", master)
                this.addProperty(
                    "hydrate_notification",
                    notificationToggleModel.value?.hydrate_notification ?: false
                )
                this.addProperty(
                    "steps_notification",
                    notificationToggleModel.value?.steps_notification ?: false
                )
                this.addProperty(
                    "sleep_notification",
                    notificationToggleModel.value?.sleep_notification ?: false
                )
                this.addProperty(
                    "female_health_notification",
                    notificationToggleModel.value?.female_health ?: false
                )
                this.addProperty(
                    "caffeine",
                    notificationToggleModel.value?.caffeine ?: false
                )
            }
            userRepositoryOld.updateNotificationToggle(request)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }

                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object : BinaryActionCallback {
                                        override fun yes() {
                                            updateNotificationToggle()
                                        }

                                        override fun no() {}
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {

                                if (notificationToggleModel.value?.hydrate_notification == true ||
                                    notificationToggleModel.value?.steps_notification == true ||
                                    notificationToggleModel.value?.sleep_notification == true
                                ) {
                                    notificationToggleModel.value?.master_notification = true
                                }

                                notificationToggleModel.postValue(notificationToggleModel.value?.copy())
                            }
                        }
                    }
                }
        }
    }

    fun getBellResource(femaleHealth: Boolean): Int {
        return when (currentSelectedPhase) {
            CyclePhase.FOLLECULAR -> {
                if (femaleHealth) {
                    R.drawable.ic_fh_notification_f_on
                } else {
                    R.drawable.ic_fh_notification_f_off
                }
            }

            CyclePhase.LUTEAL -> {
                if (femaleHealth) {
                    R.drawable.ic_fh_notification_l_on
                } else {
                    R.drawable.ic_fh_notification_l_off
                }
            }

            null -> {
                if (femaleHealth) {
                    R.drawable.ic_female_health_notification_on
                } else {
                    R.drawable.ic_female_health_notification_off
                }
            }
        }
    }

}

sealed class DayState {
    object Fertile : DayState()
    object OvulationDay : DayState()
    object Default : DayState()
    class Period(val pos: PeriodPos) : DayState()
}

enum class PeriodPos {
    START, END, CENTER, SINGLE
}

enum class CyclePhase {
    FOLLECULAR, LUTEAL
}

package com.noisefit.hybrid.utils

import com.noisefit.hybrid.base.VisionCommands
import com.noisefit.hybrid.dataconversions.DataConverter
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class BitwiseHelperUtils
@Inject constructor(
    private val bitwiseUtils: BitwiseUtils,
    private val dataConverter: DataConverter,
    private val visionCommands: VisionCommands
) {

    fun getDrinkWaterCmd(sedentaryData: SedentaryData): String {
        val freq = sedentaryData.interval * 60
        val interval = bitwiseUtils.decToHex(freq)

        var startInterval = "0x00"
        var endInterval = "0x00"
        if (interval.length == 4) {
            startInterval = "0x" + interval.substring(2)
            endInterval = "0x" + interval.substring(0, 2)
        } else {
            startInterval = "0x" + interval.substring(0, 2)
        }

        val duration = bitwiseUtils.decToHex(5)
        var startDuration = "0x00"
        var endDuration = "0x00"
        if (duration.length == 4) {
            startDuration = "0x" + duration.substring(2)
            endDuration = "0x" + duration.substring(0, 2)
        } else {
            startDuration = "0x" + duration.substring(0, 2)
        }

//        val daysInterval = getHexValue(dataConverter.toParseDay(sedentaryData.repeat))


//        val daysCmd = "${daysInterval.first},"


        var cmd = "${visionCommands.DRINK_HAND_CMD} 0x00,"
        cmd += if (sedentaryData.status) {
            " 0x01,"
        } else {
            " 0x00,"
        }

        cmd += " 0x00, 0x02, 0x${bitwiseUtils.decToHex(sedentaryData.startHour)}, 0x${
            bitwiseUtils.decToHex(
                sedentaryData.startMinute
            )
        }," //start time
        cmd += " 0x01, 0x02, 0x${bitwiseUtils.decToHex(sedentaryData.endHour)}, 0x${
            bitwiseUtils.decToHex(
                sedentaryData.endMinute
            )
        }," //end time
        cmd += " 0x02, 0x02, $startInterval, $endInterval," //frequency minute time
        cmd += " 0x03, 0x02, $startDuration, $endDuration," // duration
        cmd += " 0x04, 0x01, 0x7f,"// cycle
        cmd += " 0x8F"
//        LOGS.d("Drink cmd $cmd")
        return cmd
    }

    fun getBloodPressureCount(data: String): Int {
        //6F 59 80 04 00 64 00 2C 01 8F
        val splitData = getCmdListData(data, 9) ?: return 0

        return bitwiseUtils.hexToDec(splitData[8] + splitData[7])
    }


    fun parseSleepTypeEnd(data: String): Boolean {
        //  6F 56 80 0A 00 06 00 08 16 35 61 11 00 00 00 8F
        val splitData = getCmdListData(data, 15) ?: return false


        if (splitData[splitData.size - 5] == visionCommands.SLEEP_TYPE_QUIT) {
            return true
        }
        return false
    }


    fun parseSleepData(sleepList: List<String>): SleepData {
        val sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")
        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()

        var totalDeep = 0
        var totalLight = 0
        var totalAwake = 0
        var totalRem = 0

        LOGS.d("sleep history -- starts")
        for (i in 0 until sleepList.size - 1) {
// //  6F 56 80 0A 00 06 00 08 16 35 61 11 00 00 00 8F
            val currentSplitData = sleepList[i].split(" ")
            val nextSplitData = sleepList[i + 1].split(" ")
            val currentTimeStamp =
                bitwiseUtils.hexToDec(currentSplitData[10] + currentSplitData[9] + currentSplitData[8] + currentSplitData[7])

            val nextTimeStamp =
                bitwiseUtils.hexToDec(nextSplitData[10] + nextSplitData[9] + nextSplitData[8] + nextSplitData[7])

            //LOGS.d("sleep history -- ${sleepList[i]}")

            val sleepType = when (currentSplitData[11]) {
                visionCommands.SLEEP_TYPE_LIGHT -> SleepType.LIGHT.type
                visionCommands.SLEEP_TYPE_DEEP -> SleepType.DEEP.type
                visionCommands.SLEEP_TYPE_AWAKE -> SleepType.AWAKE.type
                visionCommands.SLEEP_TYPE_REM -> SleepType.REM.type
                visionCommands.SLEEP_TYPE_QUIT -> visionCommands.SLEEP_TYPE_QUIT
                visionCommands.SLEEP_TYPE_ENTER -> visionCommands.SLEEP_TYPE_ENTER
                else -> SleepType.AWAKE.type
            }
            LOGS.d("sleep history -- sleepType $sleepType")
            if (sleepType == visionCommands.SLEEP_TYPE_ENTER) {
                sleepData.startTime = DateFormats.convertTimestampToDate(
                    currentTimeStamp.toLong() * 1000,
                    DateFormats.timeFormat
                )
                sleepData.startTimeStamp = currentTimeStamp.toLong()
                LOGS.d("sleep history -- start ${sleepData.startTime}")
                continue
            } else if (nextSplitData[11] == visionCommands.SLEEP_TYPE_QUIT) {
                sleepData.endTime = DateFormats.convertTimestampToDate(
                    nextTimeStamp.toLong() * 1000,
                    DateFormats.timeFormat
                )
                sleepData.date = DateFormats.convertTimestampToDate(
                    nextTimeStamp.toLong() * 1000,
                    DateFormats.dateFormat
                )
                sleepData.endTimeStamp = nextTimeStamp.toLong()
                LOGS.d("sleep history -- end ${sleepData.endTime}")
            }


            val sleepBreakup = SleepData.SleepDataBreakup(sleepType = sleepType)

            try {
                //2021-09-14T03:50:00


//detail='2021-09-15T03:34:00&BEGIN,2021-09-15T03:34:00&LIGHT,2021-09-15T03:42:00&DEEP,2021-09-15T03:51:00&LIGHT,2021-09-15T04:07:00&DEEP,2021-09-15T04:22:00&LIGHT,2021-09-15T04:40:00&LIGHT,2021-09-15T04:47:00&DEEP,2021-09-15T04:57:00&LIGHT,2021-09-15T05:10:00&DEEP,2021-09-15T05:26:00&LIGHT,2021-09-15T05:38:00&DEEP,2021-09-15T05:46:00&LIGHT,2021-09-15T05:57:00&DEEP,2021-09-15T06:12:00&LIGHT,2021-09-15T06:28:00&LIGHT,2021-09-15T06:38:00&LIGHT,2021-09-15T06:46:00&DEEP,2021-09-15T06:59:00&LIGHT,2021-09-15T07:11:00&DEEP,2021-09-15T07:22:00&LIGHT,2021-09-15T07:42:00&LIGHT,2021-09-15T07:44:00&DEEP,2021-09-15T07:53:00&LIGHT,2021-09-15T07:57:00&DEEP,2021-09-15T08:04:00&LIGHT,2021-09-15T08:22:00&LIGHT,2021-09-15T08:29:00&LIGHT,2021-09-15T08:39:00&LIGHT,2021-09-15T08:50:00&LIGHT,2021-09-15T08:52:00&DEEP,2021-09-15T08:59:00&LIGHT,2021-09-15T09:07:00&LIGHT,2021-09-15T09:28:00&DEEP,2021-09-15T09:40:00&LIGHT,2021-09-15T09:46:00&DEEP,2021-09-15T09:55:00&LIGHT,2021-09-15T10:08:00&LIGHT,2021-09-15T10:19:00&END,', date='2021-09-15', flag=-1, type=0, timeStamp=0}
                val differenceMinutes = (nextTimeStamp.toLong() - currentTimeStamp.toLong()) / (60)

//                LOGS.d("today data", "${DateFormats.getDateFromTimestamp(currentTimeStamp.toLong(),DateFormats.dateFormat)} $sleepType")
//                LOGS.d("today data next", "${DateFormats.getDateFromTimestamp(nextTimeStamp.toLong(),DateFormats.dateFormat)} $sleepType")

                sleepBreakup.duration = differenceMinutes.toInt()

                if (sleepType == SleepType.DEEP.type) {
                    totalDeep += differenceMinutes.toInt()
                } else if (sleepType == SleepType.LIGHT.type) {
                    totalLight += differenceMinutes.toInt()
                } else if (sleepType == SleepType.REM.type) {
                    totalRem += differenceMinutes.toInt()
                } else if (sleepType == SleepType.AWAKE.type) {
                    totalAwake += differenceMinutes.toInt()
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

            sleepBreakup.startTime = DateFormats.convertTimestampToDate(
                currentTimeStamp.toLong() * 1000,
                DateFormats.timeFormat
            )
            sleepBreakup.endTime = DateFormats.convertTimestampToDate(
                nextTimeStamp.toLong() * 1000,
                DateFormats.timeFormat
            )

            sleepArray.add(sleepBreakup)

        }

        sleepData.sleepArray = sleepArray
        sleepData.light = totalLight
        sleepData.remCount = totalRem
        sleepData.deep = totalDeep
        sleepData.awake = totalAwake

        sleepData.total = totalLight + totalDeep + totalAwake + totalRem

        //LOGS.d( "sleep history -- ${Gson().toJson(sleepData)}")
        return sleepData

    }

    fun getHeartNumber(data: String): Int {
        //6F 5B 80 07 00 49 08 16 CA 41 61 50 8F
        val splitData = getCmdListData(data, 6) ?: return 0

        return bitwiseUtils.hexToDec(splitData[6] + splitData[5])

    }


    fun getBloodOxygenNumber(data: String): Int {
        //6F 5F 80 0B 00 01 00
        val splitData = getCmdListData(data, 6) ?: return 0


        return bitwiseUtils.hexToDec(splitData[6] + splitData[5])
    }

    fun getParseHeartRate(data: String): HeartRate {
        //6F 5B 80 07 00 01 00 80 E9 34 61 4B 8F
        val heartRate = HeartRate()
        val splitData = getCmdListData(data, 12) ?: return heartRate


        val timeStamp =
            bitwiseUtils.hexToDec(splitData[10] + splitData[9] + splitData[8] + splitData[7])
//        LoggerHelper.printVerbose(TAG, "getParseHeartRate ${timeStamp}")

        heartRate.date =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.dateFormat)
        heartRate.time =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.timeFormat)

        heartRate.timeStamp = timeStamp.toLong()
        heartRate.averageHeartRate = bitwiseUtils.hexToDec(splitData[11])
        return heartRate
    }

    fun getParseStressData(data: String): StressDataBreakup? {
        //6F 5F 80 0B 00 02 00 80 E9 34 60 00 00 24 00 63 8F
        val stressDataBreakup = StressDataBreakup()
        val splitData = getCmdListData(data, 12) ?: return null


        val value = bitwiseUtils.hexToDec(splitData[14] + splitData[13])
        if (value <= 0) {
            return null
        }

        val timeStamp =
            bitwiseUtils.hexToDec(splitData[10] + splitData[9] + splitData[8] + splitData[7])

        stressDataBreakup.date =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.dateFormat)
        stressDataBreakup.time =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.timeFormat)
        stressDataBreakup.value = value
        stressDataBreakup.timeStamp = timeStamp.toLong()
        return stressDataBreakup
    }

    fun getParseOxygenBreakup(data: String): BloodOxygenBreakup? {
        //6F 5F 80 0B 00 02 00 80 E9 34 60 00 00 24 00 63 8F
        val bloodOxygenBreakup = BloodOxygenBreakup()
        val splitData = getCmdListData(data, 12) ?: return null

        val value = bitwiseUtils.hexToDec(splitData[15])
        if (value <= 0) {
            return null
        }

        val timeStamp =
            bitwiseUtils.hexToDec(splitData[10] + splitData[9] + splitData[8] + splitData[7])


        bloodOxygenBreakup.date =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.dateFormat)
        bloodOxygenBreakup.time =
            DateFormats.convertTimestampToDate(timeStamp.toLong() * 1000, DateFormats.timeFormat)
        bloodOxygenBreakup.value = value
        bloodOxygenBreakup.timeStamp = timeStamp.toLong()
        return bloodOxygenBreakup
    }


    fun getHeartRateCount(data: String): Int {
        //6F 59 80 04 00 64 00 2C 01 8F
        val splitData = data.split(" ")
        if (splitData.isEmpty() || splitData.size < 9) {
            return 0
        }

        return bitwiseUtils.hexToDec(splitData[6] + splitData[5])
    }

    fun parseDrinkResponse(data: String): SedentaryData {
        val sedentaryData = SedentaryData()
        //6F 9D 80 15 00 01 01 00 02 0A 00 01 02 0E 1E 02 02 10 0E 03
        //6F 9D 80 15 00 00 00 00| 02 06 00 01| 02 15 00 02| 02 20 1C 03| 02 05 00 04| 01 00 8F
        val splitData = getCmdListData(data, 26) ?: return sedentaryData

        sedentaryData.status = bitwiseUtils.hexToDec(splitData[6]) != 0
        sedentaryData.startHour = bitwiseUtils.hexToDec(splitData[9])
        sedentaryData.startMinute = bitwiseUtils.hexToDec(splitData[10])

        sedentaryData.endHour = bitwiseUtils.hexToDec(splitData[13])
        sedentaryData.endMinute = bitwiseUtils.hexToDec(splitData[14])

        val frequency = bitwiseUtils.hexToDec(splitData[18] + splitData[17]) / 60 //0x00, 0x0C
        val duration = bitwiseUtils.hexToDec(splitData[22] + splitData[21])
        val repa = bitwiseUtils.hexToDec(splitData[25])
        sedentaryData.interval = frequency

        sedentaryData.repeat = dataConverter.getParseDays(repa)


        return sedentaryData
    }

    fun parseHandWashResponse(data: String): HandWashing {
        /*
        0x6F 9D 80 15 00 00 01 00 02 0A 00 01 02 0E 1E 02 02 10 0E 03 02 0A 00 04 01 01 8F

         */
        /*
        6F 9D 80 15 00 01 00 00| 02 06 00 01| 02 15 00 02| 02 20 1C 03| 02 05 00 04| 01 00 8F
         */

        //12drink 0x6F, 0x9D, 0x71, 0x15, 0x00, 0x01, 0x01, 0x00, 0x02, 0x0C, 0x00, 0x01, 0x02, 0x15, 0x00, 0x02, 0x02, 0x0E, 0x10, 0x03, 0x02, 0x00, 0x0C, 0x04, 0x01, 0x85, 0x8F,
        //6F 9D 80 15 00 01 01 00 02 0C 00 01 02 15 00 02 02 0E 10 03 02 00 0C 04 01 85 8F

        //6F 9D 80 15 00 01 01 00 02 0C 00 01 02 15 00 02 02 07 08 03 02 00 0C 04 01 85 8F
        val splitData = getCmdListData(data, 19) ?: return HandWashing()

        val startWash = bitwiseUtils.hexToDec(splitData[6]) != 0
        val startHour = bitwiseUtils.hexToDec(splitData[9])
        val startMinute = bitwiseUtils.hexToDec(splitData[10])
        val endHour = bitwiseUtils.hexToDec(splitData[13])
        val endMinute = bitwiseUtils.hexToDec(splitData[14])
        val frequency =
            bitwiseUtils.hexToDec(splitData[18] + splitData[17])  //0x00, 0x0C // should be in minutes
        val duration = bitwiseUtils.hexToDec(splitData[22] + splitData[21])

        return HandWashing(
            startWash = startWash,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            duration = duration,
            frequency = frequency / 60
        )
    }


    fun getPassword(data: String): WatchPassword {
        //6F 03 80 05 00 10 31 32 33 34 8F
        val splitData = data.split(" ")
        if (splitData.isEmpty() || splitData.size <= 6) {
            return WatchPassword(null, false)
        }

        val min = 6
        val max = splitData.size - 2

        var password = ""
        for (i in min..max) {
            password += splitData[i]
        }

        password = bitwiseUtils.hexToASCII(password)
        return WatchPassword(password, true)
    }

    fun getHandwashCmd(handWashing: HandWashing): String {
        val freq = handWashing.frequency * 60
        val interval = bitwiseUtils.decToHex(freq)// should be in minutes

        var startInterval = "0x00"
        var endInterval = "0x00"
        if (interval.length == 4) {
            startInterval = "0x" + interval.substring(2)
            endInterval = "0x" + interval.substring(0, 2)
        } else {
            startInterval = "0x" + interval.substring(0, 2)
        }

//        LOGS.d("Handwash ${startInterval} $endInterval")
        val duration = bitwiseUtils.decToHex(handWashing.duration)

        var startDuration = "0x00"
        var endDuration = "0x00"
        if (duration.length == 4) {
            startDuration = "0x" + duration.substring(2)
            endDuration = "0x" + duration.substring(0, 2)
        } else {
            startDuration = "0x" + duration.substring(0, 2)
        }

        var cmd = "${visionCommands.DRINK_HAND_CMD} 0x01,"
        cmd += if (handWashing.startWash) {
            " 0x01,"
        } else {
            " 0x00,"
        }
        cmd += " 0x00, 0x02, 0x${bitwiseUtils.decToHex(handWashing.startHour)}, 0x${
            bitwiseUtils.decToHex(
                handWashing.startMinute
            )
        }," //start time

        cmd += " 0x01, 0x02, 0x${bitwiseUtils.decToHex(handWashing.endHour)}, 0x${
            bitwiseUtils.decToHex(
                handWashing.endMinute
            )
        }," //end time
        //  0x01.or(0x02)|0x04|0x08|0x10|0x20|0x40
        cmd += " 0x02, 0x02, $startInterval, $endInterval," //frequency minute time
        cmd += " 0x03, 0x02, $startDuration, $endDuration," // duration
        cmd += " 0x04, 0x01, 0x7f,"// cycle
        // cmd += " 0x04, 0x01, 0x01|0x02|0x04|0x08|0x10|0x20|0x40,"
        cmd += " 0x8F,"

//        LOGS.d("Drink cmd hand :$cmd")
        return cmd
    }

    fun getHexLength(data: String): Int {
        var length = 0
        val splitArray = data.split(", 0x")

        splitArray.forEach { value ->
            if (value.isNotEmpty()) {
                length += 1
            }

        }

        if (length < 9) {
            length = "0$length".toInt()
        }

        return length
    }

    fun getHexValue(data: Int): Pair<String, String> {
        val duration = bitwiseUtils.decToHex(data)
        var startDuration = "0x00"
        var endDuration = "0x00"
        if (duration.length == 4) {
            startDuration = "0x" + duration.substring(2)
            endDuration = "0x" + duration.substring(0, 2)
        } else {
            startDuration = "0x" + duration.substring(0, 2)
        }
        return Pair(startDuration, endDuration)
    }


    private fun getCmdListData(data: String, length: Int): List<String>? {
        val splitData = data.split(" ")
        if (splitData.isEmpty() || splitData.size < length) {
            return null
        }
        return splitData
    }

    fun getStressStatus(data: String): Boolean {
        //6F 90 80 04 00 FF FF FF FF 8F
        val splitData = getCmdListData(data, 9) ?: return false

        val num = splitData[8] + splitData[7] + splitData[6] + splitData[5]

        if (Integer.parseInt(num, 16).and(0x01 shl 0x15) > 0) {
            return true
        }
        return false
    }

    fun bytesArrayResult(objects: Array<Any>): String {
        val bytes = objects[0] as ByteArray
        return bitwiseUtils.byteArrayToHexString(bytes)
    }
}
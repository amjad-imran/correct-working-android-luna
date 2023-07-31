package com.noisefit_commans.converter

import android.location.Address
import android.location.Geocoder
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.models.WeatherDataHourly
import com.noisefit_commans.models.weather.WeatherInfo
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS

object WeatherConverter {

    fun parseWeatherData(
        weatherInfo: WeatherInfo,
        unit: String?,
        address: Address?,
        locationString: String? = null
    ): Pair<List<WeatherData>, List<WeatherDataHourly>> {
        val weatherArray = ArrayList<WeatherData>()

        val main = weatherInfo.current
        val dailyArray = weatherInfo.daily


        if (dailyArray.isNullOrEmpty()) {
            LOGS.d("Failed array empty")
            return Pair(ArrayList(), ArrayList())
        }

        val daily1 = dailyArray[0].temp
        val temp = main.temp
        val minTemp = daily1.min
        val maxTemp = daily1.max
        val humidity = main.humidity
        val pressure = main.pressure
        val windSpeed = main.windSpeed
        val dt = main.dt

        val typeArray = main.weather
        val type = typeArray?.get(0)?.main
        val weatherId = typeArray?.get(0)?.id

        val latt = weatherInfo.lat
        val longg = weatherInfo.lon

        var city = ""
        var country = ""
        var addressNew: Address? = null
        if (locationString != null) {
            city = locationString
        } else {
            if (address == null) {
                var geocoder: Geocoder?
                try {
                    geocoder = Geocoder(NoisefitApplication.context, DateFormats.defaultLocale)
                    addressNew = geocoder.getFromLocation(latt, longg, 1)[0]
                } catch (e: Exception) {

                }
            } else {
                addressNew = address
            }

            if (addressNew != null) {
                val localityArray = ArrayList<String>()
                if (addressNew.subLocality != null) {
                    localityArray.add(addressNew.subLocality)
                }
                if (addressNew.locality != null) {
                    localityArray.add(addressNew.locality)
                }
                if (localityArray.size == 1 && addressNew.countryName != null) {
                    localityArray.add(addressNew.countryName)
                    country = addressNew.countryName
                }
                city = localityArray.joinToString(" ")
            }
        }

        weatherArray.add(
            WeatherData(
                country = country,
                city = city,
                temp = temp,
                weatherType = type,
                weatherId = weatherId,
                windSpeed = windSpeed,
                tempMax = maxTemp,
                tempMin = minTemp,
                unit = unit,
                humidity = humidity,
                pressure = pressure,
                dt = dt,
            )
        )


        for (index in 1 until dailyArray.size - 1) {
            val weatherObject = dailyArray[index]

            val daily2Temp = weatherObject.temp
            val temp1 = main.temp
            val minTemp1 = daily2Temp.min
            val maxTemp1 = daily2Temp.max
            val humidity1 = weatherObject.humidity
            val pressure1 = weatherObject.pressure
            val dailyDt = weatherObject.dt

            val typeArray1 = weatherObject.weather
            val type1 = typeArray1?.get(0)?.main
            val weatherIdNext = typeArray1?.get(0)?.id

            weatherArray.add(
                WeatherData(
                    country = country,
                    city = city,
                    temp = temp1,
                    weatherType = type1,
                    weatherId = weatherIdNext,
                    windSpeed = 0.0,
                    tempMax = maxTemp1,
                    tempMin = minTemp1,
                    unit = unit,
                    humidity = humidity1,
                    pressure = pressure1,
                    dt = dailyDt
                )
            )
        }

        return Pair(weatherArray, parseWeatherDataHourly(weatherInfo, country, city, unit))
    }

    private fun parseWeatherDataHourly(weatherInfo: WeatherInfo, country: String, city: String,unit: String?): List<WeatherDataHourly> {
        val weatherArray = ArrayList<WeatherDataHourly>()
        val current = weatherInfo.current
        val hourlyArray = weatherInfo.hourly

        if (hourlyArray.isNullOrEmpty()) {
            LOGS.d("parseWeatherDataHourly Failed array empty")
            return ArrayList()
        }

        for (index in hourlyArray.indices) {
            val weatherObject = hourlyArray[index]
            val typeArray1 = weatherObject.weather
            val type1 = typeArray1?.get(0)?.main
            val weatherIdNext = typeArray1?.get(0)?.id
            weatherArray.add(
                WeatherDataHourly(
                    country = country,
                    city = city,
                    temp = weatherObject.temp,
                    weatherType = type1,
                    weatherId = weatherIdNext,
                    windSpeed = weatherObject.windSpeed,
                    unit = unit,
                    humidity = weatherObject.humidity,
                    pressure = weatherObject.pressure,
                    uvi = weatherObject.uvi,
                    sunrise = current.sunrise,
                    sunset = current.sunset,
                    dt = weatherObject.dt
                )
            )
        }
        return weatherArray
    }
}
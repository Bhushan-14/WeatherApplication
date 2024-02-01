package com.example.myapplication.weatherapplication
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchWeatherData("Delhi")

        // Set up search functionality
        searchCity()
    }

    private fun searchCity() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    // Fetch weather data for the entered city
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text changes in the search view if needed
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val apiKey = "your_api_key"
        val units = "metric"

        val response = retrofit.getWeatherData(cityName, apiKey, units)

        response.enqueue(object : Callback<weatherapp> {
            override fun onResponse(call: Call<weatherapp>, response: Response<weatherapp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    // Extract weather details
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity.toString()
                    val sea = responseBody.main.pressure
                    val tempMax = responseBody.main.temp_max.toString()
                    val tempMin = responseBody.main.temp_min.toString()
                    val sunrise = responseBody.sys.sunrise * 1000L
                    val sunset = responseBody.sys.sunset * 1000L
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val wind = responseBody.wind.speed.toString()

                    // Obtain timezone offset from API response
                    val timezoneOffset = responseBody.timezone

                    // Update TextViews with weather details
                    findViewById<TextView>(R.id.temp).text = "$temperature °C"
                    findViewById<TextView>(R.id.condition).text = condition
                    findViewById<TextView>(R.id.condition1).text = condition
                    findViewById<TextView>(R.id.maxtemp).text = "$tempMax °C"
                    findViewById<TextView>(R.id.mintemp).text = "$tempMin °C"
                    findViewById<TextView>(R.id.humidity).text = "$humidity %"
                    findViewById<TextView>(R.id.windspeed).text = "$wind m/s"
                    findViewById<TextView>(R.id.sunrise).text = formatTime(sunrise, timezoneOffset)
                    findViewById<TextView>(R.id.sunset).text = formatTime(sunset, timezoneOffset)
                    findViewById<TextView>(R.id.sea).text = "$sea hPa"
                    findViewById<TextView>(R.id.cityname).text = cityName
                    findViewById<TextView>(R.id.date).text = formatDate(System.currentTimeMillis(), timezoneOffset)
                    findViewById<TextView>(R.id.day).text = formatDay(System.currentTimeMillis(), timezoneOffset)

                    changeBackgroundAndAnimation(condition)
                }
            }

            override fun onFailure(call: Call<weatherapp>, t: Throwable) {
                Log.e(TAG, "Error fetching weather data", t)
            }
        })
    }

    private fun changeBackgroundAndAnimation(condition: String) {
        // Change background
        changeBackground(condition)

        // Change Lottie animation
        changeLottieAnimation(condition)
    }

    private fun changeBackground(condition: String) {
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)

        when (condition) {
            "Haze" -> {
                constraintLayout.setBackgroundResource(R.drawable.haze_background)
            }
            "Sunny" -> {
                constraintLayout.setBackgroundResource(R.drawable.sunny_background)
            }
            "Clouds" -> {
                constraintLayout.setBackgroundResource(R.drawable.cloud_background)
            }
            "Snow" -> {
                constraintLayout.setBackgroundResource(R.drawable.snow_background)
            }
            "Rain" -> {
                constraintLayout.setBackgroundResource(R.drawable.rain_background)
            }
            "Smoke" -> {
                constraintLayout.setBackgroundResource(R.drawable.smoke_background)
            }
            else -> {
                // Default background for unknown conditions
                constraintLayout.setBackgroundResource(R.drawable.sunny_background)
            }
        }
    }

    private fun changeLottieAnimation(condition: String) {
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)

        val animationResId = when (condition) {
            "Clouds" -> R.raw.cloud
            "Rain" -> R.raw.rain
            "Snow" -> R.raw.snow
            "Sunny" -> R.raw.sun
            else -> R.raw.sun
        }

        lottieAnimationView.setAnimation(animationResId)
        lottieAnimationView.playAnimation()
    }

    private fun playLottieAnimation(animationFile: String, lottieAnimationView: LottieAnimationView) {
        // Set the animation file for the LottieAnimationView using the raw resource ID
        val animationResId = resources.getIdentifier(animationFile, "raw", packageName)

        if (animationResId != 0) {
            lottieAnimationView.setAnimation(animationResId)

            // Start the animation
            lottieAnimationView.playAnimation()
        } else {
            Log.e(TAG, "Animation file not found: $animationFile")
        }
    }

    private fun formatDate(timestamp: Long, timezoneOffset: Int): String? {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val adjustedTimestamp = timestamp + timezoneOffset
        return simpleDateFormat.format(Date(adjustedTimestamp))
    }

    private fun formatDay(timestamp: Long, timezoneOffset: Int): String {
        val simpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val adjustedTimestamp = timestamp + timezoneOffset
        return simpleDateFormat.format(Date(adjustedTimestamp))
    }

    private fun formatTime(timestamp: Long, timezoneOffset: Int): String {
        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val adjustedTimestamp = timestamp + timezoneOffset
        return simpleDateFormat.format(Date(adjustedTimestamp))
    }
}

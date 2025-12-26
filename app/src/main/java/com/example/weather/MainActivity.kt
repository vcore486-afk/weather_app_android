package com.example.weather

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val apiKey = "9cabf88b4c9c983d726a820860e3a5b6"
    private val TAG = "WeatherApp"

    private val cityIds = mapOf(
        "Москва" to 524901,
        "Санкт-Петербург" to 498817,
        "Новосибирск" to 1496747,
        "Екатеринбург" to 1486209,
        "Казань" to 551487,
        "Нижний Новгород" to 520555,
        "Челябинск" to 1508291,
        "Самара" to 499099,
        "Омск" to 1496153,
        "Ростов-на-Дону" to 501175
    )

    private lateinit var tempViews: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Сопоставление города с TextView
        tempViews = mapOf(
            "Москва" to findViewById(R.id.moscow_temp),
            "Санкт-Петербург" to findViewById(R.id.spb_temp),
            "Новосибирск" to findViewById(R.id.novosib_temp),
            "Екатеринбург" to findViewById(R.id.ekb_temp),
            "Казань" to findViewById(R.id.kazan_temp),
            "Нижний Новгород" to findViewById(R.id.nn_temp),
            "Челябинск" to findViewById(R.id.chelyabinsk_temp),
            "Самара" to findViewById(R.id.samara_temp),
            "Омск" to findViewById(R.id.omsk_temp),
            "Ростов-на-Дону" to findViewById(R.id.rostov_temp)
        )

        // Кнопка обновления
        val btnUpdate: Button = findViewById(R.id.btn_update)
        btnUpdate.setOnClickListener {
            Log.d(TAG, "Нажата кнопка обновления температуры")
            updateAllTemperatures()
        }

        // Первичное обновление при запуске
        updateAllTemperatures()
    }

    private fun updateAllTemperatures() {
        for ((city, id) in cityIds) {
            Log.d(TAG, "Запрос температуры для города: $city (ID=$id)")
            fetchTemperature(id) { temp ->
                Log.d(TAG, "Получена температура для $city: $temp°C")
                runOnUiThread {
                    tempViews[city]?.text = "$temp°C"
                }
            }
        }
    }

    private fun fetchTemperature(cityId: Int, callback: (Double) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url =
                    "https://api.openweathermap.org/data/2.5/weather?id=$cityId&appid=$apiKey&units=metric"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body!!.string())
                val temp = json.getJSONObject("main").getDouble("temp")
                Log.d(TAG, "Ответ от сервера для ID=$cityId: $temp°C")
                callback(temp)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении температуры для ID=$cityId", e)
            }
        }
    }
}

package com.example.weather

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val apiKey = "9cabf88b4c9c983d726a820860e3a5b6"
    private val TAG = "WeatherApp"

    private lateinit var cities: MutableList<City>
    private lateinit var adapter: CityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cities = loadCitiesFromAssets().toMutableList()

        adapter = CityAdapter(cities)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_update).setOnClickListener {
            updateAllTemperatures()
        }

        updateAllTemperatures()
    }
    private fun loadCitiesFromAssets(): List<City> {
        return try {
            val json = assets.open("cities.json")
                .bufferedReader()
                .use { it.readText() }

            val array = JSONArray(json)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                City(obj.getString("name"), obj.getInt("id"))
            }
        } catch (e: Exception) {
            Log.e("WeatherApp", "cities.json not found", e)
            emptyList()
        }
    }

    private fun updateAllTemperatures() {
        for ((index, city) in cities.withIndex()) {
            fetchTemperature(city.id) { temp ->
                runOnUiThread {
                    cities[index].temperature = temp
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun fetchTemperature(cityId: Int, callback: (Double) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url =
                    "https://api.openweathermap.org/data/2.5/weather?id=$cityId&appid=$apiKey&units=metric"
                val request = Request.Builder().url(url).build()
                val response = OkHttpClient().newCall(request).execute()
                val json = JSONObject(response.body!!.string())
                callback(json.getJSONObject("main").getDouble("temp"))
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки погоды", e)
            }
        }
    }
}

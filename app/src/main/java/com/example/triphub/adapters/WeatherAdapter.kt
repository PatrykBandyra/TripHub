package com.example.triphub.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.R
import com.example.triphub.databinding.ItemWeatherBinding
import com.example.triphub.models.weather.Daily
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(private val context: Context, var items: MutableList<Daily>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class MyViewHolder(val binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemWeatherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dailyWeather = items[position]
        if (holder is MyViewHolder) {
            when (dailyWeather.weather.first().icon) {
                "01d" -> holder.binding.ivMain.setImageResource(R.drawable.sunny)
                "02d" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "03d" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "04d" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "04n" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "10d" -> holder.binding.ivMain.setImageResource(R.drawable.rain)
                "11d" -> holder.binding.ivMain.setImageResource(R.drawable.storm)
                "13d" -> holder.binding.ivMain.setImageResource(R.drawable.snowflake)
                "50d" -> holder.binding.ivMain.setImageResource(R.drawable.mist)
                "01n" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "02n" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "03n" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "10n" -> holder.binding.ivMain.setImageResource(R.drawable.cloud)
                "11n" -> holder.binding.ivMain.setImageResource(R.drawable.rain)
                "13n" -> holder.binding.ivMain.setImageResource(R.drawable.snowflake)
                "50n" -> holder.binding.ivMain.setImageResource(R.drawable.mist)
            }

            holder.binding.tvSunriseTime.text = unixTime(dailyWeather.sunrise)
            holder.binding.tvSunsetTime.text = unixTime(dailyWeather.sunset)

            holder.binding.tvSpeed.text = dailyWeather.windSpeed.toString()
            holder.binding.tvHumidity.text = dailyWeather.humidity.toString() + " %"
        }
    }

    override fun getItemCount(): Int = items.size

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}
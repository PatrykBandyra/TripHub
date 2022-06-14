package com.example.triphub.activities

import android.os.Build
import android.os.Bundle
import com.example.triphub.databinding.ActivityWeatherBinding
import com.google.android.gms.location.R
import com.google.gson.Gson

class WeatherActivity : BaseActivity<ActivityWeatherBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getViewBinding() = ActivityWeatherBinding.inflate(layoutInflater)


}
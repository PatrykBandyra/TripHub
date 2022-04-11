package com.example.triphub.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripBinding

class MyTripActivity : BaseActivity<ActivityMyTripBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getViewBinding() = ActivityMyTripBinding.inflate(layoutInflater)
}
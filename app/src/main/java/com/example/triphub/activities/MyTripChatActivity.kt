package com.example.triphub.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripChatBinding

class MyTripChatActivity : BaseActivity<ActivityMyTripChatBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getViewBinding() = ActivityMyTripChatBinding.inflate(layoutInflater)
}
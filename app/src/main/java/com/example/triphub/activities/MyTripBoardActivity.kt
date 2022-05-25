package com.example.triphub.activities

import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripBoardBinding

class MyTripBoardActivity : BaseActivity<ActivityMyTripBoardBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getViewBinding() = ActivityMyTripBoardBinding.inflate(layoutInflater)
}
package com.example.triphub.activities

import android.content.Intent
import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripBoardBinding

class MyTripBoardActivity : BaseActivity<ActivityMyTripBoardBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpTaskBarNavigation()
    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            startActivity(Intent(this, MyTripPeopleActivity::class.java))
            finish()
        }
        binding.ivMap.setOnClickListener {
            startActivity(Intent(this, MyTripMapActivity::class.java))
            finish()
        }
        binding.ivChat.setOnClickListener {
            startActivity(Intent(this, MyTripChatActivity::class.java))
            finish()
        }
    }

    override fun getViewBinding() = ActivityMyTripBoardBinding.inflate(layoutInflater)
}
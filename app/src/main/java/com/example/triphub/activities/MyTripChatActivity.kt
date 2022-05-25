package com.example.triphub.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripChatBinding

class MyTripChatActivity : BaseActivity<ActivityMyTripChatBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpTaskBarNavigation()
    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            startActivity(Intent(this, MyTripPeopleActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            startActivity(Intent(this, MyTripMapActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivBoard.setOnClickListener {
            startActivity(Intent(this, MyTripBoardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun getViewBinding() = ActivityMyTripChatBinding.inflate(layoutInflater)
}
package com.example.triphub.activities

import android.content.Intent
import android.os.Bundle
import com.example.triphub.databinding.ActivityMyTripBoardBinding
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class MyTripBoardActivity : BaseActivity<ActivityMyTripBoardBinding>() {

    private lateinit var mTrip: MyTrip
    private lateinit var mUser: User

    private var mTaskListPosition: Int = -1
    private var mCardPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No MyTrip object provided")
        }

        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No User object provided")
        }

        setUpTaskBarNavigation()

        binding.etNameCardDetails.setText(mTrip.taskList[mTaskListPosition].cards[mCardPosition].name)

    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            val intent = Intent(this, MyTripPeopleActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            val intent = Intent(this, MyTripMapActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivChat.setOnClickListener {
            val intent = Intent(this, MyTripChatActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun getViewBinding() = ActivityMyTripBoardBinding.inflate(layoutInflater)
}
package com.example.triphub.activities

import android.content.Intent
import android.os.Bundle
import com.example.triphub.databinding.ActivityPrivateChatBinding
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class PrivateChatActivity : BaseActivity<ActivityPrivateChatBinding>() {

    private lateinit var mUser: User
    private lateinit var mFriend: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadIntentData()

    }

    override fun getViewBinding() = ActivityPrivateChatBinding.inflate(layoutInflater)

    private fun loadIntentData() {
        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("Private chat activity did not receive user data")
        }
        if (intent.hasExtra(Constants.Intent.FRIEND)) {
            mFriend = intent.getParcelableExtra(Constants.Intent.FRIEND)!!
//            binding.
        } else {
            throw Exception("Private chat activity did not receive friend data")
        }
    }
}
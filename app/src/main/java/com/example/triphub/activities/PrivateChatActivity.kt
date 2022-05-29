package com.example.triphub.activities

import android.os.Bundle
import com.example.triphub.databinding.ActivityPrivateChatBinding

class PrivateChatActivity : BaseActivity<ActivityPrivateChatBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getViewBinding() = ActivityPrivateChatBinding.inflate(layoutInflater)
}
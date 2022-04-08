package com.example.triphub.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivityIntroBinding
import com.example.triphub.utils.Constants

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }

    override fun getViewBinding() = ActivityIntroBinding.inflate(layoutInflater)

    override fun onBackPressed() {
        doubleBackToExit()
    }
}
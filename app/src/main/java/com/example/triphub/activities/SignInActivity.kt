package com.example.triphub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivitySignInBinding
import com.example.triphub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActionBarForReturnAction(
            binding.toolbarSignInActivity,
            icon = R.drawable.back_arrow_black
        )

        auth = Firebase.auth

        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    override fun getViewBinding() = ActivitySignInBinding.inflate(layoutInflater)

    private fun signInUser() {
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        hideKeyboard()

        if (validateForm(email, password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showErrorSnackBar(R.string.sign_in_email_error)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(R.string.sign_in_password_error)
                false
            }
            else -> {
                true
            }
        }
    }
}
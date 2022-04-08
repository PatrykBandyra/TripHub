package com.example.triphub.activities

import android.os.Bundle
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivitySignUpBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity<ActivitySignUpBinding>() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActionBarForReturnAction(
            binding.toolbarSignUpActivity,
            icon = R.drawable.back_arrow_black
        )

        auth = Firebase.auth

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    override fun getViewBinding() = ActivitySignUpBinding.inflate(layoutInflater)

    private fun registerUser() {
        val name: String = binding.etName.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password1: String = binding.etPassword1.text.toString().trim { it <= ' ' }
        val password2: String = binding.etPassword2.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password1, password2)) {
            showProgressDialog()
            auth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        UserFireStore().registerUser(this, user)
                    } else {
                        hideProgressDialog()
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun validateForm(
        name: String,
        email: String,
        password1: String,
        password2: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showErrorSnackBar(R.string.sign_up_name_error)
                false
            }
            email.isEmpty() -> {
                showErrorSnackBar(R.string.sign_up_email_error)
                false
            }
            password1.isEmpty() -> {
                showErrorSnackBar(R.string.sign_up_password1_error)
                false
            }
            password2.isEmpty() -> {
                showErrorSnackBar(R.string.sign_up_password2_error)
                false
            }
            else -> {
                if (password1 == password2) {
                    true
                } else {
                    showErrorSnackBar(R.string.sign_up_passwords_different_error)
                    false
                }
            }
        }
    }

    fun onUserRegistrationSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.sign_up_success),
            Toast.LENGTH_LONG
        ).show()
        auth.signOut()
        finish()  // Returns to IntroActivity
    }
}
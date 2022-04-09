package com.example.triphub.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.get
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMainBinding
import com.example.triphub.databinding.AppBarMainBinding
import com.example.triphub.databinding.NavViewHeaderChatBinding
import com.example.triphub.databinding.NavViewHeaderMenuBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : BaseActivity<ActivityMainBinding>(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navHeaderMenuBinding: NavViewHeaderMenuBinding
    private lateinit var navHeaderChatBinding: NavViewHeaderChatBinding

    private lateinit var appBarMainBinding: AppBarMainBinding

    private val myProfileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                UserFireStore().loadUserData(this@MainActivity)
            } else {
                Log.e(
                    this.javaClass.simpleName,
                    "Did not received RESULT_OK from MyProfileActivity"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpNavigationDrawers()
        setUpOnClickListeners()

        UserFireStore().loadUserData(this@MainActivity)

    }

    private fun setUpOnClickListeners() {
        binding.appBarMain.fabCreateTrip.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }
    }

    fun onMenuDrawerClicked(view: View) {
        toggleLeftDrawer(binding.drawerMenu)
    }

    fun onChatDrawerClicked(view: View) {
        toggleRightDrawer(binding.drawerMenu)
    }

    private fun setUpNavigationDrawers() {
        val viewHeaderMenu = binding.navViewMenu.getHeaderView(0)
        navHeaderMenuBinding = NavViewHeaderMenuBinding.bind(viewHeaderMenu)
        val viewHeaderChat = binding.navViewChat.getHeaderView(0)
        navHeaderChatBinding = NavViewHeaderChatBinding.bind(viewHeaderChat)

        binding.navViewMenu.setNavigationItemSelectedListener(this)
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navMyProfile -> {
                myProfileLauncher.launch(Intent(this@MainActivity, MyProfileActivity::class.java))
            }
            R.id.navSignOut -> {
                Firebase.auth.signOut()

                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerMenu.closeDrawer(GravityCompat.START)
        return true
    }

    fun onLoadUserDataSuccess(user: User) {
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(navHeaderMenuBinding.ivNavUserImage)

        navHeaderMenuBinding.tvUsername.text = user.name
        navHeaderMenuBinding.tvEmail.text = user.email
    }

    fun onLoadUserDataFailure() {
        showErrorSnackBar(R.string.load_user_data_error)
    }

    override fun onBackPressed() {
        doubleBackToExit()
    }
}
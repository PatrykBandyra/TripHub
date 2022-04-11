package com.example.triphub.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.adapters.MyTripsAdapter
import com.example.triphub.databinding.ActivityMainBinding
import com.example.triphub.databinding.AppBarMainBinding
import com.example.triphub.databinding.NavViewHeaderChatBinding
import com.example.triphub.databinding.NavViewHeaderMenuBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class MainActivity : BaseActivity<ActivityMainBinding>(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navHeaderMenuBinding: NavViewHeaderMenuBinding
    private lateinit var navHeaderChatBinding: NavViewHeaderChatBinding

    private lateinit var appBarMainBinding: AppBarMainBinding
    private var userData: User? = null
    private val myTrips: ArrayList<MyTrip> = ArrayList()
    private var latestVisibleDocument: DocumentSnapshot? = null

    private var mIsFirstTripsLoad: Boolean = true
    private lateinit var mTripsAdapter: MyTripsAdapter

    private val myProfileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                UserFireStore().loadUserData(this@MainActivity)
            }
        }

    private val createNewTripLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                myTrips.clear()
                latestVisibleDocument = null
                MyTripFireStore().loadMyTripsList(this, latestVisibleDocument)
                Log.i("Main", "Everything fine")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpNavigationDrawers()
        setUpOnClickListeners()

        UserFireStore().loadUserData(this@MainActivity)
        MyTripFireStore().loadMyTripsList(this@MainActivity, latestVisibleDocument)
    }

    private fun setUpOnClickListeners() {
        binding.appBarMain.fabCreateTrip.setOnClickListener {
            if (userData != null) {
                val intent = Intent(this, AddTripActivity::class.java)
                intent.putExtra(Constants.Intent.USER_DATA, userData)
                createNewTripLauncher.launch(intent)
            } else {
                UserFireStore().loadUserData(this@MainActivity)
            }
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
        userData = user
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

    @SuppressLint("NotifyDataSetChanged")
    fun onMyTripsListLoadSuccess(
        latestDocument: DocumentSnapshot?,
        myTripsList: ArrayList<MyTrip>
    ) {
        latestVisibleDocument = latestDocument
        myTrips.addAll(myTripsList)
        if (myTrips.isNotEmpty()) {
            if (mIsFirstTripsLoad) {
                mIsFirstTripsLoad = false
                binding.appBarMain.mainContent.tvNoTrips.visibility = View.GONE
                binding.appBarMain.mainContent.rvTrips.visibility = View.VISIBLE
                binding.appBarMain.mainContent.rvTrips.layoutManager = LinearLayoutManager(this)
                mTripsAdapter = MyTripsAdapter(this@MainActivity, myTripsList)
                binding.appBarMain.mainContent.rvTrips.adapter = mTripsAdapter
                binding.appBarMain.mainContent.rvTrips.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
//                Log.i("Scroll", "${(view as RecyclerView).computeVerticalScrollRange()}")
//                if (scrollY == (view as RecyclerView).getChildAt(0).height - view.measuredHeight) {
//                    Log.i("Scroll", "DATA LOAD")
//                }
                    val layoutManager = ((view as RecyclerView).layoutManager as LinearLayoutManager)
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == myTrips.size - 1) {
                        Log.i("Main", "Loading more trips")
                        MyTripFireStore().loadMyTripsList(this@MainActivity, latestVisibleDocument)
                    }
                }
            } else {
                Log.i("notify", "notify")
                myTrips.addAll(myTripsList)
                mTripsAdapter.addItems(myTripsList)
                mTripsAdapter.notifyDataSetChanged()
            }


            // TODO: onClick listener
        } else {
            binding.appBarMain.mainContent.tvNoTrips.visibility = View.VISIBLE
            binding.appBarMain.mainContent.rvTrips.visibility = View.GONE
        }
    }

    fun onMyTripsListLoadFailure() {
        showErrorSnackBar(R.string.load_user_trips)
    }
}
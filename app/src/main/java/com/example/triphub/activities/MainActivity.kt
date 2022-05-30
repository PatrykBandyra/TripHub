package com.example.triphub.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.adapters.MyTripsAdapter
import com.example.triphub.adapters.PrivateChatAdapter
import com.example.triphub.databinding.ActivityMainBinding
import com.example.triphub.databinding.NavViewHeaderChatBinding
import com.example.triphub.databinding.NavViewHeaderMenuBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.example.triphub.utils.SwipeToDeleteCallback
import com.example.triphub.utils.SwipeToEditCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class MainActivity : BaseActivity<ActivityMainBinding>(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navHeaderMenuBinding: NavViewHeaderMenuBinding
    private lateinit var navHeaderChatBinding: NavViewHeaderChatBinding

    var userData: User? = null
    private val myTrips: ArrayList<MyTrip> = ArrayList()
    private var latestVisibleDocument: DocumentSnapshot? = null

    private var mIsFirstTripsLoad: Boolean = true
    private lateinit var mTripsAdapter: MyTripsAdapter

    private lateinit var mPrivateChatAdapter: PrivateChatAdapter

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
                mIsFirstTripsLoad = true
                latestVisibleDocument = null
                MyTripFireStore().loadMyTripsList(this, latestVisibleDocument)
                Log.i("Main", "Everything fine")
            }
        }

    var mTripEditPosition: Int? = null
    val editTripLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedTrip: MyTrip =
                    result.data!!.extras!!.getParcelable(Constants.Intent.TRIP_UPDATE)!!
                mTripsAdapter.items[mTripEditPosition!!] = updatedTrip
                mTripsAdapter.notifyItemChanged(mTripEditPosition!!)
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
            R.id.navFriends -> {
                startActivity(Intent(this@MainActivity, FriendsActivity::class.java))
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

        UserFireStore().getFriends(this, user)
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
                mTripsAdapter.setOnClickListener(object : MyTripsAdapter.OnClickListener {
                    override fun onClick(position: Int, myTrip: MyTrip) {
                        val intent = Intent(this@MainActivity, MyTripPeopleActivity::class.java)
                        intent.putExtra(Constants.Intent.TRIP, myTrip)
                        startActivity(intent)
                    }
                })

                binding.appBarMain.mainContent.rvTrips.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
                    val layoutManager =
                        ((view as RecyclerView).layoutManager as LinearLayoutManager)
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == myTrips.size - 1) {
                        Log.i("Main", "Loading more trips")
                        MyTripFireStore().loadMyTripsList(this@MainActivity, latestVisibleDocument)
                    }
                }

                val editSwipeHandler = object : SwipeToEditCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position: Int = viewHolder.adapterPosition
                        mTripEditPosition = position
                        mTripsAdapter.notifyItemChanged(position)  // To always remove green banner
                        mTripsAdapter.notifyEditItem(this@MainActivity, position)
                    }
                }
                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(binding.appBarMain.mainContent.rvTrips)

                val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val deleteDialog = AlertDialog.Builder(this@MainActivity)
                        deleteDialog.setIcon(R.drawable.ic_delete_red_24dp)
                        deleteDialog.setTitle(R.string.delete_trip)
                        deleteDialog.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                            mTripsAdapter.removeAt(this@MainActivity, viewHolder.adapterPosition)
                            dialog.dismiss()
                            showProgressDialog()
                        }
                        deleteDialog.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                            mTripsAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                        deleteDialog.show()
                    }
                }

                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(binding.appBarMain.mainContent.rvTrips)

            } else {
                Log.i("notify", "notify")
                myTrips.addAll(myTripsList)
                mTripsAdapter.addItems(myTripsList)
                mTripsAdapter.notifyDataSetChanged()
            }
        } else {
            binding.appBarMain.mainContent.tvNoTrips.visibility = View.VISIBLE
            binding.appBarMain.mainContent.rvTrips.visibility = View.GONE
        }
    }

    fun onMyTripsListLoadFailure() {
        showErrorSnackBar(R.string.load_user_trips)
    }

    fun onTripDeletionFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.trip_deletion_error)
    }

    fun onTripDeletionSuccess(position: Int) {
        hideProgressDialog()
        myTrips.removeAt(position)
    }

    fun onGetFriendsSuccess(friends: ArrayList<User>) {
        navHeaderChatBinding.rvChat.layoutManager = LinearLayoutManager(this)
        mPrivateChatAdapter = PrivateChatAdapter(this@MainActivity, friends)
        navHeaderChatBinding.rvChat.adapter = mPrivateChatAdapter
        mPrivateChatAdapter.setOnClickListener(object : PrivateChatAdapter.OnClickListener {
            override fun onClick(position: Int, friend: User) {
                val intent = Intent(this@MainActivity, PrivateChatActivity::class.java)
                intent.putExtra(Constants.Intent.USER_DATA, userData)
                intent.putExtra(Constants.Intent.FRIEND, friend)
                startActivity(intent)
            }
        })
    }

    fun onGetFriendsFailure() {
        showErrorSnackBar(R.string.could_not_load_private_chat)
    }
}
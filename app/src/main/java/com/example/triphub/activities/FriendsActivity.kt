package com.example.triphub.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.R
import com.example.triphub.adapters.FriendRequestsAdapter
import com.example.triphub.databinding.ActivityFriendsBinding
import com.example.triphub.databinding.DialogAddPersonBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.example.triphub.utils.SwipeToDeleteCallback
import com.example.triphub.utils.SwipeToEditCallback
import com.google.firebase.firestore.DocumentSnapshot
import java.util.HashMap

class FriendsActivity : BaseActivity<ActivityFriendsBinding>() {

    private lateinit var mUser: User

    // Friend requests
    private var mLatestVisibleDocumentFriendRequests: DocumentSnapshot? = null
    private var mIsFirstFriendRequestsLoad: Boolean = true
    private lateinit var mFriendRequestsAdapter: FriendRequestsAdapter
    private var mUserFriendRequestAcceptPosition: Int? = null

    private var latestVisibleDocumentFriends: DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActionBarForReturnAction(
            binding.toolbarFriendsActivity,
            icon = R.drawable.back_arrow_white
        )

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioFriends.id -> {
                    binding.llFriends.visibility = View.VISIBLE
                    binding.llRequests.visibility = View.GONE
                    binding.tvTitle.text = getString(R.string.my_friends)
                }
                binding.radioRequests.id -> {
                    binding.llRequests.visibility = View.VISIBLE
                    binding.llFriends.visibility = View.GONE
                    binding.tvTitle.text = getString(R.string.friend_requests)
                }
            }
        }

        binding.btnAddFriend.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = DialogAddPersonBinding.inflate(LayoutInflater.from(this))
            dialog.setContentView(dialogBinding.root)
            dialogBinding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.tvAdd.setOnClickListener {
                val email = dialogBinding.etEmailSearchMember.text.toString()
                if (email.isNotEmpty()) {
                    dialog.dismiss()
                    showProgressDialog(R.string.please_wait)
                    UserFireStore().getPersonDetails(this@FriendsActivity, email)
                } else {
                    Toast.makeText(
                        this@FriendsActivity,
                        getString(R.string.enter_person_email), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
        }

        UserFireStore().loadUserData(this)
    }

    override fun getViewBinding() = ActivityFriendsBinding.inflate(layoutInflater)

    fun sendFriendRequest(user: User) {
        val myId: String = UserFireStore().getCurrentUserId()
        if (user.friendRequests.contains(myId)) {
            hideProgressDialog()
            Toast.makeText(
                this,
                "This user has already received your friend request",
                Toast.LENGTH_SHORT
            ).show()
        } else if (user.id == myId) {
            hideProgressDialog()
            Toast.makeText(
                this,
                "You cannot send friend request to yourself",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            user.friendRequests.add(myId)
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.Models.User.FRIEND_REQUESTS] = user.friendRequests
            UserFireStore().updateUser(this, user.id, userHashMap)
        }
    }

    fun onNoSuchPerson() {
        hideProgressDialog()
        showErrorSnackBar(R.string.no_such_person)
    }

    fun onGetPersonDetailsFail() {
        hideProgressDialog()
        showErrorSnackBar(R.string.error_person_data)
    }

    fun onLoadUserDataSuccess(loggedInUser: User) {
        mUser = loggedInUser
        if (mUser.friendRequests.isNotEmpty()) {
            UserFireStore().loadUsersFromFriendRequests(
                this,
                mUser,
                mLatestVisibleDocumentFriendRequests
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onUsersFromFriendRequestsLoadSuccess(
        latestDocument: DocumentSnapshot?,
        friendRequestsUsers: ArrayList<User>
    ) {
        mLatestVisibleDocumentFriendRequests = latestDocument

        // Set up friend notifications recycler view
        if (mUser.friendRequests.isNotEmpty()) {
            if (mIsFirstFriendRequestsLoad) {
                mIsFirstFriendRequestsLoad = false
                mFriendRequestsAdapter = FriendRequestsAdapter(this, friendRequestsUsers)
                binding.rvRequests.layoutManager = LinearLayoutManager(this)
                mFriendRequestsAdapter = FriendRequestsAdapter(this, friendRequestsUsers)
                binding.rvRequests.adapter = mFriendRequestsAdapter

                // Set up scroll handler
                binding.rvRequests.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
                    val layoutManager =
                        ((view as RecyclerView).layoutManager as LinearLayoutManager)
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == mFriendRequestsAdapter.itemCount - 1) {
                        if (mUser.friendRequests.isNotEmpty()) {
                            UserFireStore().loadUsersFromFriendRequests(
                                this,
                                mUser,
                                mLatestVisibleDocumentFriendRequests
                            )
                        }
                    }
                }

                // Set up swipe handlers
                val acceptFriendRequestSwipeHandler =
                    object : SwipeToEditCallback(this, R.drawable.ic_checked_white) {
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position: Int = viewHolder.adapterPosition
                            mUserFriendRequestAcceptPosition = position

                            val acceptDialog = AlertDialog.Builder(this@FriendsActivity)
                            acceptDialog.setIcon(R.drawable.ic_checked_green)
                            acceptDialog.setTitle(R.string.accept_friend_request)
                            acceptDialog.setPositiveButton(R.string.yes) { dialog, _ ->
                                mFriendRequestsAdapter.notifyItemChanged(position)  // To remove green banner
                                mFriendRequestsAdapter.notifyAcceptFriendRequest(
                                    this@FriendsActivity,
                                    mUser,
                                    position
                                )
                                dialog.dismiss()
                                showProgressDialog()
                            }
                            acceptDialog.setNegativeButton(R.string.no) { dialog, _ ->
                                mFriendRequestsAdapter.notifyItemChanged(position)  // To remove green banner
                                dialog.dismiss()
                            }
                            acceptDialog.show()
                        }
                    }
                val editItemTouchHelper = ItemTouchHelper(acceptFriendRequestSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(binding.rvRequests)

                val deleteFriendRequestSwipeHandler = object : SwipeToDeleteCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position: Int = viewHolder.adapterPosition
                        mUserFriendRequestAcceptPosition = position

                        val deleteDialog = AlertDialog.Builder(this@FriendsActivity)
                        deleteDialog.setIcon(R.drawable.ic_delete_red_24dp)
                        deleteDialog.setTitle(R.string.delete_friend_request)
                        deleteDialog.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                            mFriendRequestsAdapter.removeAt(
                                this@FriendsActivity,
                                mUser,
                                position
                            )
                            mFriendRequestsAdapter.notifyItemChanged(viewHolder.adapterPosition) // To always remove red banner
                            dialog.dismiss()
                            showProgressDialog()
                        }
                        deleteDialog.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            mFriendRequestsAdapter.notifyItemChanged(viewHolder.adapterPosition) // To remove red banner
                            dialog.dismiss()
                        }
                        deleteDialog.show()
                    }
                }

                val deleteItemTouchHelper = ItemTouchHelper(deleteFriendRequestSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(binding.rvRequests)
            } else {
                mFriendRequestsAdapter.items.addAll(friendRequestsUsers)
                mFriendRequestsAdapter.notifyDataSetChanged()
            }
        }
    }

    fun onUsersFromFriendRequestsLoadFailure() {
        showErrorSnackBar(R.string.could_not_load_friend_requests)
    }

    fun onFriendRequestRemoval() {
        hideProgressDialog()
        Toast.makeText(
            this,
            "Friend request has been deleted",
            Toast.LENGTH_SHORT
        ).show()
        mFriendRequestsAdapter.items.removeAt(mUserFriendRequestAcceptPosition!!)
        mFriendRequestsAdapter.notifyItemRemoved(mUserFriendRequestAcceptPosition!!)
    }

    fun onFriendAdded() {
        hideProgressDialog()
        Toast.makeText(
            this,
            "New friend has been added",
            Toast.LENGTH_SHORT
        ).show()
        mFriendRequestsAdapter.items.removeAt(mUserFriendRequestAcceptPosition!!)
        mFriendRequestsAdapter.notifyItemRemoved(mUserFriendRequestAcceptPosition!!)
    }
}
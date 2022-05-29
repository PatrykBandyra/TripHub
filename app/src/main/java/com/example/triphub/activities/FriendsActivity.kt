package com.example.triphub.activities

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.adapters.FriendRequestsAdapter
import com.example.triphub.databinding.ActivityFriendsBinding
import com.example.triphub.databinding.DialogAddPersonBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import java.util.HashMap

class FriendsActivity : BaseActivity<ActivityFriendsBinding>() {

    private lateinit var mFriendRequestsAdapter: FriendRequestsAdapter

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
                }
                binding.radioRequests.id -> {
                    binding.llRequests.visibility = View.VISIBLE
                    binding.llFriends.visibility = View.GONE
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
}
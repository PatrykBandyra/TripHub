package com.example.triphub.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triphub.R
import com.example.triphub.adapters.MyTripChatAdapter
import com.example.triphub.databinding.ActivityMyTripChatBinding
import com.example.triphub.firebase.ChatMessageFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.MyTripChatMessage
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class MyTripChatActivity : BaseActivity<ActivityMyTripChatBinding>() {

    private lateinit var mTrip: MyTrip
    private lateinit var mUser: User
    private lateinit var mTripUsers: ArrayList<User>
    private lateinit var mChatAdapter: MyTripChatAdapter

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private val eventListener: EventListener<QuerySnapshot> =
        EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                val count: Int = mChatAdapter.messages.size
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val data = documentChange.document.data
                        val receiverIds: ArrayList<String> =
                            data[Constants.Models.MyTripChatMessage.RECEIVER_IDS] as ArrayList<String>
                        val chatMessage = MyTripChatMessage(
                            tripId = mTrip.documentId,
                            senderId = documentChange.document.getString(Constants.Models.MyTripChatMessage.SENDER_ID)!!,
                            receiverIds = receiverIds,
                            message = documentChange.document.getString(Constants.Models.MyTripChatMessage.MESSAGE)!!,
                            timestamp = documentChange.document.getLong(Constants.Models.MyTripChatMessage.TIMESTAMP)!!
                        )
                        Log.i("CHAT", chatMessage.toString())
                        mChatAdapter.messages.add(chatMessage)
                    }
                }
                mChatAdapter.messages.sortWith(Comparator { obj1, obj2 ->
                    obj1.timestamp.compareTo(obj2.timestamp)
                })
                if (count == 0) {
                    mChatAdapter.notifyDataSetChanged()
                } else {
                    mChatAdapter.notifyItemRangeInserted(count, mChatAdapter.messages.size)
                    binding.rvChat.smoothScrollToPosition(mChatAdapter.messages.size - 1)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No MyTrip object provided")
        }

        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No User object provided")
        }

        setUpTaskBarNavigation()

        UserFireStore().getUsersByIds(this, mTrip.userIDs)
    }

    private fun sendMessage() {
        val message = MyTripChatMessage(
            tripId = mTrip.documentId,
            senderId = mUser.id,
            receiverIds = mTrip.userIDs,
            message = binding.etInputMessage.text.toString(),
            timestamp = System.currentTimeMillis()
        )
        ChatMessageFireStore().sendMyTripMessage(message)
        binding.etInputMessage.setText("")
    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            startActivity(Intent(this, MyTripPeopleActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            startActivity(Intent(this, MyTripMapActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivBoard.setOnClickListener {
            startActivity(Intent(this, MyTripBoardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun getViewBinding() = ActivityMyTripChatBinding.inflate(layoutInflater)

    fun onUsersLoadSuccess(users: ArrayList<User>) {
        mTripUsers = users
        mChatAdapter = MyTripChatAdapter(this, arrayListOf(), mTripUsers, mUser)
        binding.rvChat.adapter = mChatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(this)

        binding.flSend.setOnClickListener {
            sendMessage()
        }
        Log.i("LISTENER", "SET LISTENER")
        ChatMessageFireStore().listenForMyTripMessages(mTrip.documentId, eventListener)
    }

    fun onUsersLoadFailure() {
        showErrorSnackBar(R.string.could_not_load_chat)
    }
}
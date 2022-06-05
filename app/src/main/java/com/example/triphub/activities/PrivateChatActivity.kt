package com.example.triphub.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.adapters.ChatAdapter
import com.example.triphub.databinding.ActivityPrivateChatBinding
import com.example.triphub.firebase.ChatMessageFireStore
import com.example.triphub.models.ChatMessage
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.ArrayList

class PrivateChatActivity : BaseActivity<ActivityPrivateChatBinding>() {

    private lateinit var mUser: User
    private lateinit var mFriend: User
    private lateinit var mChatAdapter: ChatAdapter

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener: EventListener<QuerySnapshot> =
        EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                val count: Int = mChatAdapter.messages.size
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage(
                            senderId = documentChange.document.getString(Constants.Models.ChatMessage.SENDER_ID)!!,
                            receiverId = documentChange.document.getString(Constants.Models.ChatMessage.RECEIVER_ID)!!,
                            message = documentChange.document.getString(Constants.Models.ChatMessage.MESSAGE)!!,
                            timestamp = documentChange.document.getLong(Constants.Models.ChatMessage.TIMESTAMP)!!
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
        setUpActionBarForReturnAction(binding.toolbar, title = R.string.empty)
        loadIntentData()
        mChatAdapter = ChatAdapter(this@PrivateChatActivity, arrayListOf(), mFriend, mUser)
        binding.rvChat.adapter = mChatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(this)

        binding.flSend.setOnClickListener {
            sendMessage()
        }

        ChatMessageFireStore().listenForMessages(mUser, mFriend, eventListener)
    }

    override fun getViewBinding() = ActivityPrivateChatBinding.inflate(layoutInflater)

    private fun loadIntentData() {
        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("Private chat activity did not receive user data")
        }
        if (intent.hasExtra(Constants.Intent.FRIEND)) {
            mFriend = intent.getParcelableExtra(Constants.Intent.FRIEND)!!
            binding.tvUsername.text = mFriend.name
            Glide.with(this)
                .load(mFriend.image)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.civUserImage)
        } else {
            throw Exception("Private chat activity did not receive friend data")
        }
    }

    private fun sendMessage() {
        val message = ChatMessage(
            senderId = mUser.id,
            receiverId = mFriend.id,
            message = binding.etInputMessage.text.toString(),
            timestamp = System.currentTimeMillis()
        )
        ChatMessageFireStore().sendMessage(message)
        binding.etInputMessage.setText("")
    }
}
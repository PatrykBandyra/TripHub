package com.example.triphub.firebase

import com.example.triphub.models.ChatMessage
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class ChatMessageFireStore : FireStoreBaseClass() {

    fun sendMessage(message: ChatMessage) {
        mFireStore.collection(Constants.Models.ChatMessage.CHAT_MESSAGES)
            .add(message)
    }

    fun listenForMessages(user1: User, user2: User, eventListener: EventListener<QuerySnapshot>) {
        mFireStore.collection(Constants.Models.ChatMessage.CHAT_MESSAGES)
            .whereEqualTo(Constants.Models.ChatMessage.SENDER_ID, user1.id)
            .whereEqualTo(Constants.Models.ChatMessage.RECEIVER_ID, user2.id)
            .addSnapshotListener(eventListener)
        mFireStore.collection(Constants.Models.ChatMessage.CHAT_MESSAGES)
            .whereEqualTo(Constants.Models.ChatMessage.SENDER_ID, user2.id)
            .whereEqualTo(Constants.Models.ChatMessage.RECEIVER_ID, user1.id)
            .addSnapshotListener(eventListener)
    }
}
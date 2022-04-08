package com.example.triphub.firebase

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class FireStoreBaseClass {

    protected val mFireStore = Firebase.firestore

    fun getCurrentUserId(): String = Firebase.auth.currentUser?.uid ?: ""

    fun getCurrentUser(): FirebaseUser? = Firebase.auth.currentUser
}
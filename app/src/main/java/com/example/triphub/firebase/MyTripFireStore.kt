package com.example.triphub.firebase

import android.util.Log
import com.example.triphub.R
import com.example.triphub.activities.AddTripActivity
import com.example.triphub.activities.BaseActivity
import com.example.triphub.activities.MainActivity
import com.example.triphub.activities.MyTripPeopleActivity
import com.example.triphub.adapters.MyTripsAdapter
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyTripFireStore : FireStoreBaseClass() {

    fun createMyTrip(activity: AddTripActivity, myTrip: MyTrip) {
        val docRef: DocumentReference =
            mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS).document()
        myTrip.documentId = docRef.id
        docRef.set(myTrip, SetOptions.merge())
            .addOnSuccessListener {
                activity.onCreateMyTripSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(javaClass.simpleName, e.message.toString())
                activity.onCreateMyTripFailure()
            }
    }

    fun updateMyTrip(activity: AddTripActivity, myTrip: MyTrip) {
        val tripHashmap = HashMap<String, Any>()
        tripHashmap[Constants.Models.MyTrip.NAME] = myTrip.name
        tripHashmap[Constants.Models.MyTrip.DESCRIPTION] = myTrip.description
        tripHashmap[Constants.Models.MyTrip.IMAGE] = myTrip.image
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(tripHashmap)
            .addOnSuccessListener {
                activity.onUpdateMyTripSuccess(myTrip)
            }
            .addOnFailureListener { e ->
                Log.e(javaClass.simpleName, e.message.toString())
                activity.onUpdateMyTripFailure()
            }
    }

    fun getMyTrip(activity: MyTripPeopleActivity, tripId: String) {
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(tripId)
            .get()
            .addOnSuccessListener { document ->
                val myTrip: MyTrip = document.toObject()!!
                activity.onGetMyTripSuccess(myTrip)
            }
            .addOnFailureListener {
                activity.showErrorSnackBar(R.string.could_not_refresh_trip)
            }
    }

    fun loadMyTripsList(activity: MainActivity) {
        val query = mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .whereArrayContains(Constants.Models.MyTrip.USER_IDS, getCurrentUserId())
            .orderBy(Constants.Models.MyTrip.CREATED_AT, Query.Direction.DESCENDING)
        query.limit(Constants.Models.MyTrip.LOAD_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                val myTripsList: ArrayList<MyTrip> = ArrayList()
                documentSnapshots.forEach {
                    val myTrip = it.toObject(MyTrip::class.java)
                    myTrip.documentId = it.id
                    myTripsList.add(myTrip)
                    Log.i("Loaded Trip", myTrip.toString())
                }
                activity.onMyTripsListLoadSuccess(myTripsList)
            }
            .addOnFailureListener {
                activity.onMyTripsListLoadFailure()
            }
    }

    fun deleteTrip(activity: MainActivity, adapter: MyTripsAdapter, myTrip: MyTrip, position: Int) {
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .delete()
            .addOnSuccessListener {
                adapter.onTripDeletionSuccess(position)
                activity.onTripDeletionSuccess(position)
            }
            .addOnFailureListener {
                activity.onTripDeletionFailure()
            }
    }

    fun addPersonToTrip(activity: MyTripPeopleActivity, myTrip: MyTrip) {
        val usersIDsHashMap = HashMap<String, Any>()
        usersIDsHashMap[Constants.Models.MyTrip.USER_IDS] = myTrip.userIDs
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(usersIDsHashMap)
            .addOnSuccessListener {
                activity.onAddPersonSuccess()
            }
            .addOnFailureListener {
                activity.onAddPersonFailure()
            }
    }

    fun removePersonFromTrip(
        activity: MyTripPeopleActivity,
        user: User,
        trip: MyTrip,
        position: Int
    ) {
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(trip.documentId)
            .update(Constants.Models.MyTrip.USER_IDS, FieldValue.arrayRemove(user.id))
            .addOnSuccessListener {
                activity.onPersonRemovalSuccess(position)
            }
            .addOnFailureListener {
                activity.onPersonRemovalFailure(position)
            }

    }

    fun loadPeopleAssignedToTrip(activity: MyTripPeopleActivity, userIds: ArrayList<String>) {
        mFireStore.collection(Constants.Models.User.USERS)
            .whereIn(Constants.Models.User.ID, userIds).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val usersList: ArrayList<User> = arrayListOf()
                    documents.forEach { document ->
                        val user: User = document.toObject()
                        usersList.add(user)
                    }
                    activity.onPeopleLoadedSuccess(usersList)
                }
            }
            .addOnFailureListener { e ->
                activity.onPeopleLoadedFailure()
            }
    }
}
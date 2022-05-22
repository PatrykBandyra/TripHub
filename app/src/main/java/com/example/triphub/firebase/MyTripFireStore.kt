package com.example.triphub.firebase

import android.util.Log
import com.example.triphub.activities.AddTripActivity
import com.example.triphub.activities.BaseActivity
import com.example.triphub.activities.MainActivity
import com.example.triphub.activities.MyTripPeopleActivity
import com.example.triphub.adapters.MyTripsAdapter
import com.example.triphub.models.MyTrip
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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

    fun loadMyTripsList(activity: MainActivity, latestVisibleDocument: DocumentSnapshot?) {
        var query = mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .whereArrayContains(Constants.Models.MyTrip.USER_IDS, getCurrentUserId())
            .orderBy(Constants.Models.MyTrip.CREATED_AT, Query.Direction.DESCENDING)
        if (latestVisibleDocument != null) {
            query = query.startAfter(latestVisibleDocument.toObject(MyTrip::class.java)!!.createdAt)
        }
        query.limit(Constants.Models.MyTrip.LOAD_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                var latestDocument: DocumentSnapshot? = null
                if (documentSnapshots.size() > 0) {
                    latestDocument = documentSnapshots.documents[documentSnapshots.size() - 1]
                }
                Log.i("Latest Document", latestDocument.toString())
                val myTripsList: ArrayList<MyTrip> = ArrayList()
                documentSnapshots.forEach {
                    val myTrip = it.toObject(MyTrip::class.java)
                    myTrip.documentId = it.id
                    myTripsList.add(myTrip)
                    Log.i("Loaded Trip", myTrip.toString())
                }
                activity.onMyTripsListLoadSuccess(latestDocument, myTripsList)
            }
            .addOnFailureListener {
                it.printStackTrace()
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
}
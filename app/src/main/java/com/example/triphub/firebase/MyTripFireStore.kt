package com.example.triphub.firebase

import android.app.Activity
import android.util.Log
import com.example.triphub.R
import com.example.triphub.activities.*
import com.example.triphub.adapters.MyTripsAdapter
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject

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

    fun getMyTrip(activity: Activity, tripId: String) {
        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(tripId)
            .get()
            .addOnSuccessListener { document ->
                when (activity) {
                    is MyTripPeopleActivity -> {
                        val myTrip: MyTrip = document.toObject()!!
                        activity.onGetMyTripSuccess(myTrip)

                    }
                    is MyTripBoardActivity -> {
                        val myTrip: MyTrip = document.toObject()!!
                        activity.onGetMyTripSuccess(myTrip)
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is MyTripPeopleActivity -> {
                        activity.showErrorSnackBar(R.string.could_not_refresh_trip)
                    }
                    is MyTripBoardActivity -> {
                        activity.showErrorSnackBar(R.string.could_not_refresh_trip)
                    }
                }
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

    fun loadPeopleAssignedToTrip(activity: Activity, userIds: ArrayList<String>) {
        mFireStore.collection(Constants.Models.User.USERS)
            .whereIn(Constants.Models.User.ID, userIds).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val usersList: ArrayList<User> = arrayListOf()
                    documents.forEach { document ->
                        val user: User = document.toObject()
                        usersList.add(user)
                    }
                    when (activity) {
                        is MyTripPeopleActivity -> {
                            activity.onPeopleLoadedSuccess(usersList)
                        }
                        is MyTripBoardActivity -> {
                            activity.onPeopleLoadedSuccess(usersList)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MyTripPeopleActivity -> {
                        activity.onPeopleLoadedFailure()
                    }
                    is MyTripBoardActivity -> {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar(R.string.could_not_load_people_trip)
                    }
                }
            }
    }

    fun addUpdateTaskList(activity: Activity, myTrip: MyTrip) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.Models.MyTrip.TASK_LIST] = myTrip.taskList

        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is MyTripBoardActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is MyTripCardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { exception ->
                if (activity is BaseActivity<*>) {
                    activity.hideProgressDialog()
                }
                Log.e("HERE", "Error while updating a taskList", exception)
            }
    }

    fun updatePlaces(myTrip: MyTrip) {
        val placesHashmap = HashMap<String, Any>()
        placesHashmap[Constants.Models.MyTrip.PLACES] = myTrip.places

        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(placesHashmap)
    }

    fun updateCircles(myTrip: MyTrip) {
        val circlesHashmap = HashMap<String, Any>()
        circlesHashmap[Constants.Models.MyTrip.CIRCLES] = myTrip.circles

        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(circlesHashmap)
    }

    fun updatePolylines(myTrip: MyTrip) {
        val polylinesHashmap = HashMap<String, Any>()
        polylinesHashmap[Constants.Models.MyTrip.POLYLINES] = myTrip.polylines

        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(polylinesHashmap)
    }

    fun updatePolygons(myTrip: MyTrip) {
        val polygonsHashmap = HashMap<String, Any>()
        polygonsHashmap[Constants.Models.MyTrip.POLYGONS] = myTrip.polygons

        mFireStore.collection(Constants.Models.MyTrip.MY_TRIPS)
            .document(myTrip.documentId)
            .update(polygonsHashmap)
    }
}
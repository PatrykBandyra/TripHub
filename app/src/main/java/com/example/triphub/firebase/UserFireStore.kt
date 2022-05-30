package com.example.triphub.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.activities.*
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class UserFireStore : FireStoreBaseClass() {

    fun registerUser(activity: SignUpActivity, user: User) {
        mFireStore.collection(Constants.Models.User.USERS)
            .document(getCurrentUserId())
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                activity.onUserRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "ERROR: ${e.message}")
            }
    }

    fun loadUserData(activity: Activity) {
        mFireStore.collection(Constants.Models.User.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null) {
                    when (activity) {
                        is MainActivity -> {
                            activity.onLoadUserDataSuccess(loggedInUser)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                        is FriendsActivity -> {
                            activity.onLoadUserDataSuccess(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.onLoadUserDataFailure()
                    }
                    is FriendsActivity -> {
                        activity.showErrorSnackBar(R.string.could_not_load_user_data)
                    }
                }
                Log.e(activity.javaClass.simpleName, "ERROR: $e")
            }
    }

    fun updateUser(
        activity: Activity,
        userId: String,
        userHashMap: HashMap<String, Any>,
        isFromAdapter: Boolean = false,
        isAddition: Boolean = false,
        isFriendRequest: Boolean = false
    ) {
        mFireStore.collection(Constants.Models.User.USERS)
            .document(userId)
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                        Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                        Toast.makeText(
                            activity,
                            "Profile data updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        activity.onUserUpdateSuccess()
                    }
                    is FriendsActivity -> {
                        if (isFromAdapter) {
                            if (isAddition) {
                                activity.onFriendAdded()
                            } else {
                                if (isFriendRequest) {
                                    activity.onFriendRequestRemoval()
                                } else {
                                    activity.onFriendRemoval()
                                }
                            }
                        } else {
                            activity.hideProgressDialog()
                            Toast.makeText(
                                activity,
                                "Friend request has been sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is MyProfileActivity -> {
                        activity.onUserUpdateFailure()
                    }
                    is FriendsActivity -> {
                        if (isFromAdapter) {
                            if (isAddition) {
                                if (isFriendRequest) {
                                    activity.showErrorSnackBar(R.string.could_not_add_new_friend)
                                }
                            } else {
                                if (isFriendRequest) {
                                    activity.showErrorSnackBar(R.string.could_not_remove_friend_request)
                                } else {
                                    activity.showErrorSnackBar(R.string.could_not_remove_friend)
                                }
                            }
                        } else {
                            activity.showErrorSnackBar(R.string.could_not_send_friend_request)
                        }
                    }
                }
            }
    }

    fun getPersonDetails(activity: Activity, email: String) {
        mFireStore.collection(Constants.Models.User.USERS)
            .whereEqualTo(Constants.Models.User.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    when (activity) {
                        is MyTripPeopleActivity -> activity.personDetails(user)
                        is FriendsActivity -> activity.sendFriendRequest(user)
                    }
                } else {
                    when (activity) {
                        is MyTripPeopleActivity -> activity.onNoSuchPerson()
                        is FriendsActivity -> activity.onNoSuchPerson()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while getting user details", e)
                when (activity) {
                    is MyTripPeopleActivity -> activity.onGetPersonDetailsFail()
                    is FriendsActivity -> activity.onGetPersonDetailsFail()
                }
            }
    }

    fun loadUsersFromFriendRequests(
        activity: FriendsActivity,
        user: User,
        latestVisibleDocument: DocumentSnapshot?
    ) {
        var query = mFireStore.collection(Constants.Models.User.USERS)
            .whereIn(Constants.Models.User.ID, user.friendRequests)
            .orderBy(Constants.Models.User.NAME, Query.Direction.DESCENDING)
        if (latestVisibleDocument != null) {
            query = query.startAfter(latestVisibleDocument.toObject(User::class.java)!!.name)
        }
        query.limit(Constants.Models.User.LOAD_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                var latestDocument: DocumentSnapshot? = null
                if (documentSnapshots.size() > 0) {
                    latestDocument = documentSnapshots.documents[documentSnapshots.size() - 1]
                }
                val friendRequestsUsers: ArrayList<User> = arrayListOf()
                documentSnapshots.forEach {
                    val friendRequestUser = it.toObject(User::class.java)
                    friendRequestsUsers.add(friendRequestUser)
                }
                activity.onUsersFromFriendRequestsLoadSuccess(latestDocument, friendRequestsUsers)
            }
            .addOnFailureListener {
                activity.onUsersFromFriendRequestsLoadFailure()
            }
    }

    fun loadFriends(
        activity: FriendsActivity,
        user: User,
        latestVisibleDocument: DocumentSnapshot?
    ) {
        var query = mFireStore.collection(Constants.Models.User.USERS)
            .whereIn(Constants.Models.User.ID, user.friendIds)
            .orderBy(Constants.Models.User.NAME, Query.Direction.DESCENDING)
        if (latestVisibleDocument != null) {
            query = query.startAfter(latestVisibleDocument.toObject(User::class.java)!!.name)
        }
        query.limit(Constants.Models.User.LOAD_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                var latestDocument: DocumentSnapshot? = null
                if (documentSnapshots.size() > 0) {
                    latestDocument = documentSnapshots.documents[documentSnapshots.size() - 1]
                }
                val friends: ArrayList<User> = arrayListOf()
                documentSnapshots.forEach {
                    val friend = it.toObject(User::class.java)
                    friends.add(friend)
                }
                activity.onFriendsLoadSuccess(latestDocument, friends)
            }
            .addOnFailureListener {
                activity.onFriendsLoadFailure()
            }
    }

}
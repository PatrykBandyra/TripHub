package com.example.triphub.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.activities.*
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
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
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.onLoadUserDataFailure()
                    }
                }
                Log.e(activity.javaClass.simpleName, "ERROR: ${e.toString()}")
            }
    }

    fun updateUser(activity: Activity, userId: String, userHashMap: HashMap<String, Any>) {
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
                        activity.hideProgressDialog()
                        Toast.makeText(
                            activity,
                            "Friend request has been sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is MyProfileActivity -> {
                        activity.onUserUpdateFailure()
                    }
                    is FriendsActivity -> {
                        Toast.makeText(
                            activity,
                            "Could not send friend request",
                            Toast.LENGTH_SHORT
                        ).show()
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
}
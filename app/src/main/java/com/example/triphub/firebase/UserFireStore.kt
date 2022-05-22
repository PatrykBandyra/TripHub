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

    fun updateUser(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.Models.User.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                Toast.makeText(activity, "Profile data updated successfully", Toast.LENGTH_SHORT)
                    .show()
                when (activity) {
                    is MyProfileActivity -> {
                        activity.onUserUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is MyProfileActivity -> {
                        activity.onUserUpdateFailure()
                    }
                }
            }
    }

    fun getPersonDetails(activity: MyTripPeopleActivity, email: String) {
        mFireStore.collection(Constants.Models.User.USERS)
            .whereEqualTo(Constants.Models.User.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.personDetails(user)
                } else {
                    activity.onNoSuchPerson()
                }
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while getting user details", e)
                activity.onGetPersonDetailsFail()
            }
    }
}
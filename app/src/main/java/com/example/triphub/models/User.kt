package com.example.triphub.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val friendIds: ArrayList<String> = arrayListOf(),
    val friendRequests: ArrayList<String> = arrayListOf(),
    var selected: Boolean = false
) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createStringArrayList()!!,
        parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(image)
        parcel.writeStringList(friendIds)
        parcel.writeStringList(friendRequests)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

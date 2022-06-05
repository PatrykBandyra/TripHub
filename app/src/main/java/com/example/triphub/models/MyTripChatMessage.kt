package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyTripChatMessage(
    val tripId: String,
    val senderId: String,
    val receiverIds: ArrayList<String>,
    val message: String,
    val timestamp: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tripId)
        parcel.writeString(senderId)
        parcel.writeStringList(receiverIds)
        parcel.writeString(message)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyTripChatMessage> {
        override fun createFromParcel(parcel: Parcel): MyTripChatMessage {
            return MyTripChatMessage(parcel)
        }

        override fun newArray(size: Int): Array<MyTripChatMessage?> {
            return arrayOfNulls(size)
        }
    }
}

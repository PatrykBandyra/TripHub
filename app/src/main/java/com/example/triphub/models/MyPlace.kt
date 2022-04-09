package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyPlace(
    val name: String = "",
    val snippet: String = "",
    val description: String = "",
    val images: ArrayList<String> = ArrayList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val markerColor: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(snippet)
        parcel.writeString(description)
        parcel.writeStringList(images)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(markerColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyPlace> {
        override fun createFromParcel(parcel: Parcel): MyPlace {
            return MyPlace(parcel)
        }

        override fun newArray(size: Int): Array<MyPlace?> {
            return arrayOfNulls(size)
        }
    }
}

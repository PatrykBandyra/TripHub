package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyTrip(
    val id: String ="",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val users: ArrayList<String> = ArrayList(),
    val places: ArrayList<MyPlace> = ArrayList(),
    val polylines: ArrayList<MyPolyline> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(MyPlace.CREATOR)!!,
        parcel.createTypedArrayList(MyPolyline.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(image)
        parcel.writeStringList(users)
        parcel.writeTypedList(places)
        parcel.writeTypedList(polylines)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyTrip> {
        override fun createFromParcel(parcel: Parcel): MyTrip {
            return MyTrip(parcel)
        }

        override fun newArray(size: Int): Array<MyTrip?> {
            return arrayOfNulls(size)
        }
    }
}

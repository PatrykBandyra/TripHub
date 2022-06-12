package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyPolygon(
    val points: ArrayList<MyPoint> = ArrayList(),
    val fillColor: Int = 0,
    val strokeColor: Int = 0,
    val strokePattern: String = "",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(MyPoint.CREATOR)!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(points)
        parcel.writeInt(fillColor)
        parcel.writeInt(strokeColor)
        parcel.writeString(strokePattern)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyPolygon> {
        override fun createFromParcel(parcel: Parcel): MyPolygon {
            return MyPolygon(parcel)
        }

        override fun newArray(size: Int): Array<MyPolygon?> {
            return arrayOfNulls(size)
        }
    }
}

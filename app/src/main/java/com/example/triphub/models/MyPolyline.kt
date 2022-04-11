package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyPolyline(
    val points: ArrayList<MyPoint> = ArrayList(),
    val color: String = "",
    val pattern: String = "",  // dot, dash, solid
    val gap: Float = 0f
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(MyPoint.CREATOR)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(points)
        parcel.writeString(color)
        parcel.writeString(pattern)
        parcel.writeFloat(gap)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyPolyline> {
        override fun createFromParcel(parcel: Parcel): MyPolyline {
            return MyPolyline(parcel)
        }

        override fun newArray(size: Int): Array<MyPolyline?> {
            return arrayOfNulls(size)
        }
    }
}

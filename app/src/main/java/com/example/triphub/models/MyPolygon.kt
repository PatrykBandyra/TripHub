package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyPolygon(
    val points: ArrayList<MyPoint> = ArrayList(),
    val fillColor: String = "",
    val strokeColor: String = "",
    val strokePattern: String = "",
    val strokeGap: Float = 0f,
    val holePoints: ArrayList<MyPoint> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(MyPoint.CREATOR)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.createTypedArrayList(MyPoint.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(points)
        parcel.writeString(fillColor)
        parcel.writeString(strokeColor)
        parcel.writeString(strokePattern)
        parcel.writeFloat(strokeGap)
        parcel.writeTypedList(holePoints)
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

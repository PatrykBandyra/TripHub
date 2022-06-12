package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyCircle(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val fillColor: Int = 0,
    val strokeColor: Int = 0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(radius)
        parcel.writeInt(fillColor)
        parcel.writeInt(strokeColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyCircle> {
        override fun createFromParcel(parcel: Parcel): MyCircle {
            return MyCircle(parcel)
        }

        override fun newArray(size: Int): Array<MyCircle?> {
            return arrayOfNulls(size)
        }
    }

}

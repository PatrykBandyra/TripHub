package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyPoint> {
        override fun createFromParcel(parcel: Parcel): MyPoint {
            return MyPoint(parcel)
        }

        override fun newArray(size: Int): Array<MyPoint?> {
            return arrayOfNulls(size)
        }
    }
}

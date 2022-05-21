package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyTrip(
    var documentId: String = "",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val creatorID: String = "",
    val creatorName: String = "",
    val userIDs: ArrayList<String> = ArrayList(),
    val places: ArrayList<MyPlace> = ArrayList(),
    val polylines: ArrayList<MyPolyline> = ArrayList(),
    val polygons: ArrayList<MyPolygon> = ArrayList(),
    val circles: ArrayList<MyCircle> = ArrayList(),
    val createdAt: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(MyPlace.CREATOR)!!,
        parcel.createTypedArrayList(MyPolyline.CREATOR)!!,
        parcel.createTypedArrayList(MyPolygon.CREATOR)!!,
        parcel.createTypedArrayList(MyCircle.CREATOR)!!,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(image)
        parcel.writeString(creatorID)
        parcel.writeString(creatorName)
        parcel.writeStringList(userIDs)
        parcel.writeTypedList(places)
        parcel.writeTypedList(polylines)
        parcel.writeTypedList(polygons)
        parcel.writeTypedList(circles)
        parcel.writeLong(createdAt)
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

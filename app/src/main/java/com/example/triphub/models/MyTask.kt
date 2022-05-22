package com.example.triphub.models

import android.os.Parcel
import android.os.Parcelable

data class MyTask(
    val title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<MyCard> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(MyCard.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(createdBy)
        parcel.writeTypedList(cards)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyTask> {
        override fun createFromParcel(parcel: Parcel): MyTask {
            return MyTask(parcel)
        }

        override fun newArray(size: Int): Array<MyTask?> {
            return arrayOfNulls(size)
        }
    }
}


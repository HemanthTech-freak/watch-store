package com.cherry.watch_store

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//Creating a parcel class to pass the data through intents
@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description : String = "",
    val imageUrl: String = ""
) : Parcelable

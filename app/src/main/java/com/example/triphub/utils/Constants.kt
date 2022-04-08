package com.example.triphub.utils

object Constants {

    object Models {
        object User {
            const val USERS: String = "Users"
            const val ID: String = "id"
            const val NAME: String = "name"
            const val EMAIL: String = "email"
            const val IMAGE: String = "image"
        }
    }

    object Intent {
        const val USER_DATA: String = "userData"
        const val ERROR: String = "error"
    }

    object Files {
        const val IMAGE_DIRECTORY: String = "TripHubImages"
    }

    object Navigation {
        const val MARKER: String = "Marker"
        const val POLYLINE: String = "Polyline"
        const val POLYGON: String = "Polygon"
        const val CIRCLE: String = "Circle"
    }
}
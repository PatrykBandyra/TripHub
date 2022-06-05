package com.example.triphub.utils

object Constants {

    object Models {
        object User {
            const val USERS: String = "Users"
            const val ID: String = "id"
            const val NAME: String = "name"
            const val EMAIL: String = "email"
            const val IMAGE: String = "image"
            const val FRIEND_IDS: String = "friendIds"
            const val FRIEND_REQUESTS: String = "friendRequests"
            const val LOAD_LIMIT: Long = 8
        }

        object MyTrip {
            const val MY_TRIPS: String = "MyTrips"
            const val NAME: String = "name"
            const val DESCRIPTION: String = "description"
            const val IMAGE: String = "image"
            const val CREATOR_ID: String = "creatorId"
            const val USER_IDS: String = "userIDs"
            const val PLACES: String = "places"
            const val POLYLINES: String = "polylines"
            const val POLYGONS: String = "polygons"
            const val CIRCLES: String = "circles"
            const val TASK_LIST: String = "taskList"
            const val CREATED_AT: String = "createdAt"
            const val LOAD_LIMIT: Long = 8
        }

        object ChatMessage {
            const val CHAT_MESSAGES: String = "ChatMessages"
            const val SENDER_ID: String = "senderId"
            const val RECEIVER_ID: String = "receiverId"
            const val MESSAGE: String = "message"
            const val TIMESTAMP: String = "timestamp"
        }

        object MyTripChatMessage {
            const val CHAT_MESSAGES: String = "MyTripChatMessages"
            const val MY_TRIP_ID: String = "tripId"
            const val SENDER_ID: String = "senderId"
            const val RECEIVER_IDS: String = "receiverIds"
            const val MESSAGE: String = "message"
            const val TIMESTAMP: String = "timestamp"
        }
    }

    object Intent {
        const val FRIEND: String = "friend"
        const val USER_DATA: String = "userData"
        const val ERROR: String = "error"

        const val TRIP: String = "trip"
        const val TRIP_UPDATE: String = "trip_update"
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
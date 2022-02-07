package com.gajanan.messenger_chat.utils

object Constants {

    const val DB_COLLECTION_KEY = "users"
    const val KEY_NAME = "name"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"
    const val KEY_PREFERENCE_NAME = "chatAppPreference"
    const val KEY_IS_SIGNED_IN = "isSignedIn"
    const val KEY_USER_ID = "userId"
    const val KEY_IMAGE = "image"
    const val KEY_SEND_IMAGE = "image"
    const val KEY_FCM_TOKEN = "fcmToken"
    const val REMOTE_MSG_AUTHORIZATION = "Authorization"
    const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
    const val REMOTE_MSG_DATA = "data"
    const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
    var remoteMsgHeaders : HashMap<String,String>? = null


    const val KEY_USER = "user"
    const val KEY_COLLECTION_CHAT = "chat"
    const val KEY_SENDER_ID = "senderId"
    const val KEY_RECEIVER_ID = "receiverId"
    const val KEY_MESSAGE = "message"
    const val KEY_TIMESTAMP = "timestamp"

    const val KEY_COLLECTION_CONVERSATION = "conversations"
    const val KEY_SENDER_NAME = "senderName"
    const val KEY_RECEIVER_NAME = "receiverName"
    const val KEY_SENDER_IMAGE = "senderImage"
    const val KEY_RECEIVER_IMAGE = "receiverImage"
    const val KEY_LAST_MESSAGE = "LastMessage"

    const val KEY_AVAILABILITY = "availability"



}
fun getRemoteMsgHeaders() : HashMap<String,String>{
    if (Constants.remoteMsgHeaders == null){
        Constants.remoteMsgHeaders = HashMap()
        Constants.remoteMsgHeaders?.put(
            Constants.REMOTE_MSG_AUTHORIZATION,
            "key=AAAAhV4JHO0:APA91bEH1PkuI9XgYZekk5qktog2AHTeInc7vgXtsW4reP-Z6AOHi_7EA3iQm4kdxB3AR1ZYUZ0Z9GZdFG43WoYAW-GKjAyqIrw9-ueilWHtXhDi0nIuKrxCqJjEdSCjlUsY6IDKov66"
        )
        Constants.remoteMsgHeaders?.put(
            Constants.REMOTE_MSG_CONTENT_TYPE,
            "application/json")
    }
    return Constants.remoteMsgHeaders!!
}

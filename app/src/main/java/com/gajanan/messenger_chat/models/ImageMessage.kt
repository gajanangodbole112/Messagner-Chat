package com.gajanan.messenger_chat.models

import java.io.Serializable
import java.util.*

data class ImageMessage (
    var senderId : String? = null,
    var receiverId : String? = null,
    var imagePath : String? = null,
    var time : String? = null,
    var date : Date?=null,
): Serializable
package com.gajanan.messenger_chat.models

import java.io.Serializable
import java.util.*

data class ChatMessage(

    var senderId : String? = null,
    var receiverId : String? = null,
    var message : String? = null,
    var time : String? = null,
    var date : Date?=null,

    var conversationId : String?=null,
    var conversationName : String? = null,
    var conversationImg : String? = null

): Serializable

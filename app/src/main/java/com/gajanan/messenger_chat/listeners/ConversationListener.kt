package com.gajanan.messenger_chat.listeners

import com.gajanan.messenger_chat.models.User

interface ConversationListener {

  fun  onConversationClicked(user : User)
}
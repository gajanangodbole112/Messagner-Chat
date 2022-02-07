package com.gajanan.messenger_chat.listeners

import com.gajanan.messenger_chat.models.User

interface UserListener {

    fun onUserClicked(user : User)
}
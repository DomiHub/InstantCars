package com.domingo.instantcars

import com.google.firebase.Timestamp

data class ChatPreview(
    var chatId: String = "",
    var user1: String = "",
    var user2: String = "",
    var userIds: List<String> = listOf(),
    var lastMessage: String = "",
    var timestamp: Timestamp? = null
)

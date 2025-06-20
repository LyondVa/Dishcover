package com.nhatpham.dishcover.domain.model.chatbot

import com.google.firebase.Timestamp
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val sender: MessageSender = MessageSender.USER,
    val timestamp: Timestamp = Timestamp.now()
)

enum class MessageSender {
    USER, BOT
}
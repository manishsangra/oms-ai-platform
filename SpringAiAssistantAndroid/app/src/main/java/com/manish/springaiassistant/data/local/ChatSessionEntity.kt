
package com.manish.springaiassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(

    @PrimaryKey
    val id: String,

    val backendConversationId: String? = null,

    val title: String = "New Chat",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)

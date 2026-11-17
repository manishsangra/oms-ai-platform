package com.manish.springaiassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(

    @PrimaryKey
    val id: String,

    val backendConversationId: String? = null,

    val title: String,

    val createdAt: Long,

    val updatedAt: Long
)
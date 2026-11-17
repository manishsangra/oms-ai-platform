package com.manish.springaiassistant.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversationId")
    ]
)
data class MessageEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val conversationId: String,

    val text: String,

    val isUser: Boolean,

    val createdAt: Long
)

package com.manish.springaiassistant.data.local

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao
) {

    // Create a new local conversation
    suspend fun createSession(): ChatSessionEntity {

        val now = System.currentTimeMillis()

        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = "New Chat",
            createdAt = now,
            updatedAt = now
        )

        chatDao.insertSession(session)

        return session
    }

    // Save a user or AI message
    suspend fun saveMessage(
        sessionId: String,
        text: String,
        isUser: Boolean
    ) {

        chatDao.insertMessage(
            ChatMessageEntity(
                chatSessionId = sessionId,
                text = text,
                isUser = isUser
            )
        )

        val now = System.currentTimeMillis()

        // Use the first user message as the conversation title
        if (isUser) {

            val session = chatDao.getSession(sessionId)

            if (session?.title == "New Chat") {

                val title = text
                    .replace("\n", " ")
                    .trim()
                    .take(40)

                chatDao.updateSessionTitle(
                    sessionId = sessionId,
                    title = title,
                    updatedAt = now
                )
            }
        }

        chatDao.updateSessionActivity(
            sessionId = sessionId,
            updatedAt = now
        )
    }

    // Save backend conversation ID
    suspend fun updateBackendConversationId(
        sessionId: String,
        backendId: String
    ) {

        chatDao.updateBackendConversationId(
            sessionId = sessionId,
            backendId = backendId
        )
    }

    // Get all saved conversations
    fun getAllSessions(): Flow<List<ChatSessionEntity>> {

        return chatDao.getAllSessions()
    }

    // Observe messages for a conversation
    fun observeMessages(
        sessionId: String
    ): Flow<List<ChatMessageEntity>> {

        return chatDao.observeMessages(sessionId)
    }

    // Load messages for a conversation
    suspend fun getMessages(
        sessionId: String
    ): List<ChatMessageEntity> {

        return chatDao.getMessages(sessionId)
    }

    // Get the latest conversation
    suspend fun getLatestSession(): ChatSessionEntity? {

        return chatDao.getLatestSession()
    }

    // Get a conversation by ID
    suspend fun getSession(
        sessionId: String
    ): ChatSessionEntity? {

        return chatDao.getSession(sessionId)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }
}

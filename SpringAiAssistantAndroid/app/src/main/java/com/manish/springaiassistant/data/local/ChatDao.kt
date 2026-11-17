
package com.manish.springaiassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("""
        SELECT * FROM chat_sessions
        ORDER BY updatedAt DESC
        LIMIT 1
    """)
    suspend fun getLatestSession(): ChatSessionEntity?

    @Query("""
        SELECT * FROM chat_sessions
        WHERE id = :sessionId
        LIMIT 1
    """)
    suspend fun getSession(sessionId: String): ChatSessionEntity?

    @Query("""
        SELECT * FROM chat_messages
        WHERE chatSessionId = :sessionId
        ORDER BY timestamp ASC, id ASC
    """)
    suspend fun getMessages(sessionId: String): List<ChatMessageEntity>

    @Query("""
        SELECT * FROM chat_messages
        WHERE chatSessionId = :sessionId
        ORDER BY timestamp ASC, id ASC
    """)
    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_sessions
        ORDER BY updatedAt DESC
    """)
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("""
        UPDATE chat_sessions
        SET backendConversationId = :backendId,
            updatedAt = :updatedAt
        WHERE id = :sessionId
    """)
    suspend fun updateBackendConversationId(
        sessionId: String,
        backendId: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE chat_sessions
        SET title = :title,
            updatedAt = :updatedAt
        WHERE id = :sessionId
    """)
    suspend fun updateSessionTitle(
        sessionId: String,
        title: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE chat_sessions
        SET updatedAt = :updatedAt
        WHERE id = :sessionId
    """)
    suspend fun updateSessionActivity(
        sessionId: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        DELETE FROM chat_sessions
        WHERE id = :sessionId
    """)
    suspend fun deleteSession(sessionId: String)
}

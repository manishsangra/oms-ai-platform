package com.manish.springaiassistant.data.remote

import com.manish.springaiassistant.data.model.ChatRequest
import com.manish.springaiassistant.data.model.ChatResponse
import com.manish.springaiassistant.data.model.WeatherSummary
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AiApiService {

    @POST("api/ai/chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse

    @GET("api/ai/structured-weather")
    suspend fun getStructuredWeather(
        @Query("city") city: String,
        @Query("conversationId") conversationId: String
    ): WeatherSummary
}
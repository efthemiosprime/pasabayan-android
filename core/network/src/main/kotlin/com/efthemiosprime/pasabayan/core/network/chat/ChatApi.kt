package com.efthemiosprime.pasabayan.core.network.chat

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ChatApi {

    @GET("config/broadcasting")
    suspend fun getBroadcastingConfig(): Response<BroadcastingConfigResponseJson>

    @GET("chat/conversations")
    suspend fun getConversations(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
        @Query("unread_only") unreadOnly: Boolean? = null,
    ): Response<ConversationsResponseJson>

    @GET("chat/conversations/{conversationId}")
    suspend fun getConversationDetail(
        @Path("conversationId") conversationId: Int,
    ): Response<ConversationDetailResponseJson>

    @GET("chat/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: Int,
        @Query("page") page: Int? = null,
    ): Response<MessagesResponseJson>

    @POST("chat/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @retrofit2.http.Body body: SendMessageRequestJson,
    ): Response<SendMessageResponseJson>

    @DELETE("chat/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Int,
    ): Response<DeleteMessageResponseJson>

    @PUT("chat/conversations/{conversationId}/mark-read")
    suspend fun markConversationRead(
        @Path("conversationId") conversationId: Int,
    ): Response<GenericSuccessResponseJson>

    @PUT("chat/messages/{messageId}/read")
    suspend fun markMessageRead(
        @Path("messageId") messageId: Int,
    ): Response<GenericSuccessResponseJson>

    @GET("chat/messages/{messageId}/status")
    suspend fun getMessageStatus(
        @Path("messageId") messageId: Int,
    ): Response<MessageStatusResponseJson>

    @FormUrlEncoded
    @POST
    suspend fun authenticateChannel(
        @Url absoluteUrl: String,
        @Field("channel_name") channelName: String,
        @Field("socket_id") socketId: String,
    ): Response<ChannelAuthResponseJson>
}

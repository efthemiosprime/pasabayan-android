package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.chat.ChatApi
import com.efthemiosprime.pasabayan.core.network.chat.SendMessageRequestJson
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.toDomain
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val json: Json,
) : ChatRepository {

    override suspend fun loadBroadcastingConfig(): Result<ReverbConfig> {
        return try {
            val response = chatApi.getBroadcastingConfig()
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val config = response.body()?.reverb?.toDomain()
                if (config == null) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(config)
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadConversations(
        role: String?,
        status: String?,
        unreadOnly: Boolean?,
        page: Int,
        perPage: Int,
    ): Result<ConversationsPage> {
        return try {
            val response = chatApi.getConversations(
                role = role,
                status = status,
                unreadOnly = unreadOnly,
                page = page,
                perPage = perPage,
            )
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val body = response.body()
                val conversations = body?.conversationsOrEmpty()?.map { it.toDomain() }.orEmpty()
                Result.success(
                    ConversationsPage(
                        conversations = conversations,
                        currentPage = body?.resolvedCurrentPage() ?: 1,
                        lastPage = body?.resolvedLastPage() ?: 1,
                        total = body?.resolvedTotal() ?: conversations.size,
                    ),
                )
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadConversationDetail(conversationId: Int): Result<ConversationSummary> {
        return try {
            val response = chatApi.getConversationDetail(conversationId)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val detail = response.body()?.conversationOrNull()
                if (detail == null) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(
                        ConversationSummary(
                            id = detail.id,
                            matchId = detail.matchId,
                            status = detail.status,
                            statusDisplay = detail.statusDisplay,
                            userRole = detail.userRole,
                            otherParticipant = detail.otherParticipant?.toDomain()
                                ?: com.efthemiosprime.pasabayan.features.chat.model.Participant(0, "", null, null),
                            matchInfo = detail.matchInfo?.toDomain(),
                            unreadCount = detail.unreadCount,
                            lastMessage = detail.lastMessage?.toDomain(),
                            lastMessageAt = detail.lastMessageAt,
                            createdAt = detail.createdAt,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadMessages(conversationId: Int, page: Int): Result<MessagesPage> {
        return try {
            val response = chatApi.getMessages(conversationId = conversationId, page = page)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(
                        MessagesPage(
                            messages = body.messagesOrEmpty().map { it.toDomain() },
                            currentPage = body.resolvedCurrentPage(),
                            lastPage = body.resolvedLastPage(),
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun sendMessage(
        conversationId: Int,
        message: String,
        type: String,
    ): Result<MessageItem> {
        return try {
            val response = chatApi.sendMessage(
                conversationId = conversationId,
                body = SendMessageRequestJson(message = message, type = type),
            )
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val sent = response.body()?.messageOrNull()?.toDomain()
                if (sent == null) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(sent)
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun deleteMessage(messageId: Int): Result<String?> {
        return try {
            val response = chatApi.deleteMessage(messageId)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                Result.success(response.body()?.deletedAtOrNull())
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun markConversationRead(conversationId: Int): Result<Unit> {
        return try {
            val response = chatApi.markConversationRead(conversationId)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun markMessageRead(messageId: Int): Result<Unit> {
        return try {
            val response = chatApi.markMessageRead(messageId)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getMessageStatus(messageId: Int): Result<MessageDeliveryStatus> {
        return try {
            val response = chatApi.getMessageStatus(messageId)
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val status = response.body()?.data
                if (status == null) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(MessageDeliveryStatus(status.deliveryStatus, status.readReceipts))
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun authenticateChannel(
        config: ReverbConfig,
        channelName: String,
        socketId: String,
    ): Result<String> {
        return try {
            val response = chatApi.authenticateChannel(
                absoluteUrl = config.authUrl(),
                channelName = channelName,
                socketId = socketId,
            )
            if (!response.isSuccessful) {
                Result.failure(mapError(response))
            } else {
                val auth = response.body()?.auth.orEmpty()
                if (auth.isBlank()) {
                    Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
                } else {
                    Result.success(auth)
                }
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapError(response: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json))
}


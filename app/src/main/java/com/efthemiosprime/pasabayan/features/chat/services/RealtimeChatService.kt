package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import com.efthemiosprime.pasabayan.features.chat.model.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

interface RealtimeChatService {
    val isConnected: StateFlow<Boolean>
    val events: Flow<RealtimeChatEvent>
    val incomingMessages: Flow<MessageItem>
    val socketId: StateFlow<String?>

    fun connect(config: ReverbConfig)
    fun disconnect()
    fun subscribe(conversationId: Int)
    fun unsubscribe(conversationId: Int)
    fun pollingMessages(conversationId: Int): Flow<List<MessageItem>>
}

sealed interface RealtimeChatEvent {
    data class SubscriptionSucceeded(val conversationId: Int) : RealtimeChatEvent
    data class ConnectionFailed(val attempt: Int) : RealtimeChatEvent
    data object MaxReconnectExceeded : RealtimeChatEvent
}

@Singleton
class RealtimeChatServiceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val chatRepository: ChatRepository,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RealtimeChatService {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val _isConnected = MutableStateFlow(false)
    private val _events = MutableSharedFlow<RealtimeChatEvent>(extraBufferCapacity = 32)
    private val _incomingMessages = MutableSharedFlow<MessageItem>(extraBufferCapacity = 128)
    private val _socketId = MutableStateFlow<String?>(null)

    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    override val events: Flow<RealtimeChatEvent> = _events.asSharedFlow()
    override val incomingMessages: Flow<MessageItem> = _incomingMessages.asSharedFlow()
    override val socketId: StateFlow<String?> = _socketId.asStateFlow()

    private var config: ReverbConfig? = null
    private var webSocket: WebSocket? = null
    private var pingJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts: Int = 0
    private var shouldReconnect: Boolean = true
    private val subscribedChannels = linkedSetOf<Int>()

    override fun connect(config: ReverbConfig) {
        this.config = config
        shouldReconnect = true
        reconnectJob?.cancel()
        webSocket?.cancel()
        reconnectAttempts = 0

        webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(config.socketUrl).build(),
            listener = createListener(),
        )
    }

    override fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        pingJob?.cancel()
        webSocket?.close(1000, "closed")
        webSocket = null
        _isConnected.value = false
        _socketId.value = null
    }

    override fun subscribe(conversationId: Int) {
        subscribedChannels.add(conversationId)
        val activeSocket = webSocket ?: return
        val activeSocketId = _socketId.value ?: return
        scope.launch {
            val channel = channelName(conversationId)
            val auth = chatRepository.authenticateChannel(
                config = config ?: return@launch,
                channelName = channel,
                socketId = activeSocketId,
            ).getOrNull() ?: return@launch
            val payload = """{"event":"pusher:subscribe","data":"{\"channel\":\"$channel\",\"auth\":\"$auth\"}"}"""
            activeSocket.send(payload)
        }
    }

    override fun unsubscribe(conversationId: Int) {
        subscribedChannels.remove(conversationId)
        val channel = channelName(conversationId)
        webSocket?.send("""{"event":"pusher:unsubscribe","data":"{\"channel\":\"$channel\"}"}""")
    }

    override fun pollingMessages(conversationId: Int): Flow<List<MessageItem>> = flow {
        while (currentCoroutineContext().isActive) {
            if (!_isConnected.value) {
                chatRepository.loadMessages(conversationId = conversationId, page = 1)
                    .getOrNull()
                    ?.messages
                    ?.let { emit(it) }
            }
            delay(POLLING_INTERVAL_MS)
        }
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _isConnected.value = true
            reconnectAttempts = 0
            startPing(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleMessage(text, webSocket)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _isConnected.value = false
            _socketId.value = null
            pingJob?.cancel()
            if (shouldReconnect) {
                scheduleReconnect()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _isConnected.value = false
            _socketId.value = null
            pingJob?.cancel()
            if (shouldReconnect) {
                scheduleReconnect()
            }
        }
    }

    private fun handleMessage(raw: String, webSocket: WebSocket) {
        val root = parseJson(raw) ?: return
        val eventName = root["event"]?.jsonPrimitive?.contentOrNull ?: return
        when (eventName) {
            "pusher:connection_established", "connection_established" -> {
                val socket = extractSocketId(root)
                _socketId.value = socket
                resubscribeChannels(webSocket)
            }
            "pusher_internal:subscription_succeeded" -> {
                extractConversationId(root)?.let { _events.tryEmit(RealtimeChatEvent.SubscriptionSucceeded(it)) }
            }
            "pusher:pong" -> Unit
            "chat.message" -> {
                extractMessagePayload(root)?.let {
                    _incomingMessages.tryEmit(it)
                }
            }
        }
    }

    private fun extractSocketId(root: JsonObject): String? {
        val dataField = root["data"] ?: return null
        val nested = when (dataField) {
            is JsonObject -> dataField
            else -> {
                val content = dataField.jsonPrimitive.contentOrNull ?: return null
                parseJson(content) ?: return null
            }
        }
        return nested["socket_id"]?.jsonPrimitive?.contentOrNull
    }

    private fun extractConversationId(root: JsonObject): Int? {
        val dataValue = root["channel"]?.jsonPrimitive?.contentOrNull ?: return null
        return dataValue.substringAfter("private-chat.", "").toIntOrNull()
    }

    private fun extractMessagePayload(root: JsonObject): MessageItem? {
        val dataField = root["data"] ?: return null
        val dataObject = parseDataObject(dataField) ?: return null
        val rawMessage: JsonElement = resolveMessageElement(dataObject) ?: return null
        return runCatching {
            json.decodeFromJsonElement(
                com.efthemiosprime.pasabayan.core.network.chat.MessageItemJson.serializer(),
                rawMessage,
            ).toDomain()
        }.getOrNull()
    }

    private fun parseDataObject(dataField: JsonElement): JsonObject? {
        return when (dataField) {
            is JsonObject -> dataField
            else -> {
                val content = dataField.jsonPrimitive.contentOrNull ?: return null
                parseJson(content)
            }
        }
    }

    private fun resolveMessageElement(dataObject: JsonObject): JsonElement? {
        val wrapped = dataObject["chat_message"] ?: return dataObject
        return when (wrapped) {
            is JsonObject -> wrapped
            else -> {
                val content = wrapped.jsonPrimitive.contentOrNull ?: return null
                parseJson(content)
            }
        }
    }

    private fun parseJson(raw: String): JsonObject? = runCatching {
        val parsed = json.parseToJsonElement(raw)
        when (parsed) {
            is JsonObject -> parsed
            else -> {
                val nested = parsed.jsonPrimitive.contentOrNull ?: return@runCatching null
                json.parseToJsonElement(nested).jsonObject
            }
        }
    }.getOrNull()

    private fun parseJson(raw: JsonElement): JsonObject? = runCatching {
        raw.jsonObject
    }.getOrNull()

    private fun startPing(webSocket: WebSocket) {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive && _isConnected.value) {
                delay(PING_INTERVAL_MS)
                webSocket.send("""{"event":"pusher:ping","data":"{}"}""")
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            _events.tryEmit(RealtimeChatEvent.MaxReconnectExceeded)
            return
        }
        reconnectAttempts += 1
        _events.tryEmit(RealtimeChatEvent.ConnectionFailed(reconnectAttempts))
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val waitSeconds = calculateReconnectDelaySeconds(reconnectAttempts)
            delay(waitSeconds * 1_000L)
            connect(config ?: return@launch)
        }
    }

    private fun resubscribeChannels(webSocket: WebSocket) {
        val currentSocketId = _socketId.value ?: return
        val currentConfig = config ?: return
        subscribedChannels.toList().forEach { conversationId ->
            scope.launch {
                val channel = channelName(conversationId)
                val auth = chatRepository.authenticateChannel(
                    config = currentConfig,
                    channelName = channel,
                    socketId = currentSocketId,
                ).getOrNull() ?: return@launch
                webSocket.send("""{"event":"pusher:subscribe","data":"{\"channel\":\"$channel\",\"auth\":\"$auth\"}"}""")
            }
        }
    }

    private fun channelName(conversationId: Int): String = "private-chat.$conversationId"

    companion object {
        internal const val MAX_RECONNECT_ATTEMPTS = 5
        private const val PING_INTERVAL_MS = 30_000L
        private const val POLLING_INTERVAL_MS = 5_000L

        internal fun calculateReconnectDelaySeconds(attempt: Int): Int = min(1 shl attempt, 30)
    }
}

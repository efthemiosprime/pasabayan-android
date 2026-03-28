# 07 — Chat and broadcasting

**Phase:** 5 | **Feature:** Chat | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

REST chat API + real-time WebSocket via Reverb/Pusher protocol; parity with [`ChatAPIService.swift`](../../Pasabayan/Features/Chat/Services/ChatAPIService.swift), [`RealtimeChatService.swift`](../../Pasabayan/Features/Chat/Services/RealtimeChatService.swift), [`ChatViewModel.swift`](../../Pasabayan/Features/Chat/ViewModels/ChatViewModel.swift), and all files under [`Features/Chat/`](../../Pasabayan/Features/Chat/).

## Models

### `ConversationSummary` (conversation list item)

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | |
| `matchId` | Int? | Associated delivery match |
| `status` | String | `"active"`, `"closed"`, `"archived"`, `"cancelled"` |
| `statusDisplay` | String | Human-readable status |
| `userRole` | String? | Current user's role in conversation |
| `otherParticipant` | Participant | Nested: `id`, `name`, `avatar`, `verificationLevel` |
| `matchInfo` | MatchInfo? | Nested: `route`, `packageDescription`, `tripRoute`, `departureDate` |
| `unreadCount` | Int | Mutable locally for optimistic updates |
| `hasUnreadMessages` | Bool | Computed from unreadCount |
| `lastMessage` | LastMessage? | Nested: `id`, `message`, `messageType`, `senderName`, `createdAt` |
| `lastMessageAt` | String? | |
| `createdAt` | String? | |

### `MessageItem` (single message)

| Field | Type | Notes |
|-------|------|-------|
| `id` | Int | Negative IDs = optimistic/temp messages |
| `message` | String | Message text |
| `messageType` | String | `"text"`, `"system"`, `"image"` |
| `sender` | Sender | Nested: `id`, `name`, `avatar` |
| `isRead` | Bool | |
| `createdAt` | String | |
| `formattedMessage` | String? | Pre-formatted by server |
| `isSystemMessage` | Bool | Computed: `messageType == "system"` |
| `messageTypeDisplay` | String? | |
| `readAt` | String? | |
| `readReceipts` | Map<String, String>? | Keyed by user ID → read_at timestamp |
| `deliveryStatus` | String? | `"sent"`, `"delivered"`, `"read"` |
| `deliveredAt` | String? | |
| `attachments` | [Attachment]? | |
| `hasAttachments` | Bool | Computed |
| `canEdit` | Bool | |
| `canDelete` | Bool | |
| `isDeleted` | Bool? | Soft-delete flag |
| `deletedAt` | String? | |
| `metadata` | MessageMetadata? | See special message types below |

### `MessageMetadata` (special message types)

| Field | Type | Notes |
|-------|------|-------|
| `type` | String? | `"receipt_upload_prompt"`, `"service_list_item"` |
| `listItemIndex` | Int? | For service list items |
| `listItemTotal` | Int? | |
| `listItem` | ListItem? | Nested: `item`, `quantity`, `notes`, `noteUrls` |

### `BroadcastingConfigResponse`

| Field | Type |
|-------|------|
| `success` | Bool |
| `reverb` | ReverbConfig? |

### `ReverbConfig`

| Field | Type | Notes |
|-------|------|-------|
| `key` | String | App key (default: `"pasabayan"`) |
| `host` | String | WebSocket host |
| `port` | Int? | Default: 8080 |
| `scheme` | String? | `"https"` → `"wss"`, `"http"` → `"ws"` |

### `ChannelAuthResponse`

| Field | Type |
|-------|------|
| `auth` | String |

### Response wrappers

| Type | Structure |
|------|-----------|
| `ConversationsResponse` | `{ data: [ConversationSummary] }` |
| `ConversationDetailResponse` | `{ data: ConversationDetail }` |
| `MessagesResponse` | `{ data: [MessageItem], currentPage, lastPage }` |
| `SendMessageResponse` | `{ message: MessageItem }` |
| `DeleteMessageResponse` | `{ success, deletedAt }` |

## Endpoints

### REST API (`/api` prefix)

| Method | Path | Query/Body | Response | Notes |
|--------|------|-----------|----------|-------|
| GET | `/config/broadcasting` | — | `BroadcastingConfigResponse` | WebSocket URL config |
| GET | `/chat/conversations` | `role`, `status`, `unread_only` | `ConversationsResponse` | Conversation list |
| GET | `/chat/conversations/{id}` | — | `ConversationDetailResponse` | Conversation metadata |
| GET | `/chat/conversations/{id}/messages` | `page` | `MessagesResponse` | Paginated messages |
| POST | `/chat/conversations/{id}/messages` | `{message, type}` | `SendMessageResponse` | Send message; type: `"text"` or `"system"` |
| DELETE | `/chat/messages/{id}` | — | `DeleteMessageResponse` | Soft-delete own message |
| PUT | `/chat/conversations/{id}/mark-read` | — | Success | Mark whole conversation read |
| PUT | `/chat/messages/{id}/read` | — | Success | Mark specific message read |
| GET | `/chat/messages/{id}/status` | — | Read receipt status | |

### Broadcasting auth (no `/api` prefix)

- **POST** `{scheme}://{host}[:port]/broadcasting/auth`
- Content-Type: `application/x-www-form-urlencoded`
- Form body: `channel_name={name}&socket_id={id}`
- Response: `ChannelAuthResponse` with `auth` string
- Auth header: Bearer token (same as API calls)

## WebSocket protocol (Pusher/Reverb)

### Connection URL

`wss://{host}:{port}/app/{key}` (key: `"pasabayan"`)

**Construction:** Dynamic from `/config/broadcasting` response, or fallback:
```
scheme = reverb.scheme == "https" ? "wss" : "ws"
port = reverb.port ?? 8080
url = "{scheme}://{host}:{port}/app/{key}"
```

Android: use OkHttp `WebSocket`.

### Ping interval

**30 seconds** — send `{"event": "pusher:ping", "data": "{}"}` on a timer.

### Channel format

`private-chat.{conversationId}`

### Subscribe payload

```json
{
  "event": "pusher:subscribe",
  "data": "{\"channel\":\"private-chat.{conversationId}\",\"auth\":\"{auth_token}\"}"
}
```

The `auth` value is obtained from `POST /broadcasting/auth` (form body: `channel_name`, `socket_id`).

### Unsubscribe payload

```json
{
  "event": "pusher:unsubscribe",
  "data": "{\"channel\":\"private-chat.{conversationId}\"}"
}
```

### Event types handled

| Event name | Type | Action |
|-----------|------|--------|
| `connection_established` | Internal (Pusher) | Extract `socket_id` from nested JSON data; store for auth requests |
| `pusher_internal:subscription_succeeded` | Internal | Subscription confirmed; log |
| `pusher:pong` | Internal | Ping response — no action |
| `chat.message` | App event | Parse message; data contains `{"chat_message": {...}}` or direct `MessageItem` JSON |

### Reconnection logic

- Max **5** reconnection attempts
- Exponential backoff: `min(2^attempt, 30)` seconds
- On reconnect: **auto-resubscribe** to all previously subscribed channels (stored in `subscribedChannels` set)
- On max attempts exceeded: fall back to **polling**

### Polling fallback

When `realtimeChatEnabled == false` or WebSocket connection fails:
- Poll `GET /chat/conversations/{id}/messages?page=1` every **5 seconds**
- ChatViewModel monitors `RealtimeChatService.isConnectedPublisher`:
  - Connected → stop polling
  - Disconnected → start polling at 5s interval
- Apply same merge logic as real-time messages

## ViewModel — `ChatViewModel`

### State

| Field | Type | Notes |
|-------|------|-------|
| `conversations` | [ConversationSummary] | Conversation list |
| `messages` | [MessageItem] | Current conversation messages |
| `isLoading` | Bool | |
| `alertMessage` | String? | Error display |
| `allUnreadCount` | Int | Total unread across all conversations |
| `conversationId` | Int? | Currently open conversation |
| `isComposerEnabled` | Bool | Disabled for closed/archived/cancelled |
| `nextPage` | Int? | Pagination cursor |
| `isPaging` | Bool | Prevents concurrent page loads |

### Key behaviors

#### Optimistic sending

1. Create temp message with **negative ID** (client-side)
2. Insert into `messages` immediately
3. Send via POST
4. On success: replace temp message with server response
5. On failure: mark as failed (see below)

#### Failed message handling

- Track failed temp IDs in `failedMessageTempIds: Set<Int>`
- Store text in `tempIdToText: Map<Int, String>` for retry
- `isFailed(message)` → check if ID in failed set
- `retrySend(tempId)` → remove from failed set, re-send text
- **Recovery on decode error:** fetch latest page and find real message

#### Mark-as-read deduplication

- Track `markMessageAsReadRequestedIds: Set<Int>`
- Only emit **one** API call per message ID per session
- Prevents API spam from list `onAppear` during scrolling

#### Polling performance (200+ messages)

- When message list exceeds **200 messages**, rate-limit poll applies:
- Coalesce rapid poll responses into **one apply per 1 second**
- Prevents UI thrashing from frequent state updates

#### Message merging (`ChatMergeLogic`)

When polling returns data:
1. Filter new messages (ID > lastMessageId)
2. **Fast path:** if all new IDs > max current ID → append + sort
3. **Slow path:** merge by ID, deduplicate, sort
4. Refresh existing message statuses from response data (read receipts, delivery status)
5. Update `lastMessageId`

#### Conversation status → composer

| Status | Composer enabled |
|--------|-----------------|
| `"active"` | Yes |
| `"closed"` | No |
| `"archived"` | No |
| `"cancelled"` | No |

Also disable on 401/404 error or "closed" message in error response.

#### Unread count sync

- Computed: `allUnreadCount = conversations.reduce(0) { $0 + $1.unreadCount }`
- On opening conversation: call `markConversationRead()` + set local `unreadCount = 0`
- `unreadTotal` property exposes for tab badge (Messages tab)

### Read receipt display

| Condition | Icon |
|-----------|------|
| Own message, status `"sent"` | Single checkmark |
| Own message, status `"delivered"` | Checkmark circle |
| Own message, status `"read"` | Filled checkmark circle |
| Other's message, read by me | Filled checkmark circle |
| Other's message, not read | Empty checkmark circle |

### Message deletion (soft-delete)

- Only own messages can be deleted (`canDelete && !isDeleted`)
- API: `DELETE /chat/messages/{id}`
- Update local message: set `isDeleted = true`, `deletedAt` from response
- Show "[Message deleted]" placeholder in UI

### Pagination (older messages)

- `loadMoreMessages()` — fetch `page = nextPage`
- Prepend to `messages` array (older messages go before newer)
- Deduplicate by ID
- Set `nextPage = currentPage + 1` if `currentPage < lastPage`, else `nil`

## Special message types

### System messages

Sent via `POST /chat/conversations/{id}/messages` with `type: "system"`. Displayed differently (centered, no avatar, distinct styling).

### Receipt upload prompt

`metadata.type == "receipt_upload_prompt"` — shows a special bubble with receipt upload action.

### Service list item

`metadata.type == "service_list_item"` — displays shopping list item with quantity, notes, and optional note URLs (images).

## UI (iOS reference — all views to mirror)

| iOS View | Purpose | Notes |
|----------|---------|-------|
| [`ConversationsView`](../../Pasabayan/Features/Chat/Views/ConversationsView.swift) | Conversation list with unread badges | Messages tab root |
| [`ConversationDetailView`](../../Pasabayan/Features/Chat/Views/ConversationDetailView.swift) | Chat thread — messages, composer, special bubbles | Full screen |
| [`ChatReceiptUploadSheet`](../../Pasabayan/Features/Chat/Views/ChatReceiptUploadSheet.swift) | Upload receipt photo from within chat | Modal sheet |

### Conversation list features
- Role-based filtering
- Unread count per conversation
- Last message preview with timestamp
- Other participant info (name, avatar, verification)
- Match info context (route, package description)

### Chat thread features
- Message bubbles (own vs other) with read receipts
- System message styling (centered, no avatar)
- Receipt upload prompt bubble
- Service list item bubble with quantities
- Failed message indicator with retry button
- Soft-deleted message placeholder
- Pagination on scroll to top
- Composer with send button (disabled for closed conversations)
- Real-time message appearance via WebSocket

## TDD checklist

- [ ] `ConversationSummary` JSON decode — all fields including nested Participant, MatchInfo, LastMessage.
- [ ] `MessageItem` JSON decode — all fields including metadata, readReceipts, deliveryStatus, isDeleted.
- [ ] `BroadcastingConfigResponse` + `ReverbConfig` decode.
- [ ] `ChannelAuthResponse` decode.
- [ ] WebSocket: connect → extract socket_id → subscribe → receive message → parse → display.
- [ ] Reconnection: exponential backoff up to 30s, max 5 attempts, auto-resubscribe.
- [ ] Polling fallback: 5s interval, message merge logic, deduplication by ID.
- [ ] `ChatMarkAsReadDedupeTests`: only one API call per message ID per session.
- [ ] `ChatFailedMessageTests`: temp ID tracking, retry, recovery on decode error.
- [ ] `ChatViewModelPollingPerformanceTests`: rate-limit at 200+ messages (1s coalesce).
- [ ] `ChatMergeLogic`: fast path (append), slow path (merge + dedup), status refresh.
- [ ] Optimistic sending: temp negative ID → replace on success → mark failed on error.
- [ ] Pagination: prepend older messages, deduplicate, nextPage tracking.
- [ ] Soft-delete: update local state, show placeholder.
- [ ] Conversation status → composer disabled for closed/archived/cancelled.
- [ ] Unread count: sync on conversation list load, zero on open, expose for tab badge.
- [ ] Special message types: system, receipt_upload_prompt, service_list_item metadata parsing.

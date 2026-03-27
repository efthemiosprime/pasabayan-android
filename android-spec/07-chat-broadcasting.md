# 07 — Chat and broadcasting

**Phase:** 5 | **Feature:** Chat | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`ChatAPIService.swift`](../../Pasabayan/Features/Chat/Services/ChatAPIService.swift), [`APIConfiguration`](../../Pasabayan/Services/APIConfiguration.swift) for Reverb and broadcasting auth URL.

## API (`/api` prefix)

| Method | Path |
|--------|------|
| GET | `/config/broadcasting` |
| GET | `/chat/conversations` (+ query) |
| GET | `/chat/conversations/{id}` |
| GET | `/chat/conversations/{id}/messages?page=` |
| POST | `/chat/conversations/{id}/messages` |
| DELETE | `/chat/messages/{id}` |
| PUT | `/chat/conversations/{id}/mark-read` |
| PUT | `/chat/messages/{id}/read` |
| GET | `/chat/messages/{id}/status` |

## Broadcasting auth (no `/api`)

- **POST** `{scheme}://{host}[:port]/broadcasting/auth` — form body `channel_name`, `socket_id` (see iOS `broadcastingAuthURL`).
- Response: `ChannelAuthResponse` with `auth` string.

## WebSocket

- Build from `getBroadcastingConfig()` when possible; fallback `reverbWebSocketURL` in `APIConfiguration`.

## Real-time layer (client)

[`RealtimeChatService.swift`](../../Pasabayan/Features/Chat/Services/RealtimeChatService.swift) — Reverb / Pusher-protocol **WebSocket** (`URLSessionWebSocketTask`), subscribe/unsubscribe per conversation, reconnect logic; **falls back to polling** when disabled or unavailable (`APIConfiguration.realtimeChatEnabled`). Android must mirror: WS + auth + fallback behavior for parity with [`ChatViewModel`](../../Pasabayan/Features/Chat/ViewModels/ChatViewModel.swift).

## UI (iOS reference)

[`ChatViewModel`](../../Pasabayan/Features/Chat/ViewModels/ChatViewModel.swift), `ConversationsView`, realtime vs polling flags.

## TDD checklist

- [ ] `ChatMarkAsReadDedupeTests`, `ChatFailedMessageTests`, `ChatViewModelPollingPerformanceTests`.
- [ ] Realtime: WebSocket subscribe/reconnect + polling fallback behavior vs [`RealtimeChatService`](../../Pasabayan/Features/Chat/Services/RealtimeChatService.swift).

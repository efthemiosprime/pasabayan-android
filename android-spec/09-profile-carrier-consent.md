# 09 — Profile, carrier, consent, disclaimers

**Phase:** 6 | **Feature:** Profile | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`ProfileAPIService.swift`](../../Pasabayan/Features/Profile/Services/ProfileAPIService.swift), models [`ProfileModels.swift`](../../Pasabayan/Features/Profile/Models/ProfileModels.swift).

## Endpoints

| Method | Path |
|--------|------|
| GET | `/profile` |
| POST | `/profile` (JSON or multipart profile picture) |
| PUT | `/profile` |
| DELETE | `/profile/picture` |
| POST | `/profile/request-deletion` |
| POST | `/profile/disclaimer-acknowledgments` |
| GET | `/profile/disclaimer-acknowledgments` |
| GET | `/profile/consent-preferences` |
| PUT | `/profile/consent-preferences` |
| GET | `/profile/export-data` (raw `Data`) |
| GET | `/carrier/profile` |
| POST | `/carrier/profile` |
| PUT | `/carrier/profile` |
| GET | `/carrier/stats` |
| POST | `/carrier/enable` |
| POST | `/carrier/toggle-status` |

## Multipart field names — POST `/profile` (avatar upload)

From `ProfileAPIService.updateProfileWithAvatar`:

| Field name | Type | Notes |
|-----------|------|-------|
| `profile_picture` | file | Filename: `avatar.jpg`; MIME: `image/jpeg`, `image/png`, or `image/gif` |
| `full_name` | text | |
| `delivery_address` | text | |
| `preferred_contact_method` | text | Required; default: `"app_notification"` |
| `additional_info` | text | Optional; JSON string |

## Consent preferences — PUT `/profile/consent-preferences`

Request body `[String: Bool]`:

| Key | Default | Meaning |
|-----|---------|---------|
| `push_notifications` | `false` | Opt-in push |
| `location_tracking` | `false` | Location tracking consent |
| `analytics` | `false` | Analytics |
| `marketing_communications` | `false` | Marketing |

## Quirks

- iOS may use **POST** for multipart profile update where PUT multipart is problematic — match the working iOS behavior.

## UI (iOS reference)

[`ProfileView`](../../Pasabayan/Features/Profile/), carrier profile sheets.

## TDD checklist

- [ ] `ProfileAPIServiceTests`, `ProfileViewModelAvatarRefreshTests`, `AccountDeletionTests`.

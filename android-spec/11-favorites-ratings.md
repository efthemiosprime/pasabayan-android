# 11 — Favorites and ratings

**Phase:** 6 | **Features:** Favorites, Ratings | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`FavoritesAPIService.swift`](../../Pasabayan/Features/Favorites/Services/FavoritesAPIService.swift) (direct `URLSession` for many calls), [`RatingsAPIService.swift`](../../Pasabayan/Features/Ratings/Services/RatingsAPIService.swift), [`Rating.swift`](../../Pasabayan/Features/Ratings/Models/Rating.swift).

## Favorites endpoints

**Note:** `FavoritesAPIService` uses `URLSession.shared` with `async/await` (not `BaseFeatureAPIService`). Custom error enum `FavoritesAPIError`: `.invalidURL`, `.invalidResponse`, `.serverError(statusCode)`, `.decodingError`, `.networkError`, `.cannotFavorite(String)`, `.alreadyFavorited`, `.unauthorized`, `.notFound`. Status 201 = success for addFavorite; 409 = alreadyFavorited; 400 = cannotFavorite with parsed message.

| Method | Path | Query / Body |
|--------|------|-------------|
| GET | `/favorites/carriers` | query: `sort` (default: `"recent"`), `has_upcoming_trips` (bool) |
| POST | `/carriers/{carrierId}/favorite` | body: `notes` (opt), `notification_enabled` (opt) |
| DELETE | `/carriers/{carrierId}/unfavorite` | — |
| GET | `/carriers/{carrierId}/is-favorite` | — |
| POST | `/carriers/{carrierId}/request-delivery` | body: `pickup_city`, `pickup_date_preferred` (YYYY-MM-DD), `delivery_city`, `delivery_date_needed` (YYYY-MM-DD), `package_description`, `package_weight_kg`, `package_type`, `pickup_address` (opt), `delivery_address` (opt), `offered_price` (opt), `shipper_message` (opt) |
| GET | `/favorites/requests/sent` | — |

## Ratings endpoints

| Method | Path | Query / Body |
|--------|------|-------------|
| GET | `/users/{userId}/ratings` | query: `page`, `per_page`, `sort` |
| GET | `/user/stats` | — |
| GET | `/ratings/pending` | query: `page` |
| GET | `/ratings/given` | query: `page` |
| PUT | `/ratings/{ratingId}/comment` | body: comment text |

## TDD checklist

- [ ] `FavoritesContractTests`, `AddToFavoritesIntegrationTests`.

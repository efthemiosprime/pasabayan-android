# 11 — Favorites and ratings

**Phase:** 6 | **Features:** Favorites, Ratings | Roadmap: [PHASES-AND-FEATURES.md](PHASES-AND-FEATURES.md)

## Scope

[`FavoritesAPIService.swift`](../../Pasabayan/Features/Favorites/Services/FavoritesAPIService.swift) (direct `URLSession` for many calls), [`RatingsAPIService.swift`](../../Pasabayan/Features/Ratings/Services/RatingsAPIService.swift), [`Rating.swift`](../../Pasabayan/Features/Ratings/Models/Rating.swift).

## Favorites endpoints

| Method | Path |
|--------|------|
| GET | `/favorites/carriers?...` |
| POST | `/carriers/{carrierId}/favorite` |
| DELETE | `/carriers/{carrierId}/unfavorite` |
| GET | `/carriers/{carrierId}/is-favorite` |
| POST | `/carriers/{carrierId}/request-delivery` |
| GET | `/favorites/requests/sent` |

## Ratings endpoints

| Method | Path |
|--------|------|
| GET | `/users/{userId}/ratings?...` |
| GET | `/user/stats` |
| GET | `/ratings/pending?...` |
| GET | `/ratings/given?...` |
| PUT | `/ratings/{ratingId}/comment` |

(Additional overloads in service — see Swift file.)

## TDD checklist

- [ ] `FavoritesContractTests`, `AddToFavoritesIntegrationTests`.

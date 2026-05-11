package com.efthemiosprime.pasabayan.features.favorites.services

/**
 * Favorites-specific failures (`11-favorites-ratings.md` § Favorites custom error enum). Network /
 * decoding / generic 5xx errors still flow through `DomainErrorMapperException`; these cover the
 * status codes the spec calls out explicitly.
 */
sealed class FavoritesError : Throwable() {
    object AlreadyFavorited : FavoritesError() {
        private fun readResolve(): Any = AlreadyFavorited
    }

    data class CannotFavorite(val reason: String?) : FavoritesError()

    object NotFound : FavoritesError() {
        private fun readResolve(): Any = NotFound
    }
}

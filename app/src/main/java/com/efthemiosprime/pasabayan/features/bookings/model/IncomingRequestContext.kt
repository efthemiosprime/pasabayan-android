package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus

/**
 * Surface model for the `IncomingRequestSnackbar` review section above the match list
 * (see `android-spec/05-bookings-matches.md`). One per pending incoming request.
 *
 * **Role-scoped incoming bucket**:
 *   - Carrier-side: `MatchStatus.SHIPPER_REQUESTED` (a shipper sent a booking request).
 *   - Shipper-side: `MatchStatus.CARRIER_REQUESTED` (a carrier offered to carry a package).
 *
 * Counter-offers are excluded — they have their own `CounterOfferPromptSheet` flow.
 *
 * **Android-only surface**: iOS handles incoming requests inline in `CarrierRequestCard` /
 * `BookingRequestCard` / `ShipperMatchCard` via status filters; no equivalent file exists
 * in `pasabayan-ios`. Documented as a behavior delta in spec 05.
 */
data class IncomingRequestContext(
    val matchId: Int,
    val requesterName: String,
) {
    companion object {
        /** Maximum items rendered in the snackbar section. Older requests still show in the list below. */
        const val MAX_VISIBLE_ITEMS: Int = 3

        /**
         * Project a single [match] into a snackbar item for the given role, or `null` when
         * the match is not an incoming request for that role (wrong status, counter-offer,
         * or the relevant party is missing).
         */
        fun from(match: DeliveryMatch, isCarrier: Boolean): IncomingRequestContext? {
            if (match.isCounterOffer) return null
            val expectedStatus = if (isCarrier) MatchStatus.SHIPPER_REQUESTED else MatchStatus.CARRIER_REQUESTED
            if (match.matchStatus != expectedStatus) return null
            val requesterName = if (isCarrier) match.shipper?.name else match.carrier?.name
            if (requesterName.isNullOrBlank()) return null
            return IncomingRequestContext(matchId = match.id, requesterName = requesterName)
        }

        /**
         * The visible snackbar items for the current state, capped at [MAX_VISIBLE_ITEMS].
         * Preserves the input order (repository sorts newest-first). Skips items the user
         * has already dismissed via [reviewedIds].
         */
        fun incomingRequestSnackbarItems(
            matches: List<DeliveryMatch>,
            isCarrier: Boolean,
            reviewedIds: Set<Int>,
        ): List<IncomingRequestContext> =
            matches
                .asSequence()
                .mapNotNull { from(it, isCarrier) }
                .filter { it.matchId !in reviewedIds }
                .take(MAX_VISIBLE_ITEMS)
                .toList()

        /**
         * The set of match ids that are currently valid candidates for the snackbar section
         * for this role. View-models intersect their `reviewedIncomingRequestIds` with this
         * set on every match-list update so stale ids (matches the user accepted, declined,
         * or that moved out of the incoming bucket) are garbage-collected.
         */
        fun dismissedIdsAfterReview(
            matches: List<DeliveryMatch>,
            isCarrier: Boolean,
        ): Set<Int> =
            matches
                .asSequence()
                .mapNotNull { from(it, isCarrier) }
                .map { it.matchId }
                .toSet()

        /**
         * The badge count surfaced on the Matches bottom-bar tab. Counts every currently
         * pending incoming request the user hasn't yet dismissed via the snackbar — not
         * truncated to [MAX_VISIBLE_ITEMS] because the badge represents real backlog, not
         * what's visible above the list.
         */
        fun unseenIncomingRequestBadgeCount(
            matches: List<DeliveryMatch>,
            isCarrier: Boolean,
            reviewedIds: Set<Int>,
        ): Int =
            matches
                .asSequence()
                .mapNotNull { from(it, isCarrier) }
                .count { it.matchId !in reviewedIds }
    }
}

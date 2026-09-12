package com.facetlauncher.app.data.model

import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Fixed intent-type order the connections sheet renders in (Phase 9) — Call/Message/Email/
 * WhatsApp always sort first (whichever of them are actually available for this contact), any
 * dynamically-discovered third-party connection (Telegram, Signal, etc. — whatever app
 * contributed a custom `Data` row for this contact) sorts after, alphabetically by its own label.
 */
enum class ContactConnectionType { CALL, MESSAGE, EMAIL, WHATSAPP, OTHER }

/**
 * What a [ContactConnection] row actually does on tap. [Single] fires [intent] directly; the
 * row's own subtitle is [value] (e.g. the one phone number/email actually being acted on), plus
 * [typeLabel] (Mobile/Home/Work/... — from the Contacts Provider's own per-row type, `null` when
 * it declares none) when there's one to show. [Multiple] means more than one candidate exists for
 * this same connection type (a contact with two phone numbers, say) — the row instead shows
 * "Multiple options available" and opens a disambiguation sheet listing each [ConnectionOption],
 * any one of which can be picked.
 */
sealed interface ConnectionDetail {
    data class Single(val value: String, val typeLabel: String?, val intent: Intent) : ConnectionDetail
    data class Multiple(val options: List<ConnectionOption>) : ConnectionDetail
}

/** One disambiguation-sheet row — [value] is the actual phone number/email/etc. being offered, [typeLabel] its Mobile/Home/Work/... kind when the Provider declares one. */
data class ConnectionOption(val value: String, val typeLabel: String?, val intent: Intent)

/** One row in the contact connections sheet (`ContactConnectionsSheet`). */
data class ContactConnection(
    val type: ContactConnectionType,
    val label: String,
    val icon: ImageBitmap?,
    val detail: ConnectionDetail,
)

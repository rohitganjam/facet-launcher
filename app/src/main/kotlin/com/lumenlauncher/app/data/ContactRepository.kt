package com.lumenlauncher.app.data

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.lumenlauncher.app.data.model.ConnectionDetail
import com.lumenlauncher.app.data.model.ConnectionOption
import com.lumenlauncher.app.data.model.ContactConnection
import com.lumenlauncher.app.data.model.ContactConnectionType
import com.lumenlauncher.app.data.model.ContactInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** AOSP's own built-in data kinds — anything *outside* this set on a contact's `Data` rows is a third-party sync adapter's own contribution (F6/Phase 9's "dynamic connections"). */
private val STANDARD_MIMETYPES = setOf(
    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE,
    ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE,
)

/** Wraps the system Contacts Provider (`READ_CONTACTS`) for F6's drawer-search contacts section. */
@Singleton
class ContactRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    @ApplicationContext private val context: Context,
) {

    /** Contacts whose name matches [query], one row per contact (that contact's first/primary phone number only). */
    suspend fun searchContacts(query: String, limit: Int): List<ContactInfo> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val seenContactIds = mutableSetOf<String>()
            buildList {
                while (cursor.moveToNext() && size < limit) {
                    val contactId = cursor.getString(idCol)
                    // A contact with several phone numbers has one row per number here — keep
                    // only the first (its primary/first number, per F6's scoping decision).
                    if (!seenContactIds.add(contactId)) continue
                    add(
                        ContactInfo(
                            id = contactId,
                            displayName = cursor.getString(nameCol).orEmpty(),
                            phoneNumber = cursor.getString(numberCol).orEmpty(),
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    /**
     * Every way to reach this contact (Phase 9) — Call/Message/Email are the only *fixed* entries,
     * since those are baseline actions on the contact's own core Phone/Email fields, not something
     * a third-party app contributes. Everything else — WhatsApp, Telegram, Signal, or any other
     * messaging app — comes from [dynamicConnections] uniformly: whichever apps have actually
     * contributed their own `Data` row for this specific contact, auto-discovered rather than
     * hand-enumerated one app at a time (no `if installed, assume connected` special-casing for
     * any one app — a contact only gets a WhatsApp row here if WhatsApp itself synced one for
     * them, exactly like every other connection). Fetched fresh per contact only when the
     * connections sheet actually opens for them (see `HubWidgetPickerViewModel`'s identical
     * fetch-on-open precedent for F12's shortcuts) — never precomputed for every row in a list.
     */
    suspend fun getConnections(contactId: String): List<ContactConnection> = withContext(Dispatchers.IO) {
        val phoneNumbers = allPhoneNumbers(contactId)
        val emails = allEmails(contactId)

        val fixed = buildList {
            if (phoneNumbers.isNotEmpty()) {
                add(ContactConnection(ContactConnectionType.CALL, "Call", null, detailFor(phoneNumbers) { Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.value}")) }))
                add(ContactConnection(ContactConnectionType.MESSAGE, "Message", null, detailFor(phoneNumbers) { Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${it.value}")) }))
            }
            if (emails.isNotEmpty()) {
                add(ContactConnection(ContactConnectionType.EMAIL, "Email", null, detailFor(emails) { Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${it.value}")) }))
            }
        }

        (fixed + dynamicConnections(contactId, phoneNumbers)).sortedWith(
            compareBy<ContactConnection> { it.type.ordinal }.thenBy { it.label.lowercase() },
        )
    }

    /** A single value fires directly; more than one becomes a disambiguation list instead of picking one arbitrarily. */
    private fun detailFor(values: List<LabeledValue>, toIntent: (LabeledValue) -> Intent): ConnectionDetail =
        if (values.size == 1) {
            val only = values.single()
            ConnectionDetail.Single(only.value, only.typeLabel, toIntent(only))
        } else {
            ConnectionDetail.Multiple(values.map { ConnectionOption(it.value, it.typeLabel, toIntent(it)) })
        }

    /** A phone number/email plus its Contacts Provider-declared kind (Mobile/Home/Work/a custom label/`null`). */
    private data class LabeledValue(val value: String, val typeLabel: String?)

    private fun allPhoneNumbers(contactId: String): List<LabeledValue> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        return contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, arrayOf(contactId), null)?.use { cursor ->
            val numberCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val normalizedCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
            val typeCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            val customLabelCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)
            buildList {
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberCol) ?: continue
                    val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.resources, cursor.getInt(typeCol), cursor.getString(customLabelCol)).toString()
                    // Dedup key, not the displayed value — the Provider's own NORMALIZED_NUMBER
                    // (falling back to stripping whitespace/punctuation when it's null, e.g. a
                    // malformed number the Provider couldn't normalize) so "+1 555 123 4567" and
                    // "+15551234567" collapse to the one real number instead of surfacing a
                    // pointless "which of these two identical numbers?" disambiguation (confirmed
                    // on-device — see chat history).
                    val dedupKey = cursor.getString(normalizedCol) ?: number.filterNot { it.isWhitespace() || it in "()-." }
                    add(LabeledValue(number, typeLabel) to dedupKey)
                }
            }
        }.orEmpty().distinctBy { it.second }.map { it.first }
    }

    private fun allEmails(contactId: String): List<LabeledValue> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.TYPE,
            ContactsContract.CommonDataKinds.Email.LABEL,
        )
        val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        return contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, selection, arrayOf(contactId), null)?.use { cursor ->
            val addressCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
            val typeCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE)
            val customLabelCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.LABEL)
            buildList {
                while (cursor.moveToNext()) {
                    val address = cursor.getString(addressCol) ?: continue
                    val typeLabel = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.resources, cursor.getInt(typeCol), cursor.getString(customLabelCol)).toString()
                    add(LabeledValue(address, typeLabel))
                }
            }
        }.orEmpty().distinctBy { it.value.trim().lowercase() }
    }

    /**
     * The non-standard `Data` rows on this contact, one [ContactConnection] per *contributing app*
     * (not per row — a single app can own several data rows on the same contact). Each row's owning
     * account type is resolved to a package via [AccountManager.getAuthenticatorTypes] (the stable,
     * permission-free way to map an account type to the app that registered it); that package's own
     * launcher icon/label stand in for the connection's icon/label — not the sync adapter's own
     * per-mimetype `contacts.xml` icon declaration, which is real but considerably more fragile to
     * resolve correctly across OEM Contacts Provider variants (see IMPLEMENTATION_PLAN.md Phase 9's
     * own noted fallback-decision point; this is that fallback, applied from the start rather than
     * only after the fragile path was tried and failed).
     */
    private fun dynamicConnections(contactId: String, phoneNumbers: List<LabeledValue>): List<ContactConnection> {
        data class Row(val dataId: Long, val mimeType: String, val rawContactId: Long, val data1: String?)

        val dataProjection = arrayOf(ContactsContract.Data._ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.DATA1)
        val rows = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            dataProjection,
            "${ContactsContract.Data.CONTACT_ID} = ?",
            arrayOf(contactId),
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID)
            val mimeCol = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val rawCol = cursor.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID)
            val data1Col = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
            buildList {
                while (cursor.moveToNext()) {
                    val mimeType = cursor.getString(mimeCol) ?: continue
                    if (mimeType in STANDARD_MIMETYPES) continue
                    add(Row(cursor.getLong(idCol), mimeType, cursor.getLong(rawCol), cursor.getString(data1Col)))
                }
            }
        }.orEmpty()
        if (rows.isEmpty()) return emptyList()

        val rawContactIds = rows.map { it.rawContactId }.distinct()
        val accountTypeByRawId = queryAccountTypes(rawContactIds)
        val packageNameByAccountType = authenticatorPackagesByAccountType()

        return rows
            .mapNotNull { row ->
                val accountType = accountTypeByRawId[row.rawContactId] ?: return@mapNotNull null
                val packageName = packageNameByAccountType[accountType] ?: return@mapNotNull null
                Triple(packageName, row.dataId, row.data1)
            }
            .distinctBy { (packageName, _, _) -> packageName } // one connection per contributing app, not per data row
            .mapNotNull { (packageName, dataId, data1) ->
                val packageManager = context.packageManager
                val appInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull() ?: return@mapNotNull null
                val uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, dataId.toString())
                val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(packageName)
                // The account-owning app isn't necessarily a *messaging* connection — Google
                // Play services and Meet, for instance, both own auxiliary Data rows Google's own
                // sync adapter attaches to any Google-account contact (capability/metadata bits,
                // not a real "way to reach this person"), and neither actually registers a
                // handler for its own row's ACTION_VIEW — tapping them did nothing (confirmed
                // on-device, see chat history). Requiring the owning app itself to resolve this
                // exact intent is what tells a genuine contact-method integration (WhatsApp,
                // Telegram, Signal — anything that registers to open its own profile row) apart
                // from incidental sync metadata, with no hardcoded app allow/deny list either way.
                val resolved = packageManager.resolveActivity(intent, 0) ?: return@mapNotNull null
                // resolveActivity finding a match only means an intent-filter exists — it says
                // nothing about whether *this* caller is allowed to actually start it. Meet's own
                // audio-call action activity resolves fine but requires the caller to hold
                // CALL_PHONE, which this launcher deliberately doesn't request (its own Call
                // action already avoids that by using ACTION_DIAL, not ACTION_CALL) — starting it
                // anyway throws a SecurityException at launch time, which reads as "does nothing"
                // to the user (confirmed on-device via a Permission Denial in logcat — see chat
                // history). Checking the resolved activity's own required permission up front
                // catches this before it's ever shown, rather than failing silently on tap.
                val requiredPermission = resolved.activityInfo?.permission
                if (requiredPermission != null && context.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    return@mapNotNull null
                }
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = runCatching { packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap() }.getOrNull()
                // WhatsApp gets its own type purely so it sorts at its designated fixed-order
                // position (matching the approved Phase 9 spec) — it's discovered through the
                // exact same Data-row mechanism as every other app here, no special-casing of
                // *whether* it appears, only of *where in the list* it lands once it does.
                val type = if (packageName == "com.whatsapp") ContactConnectionType.WHATSAPP else ContactConnectionType.OTHER
                // WhatsApp's identity is the phone number it's tied to; anything else falls back
                // to the row's own DATA1 (many sync adapters store a readable handle/username
                // there), or no subtitle at all when neither is available.
                val labeledValue = if (type == ContactConnectionType.WHATSAPP) phoneNumbers.firstOrNull() else data1?.let { LabeledValue(it, null) }
                ContactConnection(type, label, icon, ConnectionDetail.Single(labeledValue?.value.orEmpty(), labeledValue?.typeLabel, intent))
            }
    }

    private fun queryAccountTypes(rawContactIds: List<Long>): Map<Long, String> {
        if (rawContactIds.isEmpty()) return emptyMap()
        val placeholders = rawContactIds.joinToString(",") { "?" }
        val projection = arrayOf(ContactsContract.RawContacts._ID, ContactsContract.RawContacts.ACCOUNT_TYPE)
        return contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            projection,
            "${ContactsContract.RawContacts._ID} IN ($placeholders)",
            rawContactIds.map { it.toString() }.toTypedArray(),
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts._ID)
            val typeCol = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)
            buildMap {
                while (cursor.moveToNext()) {
                    val accountType = cursor.getString(typeCol) ?: continue
                    put(cursor.getLong(idCol), accountType)
                }
            }
        }.orEmpty()
    }

    private fun authenticatorPackagesByAccountType(): Map<String, String> =
        AccountManager.get(context).authenticatorTypes.associate { it.type to it.packageName }
}

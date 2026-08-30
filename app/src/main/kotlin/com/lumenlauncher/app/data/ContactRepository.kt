package com.lumenlauncher.app.data

import android.content.ContentResolver
import android.provider.ContactsContract
import com.lumenlauncher.app.data.model.ContactInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wraps the system Contacts Provider (`READ_CONTACTS`) for F6's drawer-search contacts section. */
@Singleton
class ContactRepository @Inject constructor(
    private val contentResolver: ContentResolver,
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
}

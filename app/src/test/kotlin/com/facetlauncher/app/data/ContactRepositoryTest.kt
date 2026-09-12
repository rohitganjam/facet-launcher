package com.facetlauncher.app.data

import android.accounts.AccountManager
import android.accounts.AuthenticatorDescription
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.ConnectionDetail
import com.facetlauncher.app.data.model.ContactConnectionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ContactRepositoryTest {

    private val contentResolver = mock(ContentResolver::class.java)
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repository = ContactRepository(contentResolver, context)

    private fun stubEmptyQuery() {
        `when`(
            contentResolver.query(any(Uri::class.java), any(Array<String>::class.java), any(String::class.java), any(Array<String>::class.java), any()),
        ).thenReturn(null)
    }

    private data class Row(val contactId: String, val displayName: String, val number: String)

    private fun stubPhoneQuery(query: String, rows: List<Row>) {
        val cursor = MatrixCursor(
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
        )
        rows.forEach { row -> cursor.addRow(arrayOf(row.contactId, row.displayName, row.number)) }
        `when`(
            contentResolver.query(
                eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                any(Array<String>::class.java),
                any(String::class.java),
                eq(arrayOf("%$query%")),
                any(String::class.java),
            ),
        ).thenReturn(cursor)
    }

    @Test
    fun `searchContacts returns matches with their phone number`() = runTest {
        // Given one matching contact
        stubPhoneQuery("Jan", listOf(Row("1", "Jane Doe", "555-1234")))

        // When searching
        val result = repository.searchContacts("Jan", limit = 5)

        // Then it comes back with the expected fields
        assertEquals(listOf("Jane Doe"), result.map { it.displayName })
        assertEquals(listOf("555-1234"), result.map { it.phoneNumber })
    }

    @Test
    fun `searchContacts keeps only the first number for a contact with several`() = runTest {
        // Given one contact with two phone number rows
        stubPhoneQuery("Jane", listOf(Row("1", "Jane Doe", "555-1111"), Row("1", "Jane Doe", "555-2222")))

        // When searching
        val result = repository.searchContacts("Jane", limit = 5)

        // Then only one entry comes back, with the first number
        assertEquals(1, result.size)
        assertEquals("555-1111", result.single().phoneNumber)
    }

    @Test
    fun `searchContacts caps results at the given limit`() = runTest {
        // Given four distinct contacts
        stubPhoneQuery(
            "a",
            listOf(
                Row("1", "Alice", "1"),
                Row("2", "Amir", "2"),
                Row("3", "Anna", "3"),
                Row("4", "Aidan", "4"),
            ),
        )

        // When searching with limit=2
        val result = repository.searchContacts("a", limit = 2)

        // Then only the first two are returned
        assertEquals(2, result.size)
    }

    @Test
    fun `searchContacts returns an empty list for a blank query without querying the provider`() = runTest {
        // When searching with a blank query
        val result = repository.searchContacts("   ", limit = 5)

        // Then it short-circuits to an empty list
        assertEquals(emptyList<String>(), result.map { it.displayName })
    }

    @Test
    fun `searchContacts returns an empty list when the query returns null`() = runTest {
        // Given a resolver that returns null
        `when`(
            contentResolver.query(any(Uri::class.java), any(Array<String>::class.java), any(String::class.java), any(Array<String>::class.java), any(String::class.java)),
        ).thenReturn(null)

        // When searching
        val result = repository.searchContacts("Jane", limit = 5)

        // Then it degrades to an empty list
        assertEquals(emptyList<String>(), result.map { it.displayName })
    }

    private fun stubPrimaryPhone(number: String?) {
        val cursor = MatrixCursor(
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
            ),
        )
        if (number != null) cursor.addRow(arrayOf<Any?>(number, null, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null))
        `when`(
            contentResolver.query(eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
    }

    private fun stubPrimaryEmail(email: String?) {
        val cursor = MatrixCursor(
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.LABEL),
        )
        if (email != null) cursor.addRow(arrayOf<Any?>(email, ContactsContract.CommonDataKinds.Email.TYPE_HOME, null))
        `when`(
            contentResolver.query(eq(ContactsContract.CommonDataKinds.Email.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
    }

    private fun stubDataRows(rows: List<Triple<Long, String, Long>>) {
        val cursor = MatrixCursor(arrayOf(ContactsContract.Data._ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.DATA1))
        rows.forEach { (id, mimeType, rawContactId) -> cursor.addRow(arrayOf<Any?>(id, mimeType, rawContactId, null)) }
        `when`(
            contentResolver.query(eq(ContactsContract.Data.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
    }

    @Test
    fun `getConnections includes Call and Message when the contact has a phone number`() = runTest {
        // Given a contact with a phone number but no email
        stubPrimaryPhone("555-1234")
        stubPrimaryEmail(null)

        // When fetching connections
        val result = repository.getConnections("1")

        // Then Call and Message both appear, in that fixed order, and nothing else does
        assertEquals(listOf(ContactConnectionType.CALL, ContactConnectionType.MESSAGE), result.map { it.type })
    }

    @Test
    fun `getConnections becomes Multiple with each option's own type label when a contact has more than one phone number`() = runTest {
        // Given a contact with two phone numbers, one Mobile and one Work
        val cursor = MatrixCursor(
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
            ),
        )
        cursor.addRow(arrayOf<Any?>("555-1234", null, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null))
        cursor.addRow(arrayOf<Any?>("555-5678", null, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, null))
        `when`(
            contentResolver.query(eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
        stubPrimaryEmail(null)

        // When fetching connections
        val call = repository.getConnections("1").single { it.type == ContactConnectionType.CALL }

        // Then it's a disambiguation list, each option carrying its own value and type label
        val detail = call.detail as ConnectionDetail.Multiple
        assertEquals(
            listOf("555-1234" to "Mobile", "555-5678" to "Work"),
            detail.options.map { it.value to it.typeLabel },
        )
    }

    @Test
    fun `getConnections collapses two rows for the same number that only differ in formatting`() = runTest {
        // Given a contact whose two raw-contact sources (e.g. local + a synced account) each
        // stored the same real number with different spacing — the Provider's own
        // NORMALIZED_NUMBER agrees they're identical even though the raw strings don't (confirmed
        // on-device: two "duplicate" numbers in the sheet where the native Contacts app shows one
        // — see chat history)
        val cursor = MatrixCursor(
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
            ),
        )
        cursor.addRow(arrayOf<Any?>("+1 555 123 4567", "+15551234567", ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null))
        cursor.addRow(arrayOf<Any?>("+15551234567", "+15551234567", ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null))
        `when`(
            contentResolver.query(eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
        stubPrimaryEmail(null)

        // When fetching connections
        val call = repository.getConnections("1").single { it.type == ContactConnectionType.CALL }

        // Then it collapses to a single, direct Call action — not a "pick one of these identical
        // numbers" disambiguation — keeping the first row's own (nicely spaced) display value
        val detail = call.detail as ConnectionDetail.Single
        assertEquals("+1 555 123 4567", detail.value)
        assertEquals("Mobile", detail.typeLabel)
        assertEquals(Uri.parse("tel:+1 555 123 4567"), detail.intent.data)
    }

    @Test
    fun `getConnections includes Email when the contact has an email address`() = runTest {
        // Given a contact with an email but no phone number
        stubPrimaryPhone(null)
        stubPrimaryEmail("jane@example.com")

        // When fetching connections
        val result = repository.getConnections("1")

        // Then only Email appears
        assertEquals(listOf(ContactConnectionType.EMAIL), result.map { it.type })
    }

    @Test
    fun `getConnections is empty when the contact has neither a phone number nor an email nor any third-party data row`() = runTest {
        // Given a contact with no core data at all
        stubPrimaryPhone(null)
        stubPrimaryEmail(null)

        // When fetching connections
        val result = repository.getConnections("1")

        // Then there's nothing to show
        assertTrue(result.isEmpty())
    }

    private fun stubRawContactAccountTypes(vararg rawContactIdToAccountType: Pair<Long, String>) {
        val cursor = MatrixCursor(arrayOf(ContactsContract.RawContacts._ID, ContactsContract.RawContacts.ACCOUNT_TYPE))
        rawContactIdToAccountType.forEach { (rawContactId, accountType) -> cursor.addRow(arrayOf<Any>(rawContactId, accountType)) }
        `when`(
            contentResolver.query(eq(ContactsContract.RawContacts.CONTENT_URI), any(Array<String>::class.java), any(), any(Array<String>::class.java), any()),
        ).thenReturn(cursor)
    }

    private fun installFakePackage(packageName: String) {
        val packageInfo = PackageInfo().apply {
            this.packageName = packageName
            applicationInfo = ApplicationInfo().apply { this.packageName = packageName }
        }
        shadowOf(context.packageManager).installPackage(packageInfo)
    }

    @Test
    fun `a Data row only becomes a connection when its owning app actually handles viewing it`() = runTest {
        // Given a contact with two non-standard Data rows under the same Google-synced raw
        // contact: one from a real chat app that registers to view its own profile row, and one
        // from Play services — real-world sync metadata that owns *a* row but never registers a
        // handler for it (this is exactly what "Google Play services"/"Meet" rows looked like on
        // a real device before this fix — present, but doing nothing when tapped; see chat history)
        stubPrimaryPhone(null)
        stubPrimaryEmail(null)
        stubDataRows(
            listOf(
                Triple(1L, "vnd.android.cursor.item/vnd.com.example.chatapp.profile", 100L),
                Triple(2L, "vnd.android.cursor.item/vnd.google.contact_misc", 200L),
            ),
        )
        // Both raw contacts resolve to a package via AccountManager...
        stubRawContactAccountTypes(100L to "com.example.chataccount", 200L to "com.google")
        shadowOf(AccountManager.get(context)).apply {
            addAuthenticator(AuthenticatorDescription("com.example.chataccount", "com.example.chatapp", 0, 0, 0, 0))
            addAuthenticator(AuthenticatorDescription("com.google", "com.google.android.gms", 0, 0, 0, 0))
        }
        installFakePackage("com.example.chatapp")
        installFakePackage("com.google.android.gms")
        // ...but only the chat app actually registers to handle viewing its own row
        val chatAppUri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "1")
        val chatAppIntent = Intent(Intent.ACTION_VIEW, chatAppUri).setPackage("com.example.chatapp")
        shadowOf(context.packageManager).addResolveInfoForIntent(chatAppIntent, ResolveInfo())
        // Deliberately no resolve info registered for Play services' row/package.

        // Then only the chat app's connection appears
        val result = repository.getConnections("1")
        val otherPackages = result.filter { it.type == ContactConnectionType.OTHER }.map { (it.detail as ConnectionDetail.Single).intent.`package` }
        assertEquals(listOf("com.example.chatapp"), otherPackages)
    }

    @Test
    fun `a row is dropped when the resolved activity requires a permission this app doesn't hold`() = runTest {
        // Given a contact with a single non-standard Data row whose owning app *does* register a
        // real handler for it, but that handler requires CALL_PHONE — a permission this launcher
        // deliberately doesn't hold (its own Call action uses ACTION_DIAL specifically to avoid
        // needing it). This is exactly Meet's own ContactsMeetAudioActionActivity: resolveActivity
        // finds it, but actually starting it throws a Permission Denial (confirmed on-device via
        // logcat — see chat history) — reading as "does nothing" to the user.
        stubPrimaryPhone(null)
        stubPrimaryEmail(null)
        stubDataRows(listOf(Triple(1L, "vnd.android.cursor.item/vnd.com.example.meetlike.profile", 100L)))
        stubRawContactAccountTypes(100L to "com.example.meetaccount")
        shadowOf(AccountManager.get(context)).addAuthenticator(
            AuthenticatorDescription("com.example.meetaccount", "com.example.meetlike", 0, 0, 0, 0),
        )
        installFakePackage("com.example.meetlike")
        val uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "1")
        val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.example.meetlike")
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.example.meetlike"
                name = ".AudioActionActivity"
                permission = "android.permission.CALL_PHONE"
            }
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(intent, resolveInfo)
        // CALL_PHONE is deliberately left ungranted — Robolectric's default for a permission never added to the manifest/shadow.

        // When fetching connections
        val result = repository.getConnections("1")

        // Then the row is dropped rather than surfacing a connection that fails silently on tap
        assertTrue(result.none { it.type == ContactConnectionType.OTHER })
    }

    @Test
    fun `dynamic discovery ignores standard mimetype rows and drops rows it can't resolve an owning app for`() = runTest {
        // Given a contact with no phone/email, and two Data rows: one a standard (built-in) kind,
        // one a non-standard kind whose account type has no registered authenticator (unresolvable)
        stubPrimaryPhone(null)
        stubPrimaryEmail(null)
        stubDataRows(
            listOf(
                Triple(1L, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, 100L),
                Triple(2L, "vnd.android.cursor.item/vnd.com.example.chat.profile", 100L),
            ),
        )
        // RawContacts/AccountManager left unstubbed — resolving row 2's owning app fails closed

        // When fetching connections
        val result = repository.getConnections("1")

        // Then neither row contributes a connection — the standard one was never a candidate, and
        // the non-standard one silently drops rather than crashing when it can't be resolved
        assertTrue(result.isEmpty())
    }
}

package com.lumenlauncher.app.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContactRepositoryTest {

    private val contentResolver = mock(ContentResolver::class.java)
    private val repository = ContactRepository(contentResolver)

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
}

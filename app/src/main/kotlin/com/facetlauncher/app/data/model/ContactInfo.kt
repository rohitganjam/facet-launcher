package com.facetlauncher.app.data.model

/** One contact match from [android.provider.ContactsContract.CommonDataKinds.Phone] — the contact's primary/first phone number only (F6 scoping decision, no multi-number picker). */
data class ContactInfo(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
)

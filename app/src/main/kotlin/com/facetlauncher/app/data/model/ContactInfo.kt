package com.facetlauncher.app.data.model

import androidx.compose.ui.graphics.ImageBitmap

/** One contact match from [android.provider.ContactsContract.CommonDataKinds.Phone] — the contact's primary/first phone number only (F6 scoping decision, no multi-number picker). [photo] is the contact's thumbnail, `null` when it has none. */
data class ContactInfo(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photo: ImageBitmap? = null,
)

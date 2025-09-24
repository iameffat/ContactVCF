// com/contacts/vcf/data/ContactGroup.kt
package com.contacts.vcf.data

import kotlinx.serialization.Serializable

@Serializable
data class ContactGroup(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String, // ফাইলের নাম বা ব্যবহারকারীর দেওয়া নাম
    val contacts: List<Contact>
)
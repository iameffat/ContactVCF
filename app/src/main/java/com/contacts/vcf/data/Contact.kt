package com.contacts.vcf.data

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumbers: List<String>,
    val photoUri: String? = null,
    val email: String? = null
)
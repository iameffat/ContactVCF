// com/contacts/vcf/data/ContactRepository.kt
package com.contacts.vcf.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ContactRepository(private val context: Context) {
    private val filesDir = context.filesDir
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun saveContactGroup(group: ContactGroup) {
        val file = File(filesDir, "${group.id}.json")
        file.writeText(json.encodeToString(group))
    }

    fun getAllContactGroups(): List<ContactGroup> {
        return filesDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    json.decodeFromString<ContactGroup>(file.readText())
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
    }

    fun deleteContactGroup(groupId: String) {
        val file = File(filesDir, "$groupId.json")
        if (file.exists()) {
            file.delete()
        }
    }
}
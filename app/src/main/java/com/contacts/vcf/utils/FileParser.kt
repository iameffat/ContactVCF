package com.contacts.vcf.utils

import android.content.Context
import android.net.Uri
import com.contacts.vcf.data.Contact
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.lang.Exception

object FileParser {

    fun parseCsv(context: Context, uri: Uri): List<Contact> {
        // CSV পার্সিং আগের মতোই ঠিক আছে
        val contacts = mutableListOf<Contact>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = CSVReader(InputStreamReader(inputStream))
            reader.readNext() // Skip header line

            reader.forEach { line ->
                val name = line.getOrNull(0)?.trim()
                val phones = line.getOrNull(1)?.split(';')?.map { it.trim() }

                if (!name.isNullOrBlank() && !phones.isNullOrEmpty()) {
                    contacts.add(Contact(name = name, phoneNumbers = phones))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contacts
    }

    // VCF পার্সারটিকে নতুন এবং উন্নত করা হলো
    fun parseVcf(context: Context, uri: Uri): List<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            val fileContent = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (fileContent.isNullOrBlank()) return emptyList()

            // একটি VCF ফাইলে একাধিক কন্টাক্ট থাকতে পারে, সেগুলোকে আলাদা করা হলো
            val vcardBlocks = fileContent.split("BEGIN:VCARD").filter { it.contains("END:VCARD") }

            for (block in vcardBlocks) {
                try {
                    var name: String? = null
                    val phones = mutableSetOf<String>() // ডুপ্লিকেট নম্বর এড়ানোর জন্য Set ব্যবহার করা হলো
                    var photoUri: String? = null

                    // PHOTO ট্যাগ একাধিক লাইনে থাকতে পারে, তাই সেগুলোকে জোড়া লাগানো হচ্ছে
                    val processedBlock = block.replace("\r\n ", "").replace("\n ", "")
                    val lines = processedBlock.lines()

                    // প্রথমে FN (Formatted Name) খোঁজা হচ্ছে
                    name = lines.find { it.startsWith("FN:") }?.substring(3)?.trim()

                    // যদি FN না পাওয়া যায়, তাহলে N (Name) ট্যাগ থেকে নাম তৈরি করা হবে
                    if (name.isNullOrBlank()) {
                        lines.find { it.startsWith("N:") }?.let { nLine ->
                            val parts = nLine.substring(2).split(';').map { it.trim() }
                            val firstName = parts.getOrNull(1) ?: ""
                            val lastName = parts.getOrNull(0) ?: ""
                            name = "$firstName $lastName".trim()
                        }
                    }

                    // সব ধরনের TEL (ফোন নম্বর) ট্যাগ খোঁজা হচ্ছে
                    lines.forEach { line ->
                        if (line.contains("TEL")) {
                            val phone = line.substring(line.lastIndexOf(':') + 1).trim()
                            if (phone.isNotBlank()) {
                                phones.add(phone)
                            }
                        }
                    }

                    // PHOTO ট্যাগ থেকে URL খোঁজা হচ্ছে
                    lines.find { it.startsWith("PHOTO") && it.contains("http", ignoreCase = true) }?.let { photoLine ->
                        // URL টি কোলনের পর থেকে শুরু হয়
                        photoUri = photoLine.substring(photoLine.indexOf(':') + 1).trim()
                    }


                    if (!name.isNullOrBlank() && phones.isNotEmpty()) {
                        contacts.add(Contact(
                            name = name!!,
                            phoneNumbers = phones.toList(),
                            photoUri = photoUri
                        ))
                    }
                } catch (e: Exception) {
                    // যদি কোনো একটি কন্টাক্ট পার্স করতে সমস্যা হয়, তাহলে সেটিকে স্কিপ করে পরেরটিতে যাবে
                    e.printStackTrace()
                    continue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contacts
    }
}
package com.contacts.vcf.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.contacts.vcf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    viewModel: MainViewModel,
    groupId: String,
    contactId: String,
    onNavigateBack: () -> Unit
) {
    val contact = remember { viewModel.getContactById(groupId, contactId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (contact == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Contact not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.photoUri != null) {
                            AsyncImage(
                                model = contact.photoUri,
                                contentDescription = contact.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                fontSize = 60.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                items(contact.phoneNumbers) { phoneNumber ->
                    PhoneNumberItem(phoneNumber = phoneNumber)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneNumberItem(phoneNumber: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(phoneNumber))
                    Toast
                        .makeText(context, "Number copied!", Toast.LENGTH_SHORT)
                        .show()
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = phoneNumber, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Mobile", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        }) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
            context.startActivity(intent)
        }) {
            Icon(Icons.Default.Email, contentDescription = "SMS", tint = MaterialTheme.colorScheme.secondary)
        }
        // হোয়াটসঅ্যাপ বাটনের onClick লজিকটি এখানে ঠিক করা হয়েছে
        IconButton(onClick = {
            // এই onClick ব্লকের ভেতরে কোনো @Composable ফাংশন (যেমন Icon) নেই
            val normalizedNumber = phoneNumber.filter { it.isDigit() }.let {
                when {
                    it.startsWith("880") -> it
                    it.startsWith("0") -> "88$it"
                    else -> "88$it"
                }
            }
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=$normalizedNumber")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
            }
        }) {
            // Icon ফাংশনটি onClick ব্লকের বাইরে, এটি সঠিক জায়গায় আছে
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "WhatsApp",
                tint = Color(0xFF25D366),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
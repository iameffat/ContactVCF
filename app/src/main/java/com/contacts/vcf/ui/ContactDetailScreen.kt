package com.contacts.vcf.ui

import android.content.Context
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
import androidx.compose.runtime.*
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
                    PhoneNumberItem(phoneNumber = phoneNumber, viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneNumberItem(phoneNumber: String, viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val defaultCountryCode by viewModel.countryCodeState.collectAsState()
    val alwaysAsk by viewModel.alwaysAskState.collectAsState()

    var showCountryCodeDialog by remember { mutableStateOf(false) }

    if (showCountryCodeDialog) {
        CountryCodeDialog(
            defaultCode = defaultCountryCode,
            onDismiss = { showCountryCodeDialog = false },
            onConfirm = { countryCode ->
                showCountryCodeDialog = false
                val fullNumber = countryCode.filter { it.isDigit() } + phoneNumber.filter { it.isDigit() }.removePrefix("0")
                openWhatsApp(context, fullNumber)
            }
        )
    }

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
        IconButton(onClick = {
            val cleanedNumber = phoneNumber.filter { it.isDigit() }
            val codeWithoutPlus = defaultCountryCode.filter { it.isDigit() }

            if (cleanedNumber.startsWith(codeWithoutPlus)) {
                openWhatsApp(context, cleanedNumber)
            } else {
                if (alwaysAsk) {
                    showCountryCodeDialog = true
                } else {
                    val fullNumber = codeWithoutPlus + cleanedNumber.removePrefix("0")
                    openWhatsApp(context, fullNumber)
                }
            }
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "WhatsApp",
                tint = Color(0xFF25D366),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeDialog(
    defaultCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var countryCode by remember { mutableStateOf(defaultCode) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Country Code") },
        text = {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { countryCode = it },
                label = { Text("Country Code") },
                placeholder = { Text("+88") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(countryCode) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun openWhatsApp(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$number")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
    }
}
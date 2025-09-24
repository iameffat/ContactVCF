package com.contacts.vcf.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.contacts.vcf.BuildConfig
import com.contacts.vcf.R
import com.contacts.vcf.data.Contact
import com.contacts.vcf.data.ContactGroup
import com.contacts.vcf.data.SettingsManager
import com.contacts.vcf.utils.UpdateInfo
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onContactClick: (groupId: String, contactId: String) -> Unit
) {
    val contactGroups by viewModel.filteredContactGroups.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pagerState = rememberPagerState(pageCount = { contactGroups.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showRenameDialog by remember { mutableStateOf<ContactGroup?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ContactGroup?>(null) }
    var showMenuForGroup by remember { mutableStateOf<ContactGroup?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Pair<String, Contact>?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val updateInfo by viewModel.updateInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }

    updateInfo?.let { release ->
        val latestVersion = release.latestVersion
        val currentVersion = BuildConfig.VERSION_NAME

        if (isNewerVersion(latestVersion, currentVersion)) {
            UpdateAvailableDialog(
                release = release,
                onDismiss = { viewModel.clearUpdateInfo() }
            )
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = it.getFileName(context.contentResolver) ?: "Unnamed"
                val success = viewModel.importFile(it, fileName)
                if (!success) {
                    Toast.makeText(context, "Could not import any contacts from this file.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onNavigateToSettings = onNavigateToSettings,
                onAboutClick = { showAboutDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Import Contacts")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (contactGroups.isEmpty() && searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No contacts found.\nPress '+' to import a VCF or CSV file.")
                }
            } else {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp
                ) {
                    contactGroups.forEachIndexed { index, group ->
                        Box {
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {},
                                text = {
                                    Text(
                                        group.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                    onLongClick = {
                                        showMenuForGroup = group
                                        menuExpanded = true
                                    }
                                )
                            )
                            DropdownMenu(
                                expanded = menuExpanded && showMenuForGroup?.id == group.id,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                    showRenameDialog = showMenuForGroup
                                    menuExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                    showDeleteDialog = showMenuForGroup
                                    menuExpanded = false
                                })
                            }
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    if (page < contactGroups.size) {
                        ContactList(
                            contacts = contactGroups[page].contacts,
                            groupId = contactGroups[page].id,
                            viewModel = viewModel,
                            onContactClick = onContactClick,
                            onEditClick = { contact ->
                                contactToEdit = Pair(contactGroups[page].id, contact)
                            }
                        )
                    }
                }

                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = contactGroups.size,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                )
            }
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false },
            onCheckForUpdate = {
                showAboutDialog = false
                Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                viewModel.checkForUpdates()
            }
        )
    }

    showRenameDialog?.let { group ->
        RenameGroupDialog(
            group = group,
            onDismiss = { showRenameDialog = null },
            onRename = { newName ->
                viewModel.renameGroup(group, newName)
                showRenameDialog = null
            }
        )
    }

    showDeleteDialog?.let { group ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the group '${group.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(group)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    contactToEdit?.let { (groupId, contact) ->
        EditContactDialog(
            contact = contact,
            onDismiss = { contactToEdit = null },
            onSave = { updatedContact ->
                viewModel.updateContact(groupId, updatedContact)
                contactToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onAboutClick: () -> Unit
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { },
        active = false,
        onActiveChange = { },
        placeholder = { Text("Search in Contact VCF") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            Row {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
                var showOptionsMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("About this app") },
                        onClick = {
                            onAboutClick()
                            showOptionsMenu = false
                        }
                    )
                }
            }
        }
    ) {}
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit, onCheckForUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        title = { Text("About Contact VCF") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A simple app to manage your contacts from VCF files.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Version: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
                Text("Developed by: Effat", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onCheckForUpdate) {
                Text("Check for Update")
            }
        }
    )
}

@Composable
fun ContactList(
    contacts: List<Contact>,
    groupId: String,
    viewModel: MainViewModel,
    onContactClick: (groupId: String, contactId: String) -> Unit,
    onEditClick: (contact: Contact) -> Unit
) {
    var expandedMenuContactId by remember { mutableStateOf<String?>(null) }
    if (contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No contacts match the search.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items = contacts, key = { it.id }) { contact ->
                ContactListItem(
                    contact = contact,
                    isMenuExpanded = expandedMenuContactId == contact.id,
                    onMenuToggle = {
                        expandedMenuContactId = if (expandedMenuContactId == contact.id) null else contact.id
                    },
                    onEdit = {
                        onEditClick(contact)
                        expandedMenuContactId = null
                    },
                    onDelete = {
                        viewModel.deleteContact(groupId, contact.id)
                        expandedMenuContactId = null
                    },
                    onClick = { onContactClick(groupId, contact.id) }
                )
            }
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    isMenuExpanded: Boolean,
    onMenuToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (contact.photoUri != null) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = contact.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                } else {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = contact.phoneNumbers.firstOrNull() ?: "", fontSize = 14.sp, color = Color.Gray)
            }
            Box {
                IconButton(onClick = onMenuToggle) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onMenuToggle
                ) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = onEdit)
                    DropdownMenuItem(text = { Text("Delete") }, onClick = onDelete)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactDialog(
    contact: Contact,
    onDismiss: () -> Unit,
    onSave: (Contact) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(contact.name) }
    val phoneNumbers = remember { mutableStateListOf<String>().also { it.addAll(contact.phoneNumbers) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                phoneNumbers.forEachIndexed { index, phone ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = phone,
                            onValueChange = { phoneNumbers[index] = it },
                            label = { Text("Phone ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { phoneNumbers.removeAt(index) }) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = "Remove Phone")
                        }
                    }
                }
                TextButton(onClick = { phoneNumbers.add("") }) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Phone")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedContact = contact.copy(
                    name = name,
                    phoneNumbers = phoneNumbers.filter { it.isNotBlank() }
                )
                onSave(updatedContact)
            }) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameGroupDialog(group: ContactGroup, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var text by remember { mutableStateOf(group.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Group") },
        text = {
            TextField(value = text, onValueChange = { text = it }, label = { Text("Group Name") })
        },
        confirmButton = {
            Button(onClick = { onRename(text) }) { Text("Rename") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdateAvailableDialog(release: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Available: v${release.latestVersion}") },
        text = {
            Column {
                Text("A new version of the app is available. Please update to the latest version.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("What's new:\n${release.releaseNotes}")
            }
        },
        confirmButton = {
            Button(onClick = {
                downloadAndInstallApk(context, release.apkUrl)
                onDismiss()
            }) {
                Text("Update Now")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

private fun downloadAndInstallApk(context: Context, url: String) {
    Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show()
    Thread {
        try {
            val inputStream = java.net.URL(url).openStream()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
    try {
        val latestParts = latestVersion.split('.').map { it.toInt() }
        val currentParts = currentVersion.split('.').map { it.toInt() }
        val commonLength = minOf(latestParts.size, currentParts.size)

        for (i in 0 until commonLength) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }

        return latestParts.size > currentParts.size
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        return false
    }
}

fun Uri.getFileName(contentResolver: ContentResolver): String? {
    var result: String? = null
    if (this.scheme == "content") {
        val cursor = contentResolver.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) result = cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = this.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) result = result?.substring(cut!! + 1)
    }
    return result
}
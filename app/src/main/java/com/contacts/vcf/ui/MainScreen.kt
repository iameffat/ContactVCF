package com.contacts.vcf.ui

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.contacts.vcf.BuildConfig
import com.contacts.vcf.R
import com.contacts.vcf.data.Contact
import com.contacts.vcf.data.ContactGroup
import com.contacts.vcf.data.SettingsManager
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
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
                viewModel = viewModel,
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
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .pointerInput(group.id) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        showMenuForGroup = group
                                                        menuExpanded = true
                                                    }
                                                )
                                            }
                                    ) {
                                        Text(group.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = menuExpanded && showMenuForGroup?.id == group.id,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    onClick = {
                                        showRenameDialog = showMenuForGroup
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showDeleteDialog = showMenuForGroup
                                        menuExpanded = false
                                    }
                                )
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
        AboutAppDialog(onDismiss = { showAboutDialog = false })
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
    viewModel: MainViewModel,
    onAboutClick: () -> Unit
) {
    val themeState by viewModel.themeState.collectAsState()

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
                var showThemeMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showThemeMenu = true }) {
                    Icon(Icons.Outlined.ColorLens, contentDescription = "Change Theme")
                }
                ThemeDropdownMenu(
                    expanded = showThemeMenu,
                    onDismiss = { showThemeMenu = false },
                    currentTheme = themeState,
                    onThemeSelect = { viewModel.setTheme(it) }
                )

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
fun ThemeDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentTheme: Int,
    onThemeSelect: (Int) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("System Default") },
            onClick = { onThemeSelect(SettingsManager.THEME_SYSTEM); onDismiss() },
            leadingIcon = { Icon(Icons.Outlined.BrightnessAuto, contentDescription = null) },
            trailingIcon = { if (currentTheme == SettingsManager.THEME_SYSTEM) Icon(Icons.Default.Check, contentDescription = "Selected") }
        )
        DropdownMenuItem(
            text = { Text("Light") },
            onClick = { onThemeSelect(SettingsManager.THEME_LIGHT); onDismiss() },
            leadingIcon = { Icon(Icons.Outlined.WbSunny, contentDescription = null) },
            trailingIcon = { if (currentTheme == SettingsManager.THEME_LIGHT) Icon(Icons.Default.Check, contentDescription = "Selected") }
        )
        DropdownMenuItem(
            text = { Text("Dark") },
            onClick = { onThemeSelect(SettingsManager.THEME_DARK); onDismiss() },
            leadingIcon = { Icon(Icons.Outlined.ModeNight, contentDescription = null) },
            trailingIcon = { if (currentTheme == SettingsManager.THEME_DARK) Icon(Icons.Default.Check, contentDescription = "Selected") }
        )
    }
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
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
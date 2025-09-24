package com.contacts.vcf.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contacts.vcf.data.Contact
import com.contacts.vcf.data.ContactGroup
import com.contacts.vcf.data.ContactRepository
import com.contacts.vcf.data.SettingsManager
import com.contacts.vcf.utils.FileParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    private val repository = ContactRepository(application)

    private val _contactGroups = MutableStateFlow<List<ContactGroup>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val themeState: StateFlow<Int> = settingsManager.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsManager.THEME_SYSTEM)

    val filteredContactGroups = _contactGroups.combine(_searchQuery) { groups, query ->
        if (query.isBlank()) {
            groups
        } else {
            groups.map { group ->
                val filteredContacts = group.contacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                            contact.phoneNumbers.any { it.contains(query, ignoreCase = true) }
                }
                group.copy(contacts = filteredContacts)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadContactGroups()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setTheme(themeOption: Int) {
        viewModelScope.launch {
            settingsManager.saveThemePreference(themeOption)
        }
    }

    private fun loadContactGroups() {
        viewModelScope.launch {
            _contactGroups.value = repository.getAllContactGroups()
        }
    }

    fun importFile(uri: Uri, fileName: String): Boolean {
        val context = getApplication<Application>().applicationContext
        val contacts = when {
            fileName.endsWith(".vcf", true) -> FileParser.parseVcf(context, uri)
            fileName.endsWith(".csv", true) -> FileParser.parseCsv(context, uri)
            else -> emptyList()
        }

        if (contacts.isNotEmpty()) {
            val groupName = fileName.substringBeforeLast('.')
            val newGroup = ContactGroup(name = groupName, contacts = contacts)
            viewModelScope.launch {
                repository.saveContactGroup(newGroup)
                loadContactGroups()
            }
            return true
        }
        return false
    }

    fun deleteGroup(group: ContactGroup) {
        viewModelScope.launch {
            repository.deleteContactGroup(group.id)
            loadContactGroups()
        }
    }

    fun renameGroup(group: ContactGroup, newName: String) {
        viewModelScope.launch {
            val updatedGroup = group.copy(name = newName)
            repository.saveContactGroup(updatedGroup)
            loadContactGroups()
        }
    }

    fun deleteContact(groupId: String, contactId: String) {
        viewModelScope.launch {
            val group = _contactGroups.value.find { it.id == groupId }
            group?.let {
                val updatedContacts = it.contacts.filterNot { contact -> contact.id == contactId }
                val updatedGroup = it.copy(contacts = updatedContacts)
                repository.saveContactGroup(updatedGroup)
                loadContactGroups()
            }
        }
    }

    fun updateContact(groupId: String, updatedContact: Contact) {
        viewModelScope.launch {
            val group = _contactGroups.value.find { it.id == groupId }
            group?.let {
                val updatedContacts = it.contacts.map { contact ->
                    if (contact.id == updatedContact.id) updatedContact else contact
                }
                val updatedGroup = it.copy(contacts = updatedContacts)
                repository.saveContactGroup(updatedGroup)
                loadContactGroups()
            }
        }
    }

    fun getContactById(groupId: String, contactId: String): Contact? {
        return _contactGroups.value.find { it.id == groupId }
            ?.contacts?.find { it.id == contactId }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
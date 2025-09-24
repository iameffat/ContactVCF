package com.contacts.vcf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.contacts.vcf.data.SettingsManager
import com.contacts.vcf.ui.ContactDetailScreen
import com.contacts.vcf.ui.MainScreen
import com.contacts.vcf.ui.MainViewModel
import com.contacts.vcf.ui.MainViewModelFactory
import com.contacts.vcf.ui.SettingsScreen
import com.contacts.vcf.ui.theme.ContactVCFTheme

class MainActivity : ComponentActivity() {

    private val settingsManager by lazy { SettingsManager(this) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, settingsManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeState by viewModel.themeState.collectAsState()
            val useDarkTheme = when (themeState) {
                SettingsManager.THEME_LIGHT -> false
                SettingsManager.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            ContactVCFTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                onContactClick = { groupId, contactId ->
                                    navController.navigate("contactDetail/$groupId/$contactId")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("contactDetail/{groupId}/{contactId}") { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId")
                            val contactId = backStackEntry.arguments?.getString("contactId")
                            if (groupId != null && contactId != null) {
                                ContactDetailScreen(
                                    viewModel = viewModel,
                                    groupId = groupId,
                                    contactId = contactId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.AyushDropDatabase
import com.example.data.repository.ArcadeRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FileRepository
import com.example.ui.components.ClaimLinkDialog
import com.example.ui.components.UploadDialog
import com.example.ui.screens.ArcadeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.AyushDropTheme
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.FileShareViewModel

enum class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    VAULT("Vault", Icons.Filled.Folder, Icons.Outlined.Folder, "tab_vault"),
    ARCADE("Arcade", Icons.Filled.Gamepad, Icons.Outlined.Gamepad, "tab_arcade"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {

    private var initialDeepLinkToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming deep link
        handleDeepLink(intent)

        val database = AyushDropDatabase.getInstance(applicationContext)
        val fileRepo = FileRepository(applicationContext, database.dao())
        val authRepo = AuthRepository(database.dao())
        val arcadeRepo = ArcadeRepository(database.dao())

        val fileViewModel = FileShareViewModel(fileRepo, applicationContext)
        val authViewModel = AuthViewModel(authRepo)
        val arcadeViewModel = ArcadeViewModel(arcadeRepo)

        setContent {
            AyushDropTheme {
                MainApp(
                    fileViewModel = fileViewModel,
                    authViewModel = authViewModel,
                    arcadeViewModel = arcadeViewModel,
                    initialDeepLinkToken = initialDeepLinkToken
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "ayushdrop" && data.host == "share") {
            val path = data.pathSegments.firstOrNull() ?: data.lastPathSegment
            if (!path.isNullOrBlank()) {
                initialDeepLinkToken = path
            }
        }
    }
}

@Composable
fun MainApp(
    fileViewModel: FileShareViewModel,
    authViewModel: AuthViewModel,
    arcadeViewModel: ArcadeViewModel,
    initialDeepLinkToken: String?
) {
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showClaimDialog by remember { mutableStateOf(false) }
    var claimDialogInitialToken by remember { mutableStateOf("") }

    val currentSession by authViewModel.currentSession.collectAsStateWithLifecycle()

    // If an incoming deep link was passed, automatically open the claim dialog
    LaunchedEffect(initialDeepLinkToken) {
        if (!initialDeepLinkToken.isNullOrBlank()) {
            claimDialogInitialToken = initialDeepLinkToken
            showClaimDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.testTag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavTab.HOME -> HomeScreen(
                    fileViewModel = fileViewModel,
                    onNavigateToUpload = { showUploadDialog = true },
                    onNavigateToClaim = {
                        claimDialogInitialToken = ""
                        showClaimDialog = true
                    },
                    onNavigateToArcade = { selectedTab = NavTab.ARCADE },
                    onNavigateToVault = { selectedTab = NavTab.VAULT }
                )

                NavTab.VAULT -> VaultScreen(
                    viewModel = fileViewModel,
                    onUploadClick = { showUploadDialog = true }
                )

                NavTab.ARCADE -> ArcadeScreen(
                    viewModel = arcadeViewModel,
                    currentUser = currentSession.username
                )

                NavTab.PROFILE -> ProfileScreen(
                    authViewModel = authViewModel,
                    fileViewModel = fileViewModel
                )
            }

            // Upload Modal Dialog
            if (showUploadDialog) {
                UploadDialog(
                    viewModel = fileViewModel,
                    currentUser = currentSession.username,
                    onDismiss = { showUploadDialog = false }
                )
            }

            // Claim Link Dialog
            if (showClaimDialog) {
                ClaimLinkDialog(
                    viewModel = fileViewModel,
                    initialToken = claimDialogInitialToken,
                    onDismiss = { showClaimDialog = false }
                )
            }
        }
    }
}

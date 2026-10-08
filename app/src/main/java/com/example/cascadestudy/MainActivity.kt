package com.example.cascadestudy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cascadestudy.data.local.AppDatabase
import com.example.cascadestudy.data.repository.SessionRepository
import com.example.cascadestudy.notification.NotificationHelper
import com.example.cascadestudy.presentation.AppScreen
import com.example.cascadestudy.presentation.selection.SessionSelectionScreen
import com.example.cascadestudy.presentation.timer.TimerScreen
import com.example.cascadestudy.presentation.timer.TimerViewModel
import com.example.cascadestudy.sound.SoundManager
import com.example.cascadestudy.ui.theme.CascadeStudyTheme

// Main entry point Activity managing navigation, ViewModel initialization, and system permissions
class MainActivity : ComponentActivity() {

    // Launcher for requesting POST_NOTIFICATIONS runtime permission on Android 13+
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // TimerViewModel initialized with custom Factory supplying database, repository, notification, and sound managers
    private val viewModel: TimerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = SessionRepository(database.completedSessionDao())
                val notificationHelper = NotificationHelper(applicationContext)
                val soundManager = SoundManager(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return TimerViewModel(
                    sessionRepository = repository,
                    notificationHelper = notificationHelper,
                    soundManager = soundManager
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permissions if required on Android 13+
        requestNotificationPermission()

        // Enable edge-to-edge layout rendering
        enableEdgeToEdge()

        setContent {
            CascadeStudyTheme {
                val uiState by viewModel.uiState.collectAsState()

                // State holding current screen navigation, preserved across configuration changes
                var currentScreen by rememberSaveable {
                    mutableStateOf(AppScreen.SESSION_SELECTION)
                }

                when (currentScreen) {
                    AppScreen.SESSION_SELECTION -> {
                        // Display session preset selection screen
                        SessionSelectionScreen(
                            onSessionSelected = { preset ->
                                viewModel.selectPreset(preset)
                                currentScreen = AppScreen.TIMER
                            }
                        )
                    }
                    AppScreen.TIMER -> {
                        // Handle back press gesture to reset timer and return to selection screen
                        BackHandler {
                            viewModel.reset()
                            currentScreen = AppScreen.SESSION_SELECTION
                        }

                        // Display active timer screen
                        TimerScreen(
                            uiState = uiState,
                            onStart = viewModel::start,
                            onPause = viewModel::pause,
                            onResume = viewModel::resume,
                            onReset = viewModel::reset,
                            onFinishEarly = {
                                viewModel.finishSessionEarly()
                                currentScreen = AppScreen.SESSION_SELECTION
                            }
                        )
                    }
                }
            }
        }
    }

    // Requests POST_NOTIFICATIONS permission on Android 13+ (API 33+) if not already granted
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

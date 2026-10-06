package com.example.cascadestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cascadestudy.data.local.AppDatabase
import com.example.cascadestudy.data.repository.SessionRepository
import com.example.cascadestudy.presentation.AppScreen
import com.example.cascadestudy.presentation.selection.SessionSelectionScreen
import com.example.cascadestudy.presentation.timer.TimerScreen
import com.example.cascadestudy.presentation.timer.TimerViewModel
import com.example.cascadestudy.ui.theme.CascadeStudyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = SessionRepository(database.completedSessionDao())
                @Suppress("UNCHECKED_CAST")
                return TimerViewModel(sessionRepository = repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CascadeStudyTheme {
                val uiState by viewModel.uiState.collectAsState()

                var currentScreen by rememberSaveable {
                    mutableStateOf(AppScreen.SESSION_SELECTION)
                }

                when (currentScreen) {
                    AppScreen.SESSION_SELECTION -> {
                        SessionSelectionScreen(
                            onSessionSelected = { preset ->
                                viewModel.selectPreset(preset)
                                currentScreen = AppScreen.TIMER
                            }
                        )
                    }
                    AppScreen.TIMER -> {
                        BackHandler {
                            viewModel.reset()
                            currentScreen = AppScreen.SESSION_SELECTION
                        }

                        TimerScreen(
                            uiState = uiState,
                            onStart = viewModel::start,
                            onPause = viewModel::pause,
                            onResume = viewModel::resume,
                            onReset = viewModel::reset
                        )
                    }
                }
            }
        }
    }
}

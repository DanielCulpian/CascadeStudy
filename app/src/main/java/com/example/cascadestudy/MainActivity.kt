package com.example.cascadestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.cascadestudy.presentation.timer.TimerScreen
import com.example.cascadestudy.presentation.timer.TimerViewModel
import com.example.cascadestudy.ui.theme.CascadeStudyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CascadeStudyTheme {
                val uiState by viewModel.uiState.collectAsState()
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

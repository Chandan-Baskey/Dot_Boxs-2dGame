package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AppAudioManager
import com.example.ui.paper.AudioSettingsDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DotsAndBoxesApp()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    AppAudioManager.getInstance(this).resumeMusic()
  }

  override fun onPause() {
    super.onPause()
    AppAudioManager.getInstance(this).pauseMusic()
  }
}

@Composable
fun DotsAndBoxesApp(
    viewModel: GameViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedGridSize by viewModel.selectedGridSize.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val audioSettings by viewModel.audioSettings.collectAsStateWithLifecycle()
    val showAudioSettings by viewModel.showAudioSettingsDialog.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) { innerPadding ->
        when (screenState) {
            ScreenState.SPLASH -> {
                SplashScreen(
                    onSplashFinished = viewModel::onSplashFinished,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenState.HOME -> {
                HomeScreen(
                    selectedMode = selectedMode,
                    selectedGridSize = selectedGridSize,
                    onModeSelected = viewModel::selectMode,
                    onGridSizeSelected = viewModel::selectGridSize,
                    onStartGame = { viewModel.startNewGame() },
                    onOpenAudioSettings = viewModel::openAudioSettings,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenState.PLAYING -> {
                GameScreen(
                    state = gameState,
                    onEdgeSelected = viewModel::onEdgeClicked,
                    onRestart = viewModel::restartCurrentGame,
                    onHome = viewModel::navigateToHome,
                    onUndo = viewModel::undoMove,
                    onOpenAudioSettings = viewModel::openAudioSettings,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Global Audio Settings Dialog
        if (showAudioSettings) {
            AudioSettingsDialog(
                settings = audioSettings,
                onMusicToggled = viewModel::setMusicEnabled,
                onSfxToggled = viewModel::setSfxEnabled,
                onMusicVolumeChanged = viewModel::setMusicVolume,
                onSfxVolumeChanged = viewModel::setSfxVolume,
                onTestSfx = viewModel::testSfx,
                onDismiss = viewModel::closeAudioSettings
            )
        }
    }
}


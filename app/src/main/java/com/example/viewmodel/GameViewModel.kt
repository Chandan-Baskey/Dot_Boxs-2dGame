package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AppAudioManager
import com.example.audio.AudioSettings
import com.example.audio.SfxType
import com.example.logic.DotsAndBoxesAi
import com.example.logic.DotsAndBoxesEngine
import com.example.model.Edge
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.GridSize
import com.example.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScreenState {
    SPLASH,
    HOME,
    PLAYING
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val audioManager: AppAudioManager = AppAudioManager.getInstance(application)
    val audioSettings: StateFlow<AudioSettings> = audioManager.settings

    private val _screenState = MutableStateFlow(ScreenState.SPLASH)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.VS_AI)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _selectedGridSize = MutableStateFlow(GridSize.CLASSIC)
    val selectedGridSize: StateFlow<GridSize> = _selectedGridSize.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _showAudioSettingsDialog = MutableStateFlow(false)
    val showAudioSettingsDialog: StateFlow<Boolean> = _showAudioSettingsDialog.asStateFlow()

    private val history = mutableListOf<GameState>()
    private var aiJob: Job? = null

    init {
        // Start background music if enabled
        audioManager.playMusic()
    }

    fun onSplashFinished() {
        _screenState.value = ScreenState.HOME
        audioManager.resumeMusic()
    }

    fun openAudioSettings() {
        audioManager.playSfx(SfxType.BUTTON_CLICK)
        _showAudioSettingsDialog.value = true
    }

    fun closeAudioSettings() {
        audioManager.playSfx(SfxType.BUTTON_CLICK)
        _showAudioSettingsDialog.value = false
    }

    fun setMusicEnabled(enabled: Boolean) {
        audioManager.setMusicEnabled(enabled)
    }

    fun setSfxEnabled(enabled: Boolean) {
        audioManager.setSfxEnabled(enabled)
    }

    fun setMusicVolume(volume: Float) {
        audioManager.setMusicVolume(volume)
    }

    fun setSfxVolume(volume: Float) {
        audioManager.setSfxVolume(volume)
    }

    fun testSfx() {
        audioManager.playSfx(SfxType.PEN_STROKE)
    }

    fun playButtonSound() {
        audioManager.playSfx(SfxType.BUTTON_CLICK)
    }

    fun selectMode(mode: GameMode) {
        playButtonSound()
        _selectedMode.value = mode
    }

    fun selectGridSize(gridSize: GridSize) {
        playButtonSound()
        _selectedGridSize.value = gridSize
    }

    fun startNewGame(mode: GameMode = _selectedMode.value, gridSize: GridSize = _selectedGridSize.value) {
        playButtonSound()
        aiJob?.cancel()
        history.clear()
        _selectedMode.value = mode
        _selectedGridSize.value = gridSize

        val initial = GameState(
            mode = mode,
            gridSize = gridSize,
            currentPlayer = Player.PLAYER_1
        )
        _gameState.value = initial
        _screenState.value = ScreenState.PLAYING
    }

    fun restartCurrentGame() {
        playButtonSound()
        startNewGame(_selectedMode.value, _selectedGridSize.value)
    }

    fun navigateToHome() {
        playButtonSound()
        aiJob?.cancel()
        _screenState.value = ScreenState.HOME
    }

    fun onEdgeClicked(edge: Edge) {
        val current = _gameState.value
        if (current.isGameOver || current.isAiThinking) return
        if (current.edges.containsKey(edge)) return

        // In VS_AI mode, human only plays as Player 1
        if (current.mode == GameMode.VS_AI && current.currentPlayer != Player.PLAYER_1) return

        history.add(current)
        val next = DotsAndBoxesEngine.makeMove(current, edge)
        _gameState.value = next

        // Audio feedback: box capture vs line stroke vs game over
        handleMoveAudio(current, next)

        // If in VS_AI mode and it is now AI's turn
        if (next.mode == GameMode.VS_AI && !next.isGameOver && next.currentPlayer == Player.PLAYER_2) {
            triggerAiTurn()
        }
    }

    private fun handleMoveAudio(previous: GameState, next: GameState) {
        if (next.isGameOver) {
            if (next.isDraw) {
                audioManager.playSfx(SfxType.GAME_DRAW)
            } else {
                audioManager.playSfx(SfxType.GAME_WIN)
            }
        } else if (next.boxes.size > previous.boxes.size) {
            audioManager.playSfx(SfxType.BOX_COMPLETED)
        } else {
            audioManager.playSfx(SfxType.PEN_STROKE)
        }
    }

    private fun triggerAiTurn() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _gameState.update { it.copy(isAiThinking = true) }

            // Loop while it's AI's turn (AI gets consecutive turns if it completes a box)
            while (true) {
                val state = _gameState.value
                if (state.isGameOver || state.currentPlayer != Player.PLAYER_2) break

                // Authentic "drawing line" delay so user observes the move
                delay(650)

                val bestEdge = DotsAndBoxesAi.findBestMove(state)
                if (bestEdge == null) break

                val next = DotsAndBoxesEngine.makeMove(state, bestEdge)
                _gameState.value = next.copy(isAiThinking = !next.isGameOver && next.currentPlayer == Player.PLAYER_2)

                handleMoveAudio(state, next)

                if (next.isGameOver || next.currentPlayer != Player.PLAYER_2) {
                    break
                }
            }

            _gameState.update { it.copy(isAiThinking = false) }
        }
    }

    fun undoMove() {
        playButtonSound()
        val current = _gameState.value
        if (current.isAiThinking) return

        if (history.isNotEmpty()) {
            aiJob?.cancel()
            val previous = history.removeAt(history.lastIndex)
            _gameState.value = previous.copy(isAiThinking = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiJob?.cancel()
    }
}

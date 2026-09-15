package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Edge
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.Player
import com.example.ui.board.DotsAndBoxesBoard
import com.example.ui.paper.PaperBackgroundBox
import com.example.ui.paper.PaperButton
import com.example.ui.paper.PaperScoreHeader
import com.example.ui.theme.AmberStamp
import com.example.ui.theme.BlueInk
import com.example.ui.theme.BlueInkLight
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperCard
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.RedInk
import com.example.ui.theme.RedInkLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameState,
    onEdgeSelected: (Edge) -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onUndo: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    PaperBackgroundBox(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Adaptive Landscape Layout (Side-by-side)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Adaptive Board (Fills available height/width)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsAndBoxesBoard(
                            state = state,
                            onEdgeSelected = onEdgeSelected,
                            enabled = !state.isAiThinking && !state.isGameOver,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Controls and Score Pane
                    Column(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 360.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameTopBar(
                            mode = state.mode,
                            gridDescriptor = state.gridSize.boxDescriptor,
                            onHome = onHome,
                            onRestart = onRestart,
                            onUndo = onUndo,
                            onOpenAudioSettings = onOpenAudioSettings,
                            canUndo = state.edges.isNotEmpty() && !state.isAiThinking && !state.isGameOver
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        PaperScoreHeader(
                            mode = state.mode,
                            currentPlayer = state.currentPlayer,
                            p1Score = state.p1Score,
                            p2Score = state.p2Score,
                            isAiThinking = state.isAiThinking
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = PaperSurface,
                            border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Boxes Left: ${state.remainingBoxes}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkMedium
                                )
                                Text(
                                    text = "Lines Left: ${state.remainingEdges}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkMedium
                                )
                            }
                        }

                        if (state.edges.size < 2) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✎ Drag from a dot to an adjacent dot",
                                fontSize = 11.sp,
                                color = InkMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Adaptive Portrait Layout (Vertical)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Action Bar
                    GameTopBar(
                        mode = state.mode,
                        gridDescriptor = state.gridSize.boxDescriptor,
                        onHome = onHome,
                        onRestart = onRestart,
                        onUndo = onUndo,
                        onOpenAudioSettings = onOpenAudioSettings,
                        canUndo = state.edges.isNotEmpty() && !state.isAiThinking && !state.isGameOver
                    )

                    // Score Banner & Turn Indicator
                    PaperScoreHeader(
                        mode = state.mode,
                        currentPlayer = state.currentPlayer,
                        p1Score = state.p1Score,
                        p2Score = state.p2Score,
                        isAiThinking = state.isAiThinking
                    )

                    if (state.edges.size < 2) {
                        Text(
                            text = "✎ Drag from a dot to an adjacent dot to connect",
                            fontSize = 12.sp,
                            color = InkMuted,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Scalable Board Container: Fills all remaining screen space dynamically
                    // without pushing header or footer off screen!
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .widthIn(max = 540.dp)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsAndBoxesBoard(
                            state = state,
                            onEdgeSelected = onEdgeSelected,
                            enabled = !state.isAiThinking && !state.isGameOver,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Footer Game Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 540.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Boxes Left: ${state.remainingBoxes}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkMedium
                        )
                        Text(
                            text = "Lines Left: ${state.remainingEdges}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkMedium
                        )
                    }
                }
            }

            // Game Over Modal Dialog
            if (state.isGameOver) {
                GameOverDialog(
                    state = state,
                    onPlayAgain = onRestart,
                    onHome = onHome
                )
            }
        }
    }
}

@Composable
private fun GameTopBar(
    mode: GameMode,
    gridDescriptor: String,
    onHome: () -> Unit,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    canUndo: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home Button
        IconButton(
            onClick = onHome,
            modifier = Modifier.testTag("home_button")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = InkDark,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title and Mode
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mode.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Text(
                text = gridDescriptor,
                fontSize = 11.sp,
                color = InkMuted
            )
        }

        // Action Buttons: Settings, Undo & Restart
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenAudioSettings,
                modifier = Modifier.testTag("game_audio_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Audio Settings",
                    tint = InkDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (canUndo) {
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo Move",
                        tint = InkMedium,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            IconButton(
                onClick = onRestart,
                modifier = Modifier.testTag("restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Game",
                    tint = InkDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameOverDialog(
    state: GameState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { /* Modal */ },
        modifier = Modifier.testTag("game_over_dialog")
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .heightIn(max = 560.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = PaperCard,
            border = BorderStroke(2.dp, PaperBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val winner = state.winner
                val isDraw = state.isDraw

                val titleText = when {
                    isDraw -> "It's a Tie!"
                    state.mode == GameMode.VS_AI && winner == Player.PLAYER_1 -> "You Win!"
                    state.mode == GameMode.VS_AI && winner == Player.PLAYER_2 -> "Computer Wins!"
                    winner == Player.PLAYER_1 -> "Player 1 Wins!"
                    winner == Player.PLAYER_2 -> "Player 2 Wins!"
                    else -> "Game Finished!"
                }

                val titleColor = when {
                    isDraw -> AmberStamp
                    winner == Player.PLAYER_1 -> BlueInk
                    else -> RedInk
                }

                // Decorative Victory Stamp
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(titleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDraw) "=" else "★",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = titleText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = titleColor
                )

                Text(
                    text = "FINAL SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = InkMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Score comparison cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // P1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueInkLight)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = Player.PLAYER_1.getDisplayName(state.mode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueInk
                        )
                        Text(
                            text = "${state.p1Score}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = BlueInk
                        )
                    }

                    Text(
                        text = "—",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // P2 / AI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RedInkLight)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = Player.PLAYER_2.getDisplayName(state.mode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedInk
                        )
                        Text(
                            text = "${state.p2Score}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = RedInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Play Again and Home
                PaperButton(
                    text = "PLAY AGAIN",
                    onClick = onPlayAgain,
                    icon = Icons.Default.Refresh,
                    inkColor = titleColor,
                    isPrimary = true,
                    testTag = "play_again_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PaperButton(
                    text = "MAIN MENU",
                    onClick = onHome,
                    icon = Icons.Default.Home,
                    inkColor = InkDark,
                    isPrimary = false,
                    testTag = "dialog_home_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

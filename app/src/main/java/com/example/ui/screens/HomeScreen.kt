package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.GridSize
import com.example.model.Player
import com.example.ui.paper.PaperButton
import com.example.ui.paper.PaperCardView
import com.example.ui.paper.PenRenderer
import com.example.ui.theme.AmberStamp
import com.example.ui.theme.BlueInk
import com.example.ui.theme.BlueInkLight
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperCard
import com.example.ui.theme.PaperGridLine
import com.example.ui.theme.PaperMarginLine
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.RedInk
import com.example.ui.theme.RedInkLight

@Composable
fun HomeScreen(
    selectedMode: GameMode,
    selectedGridSize: GridSize,
    onModeSelected: (GameMode) -> Unit,
    onGridSizeSelected: (GridSize) -> Unit,
    onStartGame: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    RuledNotebookPaperBox(
        modifier = modifier,
        topMarginHeight = 76.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. SEPARATE TOP HEADER AREA (Top Margin on Ruled Paper) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Studio Name centered on the line with hand-drawn pencil stroke underline
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = "UNIQUEGAMES",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 2.8.sp,
                            color = InkDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        PencilStrokeUnderline(
                            modifier = Modifier
                                .width(110.dp)
                                .height(4.dp)
                        )
                    }

                    // Settings icon on the right side of the same line
                    IconButton(
                        onClick = onOpenAudioSettings,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .testTag("audio_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = InkDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // --- 2. GAME CONTENT CONTINUING ON THE RULED NOTEBOOK PAPER ---
                Spacer(modifier = Modifier.height(14.dp))

                // Small game icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaperCard)
                        .border(BorderStroke(1.2.dp, PaperBorder), RoundedCornerShape(8.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pad = 9.dp.toPx()
                        val cellSize = size.width - pad * 2

                        // Shaded box in blue ink
                        val rect = Rect(pad, pad, pad + cellSize, pad + cellSize)
                        PenRenderer.drawBoxHatching(this, rect, Player.PLAYER_1, 0, 0)

                        // 4 corners dots
                        val corners = listOf(
                            Offset(pad, pad),
                            Offset(pad + cellSize, pad),
                            Offset(pad, pad + cellSize),
                            Offset(pad + cellSize, pad + cellSize)
                        )

                        // Connecting edges in blue & red ink
                        drawLine(BlueInk, Offset(pad, pad), Offset(pad + cellSize, pad), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                        drawLine(RedInk, Offset(pad + cellSize, pad), Offset(pad + cellSize, pad + cellSize), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                        drawLine(BlueInk, Offset(pad + cellSize, pad + cellSize), Offset(pad, pad + cellSize), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                        drawLine(BlueInk, Offset(pad, pad + cellSize), Offset(pad, pad), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)

                        corners.forEach { pt ->
                            drawCircle(InkDark, radius = 3.2.dp.toPx(), center = pt)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Title in larger, bold, hand-drawn font
                Text(
                    text = "DotBox Duel",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = InkDark,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Small tagline underneath
                Text(
                    text = "CLASSIC PEN & PAPER GAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = InkMedium
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Game Mode Selection Section
                Text(
                    text = "SELECT GAME MODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 2 Player Local Mode Card
                ModeSelectionCard(
                    title = GameMode.LOCAL_2P.title,
                    subtitle = GameMode.LOCAL_2P.subtitle,
                    icon = Icons.Default.Groups,
                    isSelected = selectedMode == GameMode.LOCAL_2P,
                    accentColor = BlueInk,
                    onClick = { onModeSelected(GameMode.LOCAL_2P) },
                    testTag = "mode_local_2p"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Player vs Computer Mode Card
                ModeSelectionCard(
                    title = GameMode.VS_AI.title,
                    subtitle = GameMode.VS_AI.subtitle,
                    icon = Icons.Default.SmartToy,
                    isSelected = selectedMode == GameMode.VS_AI,
                    accentColor = RedInk,
                    onClick = { onModeSelected(GameMode.VS_AI) },
                    testTag = "mode_vs_ai"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Grid Size Selection
                Text(
                    text = "GRID SIZE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GridSize.values().forEach { size ->
                        GridSizeChip(
                            gridSize = size,
                            isSelected = selectedGridSize == size,
                            onSelect = { onGridSizeSelected(size) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Start Game CTA
                PaperButton(
                    text = "START GAME",
                    onClick = onStartGame,
                    icon = Icons.Default.PlayArrow,
                    inkColor = if (selectedMode == GameMode.VS_AI) RedInk else BlueInk,
                    isPrimary = true,
                    testTag = "start_game_button",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Authentic ruled notebook paper background with:
 * - A dedicated blank top header margin
 * - A classic notebook header rule dividing the top margin from the ruled lines
 * - Continuous horizontal blue ruled notebook lines across the page
 * - A vertical red margin line running down the sheet
 */
@Composable
private fun RuledNotebookPaperBox(
    modifier: Modifier = Modifier,
    topMarginHeight: androidx.compose.ui.unit.Dp = 76.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val topMarginPx = topMarginHeight.toPx()

            // 1. Red vertical notebook margin line (classic school notebook red margin)
            val marginX = (width * 0.08f).coerceIn(36.dp.toPx(), 52.dp.toPx())
            drawLine(
                color = PaperMarginLine,
                start = Offset(marginX, 0f),
                end = Offset(marginX, height),
                strokeWidth = 1.3.dp.toPx()
            )

            // 2. Notebook header rule: classic double rule separating top margin from ruled body
            drawLine(
                color = Color(0x3516489E),
                start = Offset(0f, topMarginPx),
                end = Offset(width, topMarginPx),
                strokeWidth = 1.4.dp.toPx()
            )
            drawLine(
                color = Color(0x1F16489E),
                start = Offset(0f, topMarginPx + 3.dp.toPx()),
                end = Offset(width, topMarginPx + 3.dp.toPx()),
                strokeWidth = 0.8.dp.toPx()
            )

            // 3. Regular ruled horizontal lines below the top margin
            val lineSpacing = 28.dp.toPx()
            var y = topMarginPx + 3.dp.toPx() + lineSpacing
            while (y < height) {
                drawLine(
                    color = PaperGridLine,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += lineSpacing
            }
        }
        content()
    }
}

@Composable
private fun PencilStrokeUnderline(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF455A64)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val midY = size.height / 2f

        // Primary hand-drawn pencil stroke with a natural subtle wave
        val mainPath = Path().apply {
            moveTo(2f, midY + 0.4f)
            cubicTo(
                w * 0.28f, midY - 0.9f,
                w * 0.68f, midY + 1.0f,
                w - 2f, midY - 0.3f
            )
        }
        drawPath(
            path = mainPath,
            color = color,
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Subtle overlapping graphite sketch line giving an authentic pencil feel
        val sketchPath = Path().apply {
            moveTo(6f, midY - 0.4f)
            cubicTo(
                w * 0.38f, midY + 0.7f,
                w * 0.72f, midY - 0.6f,
                w - 8f, midY + 0.3f
            )
        }
        drawPath(
            path = sketchPath,
            color = color.copy(alpha = 0.45f),
            style = Stroke(
                width = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = accentColor),
                onClick = onClick
            )
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(14.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PaperCard else PaperSurface,
        border = BorderStroke(
            width = if (isSelected) 2.2.dp else 1.2.dp,
            color = if (isSelected) accentColor else PaperBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.14f) else PaperBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else InkMedium,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else InkDark
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = InkMedium
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridSizeChip(
    gridSize: GridSize,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = InkDark),
                onClick = onSelect
            )
            .testTag("grid_size_${gridSize.name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PaperCard else PaperSurface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BlueInk else PaperBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gridSize.label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) BlueInk else InkDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${gridSize.totalBoxes} Boxes",
                fontSize = 11.sp,
                color = if (isSelected) BlueInk.copy(alpha = 0.8f) else InkMuted
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.paper.PaperBackgroundBox
import com.example.ui.paper.PenRenderer
import com.example.ui.theme.BlueInk
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperCard
import com.example.ui.theme.RedInk
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        // Smooth fade-in and gentle scale
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Hold display so user sees the logo and game brand
        delay(1400)

        // Smooth fade-out before transition
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    PaperBackgroundBox(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            )
            .testTag("splash_screen")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value)
            ) {
                // --- MAIN ELEMENT: Dots & Boxes Logo and Game Name Together ---
                DotsAndBoxesSplashLogo(
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Game Name Title
                Text(
                    text = "DotBox Duel",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = InkDark,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Game Subtitle Tagline
                Text(
                    text = "CLASSIC PEN & PAPER GAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.4.sp,
                    color = InkMedium
                )

                Spacer(modifier = Modifier.height(30.dp))

                // --- STUDIO SECTION: UNIQUEGAMES below as studio name ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UNIQUEGAMES",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 3.sp,
                        color = InkDark
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    StudioPencilUnderline(
                        modifier = Modifier
                            .width(115.dp)
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Authentic pen-and-paper Dots & Boxes logo:
 * Features a classic stationery card with a completed hatched box, corner dots,
 * and pen stroke lines in blue and red ink.
 */
@Composable
private fun DotsAndBoxesSplashLogo(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard)
            .border(BorderStroke(1.4.dp, PaperBorder), RoundedCornerShape(14.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 14.dp.toPx()
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
            val strokeW = 3.2.dp.toPx()
            drawLine(BlueInk, Offset(pad, pad), Offset(pad + cellSize, pad), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(RedInk, Offset(pad + cellSize, pad), Offset(pad + cellSize, pad + cellSize), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(BlueInk, Offset(pad + cellSize, pad + cellSize), Offset(pad, pad + cellSize), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(BlueInk, Offset(pad, pad + cellSize), Offset(pad, pad), strokeWidth = strokeW, cap = StrokeCap.Round)

            // Distinct corner dots
            corners.forEach { pt ->
                drawCircle(InkDark, radius = 4.2.dp.toPx(), center = pt)
            }
        }
    }
}

/**
 * Hand-drawn pencil stroke underline beneath the studio name.
 */
@Composable
private fun StudioPencilUnderline(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF455A64)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val midY = size.height / 2f

        val mainPath = Path().apply {
            moveTo(2f, midY + 0.3f)
            cubicTo(
                w * 0.28f, midY - 0.8f,
                w * 0.68f, midY + 0.9f,
                w - 2f, midY - 0.2f
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

        val sketchPath = Path().apply {
            moveTo(8f, midY - 0.4f)
            cubicTo(
                w * 0.36f, midY + 0.6f,
                w * 0.72f, midY - 0.5f,
                w - 8f, midY + 0.3f
            )
        }
        drawPath(
            path = sketchPath,
            color = color.copy(alpha = 0.4f),
            style = Stroke(
                width = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

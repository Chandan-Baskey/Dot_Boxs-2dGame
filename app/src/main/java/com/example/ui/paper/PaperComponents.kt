package com.example.ui.paper

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.Player
import com.example.ui.theme.AmberStamp
import com.example.ui.theme.BlueInk
import com.example.ui.theme.BlueInkDark
import com.example.ui.theme.BlueInkLight
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperCard
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.RedInk
import com.example.ui.theme.RedInkDark
import com.example.ui.theme.RedInkLight

/**
 * Stationery Paper Card with subtle notebook border and soft drop shadow.
 */
@Composable
fun PaperCardView(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PaperCard,
    borderColor: Color = PaperBorder,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x2032281E),
                spotColor = Color(0x3032281E)
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        content()
    }
}

/**
 * Hand-drawn style button with bold ink borders and paper texture.
 */
@Composable
fun PaperButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inkColor: Color = InkDark,
    icon: ImageVector? = null,
    testTag: String = "paper_button",
    isPrimary: Boolean = false
) {
    val bgColor = if (isPrimary) inkColor else PaperCard
    val textColor = if (isPrimary) Color.White else inkColor
    val borderColor = if (isPrimary) inkColor else inkColor.copy(alpha = 0.5f)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = inkColor),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.8.dp, borderColor),
        shadowElevation = if (isPrimary) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Score banner showing both players' scores and indicating whose turn it is.
 */
@Composable
fun PaperScoreHeader(
    mode: GameMode,
    currentPlayer: Player,
    p1Score: Int,
    p2Score: Int,
    isAiThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Two Player Score Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1 (Blue Ink)
            PlayerScoreBadge(
                name = Player.PLAYER_1.getDisplayName(mode),
                score = p1Score,
                inkColor = BlueInk,
                inkBg = BlueInkLight,
                isTurn = currentPlayer == Player.PLAYER_1,
                turnPulse = if (currentPlayer == Player.PLAYER_1) pulseAlpha else 1f,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Player 2 / AI (Red Ink)
            PlayerScoreBadge(
                name = Player.PLAYER_2.getDisplayName(mode),
                score = p2Score,
                inkColor = RedInk,
                inkBg = RedInkLight,
                isTurn = currentPlayer == Player.PLAYER_2,
                turnPulse = if (currentPlayer == Player.PLAYER_2) pulseAlpha else 1f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Turn Indicator Banner
        val turnInk = if (currentPlayer == Player.PLAYER_1) BlueInk else RedInk
        val turnText = if (isAiThinking) {
            "Computer is drawing a line..."
        } else if (mode == GameMode.VS_AI && currentPlayer == Player.PLAYER_1) {
            "Your Turn (Blue Ink)"
        } else if (mode == GameMode.VS_AI && currentPlayer == Player.PLAYER_2) {
            "Computer's Turn (Red Ink)"
        } else {
            "${currentPlayer.defaultName}'s Turn (${currentPlayer.inkName})"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (currentPlayer == Player.PLAYER_1) BlueInkLight else RedInkLight)
                .border(
                    BorderStroke(1.2.dp, turnInk.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = turnInk,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = turnText,
                color = turnInk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PlayerScoreBadge(
    name: String,
    score: Int,
    inkColor: Color,
    inkBg: Color,
    isTurn: Boolean,
    turnPulse: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = if (isTurn) 3.dp else 1.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isTurn) inkBg else PaperCard,
        border = BorderStroke(
            width = if (isTurn) 2.2.dp else 1.2.dp,
            color = if (isTurn) inkColor else PaperBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(inkColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Medium,
                    color = if (isTurn) inkColor else InkDark
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Big Score Number
            Text(
                text = "$score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = inkColor
            )

            Text(
                text = "BOXES",
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isTurn) inkColor.copy(alpha = 0.8f) else InkMuted
            )
        }
    }
}

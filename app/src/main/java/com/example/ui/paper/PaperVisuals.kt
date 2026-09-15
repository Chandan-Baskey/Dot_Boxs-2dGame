package com.example.ui.paper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.example.model.Edge
import com.example.model.Player
import com.example.ui.theme.BlueInk
import com.example.ui.theme.BlueInkDark
import com.example.ui.theme.InkDark
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PaperGridLine
import com.example.ui.theme.PaperMarginLine
import com.example.ui.theme.RedInk
import com.example.ui.theme.RedInkDark
import kotlin.math.abs
import kotlin.math.sin

/**
 * Authentic paper background with lined notebook paper rules,
 * red left margin, and subtle stationery texture.
 */
@Composable
fun PaperBackgroundBox(
    modifier: Modifier = Modifier,
    showMargin: Boolean = true,
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

            // 1. Horizontal notebook lines
            val lineSpacing = 28.dp.toPx()
            var y = lineSpacing
            while (y < height) {
                drawLine(
                    color = PaperGridLine,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += lineSpacing
            }

            // 2. Vertical margin line (classic red notebook margin)
            if (showMargin) {
                val marginX = (width * 0.08f).coerceIn(32.dp.toPx(), 56.dp.toPx())
                drawLine(
                    color = PaperMarginLine,
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, height),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
        }
        content()
    }
}

/**
 * Utility to render natural, organic pen-style strokes and dots on Canvas.
 */
object PenRenderer {

    /**
     * Deterministic pseudo-random seed based on edge attributes.
     */
    private fun edgeSeed(edge: Edge): Float {
        val h = (edge.row * 31 + edge.col * 17 + if (edge.isHorizontal) 13 else 7)
        return (abs(sin(h.toDouble())) * 1000f).toFloat()
    }

    /**
     * Draws an authentic hand-drawn ballpoint pen line between two dot coordinates.
     */
    fun drawPenLine(
        drawScope: DrawScope,
        start: Offset,
        end: Offset,
        player: Player,
        edge: Edge,
        isLatest: Boolean = false
    ) {
        with(drawScope) {
            val mainColor = if (player == Player.PLAYER_1) BlueInk else RedInk
            val darkColor = if (player == Player.PLAYER_1) BlueInkDark else RedInkDark

            val seed = edgeSeed(edge)
            val wobble1 = ((seed % 7) - 3.5f) * 0.6f
            val wobble2 = (((seed * 3) % 7) - 3.5f) * 0.6f

            val dx = end.x - start.x
            val dy = end.y - start.y

            // Normal vector perpendicular to the line
            val length = kotlin.math.hypot(dx, dy)
            if (length < 1f) return

            val nx = -dy / length
            val ny = dx / length

            // Control points with slight organic hand wobble
            val cp1 = Offset(
                start.x + dx * 0.33f + nx * wobble1,
                start.y + dy * 0.33f + ny * wobble1
            )
            val cp2 = Offset(
                start.x + dx * 0.66f + nx * wobble2,
                start.y + dy * 0.66f + ny * wobble2
            )

            val penPath = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
            }

            // Primary ballpoint ink stroke
            drawPath(
                path = penPath,
                color = mainColor,
                style = Stroke(
                    width = 5.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Micro ink-bleed core for authentic ballpoint pen texture
            drawPath(
                path = penPath,
                color = darkColor.copy(alpha = 0.45f),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // If it's the latest move, draw a subtle fresh ink glow
            if (isLatest) {
                drawPath(
                    path = penPath,
                    color = mainColor.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 9.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }

    /**
     * Draws an interactive preview line while the player drags from one dot to another.
     */
    fun drawPreviewPenLine(
        drawScope: DrawScope,
        start: Offset,
        end: Offset,
        player: Player,
        isValidSnap: Boolean,
        isInvalidTarget: Boolean = false
    ) {
        with(drawScope) {
            val baseColor = if (player == Player.PLAYER_1) BlueInk else RedInk
            val strokeColor = when {
                isInvalidTarget -> Color(0xFFEF4444).copy(alpha = 0.6f) // Red warning if trying to draw invalid
                isValidSnap -> baseColor
                else -> baseColor.copy(alpha = 0.75f)
            }

            val dx = end.x - start.x
            val dy = end.y - start.y
            val length = kotlin.math.hypot(dx, dy)
            if (length < 2f) return

            // Subtle organic curve while dragging
            val nx = -dy / length
            val ny = dx / length
            val wobble = (kotlin.math.sin(length.toDouble() * 0.05).toFloat() * 1.5f).coerceIn(-2f, 2f)

            val cp1 = Offset(start.x + dx * 0.35f + nx * wobble, start.y + dy * 0.35f + ny * wobble)
            val cp2 = Offset(start.x + dx * 0.65f - nx * wobble, start.y + dy * 0.65f - ny * wobble)

            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
            }

            // Outer soft pen trail
            drawPath(
                path = path,
                color = strokeColor.copy(alpha = if (isValidSnap) 0.35f else 0.2f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Main stroke
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = if (isValidSnap) 5.dp.toPx() else 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Ballpoint pen nib indicator at the current drag tip
            drawCircle(
                color = strokeColor,
                radius = if (isValidSnap) 5.dp.toPx() else 4.dp.toPx(),
                center = end
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 1.8.dp.toPx(),
                center = Offset(end.x - 1f, end.y - 1f)
            )
        }
    }

    /**
     * Draws an ink highlight ring around the starting or hovered dot.
     */
    fun drawDotHighlight(
        drawScope: DrawScope,
        center: Offset,
        player: Player,
        isSnapped: Boolean = false
    ) {
        with(drawScope) {
            val color = if (player == Player.PLAYER_1) BlueInk else RedInk
            val radius = if (isSnapped) 13.dp.toPx() else 11.dp.toPx()

            // Subtle pulsing ink aura
            drawCircle(
                color = color.copy(alpha = if (isSnapped) 0.25f else 0.15f),
                radius = radius + 3.dp.toPx(),
                center = center
            )
            // Hand-drawn ring
            drawCircle(
                color = color.copy(alpha = if (isSnapped) 0.9f else 0.7f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    /**
     * Draws an organic hand-drawn ink dot.
     */
    fun drawInkDot(
        drawScope: DrawScope,
        center: Offset,
        indexRow: Int,
        indexCol: Int
    ) {
        with(drawScope) {
            val hash = (indexRow * 47 + indexCol * 19)
            val wobbleX = ((hash % 5) - 2f) * 0.3f
            val wobbleY = (((hash * 7) % 5) - 2f) * 0.3f
            val baseRadius = 6.2.dp.toPx()

            // Soft ink edge
            drawCircle(
                color = InkDark.copy(alpha = 0.25f),
                radius = baseRadius + 1.2.dp.toPx(),
                center = Offset(center.x + wobbleX, center.y + wobbleY)
            )

            // Main deep graphite ink dot
            drawCircle(
                color = InkDark,
                radius = baseRadius,
                center = Offset(center.x + wobbleX, center.y + wobbleY)
            )

            // Organic ink highlight center
            drawCircle(
                color = Color(0xFF333E4D),
                radius = baseRadius * 0.5f,
                center = Offset(center.x + wobbleX - 0.8f, center.y + wobbleY - 0.8f)
            )
        }
    }

    /**
     * Draws light hand-drawn diagonal pen hatching shading inside a captured box.
     */
    fun drawBoxHatching(
        drawScope: DrawScope,
        rect: Rect,
        player: Player,
        boxRow: Int,
        boxCol: Int
    ) {
        with(drawScope) {
            val inkColor = if (player == Player.PLAYER_1) BlueInk else RedInk

            // 1. Light translucent ink wash background
            drawRect(
                color = inkColor.copy(alpha = 0.12f),
                topLeft = rect.topLeft,
                size = rect.size
            )

            // 2. Hand-drawn diagonal hatching lines clipped strictly inside the box
            clipRect(
                left = rect.left + 2f,
                top = rect.top + 2f,
                right = rect.right - 2f,
                bottom = rect.bottom - 2f
            ) {
                val step = 11.dp.toPx()
                val totalWidth = rect.width + rect.height
                val startX = rect.left - rect.height
                var currentX = startX

                val seed = (boxRow * 23 + boxCol * 13)

                while (currentX < rect.right + step) {
                    val wobble = (((seed + currentX.toInt()) % 5) - 2f) * 0.4f
                    val p1 = Offset(currentX, rect.top)
                    val p2 = Offset(currentX + rect.height + wobble, rect.bottom)

                    drawLine(
                        color = inkColor.copy(alpha = 0.38f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    currentX += step
                }

                // 3. Subtle hand-drawn border vignette
                drawRect(
                    color = inkColor.copy(alpha = 0.18f),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

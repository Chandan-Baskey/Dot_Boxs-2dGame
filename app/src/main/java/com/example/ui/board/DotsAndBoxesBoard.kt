package com.example.ui.board

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.logic.DotsAndBoxesEngine
import com.example.model.BoxCoord
import com.example.model.Edge
import com.example.model.GameState
import com.example.model.Player
import com.example.ui.paper.PenRenderer
import com.example.ui.theme.BlueInkDark
import com.example.ui.theme.PencilGray
import com.example.ui.theme.RedInkDark
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

data class DotCoord(val row: Int, val col: Int)

@Composable
fun DotsAndBoxesBoard(
    state: GameState,
    onEdgeSelected: (Edge) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val density = LocalDensity.current
    val allEdges = remember(state.gridSize) {
        DotsAndBoxesEngine.getAllEdges(state.gridSize)
    }

    // Active drag gesture state
    var activeStartDot by remember { mutableStateOf<DotCoord?>(null) }
    var currentDragOffset by remember { mutableStateOf<Offset?>(null) }
    var hoveredTargetDot by remember { mutableStateOf<DotCoord?>(null) }
    var isCandidateValid by remember { mutableStateOf(false) }
    var isDuplicateEdge by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.testTag("game_board")
    ) {
        val boardWidth = constraints.maxWidth.toFloat()
        val boardHeight = constraints.maxHeight.toFloat()
        val boardSize = min(boardWidth, boardHeight)
        val offsetX = (boardWidth - boardSize) / 2f
        val offsetY = (boardHeight - boardSize) / 2f

        val n = state.gridSize.dotsCount
        val padding = boardSize * 0.10f
        val gridWidth = boardSize - (padding * 2f)
        val cellSize = gridWidth / (n - 1).coerceAtLeast(1)

        fun getDotOffset(r: Int, c: Int): Offset {
            return Offset(
                x = offsetX + padding + c * cellSize,
                y = offsetY + padding + r * cellSize
            )
        }

        fun getEdgeEndpoints(edge: Edge): Pair<Offset, Offset> {
            val start = getDotOffset(edge.row, edge.col)
            val end = if (edge.isHorizontal) {
                getDotOffset(edge.row, edge.col + 1)
            } else {
                getDotOffset(edge.row + 1, edge.col)
            }
            return Pair(start, end)
        }

        // Helper to calculate distance from a point to a line segment
        fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
            val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
            if (l2 == 0f) return hypot(p.x - a.x, p.y - a.y)
            val t = (((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2).coerceIn(0f, 1f)
            val proj = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
            return hypot(p.x - proj.x, p.y - proj.y)
        }

        // Find nearest uncompleted edge to a touch point
        fun findNearestEdge(pos: Offset): Edge? {
            var closestEdge: Edge? = null
            var minDistance = Float.MAX_VALUE
            for (edge in allEdges) {
                if (state.edges.containsKey(edge)) continue
                val (start, end) = getEdgeEndpoints(edge)
                val dist = distancePointToSegment(pos, start, end)
                if (dist < minDistance && dist <= cellSize * 0.44f) {
                    minDistance = dist
                    closestEdge = edge
                }
            }
            return closestEdge
        }

        // Find dot near an offset within generous hit radius
        fun findNearestDot(pos: Offset): DotCoord? {
            val approxCol = ((pos.x - offsetX - padding) / cellSize).roundToInt().coerceIn(0, n - 1)
            val approxRow = ((pos.y - offsetY - padding) / cellSize).roundToInt().coerceIn(0, n - 1)
            val dotPos = getDotOffset(approxRow, approxCol)
            val dist = hypot(pos.x - dotPos.x, pos.y - dotPos.y)
            return if (dist <= cellSize * 0.48f) DotCoord(approxRow, approxCol) else null
        }

        fun updateHoverState(start: DotCoord, currentPos: Offset) {
            val nearest = findNearestDot(currentPos)
            if (nearest != null && nearest != start) {
                val dRow = abs(nearest.row - start.row)
                val dCol = abs(nearest.col - start.col)
                val isAdjacent = (dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1)

                if (isAdjacent) {
                    val edge = if (dRow == 0) {
                        Edge(row = start.row, col = min(start.col, nearest.col), isHorizontal = true)
                    } else {
                        Edge(row = min(start.row, nearest.row), col = start.col, isHorizontal = false)
                    }
                    val alreadyDrawn = state.edges.containsKey(edge)
                    hoveredTargetDot = nearest
                    isCandidateValid = !alreadyDrawn
                    isDuplicateEdge = alreadyDrawn
                    return
                }
            }

            // Also check if dragging in an adjacent direction (generous threshold)
            val startCenter = getDotOffset(start.row, start.col)
            val dx = currentPos.x - startCenter.x
            val dy = currentPos.y - startCenter.y
            val absX = abs(dx)
            val absY = abs(dy)

            if (absX > cellSize * 0.35f && absX > absY * 1.15f) {
                val nextCol = start.col + if (dx > 0) 1 else -1
                if (nextCol in 0 until n) {
                    val candidate = DotCoord(start.row, nextCol)
                    val edge = Edge(row = start.row, col = min(start.col, nextCol), isHorizontal = true)
                    val alreadyDrawn = state.edges.containsKey(edge)
                    hoveredTargetDot = candidate
                    isCandidateValid = !alreadyDrawn
                    isDuplicateEdge = alreadyDrawn
                    return
                }
            } else if (absY > cellSize * 0.35f && absY > absX * 1.15f) {
                val nextRow = start.row + if (dy > 0) 1 else -1
                if (nextRow in 0 until n) {
                    val candidate = DotCoord(nextRow, start.col)
                    val edge = Edge(row = min(start.row, nextRow), col = start.col, isHorizontal = false)
                    val alreadyDrawn = state.edges.containsKey(edge)
                    hoveredTargetDot = candidate
                    isCandidateValid = !alreadyDrawn
                    isDuplicateEdge = alreadyDrawn
                    return
                }
            }

            hoveredTargetDot = null
            isCandidateValid = false
            isDuplicateEdge = false
        }

        fun finalizeDrag(start: DotCoord, endPos: Offset) {
            if (hoveredTargetDot == null) {
                updateHoverState(start, endPos)
            }
            val target = hoveredTargetDot
            if (target != null && isCandidateValid) {
                val dRow = abs(target.row - start.row)
                val dCol = abs(target.col - start.col)
                if ((dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1)) {
                    val edge = if (dRow == 0) {
                        Edge(row = start.row, col = min(start.col, target.col), isHorizontal = true)
                    } else {
                        Edge(row = min(start.row, target.row), col = start.col, isHorizontal = false)
                    }
                    if (!state.edges.containsKey(edge)) {
                        onEdgeSelected(edge)
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.edges, state.isGameOver, enabled) {
                    if (!enabled || state.isGameOver) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startDot = findNearestDot(down.position)

                        if (startDot != null) {
                            activeStartDot = startDot
                            currentDragOffset = down.position
                            hoveredTargetDot = null
                            isCandidateValid = false
                            isDuplicateEdge = false
                            down.consume()

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (change.pressed) {
                                        currentDragOffset = change.position
                                        updateHoverState(startDot, change.position)
                                        change.consume()
                                    } else {
                                        // Pointer released
                                        change.consume()
                                        finalizeDrag(startDot, change.position)
                                        break
                                    }
                                }
                            } finally {
                                activeStartDot = null
                                currentDragOffset = null
                                hoveredTargetDot = null
                                isCandidateValid = false
                                isDuplicateEdge = false
                            }
                        } else {
                            // Touch was not directly on a dot; check if player tapped on an edge
                            val tappedEdge = findNearestEdge(down.position)
                            if (tappedEdge != null && !state.edges.containsKey(tappedEdge)) {
                                var isDrag = false
                                val startPos = down.position
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (change.pressed) {
                                            val moveDist = hypot(change.position.x - startPos.x, change.position.y - startPos.y)
                                            if (moveDist > 20.dp.toPx()) {
                                                isDrag = true
                                            }
                                        } else {
                                            if (!isDrag) {
                                                change.consume()
                                                onEdgeSelected(tappedEdge)
                                            }
                                            break
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
        ) {
            // 1. Draw subtle pencil guideline grid (like graph paper)
            for (edge in allEdges) {
                if (!state.edges.containsKey(edge)) {
                    val (start, end) = getEdgeEndpoints(edge)
                    drawLine(
                        color = PencilGray.copy(alpha = 0.22f),
                        start = start,
                        end = end,
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
            }

            // 2. Draw Captured Boxes (light hand-drawn shading + ink monogram initial)
            val boxGrid = state.gridSize.boxGridSize
            val paintP1 = Paint().apply {
                color = BlueInkDark.toArgb()
                textSize = cellSize * 0.38f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val paintP2 = Paint().apply {
                color = RedInkDark.toArgb()
                textSize = cellSize * 0.38f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            for (r in 0 until boxGrid) {
                for (c in 0 until boxGrid) {
                    val coord = BoxCoord(r, c)
                    val owner = state.boxes[coord]
                    if (owner != null) {
                        val topLeft = getDotOffset(r, c)
                        val boxRect = Rect(
                            topLeft = topLeft,
                            bottomRight = Offset(topLeft.x + cellSize, topLeft.y + cellSize)
                        )

                        // Light hand-drawn hatching
                        PenRenderer.drawBoxHatching(
                            drawScope = this,
                            rect = boxRect,
                            player = owner,
                            boxRow = r,
                            boxCol = c
                        )

                        // Centered player initial / monogram
                        val text = if (owner == Player.PLAYER_1) "1" else "2"
                        val paint = if (owner == Player.PLAYER_1) paintP1 else paintP2
                        val centerX = boxRect.center.x
                        val centerY = boxRect.center.y - ((paint.descent() + paint.ascent()) / 2)

                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            centerX,
                            centerY,
                            paint
                        )
                    }
                }
            }

            // 3. Draw Drawn Edges (Natural Ballpoint Pen Strokes)
            for ((edge, player) in state.edges) {
                val (start, end) = getEdgeEndpoints(edge)
                val isLatest = edge == state.lastDrawnEdge
                PenRenderer.drawPenLine(
                    drawScope = this,
                    start = start,
                    end = end,
                    player = player,
                    edge = edge,
                    isLatest = isLatest
                )
            }

            // 4. Draw Interactive Drag Preview Line (Real-time Pen Drawing)
            val currentStart = activeStartDot
            val currentPos = currentDragOffset
            if (currentStart != null && currentPos != null) {
                val startCenter = getDotOffset(currentStart.row, currentStart.col)
                val targetDot = hoveredTargetDot

                // Start dot highlight ring
                PenRenderer.drawDotHighlight(
                    drawScope = this,
                    center = startCenter,
                    player = state.currentPlayer,
                    isSnapped = false
                )

                if (targetDot != null) {
                    val targetCenter = getDotOffset(targetDot.row, targetDot.col)
                    if (isCandidateValid) {
                        // Highlight target dot snap
                        PenRenderer.drawDotHighlight(
                            drawScope = this,
                            center = targetCenter,
                            player = state.currentPlayer,
                            isSnapped = true
                        )
                        // Snap preview line directly to target dot
                        PenRenderer.drawPreviewPenLine(
                            drawScope = this,
                            start = startCenter,
                            end = targetCenter,
                            player = state.currentPlayer,
                            isValidSnap = true
                        )
                    } else if (isDuplicateEdge) {
                        // Already drawn edge warning
                        PenRenderer.drawPreviewPenLine(
                            drawScope = this,
                            start = startCenter,
                            end = targetCenter,
                            player = state.currentPlayer,
                            isValidSnap = false,
                            isInvalidTarget = true
                        )
                    }
                } else {
                    // Smooth pen stroke following cursor
                    PenRenderer.drawPreviewPenLine(
                        drawScope = this,
                        start = startCenter,
                        end = currentPos,
                        player = state.currentPlayer,
                        isValidSnap = false
                    )
                }
            }

            // 5. Draw Hand-Drawn Ink Dots on top of lines
            for (r in 0 until n) {
                for (c in 0 until n) {
                    val dotCenter = getDotOffset(r, c)
                    PenRenderer.drawInkDot(
                        drawScope = this,
                        center = dotCenter,
                        indexRow = r,
                        indexCol = c
                    )
                }
            }
        }
    }
}


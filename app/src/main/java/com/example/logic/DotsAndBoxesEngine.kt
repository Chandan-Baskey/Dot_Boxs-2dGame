package com.example.logic

import com.example.model.BoxCoord
import com.example.model.Edge
import com.example.model.GameState
import com.example.model.GridSize
import com.example.model.Player

object DotsAndBoxesEngine {

    /**
     * Returns the 4 boundary edges of a box.
     */
    fun getBoxEdges(coord: BoxCoord): List<Edge> {
        val r = coord.row
        val c = coord.col
        return listOf(
            Edge(row = r, col = c, isHorizontal = true),       // Top
            Edge(row = r + 1, col = c, isHorizontal = true),   // Bottom
            Edge(row = r, col = c, isHorizontal = false),      // Left
            Edge(row = r, col = c + 1, isHorizontal = false)   // Right
        )
    }

    /**
     * Returns the boxes adjacent to a given edge (up to 2).
     */
    fun getAdjacentBoxes(edge: Edge, gridSize: GridSize): List<BoxCoord> {
        val boxes = mutableListOf<BoxCoord>()
        val maxBox = gridSize.boxGridSize

        if (edge.isHorizontal) {
            // Box above the edge
            if (edge.row > 0 && edge.col in 0 until maxBox) {
                boxes.add(BoxCoord(edge.row - 1, edge.col))
            }
            // Box below the edge
            if (edge.row < maxBox && edge.col in 0 until maxBox) {
                boxes.add(BoxCoord(edge.row, edge.col))
            }
        } else {
            // Box to the left of the edge
            if (edge.col > 0 && edge.row in 0 until maxBox) {
                boxes.add(BoxCoord(edge.row, edge.col - 1))
            }
            // Box to the right of the edge
            if (edge.col < maxBox && edge.row in 0 until maxBox) {
                boxes.add(BoxCoord(edge.row, edge.col))
            }
        }
        return boxes
    }

    /**
     * All valid edges for a given grid size.
     */
    fun getAllEdges(gridSize: GridSize): List<Edge> {
        val edges = mutableListOf<Edge>()
        val n = gridSize.dotsCount

        // Horizontal edges: n rows, (n - 1) columns each
        for (r in 0 until n) {
            for (c in 0 until (n - 1)) {
                edges.add(Edge(row = r, col = c, isHorizontal = true))
            }
        }
        // Vertical edges: (n - 1) rows, n columns each
        for (r in 0 until (n - 1)) {
            for (c in 0 until n) {
                edges.add(Edge(row = r, col = c, isHorizontal = false))
            }
        }
        return edges
    }

    /**
     * All box coordinates for a grid size.
     */
    fun getAllBoxes(gridSize: GridSize): List<BoxCoord> {
        val boxes = mutableListOf<BoxCoord>()
        val max = gridSize.boxGridSize
        for (r in 0 until max) {
            for (c in 0 until max) {
                boxes.add(BoxCoord(r, c))
            }
        }
        return boxes
    }

    /**
     * Count how many edges are filled for a given box.
     */
    fun countBoxFilledEdges(coord: BoxCoord, drawnEdges: Set<Edge>): Int {
        val boxEdges = getBoxEdges(coord)
        return boxEdges.count { it in drawnEdges }
    }

    /**
     * Checks if a box is completely surrounded by drawn edges.
     */
    fun isBoxComplete(coord: BoxCoord, drawnEdges: Set<Edge>): Boolean {
        val boxEdges = getBoxEdges(coord)
        return boxEdges.all { it in drawnEdges }
    }

    /**
     * Executes a move and returns the updated GameState.
     */
    fun makeMove(state: GameState, edge: Edge): GameState {
        // If already drawn or game is over, return unchanged
        if (state.isGameOver || state.edges.containsKey(edge)) {
            return state
        }

        val player = state.currentPlayer
        val newEdges = state.edges + (edge to player)
        val drawnEdgeSet = newEdges.keys

        // Check which adjacent boxes are newly completed
        val adjacentBoxes = getAdjacentBoxes(edge, state.gridSize)
        val newlyCompletedBoxes = adjacentBoxes.filter { boxCoord ->
            !state.boxes.containsKey(boxCoord) && isBoxComplete(boxCoord, drawnEdgeSet)
        }

        val newBoxes = if (newlyCompletedBoxes.isNotEmpty()) {
            state.boxes + newlyCompletedBoxes.associateWith { player }
        } else {
            state.boxes
        }

        val isGameOver = newBoxes.size == state.gridSize.totalBoxes

        val nextPlayer: Player
        if (isGameOver) {
            nextPlayer = player
        } else if (newlyCompletedBoxes.isNotEmpty()) {
            // Player gets another turn if they completed at least one box!
            nextPlayer = player
        } else {
            // No box completed: turn passes to opponent
            nextPlayer = player.opponent
        }

        val p1Score = newBoxes.values.count { it == Player.PLAYER_1 }
        val p2Score = newBoxes.values.count { it == Player.PLAYER_2 }

        val winner = if (isGameOver) {
            when {
                p1Score > p2Score -> Player.PLAYER_1
                p2Score > p1Score -> Player.PLAYER_2
                else -> null
            }
        } else null

        val isDraw = isGameOver && (p1Score == p2Score)

        return state.copy(
            edges = newEdges,
            boxes = newBoxes,
            currentPlayer = nextPlayer,
            isGameOver = isGameOver,
            winner = winner,
            isDraw = isDraw,
            lastDrawnEdge = edge,
            lastCompletedBoxes = newlyCompletedBoxes
        )
    }
}

package com.example.model

enum class GameMode(val title: String, val subtitle: String) {
    LOCAL_2P("2 Player Local", "Take turns on this device"),
    VS_AI("Player vs Computer", "Smart pen-and-paper opponent")
}

enum class Player(
    val defaultName: String,
    val initial: String,
    val inkName: String
) {
    PLAYER_1("Player 1", "P1", "Blue Ink"),
    PLAYER_2("Player 2", "P2", "Red Ink");

    fun getDisplayName(mode: GameMode): String = when {
        this == PLAYER_1 && mode == GameMode.VS_AI -> "You"
        this == PLAYER_2 && mode == GameMode.VS_AI -> "Computer"
        else -> defaultName
    }

    val opponent: Player
        get() = if (this == PLAYER_1) PLAYER_2 else PLAYER_1
}

enum class GridSize(
    val dotsCount: Int,
    val label: String,
    val boxDescriptor: String
) {
    SMALL(4, "4 × 4", "3 × 3 Boxes (9 Boxes)"),
    CLASSIC(5, "5 × 5", "4 × 4 Boxes (16 Boxes)"),
    LARGE(6, "6 × 6", "5 × 5 Boxes (25 Boxes)");

    val boxGridSize: Int get() = dotsCount - 1
    val totalBoxes: Int get() = boxGridSize * boxGridSize
    val totalEdges: Int get() = 2 * dotsCount * (dotsCount - 1)
}

data class Edge(
    val row: Int,
    val col: Int,
    val isHorizontal: Boolean
)

data class BoxCoord(
    val row: Int,
    val col: Int
)

data class GameState(
    val mode: GameMode = GameMode.VS_AI,
    val gridSize: GridSize = GridSize.CLASSIC,
    val edges: Map<Edge, Player> = emptyMap(),
    val boxes: Map<BoxCoord, Player> = emptyMap(),
    val currentPlayer: Player = Player.PLAYER_1,
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val lastDrawnEdge: Edge? = null,
    val lastCompletedBoxes: List<BoxCoord> = emptyList(),
    val isAiThinking: Boolean = false
) {
    val p1Score: Int get() = boxes.values.count { it == Player.PLAYER_1 }
    val p2Score: Int get() = boxes.values.count { it == Player.PLAYER_2 }
    val remainingEdges: Int get() = gridSize.totalEdges - edges.size
    val remainingBoxes: Int get() = gridSize.totalBoxes - boxes.size
}

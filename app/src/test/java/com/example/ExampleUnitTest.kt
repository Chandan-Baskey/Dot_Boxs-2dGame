package com.example

import com.example.logic.DotsAndBoxesAi
import com.example.logic.DotsAndBoxesEngine
import com.example.model.BoxCoord
import com.example.model.Edge
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.GridSize
import com.example.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testTurnSwitchesWhenNoBoxCompleted() {
    val initial = GameState(mode = GameMode.LOCAL_2P, gridSize = GridSize.SMALL)
    val edge1 = Edge(row = 0, col = 0, isHorizontal = true)
    val state1 = DotsAndBoxesEngine.makeMove(initial, edge1)

    assertEquals(Player.PLAYER_2, state1.currentPlayer)
    assertEquals(1, state1.edges.size)
    assertEquals(0, state1.boxes.size)
  }

  @Test
  fun testBoxCompletionGrantsExtraTurnAndScore() {
    var state = GameState(mode = GameMode.LOCAL_2P, gridSize = GridSize.SMALL)
    // Box (0, 0) edges:
    // Top: (0, 0, H)
    // Bottom: (1, 0, H)
    // Left: (0, 0, V)
    // Right: (0, 1, V)
    state = DotsAndBoxesEngine.makeMove(state, Edge(0, 0, true)) // P1 moves
    state = DotsAndBoxesEngine.makeMove(state, Edge(1, 0, true)) // P2 moves
    state = DotsAndBoxesEngine.makeMove(state, Edge(0, 0, false)) // P1 moves

    // Now box (0, 0) has 3 edges drawn.
    // Turn is currently with P2.
    assertEquals(Player.PLAYER_2, state.currentPlayer)

    // P2 closes the box!
    state = DotsAndBoxesEngine.makeMove(state, Edge(0, 1, false))

    // P2 should own the box
    assertEquals(1, state.p2Score)
    assertEquals(0, state.p1Score)
    assertEquals(Player.PLAYER_2, state.boxes[BoxCoord(0, 0)])

    // P2 should get ANOTHER turn!
    assertEquals(Player.PLAYER_2, state.currentPlayer)
  }

  @Test
  fun testAiCapturesAvailableBox() {
    var state = GameState(mode = GameMode.VS_AI, gridSize = GridSize.SMALL)
    // Draw 3 sides of box (0,0)
    state = state.copy(
      edges = mapOf(
        Edge(0, 0, true) to Player.PLAYER_1,
        Edge(1, 0, true) to Player.PLAYER_1,
        Edge(0, 0, false) to Player.PLAYER_1
      ),
      currentPlayer = Player.PLAYER_2
    )

    // AI should select the closing edge (0, 1, false)
    val aiMove = DotsAndBoxesAi.findBestMove(state)
    assertNotNull(aiMove)
    assertEquals(Edge(0, 1, false), aiMove)
  }

  @Test
  fun testAiAvoidsGivingEasyBox() {
    var state = GameState(mode = GameMode.VS_AI, gridSize = GridSize.SMALL)
    // Give box (0, 0) two sides: top and left
    state = state.copy(
      edges = mapOf(
        Edge(0, 0, true) to Player.PLAYER_1,
        Edge(0, 0, false) to Player.PLAYER_1
      ),
      currentPlayer = Player.PLAYER_2
    )

    // Playing Edge(1, 0, true) or Edge(0, 1, false) would make box (0,0) 3-sided (dangerous)!
    // AI should choose a safe move on an untainted box/edge instead.
    val aiMove = DotsAndBoxesAi.findBestMove(state)
    assertNotNull(aiMove)
    val dangerousEdges = setOf(Edge(1, 0, true), Edge(0, 1, false))
    assertFalse("AI should not play an edge that creates a 3-sided box when safe moves exist", aiMove in dangerousEdges)
  }
}


package com.example.logic

import com.example.model.Edge
import com.example.model.GameState
import kotlin.random.Random

object DotsAndBoxesAi {

    /**
     * Calculates the smart AI move for the current GameState.
     */
    fun findBestMove(state: GameState): Edge? {
        if (state.isGameOver) return null

        val allEdges = DotsAndBoxesEngine.getAllEdges(state.gridSize)
        val availableEdges = allEdges.filter { !state.edges.containsKey(it) }
        if (availableEdges.isEmpty()) return null

        val drawnEdges = state.edges.keys

        // 1. CAPTURE PRIORITY: Can we complete any box right now?
        // An edge that completes 2 boxes is the best, followed by 1 box.
        var bestCapturingEdge: Edge? = null
        var maxCaptures = 0

        for (edge in availableEdges) {
            val adjBoxes = DotsAndBoxesEngine.getAdjacentBoxes(edge, state.gridSize)
            val uncompletedAdj = adjBoxes.filter { !state.boxes.containsKey(it) }
            val capturedCount = uncompletedAdj.count { box ->
                DotsAndBoxesEngine.countBoxFilledEdges(box, drawnEdges) == 3
            }
            if (capturedCount > maxCaptures) {
                maxCaptures = capturedCount
                bestCapturingEdge = edge
            }
        }

        if (bestCapturingEdge != null && maxCaptures > 0) {
            return bestCapturingEdge
        }

        // 2. SAFE MOVES PRIORITY: Edges that do not give away any box.
        // A move gives away a box if it makes ANY adjacent uncompleted box have 3 edges.
        // (i.e. adjacent uncompleted box already has 2 edges).
        val safeEdges = availableEdges.filter { edge ->
            val adjBoxes = DotsAndBoxesEngine.getAdjacentBoxes(edge, state.gridSize)
            val uncompletedAdj = adjBoxes.filter { !state.boxes.containsKey(it) }
            // Must not give 3 edges to any box
            uncompletedAdj.none { box ->
                DotsAndBoxesEngine.countBoxFilledEdges(box, drawnEdges) == 2
            }
        }

        if (safeEdges.isNotEmpty()) {
            // Rank safe moves:
            // Score based on opening boxes with 0 edges (most neutral) vs 1 edge
            val scoredSafe = safeEdges.map { edge ->
                val adjBoxes = DotsAndBoxesEngine.getAdjacentBoxes(edge, state.gridSize)
                val uncompletedAdj = adjBoxes.filter { !state.boxes.containsKey(it) }
                // Sum of current edges on adjacent boxes (lower is safer/cleaner)
                val edgeSum = uncompletedAdj.sumOf { box ->
                    DotsAndBoxesEngine.countBoxFilledEdges(box, drawnEdges)
                }
                edge to edgeSum
            }

            val minScore = scoredSafe.minOf { it.second }
            val bestSafeCandidates = scoredSafe.filter { it.second == minScore }.map { it.first }
            return bestSafeCandidates.random(Random)
        }

        // 3. SACRIFICE / MINIMIZE DAMAGE: All available moves create a 3-sided box.
        // Choose the move that gives away the smallest chain of boxes.
        var bestSacrificeEdge: Edge = availableEdges.first()
        var minChainLoss = Int.MAX_VALUE

        for (edge in availableEdges) {
            val chainLoss = estimateChainLoss(edge, state)
            if (chainLoss < minChainLoss) {
                minChainLoss = chainLoss
                bestSacrificeEdge = edge
            }
        }

        return bestSacrificeEdge
    }

    /**
     * Estimates how many consecutive boxes the opponent can take if this edge is played.
     */
    private fun estimateChainLoss(edge: Edge, state: GameState): Int {
        val simulatedDrawn = state.edges.keys.toMutableSet()
        simulatedDrawn.add(edge)

        val uncompletedBoxes = DotsAndBoxesEngine.getAllBoxes(state.gridSize)
            .filter { !state.boxes.containsKey(it) }
            .toMutableSet()

        var lossCount = 0
        var foundChain = true

        while (foundChain) {
            foundChain = false
            val capturable = uncompletedBoxes.filter { box ->
                DotsAndBoxesEngine.countBoxFilledEdges(box, simulatedDrawn) >= 3
            }

            if (capturable.isNotEmpty()) {
                val boxToCapture = capturable.first()
                uncompletedBoxes.remove(boxToCapture)
                lossCount++

                // Fill all remaining edges of this box
                val boxEdges = DotsAndBoxesEngine.getBoxEdges(boxToCapture)
                for (be in boxEdges) {
                    simulatedDrawn.add(be)
                }
                foundChain = true
            }
        }

        return lossCount
    }
}

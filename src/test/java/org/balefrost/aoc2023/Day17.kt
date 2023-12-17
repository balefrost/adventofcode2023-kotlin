package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.util.PriorityQueue

class Day17 {
    val sampleText = """
111111111111
999999999991
999999999991
999999999991
999999999991
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day17.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val adjacentDirs = listOf(
        Dir2D(0, 1),
        Dir2D(0, -1),
        Dir2D(1, 0),
        Dir2D(-1, 0)
    )

    data class ResultKey(val pos: Point2D, val dir: Dir2D, val distanceTraveled: Int)

    fun getAdjacent(key: ResultKey, bounds: Region2D, minStraight: Int, maxStraight: Int): List<ResultKey> {
        val adjacentKeys = mutableListOf<ResultKey>()
        for (dir in adjacentDirs) {
            val pt = key.pos + dir
            if (pt !in bounds) {
                // can't move out of bounds
                continue
            }
            if (key.dir != dir && key.distanceTraveled < minStraight) {
                // can't turn too early
                continue
            }
            if (key.dir == dir && key.distanceTraveled >= maxStraight) {
                // can't travel too far in a straight line
                continue
            }
            if (key.dir == -dir) {
                // can't reverse
                continue
            }

            val distanceTraveled = if (dir == key.dir) key.distanceTraveled + 1 else 1
            adjacentKeys += ResultKey(pt, dir, distanceTraveled)
        }
        return adjacentKeys
    }

    @Test
    fun part01() {
        val w = lines.first().length
        val h = lines.size
        val bounds = Region2D(0, 0, w, h)
        fun getCost(pos: Point2D) = lines[pos.y][pos.x].digitToInt()

        val initialKey = ResultKey(Point2D(0, 0), Dir2D(1, 0), 0)
        val targetPoint = Point2D(w - 1, h - 1)
        val (cost, path) = checkNotNull(
            dijkstra(
                setOf(initialKey to 0),
                { key -> getAdjacent(key, bounds, 0, 3) },
                { score, k -> score + getCost(k.pos) },
                { k -> k.pos == targetPoint })
        )

        println(cost)
    }

    @Test
    fun part02() {
        val w = lines.first().length
        val h = lines.size
        val bounds = Region2D(0, 0, w, h)
        fun getCost(pos: Point2D) = lines[pos.y][pos.x].digitToInt()

        val initialStates = listOf(
            ResultKey(Point2D(0, 0), Dir2D(1, 0), 0) to 0,
            ResultKey(Point2D(0, 0), Dir2D(0, 1), 0) to 0
        )
        val targetPoint = Point2D(w - 1, h - 1)
        val (cost, path) = checkNotNull(
            dijkstra(
                initialStates,
                { key -> getAdjacent(key, bounds, 4, 10) },
                { score, k -> score + getCost(k.pos) },
                { k -> k.pos == targetPoint && k.distanceTraveled >= 4 })
        )

        println(cost)
    }

}
package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day21 {
    val sampleText = """
        ...........
        .....###.#.
        .###.##..#.
        ..#.#...#..
        ....#.#....
        .##..S####.
        .##..#...#.
        .......##..
        .##.#.####.
        .##..##.##.
        ...........
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day21.txt")!!.readText()

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val startingPos = lines.withIndex().mapNotNull { (y, line) ->
        val x = line.indexOf('S')
        when {
            x < 0 -> null
            else -> Point2D(x, y)
        }
    }.first()

    val board = lines.map { line ->
        line.replace('S', '.')
    }

    val bounds = Region2D(0, 0, lines.first().length, lines.size)

    fun get(pt: Point2D): Char {
        return when (pt) {
            in bounds -> board[pt.y][pt.x]
            else -> '#'
        }
    }

    fun adjacentPositions(pt: Point2D): List<Point2D> {
        return listOf(
            pt + Dir2D(0, 1),
            pt + Dir2D(0, -1),
            pt + Dir2D(1, 0),
            pt + Dir2D(-1, 0)
        )
    }

    @Test
    fun part01() {
        val steps = generateSequence(setOf(startingPos)) { fringe ->
            fringe.flatMapTo(mutableSetOf()) { pt -> adjacentPositions(pt).filter { get(it) == '.' } }
        }
        println(steps.drop(64).first().count())
    }

    @Test
    fun part02a() {
    }

}
package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day13 {
    val sampleText = """
        #.##..##.
        ..#.##.#.
        ##......#
        ##......#
        ..#.##.#.
        ..##..##.
        #.#.##.#.
        
        #...##..#
        #....#..#
        ..##..###
        #####.##.
        #####.##.
        ..##..###
        #....#..#
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day13.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val input = lines.asSequence().groupRunsBy { it.isNotBlank() }.filter { it.key }.map { it.values }.toList()

    fun transposeMatrix(lines: List<List<Char>>): List<List<Char>> {
        return (0 until lines.first().size).map { idx ->
            lines.map { it[idx] }
        }
    }

    fun checkForReflectionAt(lines: List<List<Char>>, startOfRefl: Int): Boolean {
        var ai = startOfRefl - 1
        var bi = startOfRefl
        while (ai >= 0 && bi < lines.size) {
            if (lines[ai] != lines[bi]) {
                return false
            }
            --ai
            ++bi
        }
        return true
    }

    enum class ReflectionDir {
        Horizontal,
        Vertical
    }

    data class ReflectionResult(val dir: ReflectionDir, val countBefore: Int)

    fun checkForReflection(lines: List<List<Char>>): ReflectionResult {
        // indices of the start of the second part of a reflection
        val potentialHorizReflections =
            lines.withIndex().zipWithNext().filter { (a, b) -> a.value == b.value }.map { (_, b) -> b.index }
        val horizReflections = potentialHorizReflections.filter { idx ->
            checkForReflectionAt(lines, idx)
        }

        val cols = transposeMatrix(lines)
        val potentialVertReflections =
            cols.withIndex().zipWithNext().filter { (a, b) -> a.value == b.value }.map { (_, b) -> b.index }
        val vertReflections = potentialVertReflections.filter { idx ->
            checkForReflectionAt(cols, idx)
        }

        check(horizReflections.size + vertReflections.size == 1)

        if (horizReflections.isNotEmpty()) {
            return ReflectionResult(ReflectionDir.Horizontal, horizReflections.first())
        } else {
            return ReflectionResult(ReflectionDir.Vertical, vertReflections.first())
        }
    }

    @Test
    fun part01() {
        val result = input.map {
            val info = checkForReflection(it.map { it.toList() })
            when (info.dir) {
                ReflectionDir.Vertical -> info.countBefore
                ReflectionDir.Horizontal -> 100 * info.countBefore
            }
        }.sum()

        println(result)
    }

    @Test
    fun part02() {
    }

}
package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day12 {
    val sampleText = """
        ???.### 1,1,3
        .??..??...?##. 1,1,3
        ?#?#?#?#?#?#?#? 1,3,1,6
        ????.#...#... 4,1,1
        ????.######..#####. 1,6,5
        ?###???????? 3,2,1
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day12.txt")!!.readText()

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Line(val springs: String, val runs: List<Int>)

    val input = lines.map { line ->
        val (springs, runs) = line.split(' ')
        Line(springs, runs.split(',').map { it.toInt() })
    }

    fun <T, K> Iterable<T>.groupRunsBy(fn: (T) -> K): List<Pair<K, List<T>>> {
        val result = mutableListOf<Pair<K, List<T>>>()
        val iter = iterator()
        if (iter.hasNext()) {
            var v = iter.next()
            var currentKey = fn(v)
            var run = mutableListOf(v)
            while (iter.hasNext()) {
                v = iter.next()
                val k = fn(v)
                if (k == currentKey) {
                    run += v
                } else {
                    result += currentKey to run
                    currentKey = k
                    run = mutableListOf(v)
                }
            }
            if (run.isNotEmpty()) {
                result += currentKey to run
            }
        }
        return result
    }

    fun checkSolution(solution: Iterable<Char>, runs: List<Int>): Boolean {
        val solutionPattern = solution.groupRunsBy { it }.filter { it.first == '#' }.map { it.second.size }
        return solutionPattern == runs
    }

    @Test
    fun part01() {
        val totalSolutions = input.map { line ->
            val unknownIndices = line.springs.mapIndexedNotNull { i, ch ->
                when (ch) {
                    '?' -> i
                    else -> null
                }
            }.asReversed()

            val solutions = (0 until Math.pow(2.0, unknownIndices.size.toDouble()).toInt()).map { i ->
                val replacement = line.springs.toMutableList()
                var bits = i
                for (bitIdx in unknownIndices.indices) {
                    replacement[unknownIndices[bitIdx]] = when (bits and 0x01) {
                        0 -> '#'
                        else -> '.'
                    }
                    bits = bits shr 1
                }
                checkSolution(replacement, line.runs)
            }.count { it }

            solutions
        }.sum()

        println(totalSolutions)
    }

    @Test
    fun part02() {
    }

}
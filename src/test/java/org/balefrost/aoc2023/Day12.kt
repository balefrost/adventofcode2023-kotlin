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

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Line(val springs: String, val runs: List<Int>)

    val input = lines.map { line ->
        val (springs, runs) = line.split(' ')
        Line(springs, runs.split(',').map { it.toInt() })
    }

    data class CacheKey(val springs: List<Char>, val runs: List<Int>)

    fun countSolutions(cache: MutableMap<CacheKey, Long>, springs: List<Char>, runs: List<Int>): Long {
        fun helper(depth: Int, springs: List<Char>, runs: List<Int>): Long {
            val existing = cache[CacheKey(springs, runs)]
            if (existing != null) {
                return existing
            }
            fun checkRange(range: IntRange, target: Char): Boolean {
                return range.all { springs[it] == '?' || springs[it] == target }
            }

            if (runs.isEmpty()) {
                if (checkRange(springs.indices, '.')) {
                    return 1
                } else {
                    return 0
                }
            }
            val minNeeded = runs.sum() + runs.size - 1
            val slack = springs.size - minNeeded
            var count = 0L
            for (i in 0 .. slack) {
                val j = i + runs.first()
                val k = j + 1
                if (!checkRange(0 until i, '.')) {
                    continue
                }
                if (!checkRange(i until j, '#')) {
                    continue
                }
                val substringIndex: Int
                if (j < springs.size) {
                    if (!checkRange(j until k, '.')) {
                        continue
                    }
                    substringIndex = k
                } else {
                    substringIndex = j
                }
                count += helper(depth + 1, springs.subList(substringIndex, springs.size), runs.subList(1, runs.size))
            }

            cache[CacheKey(springs, runs)] = count
            return count
        }

        return helper(0, springs, runs)
    }

    @Test
    fun part01() {
        val cache = mutableMapOf<CacheKey, Long>()
        val totalSolutions = input.map { line ->
            countSolutions(cache, line.springs.toList(), line.runs)
        }.sum()

        println(totalSolutions)
    }

    @Test
    fun part02() {
        val longInput = input.map { line ->
            Line(
                (0..4).map { line.springs }.joinToString("?"),
                (0..4).flatMap { line.runs })
        }
        val cache = mutableMapOf<CacheKey, Long>()
        val totalSolutions = longInput.map { line ->
            countSolutions(cache, line.springs.toList(), line.runs)
        }.sum()

        println(totalSolutions)
    }

}
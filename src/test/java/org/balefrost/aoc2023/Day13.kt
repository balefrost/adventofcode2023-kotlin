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

    data class UnmatchedLines(val a: List<Char>, val b: List<Char>, val ai: Int, val bi: Int)

    fun checkForReflectionAt(lines: List<List<Char>>, startOfRefl: Int): List<UnmatchedLines> {
        var ai = startOfRefl - 1
        var bi = startOfRefl
        val result = mutableListOf<UnmatchedLines>()
        while (ai >= 0 && bi < lines.size) {
            if (lines[ai] != lines[bi]) {
                result += UnmatchedLines(lines[ai], lines[bi], ai, bi)
            }
            --ai
            ++bi
        }
        return result
    }

    enum class ReflectionDir {
        Vertical,
        Horizontal
    }

    data class ReflectionResult(val dir: ReflectionDir, val countBefore: Int)

    fun checkForReflection(lines: List<List<Char>>, pred: (List<UnmatchedLines>) -> Boolean): List<ReflectionResult> {

        fun checkForReflectionsInDir(elems: List<List<Char>>): List<Int> {
            return (1..elems.lastIndex).filter { idx ->
                pred(checkForReflectionAt(elems, idx))
            }
        }

        // indices of the start of the second part of a reflection
        val vertReflections = checkForReflectionsInDir(lines).map {
            ReflectionResult(ReflectionDir.Vertical, it)
        }

        val horizReflections = checkForReflectionsInDir(transposeMatrix(lines)).map {
            ReflectionResult(ReflectionDir.Horizontal, it)
        }

        return horizReflections + vertReflections
    }

    @Test
    fun part01() {
        val result = input.map {
            val reflections = checkForReflection(it.map { it.toList() }) { unmatched -> unmatched.isEmpty() }
            check(reflections.size == 1)
            val refl = reflections.first()
            when (refl.dir) {
                ReflectionDir.Horizontal -> refl.countBefore
                ReflectionDir.Vertical -> 100 * refl.countBefore
            }
        }.sum()

        println(result)
    }

    @Test
    fun part02() {
        val result = input.map {
            val reflections = checkForReflection(it.map { it.toList() }) { unmatched ->
                unmatched.size == 1 && unmatched.first().a.zip(unmatched.first().b).count { (a, b) -> a != b } == 1
            }
            check(reflections.size == 1)
            val refl = reflections.first()
            when (refl.dir) {
                ReflectionDir.Horizontal -> refl.countBefore
                ReflectionDir.Vertical -> 100 * refl.countBefore
            }
        }.sum()

        println(result)
    }

}
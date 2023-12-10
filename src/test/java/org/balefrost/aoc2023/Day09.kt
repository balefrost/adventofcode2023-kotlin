package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day09 {
    val sampleText = """
0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day09.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val progressions = lines.map { it.trim().split(" +".toRegex()).map { it.toInt()} }

    fun derivative(progression: List<Int>): List<Int> = progression.zipWithNext { a, b -> b - a }

    fun allDerivatives(progression: List<Int>) = generateSequence(progression) { p ->
        if (p.all { it == 0 }) {
            null
        } else {
            derivative(p)
        }
    }

    fun extendProgressionsForward(progressions: List<List<Int>>): List<Int> {
        val result = ArrayList<Int>(progressions.size)
        result += 0
        for (i in 1..progressions.lastIndex) {
            result += progressions[progressions.lastIndex - i].last() + result[i - 1]
        }
        result.reverse()
        return result
    }

    fun extendProgressionsBackwards(progressions: List<List<Int>>): List<Int> {
        val result = ArrayList<Int>(progressions.size)
        result += 0
        for (i in 1..progressions.lastIndex) {
            result += progressions[progressions.lastIndex - i].first() - result[i - 1]
        }
        result.reverse()
        return result
    }

    @Test
    fun part01() {
        val result = progressions.map { p ->
            val derivs = allDerivatives(p).toList()
            extendProgressionsForward(derivs).first()
        }.sum()

        println(result)
    }

    @Test
    fun part02() {
        val result = progressions.map { p ->
            val derivs = allDerivatives(p).toList()
            extendProgressionsBackwards(derivs).first()
        }.sum()

        println(result)
    }

}
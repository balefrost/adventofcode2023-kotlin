package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.math.min

class Day04 {
    val sampleText = """
        Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
        Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
        Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
        Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
        Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
        Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day04.txt")!!.readText()

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().filterNot { it.isEmpty() }

    val lineRegex = """Card +(\d+): +(.*?) \| (.*)""".toRegex()

    data class Card(val number: Int, val winning: Map<Int, Int>, val mine: Map<Int, Int>)

    fun numberOfMatches(card: Card): Int {
        val remainingWinning = card.winning.toMutableMap()
        var matchCount = 0
        for ((number, count) in card.mine) {
            val pairs = min(count, remainingWinning.getOrDefault(number, 0))
            matchCount += pairs
        }
        return matchCount
    }
    val cards = lines.map { line ->
        try {
            val (_, cardNo, winning, mine) = lineRegex.matchEntire(line)!!.groupValues
            val number = cardNo.toInt()
            val winningNos = winning.trim().split(" +".toRegex()).map { it.toInt() }.groupingBy { it }.eachCount()
            val myNos = mine.trim().split(" +".toRegex()).map { it.toInt() }.groupingBy { it }.eachCount()
            Card(number, winningNos, myNos)
        } catch (e: Exception) {
            throw Exception("Bad input line '$line'", e)
        }
    }

    @Test
    fun part01() {
        val scores = cards.map { card ->
            val matchCount = numberOfMatches(card)
            if (matchCount == 0) {
                0
            } else {
                Math.pow(2.0, (matchCount - 1).toDouble()).toInt()
            }
        }

        println(scores.sum())
    }

    @Test
    fun part02() {
        val allDeltas = cards.map { card ->
            val matchCount = numberOfMatches(card)
            val myDeltas = MutableList(cards.size) { 0 }
            // Card numbers are one-based
            for (i in (card.number)..(card.number + matchCount - 1)) {
                myDeltas[i] = 1
            }
            myDeltas
        }

        fun accumulateList(acc: MutableList<Int>, list: List<Int>, scale: Int) {
            if (scale == 0) return
            assert(acc.size == list.size)
            for (i in list.indices) {
                acc[i] += list[i] * scale
            }
        }

        val s = generateSequence(List(cards.size) { 1 }) { lastRound ->
            val newCards = MutableList(cards.size) { 0 }
            lastRound.forEachIndexed { index, count ->
                accumulateList(newCards, allDeltas[index], count)
            }
            if (newCards.all { it == 0 }) {
                null
            } else {
                newCards
            }
        }

        println(s.map { it.sum() }.sum())
    }
}

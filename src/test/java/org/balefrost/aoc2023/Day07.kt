package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.math.min

class Day07 {
    val sampleText = """
        32T3K 765
        T55J5 684
        KK677 28
        KTJJT 220
        QQQJA 483
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day07.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().filterNot { it.isEmpty() }

    val lineRegex = """(.{5}) (\d+)""".toRegex()

    data class HandCategorization(val similarCardCounts: List<Int>, val numJokers: Int)

    sealed interface HandCategory {
        fun check(cards: List<Char>): Boolean

        data object FiveOfAKind : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(1).sum() + numJokers == 5
            }
        }

        data object FourOfAKind : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(1).sum() + numJokers == 4
            }
        }

        data object FullHouse : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(2).sum() + numJokers == 5
            }
        }

        data object ThreeOfAKind : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(1).sum() + numJokers == 3
            }
        }

        data object TwoPair : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(2).sum() + numJokers >= 4
            }
        }

        data object OnePair : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                val (categorized, numJokers) = categorize(cards)
                return categorized.take(1).sum() + numJokers >= 2
            }
        }

        data object HighCard : HandCategory {
            override fun check(cards: List<Char>): Boolean {
                return true
            }
        }
    }

    companion object {
        val categoriesInOrder = listOf(
            HandCategory.FiveOfAKind,
            HandCategory.FourOfAKind,
            HandCategory.FullHouse,
            HandCategory.ThreeOfAKind,
            HandCategory.TwoPair,
            HandCategory.OnePair,
            HandCategory.HighCard
        )

        val cardToStrength = listOf(
            'A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2', '?'
        ).mapIndexed { idx, i -> i to idx }.toMap()

        fun categorize(cards: List<Char>): HandCategorization {
            val cardCounts = cards.filter { it != '?' }.groupingBy { it }.eachCount().values.sortedDescending()
            val numJokers = cards.count { it == '?' }
            return HandCategorization(cardCounts, numJokers)
        }
    }

    data class Hand(val cards: List<Char>, val bid: Int)

    fun compareHands(x: List<Char>, y: List<Char>): Int {
        assert(x.size == 5)
        assert(y.size == 5)
        val thisHandCategory = categoriesInOrder.indexOfFirst { it.check(x) }
        val otherHandCategory = categoriesInOrder.indexOfFirst { it.check(y) }
        if (thisHandCategory < otherHandCategory) {
            return 1 // this hand is better
        } else if (thisHandCategory > otherHandCategory) {
            return -1 // this hand is worse
        }

        for (index in x.indices) {
            val myStrength = cardToStrength.getValue(x[index])
            val otherStrength = cardToStrength.getValue(y[index])
            if (myStrength < otherStrength) {
                return 1 // this card is better
            } else if (myStrength > otherStrength) {
                return -1 // this card is worse
            }
        }
        return 0
    }

    val hands = lines.map { line ->
        try {
            val (_, cards, bid) = lineRegex.matchEntire(line)!!.groupValues
            Hand(cards.toList(), bid.toInt())
        } catch (ex: Exception) {
            throw Exception("Bad line: '$line'", ex)
        }
    }

    @Test
    fun part01() {
        val winnings = hands.sortedWith(compareBy(::compareHands) { it.cards }).foldIndexed(0) { idx, acc, hand ->
            acc + (idx + 1) * hand.bid
        }
        println(winnings)
    }

    // 251565697 wrong
    @Test
    fun part02() {
        val newHands = hands.map { (cards, bid) ->
            Hand(cards.map { if (it == 'J') '?' else it }, bid)
        }
        val winnings = newHands.sortedWith(compareBy(::compareHands) { it.cards }).foldIndexed(0) { idx, acc, hand ->
            acc + (idx + 1) * hand.bid
        }
        println(winnings)
    }
}

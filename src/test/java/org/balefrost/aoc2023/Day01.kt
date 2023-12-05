package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day01 {
    @Test
    fun part01() {
        val lines = Day01::class.java.getResource("day01.txt")!!.readText().lines().filterNot { it.isEmpty() }
        val numbers = lines.map {line ->
            val digits = line.filter { it.isDigit() }
            val firstDigit = digits.first()
            val lastDigit = digits.last()
            firstDigit.digitToInt() * 10 + lastDigit.digitToInt()
        }
        val sum = numbers.sum()
        println(sum)
    }

    @Test
    fun part02() {
        val lines = Day01::class.java.getResource("day01.txt")!!.readText().lines().filterNot { it.isEmpty() }
        val numberStrings = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9
        )
        val digitStrings = (1..9).map { it.toString() to it }.toMap()
        val allStrings = numberStrings + digitStrings

        val numbers = lines.map {line ->
            val firstDigit = line.suffixesLongToShort().mapNotNull { p ->
                allStrings.entries.firstOrNull { (k, _) ->
                    p.startsWith(k)
                }?.value
            }.first()

            val lastDigit = line.prefixesLongToShort().mapNotNull { s ->
                allStrings.entries.firstOrNull { (k, _) ->
                    s.endsWith(k)
                }?.value
            }.first()

            firstDigit * 10 + lastDigit
        }
        val sum = numbers.sum()
        println(sum)
    }
}
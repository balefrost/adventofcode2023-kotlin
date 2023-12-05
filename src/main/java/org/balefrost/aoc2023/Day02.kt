package org.balefrost.aoc2023

import kotlin.math.max

object Day02Part1 {

    fun parse(lines: Iterable<String>): Map<Int, List<Map<String, Int>>> {
        val gameRegex = """Game (\d+): (.*)""".toRegex()
        return lines.associate { line ->
            val matches = gameRegex.matchEntire(line)!!
            val gameNo = matches.groupValues[1].toInt()
            val rest = matches.groupValues[2]
            val rounds = rest.split("; ")
            val roundData = rounds.map { round ->
                val cubes = round.split(", ")
                cubes.associate { c ->
                    val (number, color) = c.split(" ")
                    color to number.toInt()
                }
            }
            gameNo to roundData
        }
    }
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Day01Part1::class.java.getResource("day02.txt")!!.readText().lines().filterNot { it.isEmpty() }
        val all = parse(lines)

        val target = mapOf(
            "red" to 12,
            "green" to 13,
            "blue" to 14
        )
        val possible = all.filter { (_, rounds) ->
            rounds.all { round ->
                round.entries.all { (color, count) ->
                    count <= target.getOrDefault(color, 0)
                }
            }
        }

        println(possible.map { it.key }.sum())
    }
}

object Day02Part2 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Day01Part1::class.java.getResource("day02.txt")!!.readText().lines().filterNot { it.isEmpty() }
        val all = Day02Part1.parse(lines)

        val powers = all.map { (_, rounds) ->
            val maxs = mutableMapOf<String, Int>()
            for (round in rounds) {
                for ((color, count) in round) {
                    maxs.merge(color, count) { old, new -> max(old, new) }
                }
            }
            maxs.getValue("red") * maxs.getValue("green") * maxs.getValue("blue")
        }

        println(powers.sum())
    }
}
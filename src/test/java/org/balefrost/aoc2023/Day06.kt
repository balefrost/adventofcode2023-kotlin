package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day06 {
    val sampleInput = """
        Time:      7  15   30
        Distance:  9  40  200
    """.trimIndent()

    data class Race(val time: Int, val record: Int)

    val races: List<Race>

//    val lines = sampleInput.lines().toMutableList()
    val lines = this::class.java.getResource("day06.txt")!!.readText().lines()

    init {
        val times = lines[0].split(':')[1].trim().split(" +".toRegex())
        val dists = lines[1].split(':')[1].trim().split(" +".toRegex())
        races = times.zip(dists) { t, d -> Race(t.toInt(), d.toInt()) }
    }

    @Test
    fun part01() {
        val waysToBeatEachRace = races.map { race ->
            val numberOfWaysToBeatRecord = (0..race.time).count { held ->
                val totalDist = held * race.time - held * held
                totalDist > race.record
            }
            numberOfWaysToBeatRecord
        }
        println(waysToBeatEachRace.reduce { a, b -> a * b })
    }

    @Test
    fun part02() {
        val aggTime = races.map { it.time.toString() }.joinToString("").toLong()
        val aggRecord = races.map { it.record.toString() }.joinToString("").toLong()
        val numberOfWaysToBeatRecord = (0..aggTime).count { held ->
            val totalDist = held * aggTime - held * held
            totalDist > aggRecord
        }
        println(numberOfWaysToBeatRecord)
    }
}
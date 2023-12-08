package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.lang.Exception

class Day08 {
    val sampleText = """
LR

11A = (11B, XXX)
11B = (XXX, 11Z)
11Z = (11B, XXX)
22A = (22B, XXX)
22B = (22C, 22C)
22C = (22Z, 22Z)
22Z = (22B, 22B)
XXX = (XXX, XXX)
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day08.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val lineRegex = """(\w+) = \((\w+), (\w+)\)""".toRegex()

    val directions: List<Char>
    val states: Map<String, Pair<String, String>>

    init {
        directions = lines[0].trim().toList()
        states = lines.drop(2).associate { line ->
            val (_, state, left, right) = lineRegex.matchEntire(line)!!.groupValues
            state to (left to right)
        }
    }

    @Test
    fun part01() {
        val progression: Sequence<String> = sequence {
            var currentState = "AAA"
            while (currentState != "ZZZ") {
                for (dir in directions) {
                    val state = states.getValue(currentState)
                    currentState = when (dir) {
                        'L' -> state.first
                        'R' -> state.second
                        else -> throw Exception("Invalid dir $dir")
                    }
                    yield(currentState)
                }
            }
        }

        println(progression.count())
    }

    @Test
    fun part02() {
        val startingNodes = states.keys.filter { it.endsWith('A') }

        data class StepInfo(val count: Int, val final: String)

        fun step(start: String): StepInfo {
            var currentState = start
            var steps = 0
            do {
                for (dir in directions) {
                    val state = states.getValue(currentState)
                    currentState = when (dir) {
                        'L' -> state.first
                        'R' -> state.second
                        else -> throw Exception("Invalid dir $dir")
                    }
                    steps += 1
                }
            } while(!currentState.endsWith('Z'))
            return StepInfo(steps, currentState)
        }

        fun generateProgression(node: String): Map<String, StepInfo> {
            var currentState = node
            val transitionMap = mutableMapOf<String, StepInfo>()
            while (currentState !in transitionMap) {
                val stepInfo = step(currentState)
                transitionMap[currentState] = stepInfo
                currentState = stepInfo.final
            }
            return transitionMap
        }

        val progression = startingNodes.associateWith { generateProgression(it) }

        val bad = progression.filter { it.value.size != 2 || it.value.values.map { it.final }.toSet().size != 1 }
        check(bad.isEmpty()) { "assumption violated" }

        val counts = progression.values.map { it.values.first().count.toLong() }

        val iterations = counts.reduce(::lcm)

        println(iterations)
    }

    fun gcd(a: Long, b: Long): Long {
        var x = a
        var y = b

        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    fun lcm(a: Long, b: Long): Long {
        return a / gcd(a, b) * b
    }
}

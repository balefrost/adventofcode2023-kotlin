package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.util.Stack
import kotlin.math.min

class Day20 {
    val sampleText = """
        broadcaster -> a
        %a -> inv, con
        &inv -> b
        %b -> con
        &con -> output
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day20.txt")!!.readText()

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Line(val type: Char?, val name: String, val destinations: List<String>)
    val lineRegex = """([%&])?(\w+) -> (.*)""".toRegex()

    val input = lines.map { line ->
        val m = lineRegex.matchEntire(line)
        val (_, type, name, rest) = m!!.groupValues
        val targets = rest.split(", ")
        val typeChar = when (type) {
            "" -> null
            else -> type[0]
        }
        Line(typeChar, name, targets)
    }

    sealed interface Module<M : Module<M>> {
        fun acceptPulse(isHigh: Boolean, from: String): Pair<M, Boolean?>

        data class FlipFlop(val isOn: Boolean = false) : Module<FlipFlop> {
            override fun acceptPulse(isHigh: Boolean, from: String): Pair<FlipFlop, Boolean?> {
                if (isHigh) {
                    return this to null
                }
                return FlipFlop(!isOn) to !isOn
            }
        }

        data class Conjunction(val states: Map<String, Boolean>) : Module<Conjunction> {
            constructor(sources: Iterable<String>) : this(sources.associateWith { false })

            override fun acceptPulse(isHigh: Boolean, from: String): Pair<Conjunction, Boolean?> {
                check(from in states)
                val updState = states + (from to isHigh)
                val outSignal = !updState.values.all { it }
                return Conjunction(updState) to outSignal
            }
        }

        data object Broadcaster : Module<Broadcaster> {
            override fun acceptPulse(isHigh: Boolean, from: String): Pair<Broadcaster, Boolean?> {
                return this to isHigh
            }
        }
    }

    data class ModuleInfo(val module: Module<*>, val destinations: List<String>)

    @Test
    fun part01() {
        val sources = mutableMapOf<String, MutableList<String>>()
        for (line in input) {
            for (destination in line.destinations) {
                sources.getOrPut(destination, ::mutableListOf) += line.name
            }
        }
        var moduleInfos = input.associate { line ->
            val module = when (line.type) {
                null -> Module.Broadcaster
                '%' -> Module.FlipFlop()
                '&' -> Module.Conjunction(sources.getValue(line.name))
                else -> error("Invalid type")
            }
            line.name to ModuleInfo(module, line.destinations)
        }

        var lowPulsesSent = 0L
        var highPulsesSent = 0L

        repeat(1000) {
            data class PulseSpec(val from: String, val to: String, val isHigh: Boolean)
            val inFlight = ArrayDeque<PulseSpec>()
            inFlight.addLast(PulseSpec("button", "broadcaster", false))

            while (inFlight.isNotEmpty()) {
                val pulse = inFlight.removeFirst()
                if (pulse.isHigh) {
                    ++highPulsesSent
                } else {
                    ++lowPulsesSent
                }
                val currentModule = pulse.to
                val moduleInfo = moduleInfos[currentModule]
                if (moduleInfo == null) {
                    continue
                }
                val (newModule, outputPulse) = moduleInfo.module.acceptPulse(pulse.isHigh, pulse.from)
                moduleInfos = moduleInfos + (currentModule to moduleInfo.copy(module = newModule))
                if (outputPulse != null) {
                    for (destination in moduleInfo.destinations) {
                        inFlight.addLast(PulseSpec(currentModule, destination, outputPulse))
                    }
                }
            }
        }

        println(lowPulsesSent * highPulsesSent)
    }

    @Test
    fun part02a() {
    }

}
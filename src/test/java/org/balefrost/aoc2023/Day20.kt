package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day20 {
    val sampleText = """
        broadcaster -> a
        %a -> inv, con
        &inv -> b
        %b -> con
        &con -> output
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day20.txt")!!.readText()

//            val inputText = sampleText
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
        val destinations: List<String>
        fun acceptPulse(isHigh: Boolean, from: String): Pair<M, Boolean?>

        data class FlipFlop(val isOn: Boolean, override val destinations: List<String>) : Module<FlipFlop> {
            constructor(destinations: List<String>) : this(false, destinations)

            override fun acceptPulse(isHigh: Boolean, from: String): Pair<FlipFlop, Boolean?> {
                if (isHigh) {
                    return this to null
                }
                return copy(isOn = !isOn) to !isOn
            }
        }

        data class Conjunction(val states: Map<String, Boolean>, override val destinations: List<String>) :
            Module<Conjunction> {
            constructor(sources: Iterable<String>, destinations: List<String>) : this(
                sources.associateWith { false },
                destinations
            )

            override fun acceptPulse(isHigh: Boolean, from: String): Pair<Conjunction, Boolean?> {
                check(from in states)
                val updStates = states + (from to isHigh)
                val outSignal = !updStates.values.all { it }
                return copy(states = updStates) to outSignal
            }
        }

        data class Broadcaster(override val destinations: List<String>) : Module<Broadcaster> {
            override fun acceptPulse(isHigh: Boolean, from: String): Pair<Broadcaster, Boolean?> {
                return this to isHigh
            }
        }
    }

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
                null -> Module.Broadcaster(line.destinations)
                '%' -> Module.FlipFlop(line.destinations)
                '&' -> Module.Conjunction(sources.getValue(line.name), line.destinations)
                else -> error("Invalid type")
            }
            line.name to module
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
                val module = moduleInfos[currentModule]
                if (module == null) {
                    continue
                }
                val (newModule, outputPulse) = module.acceptPulse(pulse.isHigh, pulse.from)
                moduleInfos = moduleInfos + (currentModule to newModule)
                if (outputPulse != null) {
                    for (destination in module.destinations) {
                        inFlight.addLast(PulseSpec(currentModule, destination, outputPulse))
                    }
                }
            }
        }

        println(lowPulsesSent * highPulsesSent)
    }

    @Test
    fun part02a() {
        val sources = mutableMapOf<String, MutableList<String>>()
        for (line in input) {
            for (destination in line.destinations) {
                sources.getOrPut(destination, ::mutableListOf) += line.name
            }
        }

        val components = stronglyConnectedComponents(listOf("broadcaster", "rx") + input.map { it.name }
            .filterNot { it == "broadcaster" }) { m ->
            input.firstOrNull { it.name == m }?.destinations ?: emptyList()
        }

        val memberToComponent = components.flatMap { comp ->
            comp.map { it to comp }
        }.toMap()

        val initialModules = input.associate { line ->
            val module = when (line.type) {
                null -> Module.Broadcaster(line.destinations)
                '%' -> Module.FlipFlop(line.destinations)
                '&' -> Module.Conjunction(sources.getValue(line.name), line.destinations)
                else -> error("Invalid type")
            }
            line.name to module
        }

        data class PulseSpec(val from: String, val to: String, val isHigh: Boolean)

        fun stepMachine(
            modules: Map<String, Module<*>>,
            initialPulse: PulseSpec
        ): Pair<Map<String, Module<*>>, List<Pair<Int, PulseSpec>>> {
            var currentModules = modules
            val pulses = mutableListOf<Pair<Int, PulseSpec>>()
            val inFlight = ArrayDeque<Pair<Int, PulseSpec>>()
            inFlight.addLast(0 to initialPulse)

            while (inFlight.isNotEmpty()) {
                val p = inFlight.removeFirst()
                val (subTime, pulse) = p
                val currentModule = pulse.to
                val module = currentModules[currentModule]
                if (module == null) {
                    pulses.add(p)
                    continue
                }
                val (newModule, outputPulse) = module.acceptPulse(pulse.isHigh, pulse.from)
                currentModules = currentModules + (currentModule to newModule)
                if (outputPulse != null) {
                    for (destination in module.destinations) {
                        inFlight.addLast((subTime + 1) to PulseSpec(currentModule, destination, outputPulse))
                    }
                }
            }

            return currentModules to pulses
        }

        fun findCycleInfo(
            component: Set<String>,
            initialPulse: PulseSpec
        ): CycleInfo<Pair<Map<String, Module<*>>, List<Pair<Int, PulseSpec>>>> {
            val modules = initialModules.filterKeys { it in component }
            val cycleInfo = generateSequence(modules to emptyList<Pair<Int, PulseSpec>>()) { (ms, _) ->
                stepMachine(ms, initialPulse)
            }.findCycle()
            return cycleInfo
        }

        fun printComponentInfo(component: Set<String>, initialPulse: PulseSpec) {
            val cycleInfo = findCycleInfo(component, initialPulse)

            println("${cycleInfo.prefix.size}, ${cycleInfo.cycle.size}")

//            cycleInfo.items.withIndex().filter { (_, p) -> p.second.any { it.second.to == "tx" && !it.second.isHigh } }.map {
            var wasHigh = true
            cycleInfo.items.withIndex().forEach { (idx, stuff) ->
                val (machine, pulses) = stuff
                pulses.forEach { (subtime, pulse) ->
                    if (pulse.isHigh != wasHigh || !pulse.isHigh) {
                        println("$idx - $subtime - $pulse")
                        wasHigh = pulse.isHigh
                    }
                }
            }
            println()
        }

        val result = listOf("mh", "sp", "lg", "vh").map {
            findCycleInfo(memberToComponent.getValue(it), PulseSpec("broadcaster", it, false)).cycle.size
        }.fold(1L) { a, b -> lcm(a, b.toLong()) }

        println(result)

    }

}
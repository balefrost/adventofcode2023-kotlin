package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.max

class Day19 {
    val sampleText = """
        px{a<2006:qkq,m>2090:A,rfg}
        pv{a>1716:R,A}
        lnx{m>1548:A,A}
        rfg{s<537:gd,x>2440:R,A}
        qs{s>3448:A,lnx}
        qkq{x<1416:A,crn}
        crn{x>2662:A,R}
        in{s<1351:px,qqz}
        qqz{s>2770:qs,m<1801:hdj,R}
        gd{a>3333:R,R}
        hdj{m>838:A,pv}
        
        {x=787,m=2655,a=1222,s=2876}
        {x=1679,m=44,a=2067,s=496}
        {x=2036,m=264,a=79,s=2244}
        {x=2461,m=1339,a=466,s=291}
        {x=2127,m=1623,a=2188,s=1013}
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day19.txt")!!.readText()

    //    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Condition(val k: String, val comparison: Char, val target: Int) {
        fun test(part: Map<String, Int>): Boolean {
            val v = part.getValue(k)
            return when (comparison) {
                '<' -> v < target
                '>' -> v > target
                else -> error("Invalid comparison $comparison")
            }
        }

        override fun toString(): String {
            return "$k$comparison$target"
        }

        fun complement(): Condition {
            return when (comparison) {
                '<' -> Condition(k, '>', target - 1)
                '>' -> Condition(k, '<', target + 1)
                else -> error("Invalid comparison $comparison")
            }
        }
    }

    data class Rule(val cond: Condition?, val next: String) {
        fun test(part: Map<String, Int>): Boolean {
            return cond?.test(part) ?: true
        }
    }

    data class Workflow(val name: String, val rules: List<Rule>) {
        fun run(part: Map<String, Int>): String {
            for (rule in rules) {
                if (rule.test(part)) {
                    return rule.next
                }
            }
            error("Ran out of rules in workflow $name")
        }
    }

    val ruleRegex = """(\w+)([><])(\d+):(\w+)""".toRegex()

    val workflows = lines.takeWhile { it.isNotBlank() }.map { line ->
        val parts = line.removeSuffix("}").split('{', ',')
        val workflowName = parts[0]
        val rules = parts.asSequence().drop(1).map { str ->
            val match = ruleRegex.matchEntire(str)
            if (match == null) {
                Rule(null, str)
            } else {
                val (_, k, condition, target, next) = match.groupValues
                val c = condition[0]
                val t = target.toInt()
                Rule(Condition(k, c, t), next)
            }
        }.toList()
        Workflow(workflowName, rules)
    }.associateBy { it.name }

    val parts = lines.takeLastWhile { it.isNotBlank() }.map { line ->
        line.removePrefix("{").removeSuffix("}").split(",").map { elem ->
            val (k, v) = elem.split("=")
            k to v.toInt()
        }.toMap()
    }

    @Test
    fun part01() {
        val result = parts.filter { part ->
            val steps = generateSequence("in") {
                if (it in setOf("A", "R")) null else workflows.getValue(it).run(part)
            }.toList()
            val final = steps.last()
            final == "A"
        }.map { it.values.sum() }.sum()
        println(result)
    }

    @Test
    fun part02() {

        data class WorkflowWithConditions(val workflow: String, val conditions: List<Condition>)

        fun generateAccepting(): Sequence<List<WorkflowWithConditions>> {
            return sequence {
                suspend fun SequenceScope<List<WorkflowWithConditions>>.helper(
                    soFar: List<WorkflowWithConditions>,
                    wf: String
                ) {
                    if (wf == "R") {
                        return
                    }
                    if (wf == "A") {
                        yield(soFar)
                        return
                    }
                    val workflow = workflows.getValue(wf)
                    var conditionList = emptyList<Condition>()
                    for (rule in workflow.rules) {
                        val cond = rule.cond
                        if (cond == null) {
                            helper(soFar + WorkflowWithConditions(wf, conditionList), rule.next)
                        } else {
                            helper(soFar + WorkflowWithConditions(wf, conditionList + cond), rule.next)
                            conditionList = conditionList + cond.complement()
                        }
                    }
                }

                helper(emptyList(), "in")
            }
        }

        val accepting = generateAccepting()

        val acceptingRanges = accepting.map { steps ->
            val ranges = "xmas".associate { it.toString() to IntRange(1, 4000) }.toMutableMap()
            for (cond in steps.flatMap { it.conditions }) {
                val existingRange = ranges.getValue(cond.k)
                val newRange = when (cond.comparison) {
                    '<' -> existingRange.updateLast(cond.target - 1)
                    '>' -> existingRange.updateFirst(cond.target + 1)
                    else -> error("bad cond")
                }
                ranges[cond.k] = newRange
            }
            ranges
        }

        val result = acceptingRanges.map { ranges ->
            ranges.values.map { max(0L, it.last.toLong() - it.first + 1) }.reduce { a, b -> a * b }
        }.sum()

        println(result)
    }

}
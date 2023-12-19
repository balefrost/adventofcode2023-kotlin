package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

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

    data class Rule(val next: String, val pred: (Map<String, Int>) -> Boolean)
    data class Workflow(val name: String, val rules: List<Rule>) {
        fun run(part: Map<String, Int>): String {
            for (rule in rules) {
                if (rule.pred(part)) {
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
                Rule(str){true}
            } else {
                val (_, k, condition, target, next) = match.groupValues
                val c = condition[0]
                val t = target.toInt()
                Rule(next){ m ->
                    val v = m.getValue(k)
                    when(c) {
                        '<' -> v < t
                        '>' -> v > t
                        else -> error("Invalid condition $condition")
                    }
                }
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
            val steps = generateSequence("in") { if (it in setOf("A", "R")) null else workflows.getValue(it).run(part) }.toList()
            val final = steps.last()
            final == "A"
        }.map { it.values.sum() }.sum()
        println(result)
    }

    @Test
    fun part02() {
    }

}
package com.spanishapp

import com.spanishapp.domain.algorithm.SpanishVerbBank
import com.spanishapp.domain.algorithm.VerbKind
import org.junit.Test

class VerbBankSizeTest {
    @Test fun `report bank size and tier distribution`() {
        val all = SpanishVerbBank.all
        println("══════════════════════════════════════════")
        println(" Spanish Verb Bank — totals")
        println("══════════════════════════════════════════")
        println(" Unique verbs: ${all.size}")
        println()
        println(" By tier:")
        for (tier in 1..5) {
            val n = all.count { it.tier == tier }
            println("   Tier $tier: $n")
        }
        println()
        println(" By kind:")
        VerbKind.values().forEach { k ->
            val n = all.count { it.kind == k }
            println("   ${k.name.padEnd(15)} $n")
        }
        println("══════════════════════════════════════════")
    }
}

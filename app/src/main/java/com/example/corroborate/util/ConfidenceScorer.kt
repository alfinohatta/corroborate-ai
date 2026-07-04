package com.example.corroborate.util

import kotlin.math.exp
import kotlin.math.pow

object ConfidenceScorer {

    private val SOURCE_RELIABILITY_WEIGHTS = mapOf(
        "regulatory_filing" to 0.97,
        "internal_certified_doc" to 0.90,
        "verified_human_input" to 0.85,
        "structured_business_system" to 0.80,
        "customer_transcript" to 0.55,
        "inferred_from_conversation" to 0.35
    )

    private val LAMBDA_CLASSES = mapOf(
        "pricing" to 0.05,
        "regulatory_clause" to 0.004,
        "user_preference" to 0.01
    )

    fun computeSourceReliability(sourceType: String): Double {
        return SOURCE_RELIABILITY_WEIGHTS[sourceType] ?: 0.50
    }

    fun computeRecency(ageInDays: Int, entityClass: String): Double {
        val lambda = LAMBDA_CLASSES[entityClass] ?: 0.01
        return exp(-lambda * ageInDays)
    }

    fun computeCorroboration(independentSourcesCount: Int): Double {
        val k = 0.6
        return 1.0 - (1.0 / (1.0 + k * independentSourcesCount))
    }

    fun computeRegionalAuthority(sourceJurisdiction: String, claimRegionScope: String): Double {
        return when {
            sourceJurisdiction == claimRegionScope -> 1.0
            isSuperset(sourceJurisdiction, claimRegionScope) -> 0.7
            isHistoricallyTransferable(sourceJurisdiction, claimRegionScope) -> 0.3
            else -> 0.0
        }
    }

    private fun isSuperset(source: String, claim: String): Boolean {
        // Simplified logic: e.g., 'EU' is a superset of 'DE'
        return source == "EU" && (claim == "DE" || claim == "AT" || claim == "FR")
    }

    private fun isHistoricallyTransferable(source: String, claim: String): Boolean {
        // Placeholder for historical transfer logic
        return false
    }

    fun computeConfidence(
        sr: Double,
        st: Double,
        sc: Double,
        sa: Double,
        sh: Boolean,
        wr: Double = 1.4,
        wt: Double = 1.0,
        wc: Double = 1.0,
        wa: Double = 1.6
    ): Double {
        val weightsSum = wr + wt + wc + wa
        val baseConfidence = (sr.pow(wr) * st.pow(wt) * sc.pow(wc) * sa.pow(wa)).pow(1.0 / weightsSum)
        val shValue = if (sh) 1.0 else 0.0
        return baseConfidence + (1.0 - baseConfidence) * 0.25 * shValue
    }
}

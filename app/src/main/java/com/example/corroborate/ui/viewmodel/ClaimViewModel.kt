package com.example.corroborate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corroborate.data.model.Claim
import com.example.corroborate.data.repository.ClaimRepository
import com.example.corroborate.util.ConfidenceScorer
import kotlinx.coroutines.launch

class ClaimViewModel(private val repository: ClaimRepository) : ViewModel() {

    var query by mutableStateOf("")
    var selectedEntityClass by mutableStateOf("regulatory_clause")
    var selectedRegion by mutableStateOf("DE")
    var resultClaim by mutableStateOf<Claim?>(null)
    var confidence by mutableDoubleStateOf(0.0)
    var alternatives by mutableStateOf<List<Claim>>(emptyList())
    var provenance by mutableStateOf<List<com.example.corroborate.data.model.ClaimProvenance>>(emptyList())
    var history by mutableStateOf<List<Claim>>(emptyList())
    var edges by mutableStateOf<List<com.example.corroborate.data.model.ClaimEdge>>(emptyList())
    var isAmbiguous by mutableStateOf(false)
    var isVerifying by mutableStateOf(false)
    var isScorerDegraded by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun resolveClaim() {
        if (query.isBlank()) return

        isLoading = true
        errorMessage = null
        isAmbiguous = false

        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            
            // Simulating multiple candidates
            val winner = Claim(
                claimId = "clm_9f2a4b10",
                tenantId = "example-tenant",
                entityId = "entity_flood_exclusion",
                statement = "Flood damage exclusion applies to policies issued after 2024-01-01 in Germany.",
                claimType = com.example.corroborate.data.model.ClaimType.SEMANTIC,
                status = com.example.corroborate.data.model.ClaimStatus.ACTIVE,
                confidenceScore = 0.91,
                sourceReliabilityScore = 0.95,
                recencyScore = 0.88,
                corroborationCount = 3,
                regionalAuthorityScore = 0.97,
                humanVerified = true,
                validFrom = "2026-01-15T00:00:00Z",
                createdAt = "2026-01-15T09:12:44Z"
            )
            
            val runnerUp = Claim(
                claimId = "clm_7b11c209",
                tenantId = "example-tenant",
                entityId = "entity_flood_exclusion",
                statement = "Flood damage exclusion applies to all policies regardless of issue date.",
                claimType = com.example.corroborate.data.model.ClaimType.SEMANTIC,
                status = com.example.corroborate.data.model.ClaimStatus.SUPERSEDED,
                confidenceScore = 0.82, // High enough to trigger ambiguity in this mock
                sourceReliabilityScore = 0.65,
                recencyScore = 0.20,
                corroborationCount = 1,
                regionalAuthorityScore = 0.60,
                humanVerified = false,
                validFrom = "2023-06-01T00:00:00Z",
                createdAt = "2023-06-01T00:00:00Z"
            )

            val winnerConf = ConfidenceScorer.computeConfidence(
                sr = winner.sourceReliabilityScore,
                st = ConfidenceScorer.computeRecency(5, selectedEntityClass),
                sc = ConfidenceScorer.computeCorroboration(winner.corroborationCount),
                sa = ConfidenceScorer.computeRegionalAuthority("DE", selectedRegion), // Score against query region
                sh = winner.humanVerified
            )
            
            val runnerConf = if (query.lowercase().contains("ambiguous")) {
                winnerConf - 0.05 // Trigger contradiction guard
            } else {
                ConfidenceScorer.computeConfidence(
                    sr = runnerUp.sourceReliabilityScore,
                    st = ConfidenceScorer.computeRecency(90, selectedEntityClass),
                    sc = ConfidenceScorer.computeCorroboration(runnerUp.corroborationCount),
                    sa = ConfidenceScorer.computeRegionalAuthority("US-EAST", selectedRegion),
                    sh = runnerUp.humanVerified
                )
            }

            // Contradiction Guard (§2.3)
            if (winnerConf - runnerConf < 0.10) {
                isAmbiguous = true
                resultClaim = winner
                confidence = winnerConf
                alternatives = listOf(runnerUp)
                provenance = listOf(
                    com.example.corroborate.data.model.ClaimProvenance(
                        provenanceId = "prov_0001",
                        claimId = "clm_9f2a4b10",
                        episodeId = "episode_reg_2026_de",
                        sourceType = "regulatory_filing",
                        excerptHash = "hash_123",
                        createdAt = "2026-01-15T09:12:44Z"
                    )
                )
            } else {
                resultClaim = winner
                confidence = winnerConf
                alternatives = emptyList()
                provenance = emptyList()
            }

            if (isScorerDegraded) {
                // In degraded mode, we serve last known score labeled stale (§9)
                errorMessage = "Scorer Ensemble Unreachable: serving stale_scoring"
            }

            isLoading = false
        }
    }

    fun fetchHistory(claimId: String) {
        viewModelScope.launch {
            // Mocking history (§4.1)
            kotlinx.coroutines.delay(500)
            history = listOf(
                Claim(
                    claimId = "clm_7b11c209",
                    tenantId = "example-tenant",
                    entityId = "entity_flood_exclusion",
                    statement = "Flood damage exclusion applies to all policies regardless of issue date.",
                    claimType = com.example.corroborate.data.model.ClaimType.SEMANTIC,
                    status = com.example.corroborate.data.model.ClaimStatus.SUPERSEDED,
                    confidenceScore = 0.52,
                    sourceReliabilityScore = 0.65,
                    recencyScore = 0.20,
                    corroborationCount = 1,
                    regionalAuthorityScore = 0.60,
                    humanVerified = false,
                    validFrom = "2023-06-01T00:00:00Z",
                    createdAt = "2023-06-01T00:00:00Z"
                )
            )
            edges = listOf(
                com.example.corroborate.data.model.ClaimEdge(
                    edgeId = "edge_0001",
                    fromClaimId = claimId,
                    toClaimId = "clm_7b11c209",
                    edgeType = com.example.corroborate.data.model.EdgeType.SUPERSEDES,
                    createdAt = "2026-01-15T09:12:44Z"
                )
            )
        }
    }

    fun verifyClaim(claimId: String, action: String) {
        isVerifying = true
        viewModelScope.launch {
            // Mocking human verification (§4.1)
            kotlinx.coroutines.delay(1000)
            
            // Recompute confidence with S_h = 1.0 if confirmed
            if (action == "confirm" && resultClaim?.claimId == claimId) {
                resultClaim = resultClaim?.copy(humanVerified = true)
                confidence = ConfidenceScorer.computeConfidence(
                    sr = resultClaim!!.sourceReliabilityScore,
                    st = resultClaim!!.recencyScore,
                    sc = ConfidenceScorer.computeCorroboration(resultClaim!!.corroborationCount),
                    sa = resultClaim!!.regionalAuthorityScore,
                    sh = true
                )
            }
            isVerifying = false
        }
    }
}

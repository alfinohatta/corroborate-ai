package com.example.corroborate.data.api

import com.example.corroborate.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface CorroborateApi {

    @POST("v1/episodes")
    suspend fun createEpisode(@Body request: CreateEpisodeRequest): Response<CreateEpisodeResponse>

    @POST("v1/claims/resolve")
    suspend fun resolveClaim(@Body request: ResolveClaimRequest): Response<ResolveClaimResponse>

    @POST("v1/claims/{claim_id}/verify")
    suspend fun verifyClaim(
        @Path("claim_id") claimId: String,
        @Body request: VerifyClaimRequest
    ): Response<Unit>

    @GET("v1/claims/{claim_id}/history")
    suspend fun getClaimHistory(@Path("claim_id") claimId: String): Response<List<Claim>>

    @DELETE("v1/subjects/{user_id}")
    suspend fun deleteSubject(@Path("user_id") userId: String): Response<Unit>

    @GET("v1/audit/{audit_ref}")
    suspend fun getAuditRecord(@Path("audit_ref") auditRef: String): Response<AuditRecord>

    @POST("v1/playbooks/{playbook_id}/outcome")
    suspend fun reportPlaybookOutcome(
        @Path("playbook_id") playbookId: String,
        @Body request: PlaybookOutcomeRequest
    ): Response<Unit>
}

@Serializable
data class CreateEpisodeRequest(
    @SerialName("tenant_id") val tenantId: String,
    val region: String,
    val content: String,
    @SerialName("source_type") val sourceType: SourceType,
    val actor: String
)

@Serializable
data class CreateEpisodeResponse(
    @SerialName("episode_id") val episodeId: String,
    val status: String
)

@Serializable
data class ResolveClaimRequest(
    @SerialName("tenant_id") val tenantId: String,
    val region: String,
    val query: String,
    @SerialName("subject_entity_hint") val subjectEntityHint: String? = null
)

@Serializable
data class ResolveClaimResponse(
    val claim: Claim,
    val confidence: Double,
    val provenance: List<ClaimProvenance>,
    val alternatives: List<Claim>,
    @SerialName("audit_ref") val auditRef: String
)

@Serializable
data class VerifyClaimRequest(
    @SerialName("verifying_party") val verifyingParty: String,
    val action: String // enum [confirm, reject]
)

@Serializable
data class PlaybookOutcomeRequest(
    val outcome: OutcomeResult,
    val notes: String? = null
)

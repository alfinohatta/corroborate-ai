package com.example.corroborate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tenant(
    @SerialName("tenant_id") val tenantId: String,
    val name: String,
    val industry: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class Region(
    @SerialName("region_code") val regionCode: String,
    @SerialName("region_name") val regionName: String,
    @SerialName("data_residency_zone") val dataResidencyZone: String
)

@Serializable
data class User(
    @SerialName("user_id") val userId: String,
    @SerialName("tenant_id") val tenantId: String,
    val username: String,
    val email: String,
    val role: UserRole,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class Device(
    @SerialName("device_id") val deviceId: String,
    @SerialName("user_id") val userId: String,
    val platform: Platform,
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("app_version") val appVersion: String,
    @SerialName("device_model") val deviceModel: String? = null,
    @SerialName("last_active_at") val lastActiveAt: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Session(
    @SerialName("session_id") val sessionId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("refresh_token_hash") val refreshTokenHash: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("revoked_at") val revokedAt: String? = null
)

@Serializable
data class SubjectEntity(
    @SerialName("entity_id") val entityId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("entity_key") val entityKey: String,
    @SerialName("entity_type") val entityType: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Episode(
    @SerialName("episode_id") val episodeId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("region_code") val regionCode: String,
    @SerialName("source_type") val sourceType: SourceType,
    @SerialName("source_channel") val sourceChannel: String? = null,
    @SerialName("actor_user_id") val actorUserId: String? = null,
    @SerialName("raw_content_ref") val rawContentRef: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Claim(
    @SerialName("claim_id") val claimId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("entity_id") val entityId: String,
    val statement: String,
    @SerialName("claim_type") val claimType: ClaimType,
    val status: ClaimStatus,
    @SerialName("confidence_score") val confidenceScore: Double,
    @SerialName("source_reliability_score") val sourceReliabilityScore: Double,
    @SerialName("recency_score") val recencyScore: Double,
    @SerialName("corroboration_count") val corroborationCount: Int,
    @SerialName("regional_authority_score") val regionalAuthorityScore: Double,
    @SerialName("human_verified") val humanVerified: Boolean,
    @SerialName("valid_from") val validFrom: String,
    @SerialName("valid_to") val validTo: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_verified_at") val lastVerifiedAt: String? = null,
    @SerialName("verifying_user_id") val verifyingUserId: String? = null
)

@Serializable
data class ClaimRegionScope(
    @SerialName("claim_id") val claimId: String,
    @SerialName("region_code") val regionCode: String
)

@Serializable
data class ClaimProvenance(
    @SerialName("provenance_id") val provenanceId: String,
    @SerialName("claim_id") val claimId: String,
    @SerialName("episode_id") val episodeId: String? = null,
    @SerialName("source_type") val sourceType: String,
    @SerialName("source_ref") val sourceRef: String? = null,
    @SerialName("excerpt_hash") val excerptHash: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ClaimEdge(
    @SerialName("edge_id") val edgeId: String,
    @SerialName("from_claim_id") val fromClaimId: String,
    @SerialName("to_claim_id") val toClaimId: String,
    @SerialName("edge_type") val edgeType: EdgeType,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Playbook(
    @SerialName("playbook_id") val playbookId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("trigger_context") val triggerContext: String,
    @SerialName("steps_json") val stepsJson: List<String>,
    @SerialName("success_count") val successCount: Int,
    @SerialName("failure_count") val failureCount: Int,
    @SerialName("last_outcome_at") val lastOutcomeAt: String? = null,
    @SerialName("re_verification_due") val reVerificationDue: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class PlaybookOutcome(
    @SerialName("outcome_id") val outcomeId: String,
    @SerialName("playbook_id") val playbookId: String,
    val outcome: OutcomeResult,
    val notes: String? = null,
    @SerialName("recorded_at") val recordedAt: String
)

@Serializable
data class AuditRecord(
    @SerialName("audit_id") val auditId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("region_code") val regionCode: String,
    @SerialName("query_text") val queryText: String,
    @SerialName("winning_claim_id") val winningClaimId: String? = null,
    val confidence: Double? = null,
    @SerialName("candidates_json") val candidatesJson: String, // Or a more specific type if possible
    val signature: String,
    @SerialName("created_at") val createdAt: String
)

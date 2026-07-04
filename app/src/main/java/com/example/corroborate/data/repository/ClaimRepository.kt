package com.example.corroborate.data.repository

import com.example.corroborate.data.api.*
import com.example.corroborate.data.model.Claim
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ClaimRepository(private val api: CorroborateApi) {

    suspend fun resolveClaim(
        tenantId: String,
        region: String,
        query: String,
        subjectEntityHint: String? = null
    ): Result<ResolveClaimResponse> {
        return try {
            val response = api.resolveClaim(ResolveClaimRequest(tenantId, region, query, subjectEntityHint))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error resolving claim: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyClaim(claimId: String, verifier: String, action: String): Result<Unit> {
        return try {
            val response = api.verifyClaim(claimId, VerifyClaimRequest(verifier, action))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error verifying claim: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

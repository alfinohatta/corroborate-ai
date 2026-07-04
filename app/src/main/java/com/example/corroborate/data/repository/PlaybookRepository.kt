package com.example.corroborate.data.repository

import com.example.corroborate.data.api.CorroborateApi
import com.example.corroborate.data.api.PlaybookOutcomeRequest
import com.example.corroborate.data.model.OutcomeResult

class PlaybookRepository(private val api: CorroborateApi) {

    suspend fun reportOutcome(playbookId: String, outcome: OutcomeResult, notes: String? = null): Result<Unit> {
        return try {
            val response = api.reportPlaybookOutcome(playbookId, PlaybookOutcomeRequest(outcome, notes))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error reporting outcome: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

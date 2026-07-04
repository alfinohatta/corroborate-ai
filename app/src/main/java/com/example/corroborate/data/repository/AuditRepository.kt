package com.example.corroborate.data.repository

import com.example.corroborate.data.api.CorroborateApi
import com.example.corroborate.data.model.AuditRecord

class AuditRepository(private val api: CorroborateApi) {

    suspend fun getAuditRecord(auditRef: String): Result<AuditRecord> {
        return try {
            val response = api.getAuditRecord(auditRef)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching audit record: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

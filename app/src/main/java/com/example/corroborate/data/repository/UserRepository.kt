package com.example.corroborate.data.repository

import com.example.corroborate.data.api.CorroborateApi

class UserRepository(private val api: CorroborateApi) {

    suspend fun deleteSubject(userId: String): Result<Unit> {
        return try {
            val response = api.deleteSubject(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error deleting subject: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

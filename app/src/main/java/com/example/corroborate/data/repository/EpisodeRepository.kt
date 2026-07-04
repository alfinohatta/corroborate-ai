package com.example.corroborate.data.repository

import com.example.corroborate.data.api.CorroborateApi
import com.example.corroborate.data.api.CreateEpisodeRequest
import com.example.corroborate.data.api.CreateEpisodeResponse
import com.example.corroborate.data.model.SourceType

class EpisodeRepository(private val api: CorroborateApi) {

    suspend fun createEpisode(
        tenantId: String,
        region: String,
        content: String,
        sourceType: SourceType,
        actor: String
    ): Result<CreateEpisodeResponse> {
        return try {
            val response = api.createEpisode(CreateEpisodeRequest(tenantId, region, content, sourceType, actor))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error creating episode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

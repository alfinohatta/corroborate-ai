package com.example.corroborate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corroborate.data.model.SourceType
import com.example.corroborate.data.repository.EpisodeRepository
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: EpisodeRepository) : ViewModel() {

    var content by mutableStateOf("")
    var sourceType by mutableStateOf(SourceType.CONVERSATION)
    var isLoading by mutableStateOf(false)
    var conflictDetected by mutableStateOf(false)
    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun createEpisode() {
        if (content.isBlank()) return

        isLoading = true
        errorMessage = null
        successMessage = null
        conflictDetected = false

        viewModelScope.launch {
            // Mocking for now
            kotlinx.coroutines.delay(1000)
            
            // Simulating contradiction detection §3.1
            if (content.lowercase().contains("flood")) {
                conflictDetected = true
                successMessage = "Episode ingested. Conflict detected with existing claims (status: contested) (§3.1). Stored at: s3://corroborate-prod/raw/ep_${System.currentTimeMillis()}.enc"
            } else {
                successMessage = "Episode ingested and queued for extraction. ID: ep_${System.currentTimeMillis()}. Stored at: s3://corroborate-prod/raw/ep_${System.currentTimeMillis()}.enc"
            }

            content = ""
            isLoading = false
            
            /* Real implementation:
            val result = repository.createEpisode(
                tenantId = "[TENANT_ID]",
                region = "DE",
                content = content,
                sourceType = sourceType,
                actor = "user_agent_001"
            )
            result.fold(
                onSuccess = { response ->
                    successMessage = "Episode ${response.episodeId} ${response.status}"
                    content = ""
                },
                onFailure = { e ->
                    errorMessage = e.message
                }
            )
            isLoading = false
            */
        }
    }
}

package com.example.corroborate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corroborate.data.model.OutcomeResult
import com.example.corroborate.data.model.Playbook
import com.example.corroborate.data.repository.PlaybookRepository
import kotlinx.coroutines.launch

class PlaybookViewModel(private val repository: PlaybookRepository) : ViewModel() {

    var playbooks by mutableStateOf<List<Playbook>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchPlaybooks() {
        isLoading = true
        viewModelScope.launch {
            // Mocking
            kotlinx.coroutines.delay(600)
            playbooks = listOf(
                Playbook(
                    playbookId = "pbk_1122",
                    tenantId = "example-tenant",
                    triggerContext = "customer disputes flood exclusion",
                    stepsJson = listOf("verify_policy_date", "check_regional_exclusion_table", "escalate_if_pre_2024"),
                    successCount = 142,
                    failureCount = 9,
                    lastOutcomeAt = "2026-06-28T00:00:00Z",
                    reVerificationDue = "2026-07-28T00:00:00Z",
                    createdAt = "2026-01-01T00:00:00Z"
                )
            )
            isLoading = false
        }
    }

    fun reportOutcome(playbookId: String, outcome: OutcomeResult) {
        viewModelScope.launch {
            repository.reportOutcome(playbookId, outcome)
            // Simulating schema update §13
            val pb = playbooks.find { it.playbookId == playbookId }
            if (pb != null) {
                playbooks = playbooks.map {
                    if (it.playbookId == playbookId) {
                        it.copy(
                            successCount = it.successCount + (if (outcome == OutcomeResult.SUCCESS) 1 else 0),
                            failureCount = it.failureCount + (if (outcome == OutcomeResult.FAILURE) 1 else 0),
                            lastOutcomeAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
                        )
                    } else it
                }
            }
        }
    }
}

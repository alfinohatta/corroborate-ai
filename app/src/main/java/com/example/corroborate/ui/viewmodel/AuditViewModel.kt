package com.example.corroborate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corroborate.data.model.AuditRecord
import com.example.corroborate.data.repository.AuditRepository
import kotlinx.coroutines.launch

class AuditViewModel(private val repository: AuditRepository) : ViewModel() {

    var auditRecord by mutableStateOf<AuditRecord?>(null)
    var merkleRoot by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchAuditRecord(auditRef: String) {
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            // Mocking based on sample data
            kotlinx.coroutines.delay(800)
            auditRecord = AuditRecord(
                auditId = "audit_772f1a",
                tenantId = "example-tenant",
                regionCode = "DE",
                queryText = "flood damage exclusion clause",
                winningClaimId = "clm_9f2a4b10",
                confidence = 0.91,
                candidatesJson = """[{"claim_id": "clm_9f2a4b10", "confidence": 0.91, "selected": true}]""",
                signature = "signed_hash_placeholder",
                createdAt = "2026-06-30T10:00:00Z"
            )
            merkleRoot = " merkle_root_anchor_2026_q2_active"
            isLoading = false
        }
    }
}

package com.example.corroborate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corroborate.data.model.UserRole
import com.example.corroborate.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    var currentRole by mutableStateOf(com.example.corroborate.data.model.UserRole.AGENT)
    var devices by mutableStateOf<List<com.example.corroborate.data.model.Device>>(emptyList())
    var isDeleting by mutableStateOf(false)
    var deleteSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchUserData() {
        // Mocking user session data (§4, §5 schema)
        devices = listOf(
            com.example.corroborate.data.model.Device(
                deviceId = "device_002",
                userId = "user_agent_001",
                platform = com.example.corroborate.data.model.Platform.ANDROID,
                fcmToken = "[MOCK_TOKEN]",
                appVersion = "1.4.2",
                deviceModel = "Samsung Galaxy S24",
                lastActiveAt = "2026-07-05T04:47:00Z",
                createdAt = "2026-01-10T10:00:00Z"
            )
        )
    }

    fun deleteSubject(userId: String) {
        isDeleting = true
        deleteSuccess = false
        errorMessage = null
        
        viewModelScope.launch {
            // Mocking GDPR erasure cascade (§3.3)
            kotlinx.coroutines.delay(1500)
            deleteSuccess = true
            isDeleting = false
            
            /* Real implementation:
            val result = repository.deleteSubject(userId)
            result.fold(
                onSuccess = { 
                    deleteSuccess = true 
                },
                onFailure = { e -> 
                    errorMessage = e.message 
                }
            )
            isDeleting = false
            */
        }
    }
}

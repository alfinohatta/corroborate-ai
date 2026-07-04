package com.example.corroborate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.corroborate.data.repository.AuditRepository
import com.example.corroborate.data.repository.ClaimRepository
import com.example.corroborate.data.repository.EpisodeRepository
import com.example.corroborate.data.repository.PlaybookRepository
import com.example.corroborate.data.repository.UserRepository

class ViewModelFactory(
    private val claimRepository: ClaimRepository,
    private val episodeRepository: EpisodeRepository,
    private val auditRepository: AuditRepository,
    private val playbookRepository: PlaybookRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ClaimViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ClaimViewModel(claimRepository) as T
            }
            modelClass.isAssignableFrom(EpisodeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                EpisodeViewModel(episodeRepository) as T
            }
            modelClass.isAssignableFrom(AuditViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuditViewModel(auditRepository) as T
            }
            modelClass.isAssignableFrom(PlaybookViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PlaybookViewModel(playbookRepository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                UserViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

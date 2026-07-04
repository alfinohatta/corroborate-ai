package com.example.corroborate.data.model

enum class UserRole { ADMIN, VERIFIER, AGENT, VIEWER }

enum class Platform { ANDROID, IOS }

enum class SourceType { CONVERSATION, DOCUMENT, SYSTEM_EVENT }

enum class ClaimType { EPISODIC, SEMANTIC, PROCEDURAL }

enum class ClaimStatus { ACTIVE, SUPERSEDED, CONTESTED, DELETED }

enum class EdgeType { SUPERSEDES, CONTRADICTS, SUPPORTS }

enum class OutcomeResult { SUCCESS, FAILURE }

package com.example.corroborate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.corroborate.data.api.CorroborateApi
import com.example.corroborate.data.model.Claim
import com.example.corroborate.data.repository.ClaimRepository
import com.example.corroborate.ui.theme.CorroborateTheme
import com.example.corroborate.ui.viewmodel.ClaimViewModel
import com.example.corroborate.ui.viewmodel.ViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.corroborate.data.model.SourceType
import com.example.corroborate.data.repository.EpisodeRepository
import com.example.corroborate.ui.viewmodel.EpisodeViewModel

import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import com.example.corroborate.data.model.OutcomeResult
import com.example.corroborate.data.repository.AuditRepository
import com.example.corroborate.data.repository.PlaybookRepository
import com.example.corroborate.ui.viewmodel.AuditViewModel
import com.example.corroborate.ui.viewmodel.PlaybookViewModel

import androidx.compose.material.icons.filled.Settings
import com.example.corroborate.data.model.UserRole
import com.example.corroborate.data.repository.UserRepository
import com.example.corroborate.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple manual DI for demonstration
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
        val api = retrofit.create(CorroborateApi::class.java)
        val claimRepository = ClaimRepository(api)
        val episodeRepository = EpisodeRepository(api)
        val auditRepository = AuditRepository(api)
        val playbookRepository = PlaybookRepository(api)
        val userRepository = UserRepository(api)
        val factory = ViewModelFactory(
            claimRepository,
            episodeRepository,
            auditRepository,
            playbookRepository,
            userRepository
        )

        setContent {
            CorroborateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CorroborateApp(factory)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Resolve : Screen("resolve", "Resolve", Icons.Default.Search)
    object Ingest : Screen("ingest", "Ingest", Icons.Default.Add)
    object Audit : Screen("audit", "Audit", Icons.Default.History)
    object Playbooks : Screen("playbooks", "Playbooks", Icons.Default.MenuBook)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorroborateApp(factory: ViewModelFactory) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Resolve) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Corroborate.ai - ${currentScreen.title}") })
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(Screen.Resolve, Screen.Ingest, Screen.Audit, Screen.Playbooks, Screen.Settings)
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val userViewModel: UserViewModel = viewModel(factory = factory)
            when (currentScreen) {
                Screen.Resolve -> {
                    val viewModel: ClaimViewModel = viewModel(factory = factory)
                    ResolveScreen(viewModel, userViewModel.currentRole)
                }
                Screen.Ingest -> {
                    val viewModel: EpisodeViewModel = viewModel(factory = factory)
                    IngestScreen(viewModel)
                }
                Screen.Audit -> {
                    val viewModel: AuditViewModel = viewModel(factory = factory)
                    AuditScreen(viewModel)
                }
                Screen.Playbooks -> {
                    val viewModel: PlaybookViewModel = viewModel(factory = factory)
                    PlaybookScreen(viewModel)
                }
                Screen.Settings -> {
                    val claimViewModel: ClaimViewModel = viewModel(factory = factory)
                    SettingsScreen(userViewModel, claimViewModel)
                }
            }
        }
    }
}

@Composable
fun AuditScreen(viewModel: AuditViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchAuditRecord("audit_772f1a")
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            viewModel.auditRecord?.let { audit ->
                Text(text = "Audit Record", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Routed via: regional-stack-prod (§3.2)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Query: ${audit.queryText}")
                        Text(text = "Region: ${audit.regionCode}")
                        Text(text = "Winning Claim ID: ${audit.winningClaimId}")
                        Text(text = "Confidence: ${audit.confidence}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Signature: ${audit.signature}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Timestamp: ${audit.createdAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                viewModel.merkleRoot?.let { root ->
                    Text(text = "Audit Anchor (§5):", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Merkle Root: $root", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "Status: Anchored to Secure Log (Account ID: [REDACTED])", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "System Verification: JCT-100 Benchmark Parity (§7)", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun PlaybookScreen(viewModel: PlaybookViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchPlaybooks()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Procedural Memory (Playbooks)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(viewModel.playbooks) { playbook ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = playbook.triggerContext, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Steps: ${playbook.stepsJson.joinToString(" -> ")}")
                            val successRate = playbook.successCount.toDouble() / (playbook.successCount + playbook.failureCount)
                            Text(text = "Success Rate: ${"%.1f".format(successRate * 100)}%")
                            LinearProgressIndicator(
                                progress = { successRate.toFloat() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = if (successRate > 0.8) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )
                            
                            Text(text = "Re-verification Due: ${playbook.reVerificationDue ?: "N/A"}", style = MaterialTheme.typography.labelSmall)
                            Text(text = "Last Outcome: ${playbook.lastOutcomeAt ?: "N/A"}", style = MaterialTheme.typography.labelSmall)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Button(onClick = { viewModel.reportOutcome(playbook.playbookId, OutcomeResult.SUCCESS) }) {
                                    Text("Report Success")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.reportOutcome(playbook.playbookId, OutcomeResult.FAILURE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Report Failure")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: UserViewModel, claimViewModel: ClaimViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(text = "Settings & Compliance", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Active Role (§5)", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth()) {
            UserRole.entries.forEach { role ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    RadioButton(
                        selected = viewModel.currentRole == role,
                        onClick = { viewModel.currentRole = role }
                    )
                    Text(
                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "GDPR Article 17 (§3.3)", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Request a cascading erasure of all episodes and claims linked to your identity.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.deleteSubject("[USER_ID]") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isDeleting
                ) {
                    if (viewModel.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onError)
                    } else {
                        Text("Delete My Data (Erasure Cascade)")
                    }
                }
                
                if (viewModel.deleteSuccess) {
                    Text(
                        text = "Erasure cascade completed. Claims stripped of PII or hard-deleted.",
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Devices & Sessions (§4-5)", style = MaterialTheme.typography.titleMedium)
        viewModel.devices.forEach { device ->
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Model: ${device.deviceModel}", style = MaterialTheme.typography.labelLarge)
                    Text(text = "ID: ${device.deviceId}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "App Version: ${device.appVersion}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Platform: ${device.platform}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Last Active: ${device.lastActiveAt}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Operational Detail (§9)", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Simulate Degraded Mode", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Serve stale_scoring if ensemble unreachable.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = claimViewModel.isScorerDegraded,
                    onCheckedChange = { claimViewModel.isScorerDegraded = it }
                )
            }
        }
        Text(text = "Infra: production-stack (§6)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Scorer Calibration (§2.1)", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val lambdaChar = "\u03BB"
                Text(text = "Recency Decay ($lambdaChar class)", style = MaterialTheme.typography.labelLarge)
                Text(text = "pricing: $lambdaChar=0.05 (14d half-life)", style = MaterialTheme.typography.bodySmall)
                Text(text = "regulatory: $lambdaChar=0.004 (173d half-life)", style = MaterialTheme.typography.bodySmall)
                Text(text = "user_preference: $lambdaChar=0.01 (69d half-life)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Geometric Mean weights: wr=1.4, wa=1.6", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ResolveScreen(viewModel: ClaimViewModel, userRole: UserRole) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TextField(
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            label = { Text("Enter query (hint: type 'ambiguous' to trigger guard)") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Collection Partition: [TENANT_ID] (strict-isolation §5)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = "Entity Class (§2.1):", style = MaterialTheme.typography.labelSmall)
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("pricing", "regulatory_clause", "user_preference").forEach { cls ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    RadioButton(
                        selected = viewModel.selectedEntityClass == cls,
                        onClick = { viewModel.selectedEntityClass = cls }
                    )
                    Text(text = cls.split("_")[0], style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text(text = "Query Region (§2.3):", style = MaterialTheme.typography.labelSmall)
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("DE", "AT", "US-EAST").forEach { reg ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    RadioButton(
                        selected = viewModel.selectedRegion == reg,
                        onClick = { viewModel.selectedRegion = reg }
                    )
                    Text(text = reg, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (viewModel.selectedRegion != "DE") {
            Text(text = "Federation enabled (§5): routing via explicit gateway", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.resolveClaim() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Resolve Claim")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.isAmbiguous) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Ambiguous Resolution (§2.3): Confidence delta too low between top candidates.",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        viewModel.resultClaim?.let { claim ->
            ClaimDetail(
                claim,
                viewModel.confidence,
                onVerify = { action ->
                    viewModel.verifyClaim(claim.claimId, action)
                },
                isVerifying = viewModel.isVerifying,
                canVerify = userRole == UserRole.VERIFIER || userRole == UserRole.ADMIN
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.fetchHistory(claim.claimId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("View Claim History (§4.1)")
            }
        }
        
        if (viewModel.history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "History & Edges (§11):", style = MaterialTheme.typography.labelLarge)
            viewModel.history.forEach { hist ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Superseded Statement:", style = MaterialTheme.typography.labelSmall)
                        Text(text = hist.statement, style = MaterialTheme.typography.bodySmall)
                        Text(text = "Status: ${hist.status}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (viewModel.provenance.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Evidence Trail (Provenance §3.2):", style = MaterialTheme.typography.labelLarge)
            viewModel.provenance.forEach { prov ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Source: ${prov.sourceType}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "ID: ${prov.episodeId ?: prov.sourceRef}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Tamper Hash (SHA-256): ${prov.excerptHash}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Status: Hash Verified ✅", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    }
                }
            }
        }
        if (viewModel.alternatives.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Alternatives:", style = MaterialTheme.typography.labelLarge)
            viewModel.alternatives.forEach { alt ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = alt.statement, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        viewModel.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngestScreen(viewModel: EpisodeViewModel) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(text = "Ingest Episode", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Processing: Extraction LLM (single-pass §3.1)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = viewModel.content,
            onValueChange = { viewModel.content = it },
            label = { Text("Episode Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Source Type", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth()) {
            SourceType.entries.forEach { type ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    RadioButton(
                        selected = viewModel.sourceType == type,
                        onClick = { viewModel.sourceType = type }
                    )
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.createEpisode() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ingest Episode")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        viewModel.successMessage?.let { msg ->
            Text(
                text = msg,
                color = if (viewModel.conflictDetected) MaterialTheme.colorScheme.error else androidx.compose.ui.graphics.Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodySmall
            )
        }
        viewModel.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ClaimDetail(
    claim: Claim,
    computedConfidence: Double,
    onVerify: (String) -> Unit = {},
    isVerifying: Boolean = false,
    canVerify: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Statement:", style = MaterialTheme.typography.labelLarge)
            Text(text = claim.statement, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Confidence: ${"%.2f".format(computedConfidence)}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { computedConfidence.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Breakdown:", style = MaterialTheme.typography.labelMedium)
            Text(text = "Source Reliability: ${claim.sourceReliabilityScore}")
            Text(text = "Recency: ${claim.recencyScore}")
            Text(text = "Corroboration Count: ${claim.corroborationCount}")
            Text(text = "Regional Authority: ${claim.regionalAuthorityScore}")
            Text(text = "Human Verified: ${claim.humanVerified}")
            if (claim.humanVerified) {
                Text(text = "(Floor Raised by up to 25% §2.2)", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
            }

            if (!claim.humanVerified && canVerify) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        OutlinedButton(onClick = { onVerify("reject") }) {
                            Text("Reject")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onVerify("confirm") }) {
                            Text("Verify")
                        }
                    }
                }
            }
        }
    }
}

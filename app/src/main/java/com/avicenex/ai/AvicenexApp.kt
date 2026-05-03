package com.avicenex.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AvicenexApp() {
    var tab by remember { mutableStateOf(MobileTab.Review) }
    var selectedClaimId by remember { mutableStateOf(DemoData.claims.first().id) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF007A78), secondary = Color(0xFF5B6B7A))) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    MobileTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    MobileTab.Review -> ReviewScreen(selectedClaimId, onSelectClaim = { selectedClaimId = it })
                    MobileTab.Assistant -> AssistantScreen()
                    MobileTab.Tools -> ToolsScreen()
                    MobileTab.Profiles -> ProfilesScreen()
                }
            }
        }
    }
}

private enum class MobileTab(val label: String, val icon: ImageVector) {
    Review("Review", Icons.Default.Assignment),
    Assistant("Assistant", Icons.Default.AutoAwesome),
    Tools("Tools", Icons.Default.Dashboard),
    Profiles("Profiles", Icons.Default.Book)
}

@Composable
private fun ReviewScreen(selectedClaimId: String, onSelectClaim: (String) -> Unit) {
    val selected = DemoData.claims.first { it.id == selectedClaimId }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Avicenex AI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Pre-bill review workbench", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
        item { ComplianceBanner() }
        items(DemoData.claims) { claim ->
            ClaimQueueCard(claim = claim, selected = claim.id == selectedClaimId, onClick = { onSelectClaim(claim.id) })
        }
        item { ClaimDetail(claim = selected) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimQueueCard(claim: ReviewClaim, selected: Boolean, onClick: () -> Unit) {
    val assessment = assessmentFor(claim)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFE7F5F4) else Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(claim.id, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                RiskBadge(assessment.level)
            }
            Text(claim.summary, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            Row {
                Text(claim.owner, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                Text("${assessment.readiness}% ready", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun ClaimDetail(claim: ReviewClaim) {
    val assessment = assessmentFor(claim)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Claim detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${claim.specialty} - ${claim.encounter}", color = MaterialTheme.colorScheme.secondary)
        Text(claim.summary)
        Text("Next action: ${assessment.nextAction}", fontWeight = FontWeight.SemiBold)
        RiskPanel(assessment)
        ReferenceSection("ICD-10-CM", claim.proposedIcd, claim.proposedIcd.mapNotNull { code ->
            DemoData.icdCodes.firstOrNull { it.code == code }?.let { "${it.code} - ${it.shortDescription}" }
        })
        ReferenceSection("CPT/HCPCS", claim.proposedCpt, claim.proposedCpt.mapNotNull { code ->
            DemoData.cptCodes.firstOrNull { it.code == code }?.let { "${it.code} - ${it.plainLanguageLabel}" }
        })
    }
}

@Composable
private fun RiskPanel(assessment: ClaimRiskAssessment) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Calculated risk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(assessment.score.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { assessment.readiness / 100f }, modifier = Modifier.fillMaxWidth())
            Text("${assessment.readiness}% readiness", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            assessment.reasons.take(5).forEach { reason ->
                Row {
                    Text("+${reason.points}", modifier = Modifier.width(48.dp), fontWeight = FontWeight.Bold)
                    Column {
                        Text(reason.label, fontWeight = FontWeight.SemiBold)
                        Text(reason.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReferenceSection(title: String, codes: List<String>, references: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            codes.forEach { code -> AssistChip(onClick = {}, label = { Text(code) }) }
        }
        references.forEach { reference ->
            Text(reference, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun ComplianceBanner() {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFE7F5F4), RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF007A78))
        Spacer(Modifier.width(8.dp))
        Text(
            "Use de-identified claim summaries only. Avicenex AI supports pre-bill QA and does not replace official coding, payer, NCCI, LCD/NCD, or compliance review.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RiskBadge(level: ReviewRisk) {
    val color = when (level) {
        ReviewRisk.Low -> Color(0xFF047857)
        ReviewRisk.Medium -> Color(0xFFB45309)
        ReviewRisk.High -> Color(0xFFB91C1C)
    }
    Text(level.name, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp))
}

@Composable
private fun AssistantScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Assistant", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Workflow-style Avicenex assistant", color = MaterialTheme.colorScheme.secondary)
        }
        items(ToolCatalog.toolsFor(ToolGroup.AiWorkspace)) { item ->
            Card(shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.title, fontWeight = FontWeight.SemiBold)
                    Text(item.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item { ComplianceBanner() }
    }
}

@Composable
private fun ToolsScreen() {
    var selectedToolId by remember { mutableStateOf(ToolCatalog.tools.first().id) }
    val selected = ToolCatalog.tools.first { it.id == selectedToolId }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Tools", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Full Avicenex web toolset mapped into native mobile workflows.", color = MaterialTheme.colorScheme.secondary)
        }
        item { ComplianceBanner() }
        ToolGroup.entries.forEach { group ->
            item { Text(group.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(ToolCatalog.toolsFor(group)) { tool ->
                ToolCard(tool = tool, selected = tool.id == selectedToolId, onClick = { selectedToolId = tool.id })
            }
        }
        item { ToolDetail(tool = selected) }
    }
}

@Composable
private fun ToolCard(tool: AvicenexTool, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFE7F5F4) else Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tool.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (tool.requiresAi) ToolPill("AI")
                if (tool.offlineCapable) {
                    Spacer(Modifier.width(6.dp))
                    ToolPill("Offline")
                }
            }
            Text(tool.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Text(tool.webRoute, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun ToolDetail(tool: AvicenexTool) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(tool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(tool.nativeWorkflow)
            if (tool.requiresAi) {
                Text(
                    "Backend/API connection required for live AI output. Keep PHI out of prompts and verify all coding guidance against official sources and payer policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            ReferenceSection("Example inputs", tool.exampleInputs, emptyList())
            ReferenceSection("Expected output", tool.expectedOutput, toolReferencePreview(tool))
        }
    }
}

@Composable
private fun ToolPill(text: String) {
    Text(
        text,
        color = Color(0xFF007A78),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.background(Color(0xFFE7F5F4), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun toolReferencePreview(tool: AvicenexTool): List<String> {
    return when (tool.id) {
        "lookup", "batch-validate", "bookmarks" ->
            DemoData.icdCodes.take(2).map { "${it.code} - ${it.shortDescription}" } +
                DemoData.cptCodes.take(2).map { "${it.code} - ${it.plainLanguageLabel}" }
        "em-calculator" -> listOf("Established patient + moderate MDM -> 99214 when supported by documentation.")
        "claim-scrubber" -> listOf("Watch for E/M + procedure without modifier -25 and CCI edits.")
        "pa-tracker" -> listOf("Status, urgency, payer, CPT/HCPCS, submitted date, auth number, expiration.")
        else -> emptyList()
    }
}

@Composable
private fun ProfilesScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Profiles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(DemoData.codeSets) { codeSet ->
            Card(shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(codeSet.name, fontWeight = FontWeight.Bold)
                    Text(codeSet.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Text(codeSet.codes.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun assessmentFor(claim: ReviewClaim): ClaimRiskAssessment {
    return ClaimRiskScorer.assess(
        ClaimRiskInput(
            summary = claim.summary,
            icdCodes = claim.proposedIcd,
            cptCodes = claim.proposedCpt,
            specialty = claim.specialty,
            codeSet = DemoData.codeSets.firstOrNull { it.id == claim.profileId },
            icdReferences = claim.proposedIcd.mapNotNull { code -> DemoData.icdCodes.firstOrNull { it.code.equals(code, true) } },
            cptReferences = claim.proposedCpt.mapNotNull { code -> DemoData.cptCodes.firstOrNull { it.code.equals(code, true) } }
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AvicenexAppPreview() {
    AvicenexApp()
}

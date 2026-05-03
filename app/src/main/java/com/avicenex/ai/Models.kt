package com.avicenex.ai

enum class ReviewRisk {
    Low,
    Medium,
    High
}

data class ReviewClaim(
    val id: String,
    val owner: String,
    val specialty: String,
    val encounter: String,
    val summary: String,
    val proposedIcd: List<String>,
    val proposedCpt: List<String>,
    val profileId: String,
    val status: String
)

data class IcdCode(
    val code: String,
    val description: String,
    val shortDescription: String,
    val billable: Boolean
)

data class CodeSet(
    val id: String,
    val name: String,
    val ownerType: String,
    val description: String,
    val codes: List<String>
)

data class CptHcpcsCode(
    val code: String,
    val codeSystem: String,
    val category: String,
    val subCategory: String,
    val plainLanguageLabel: String,
    val plainLanguageDescription: String,
    val clinicalArea: List<String>,
    val commonSettings: List<String>,
    val documentationPrompts: List<String>,
    val modifierHints: List<String>,
    val riskFlags: List<String>,
    val keywords: List<String>
)

data class ClaimRiskReason(
    val label: String,
    val points: Int,
    val detail: String
)

data class ClaimRiskAssessment(
    val score: Int,
    val readiness: Int,
    val level: ReviewRisk,
    val reasons: List<ClaimRiskReason>,
    val nextAction: String
)

data class ClaimRiskInput(
    val summary: String,
    val icdCodes: List<String>,
    val cptCodes: List<String>,
    val specialty: String?,
    val codeSet: CodeSet?,
    val icdReferences: List<IcdCode>,
    val cptReferences: List<CptHcpcsCode>
)

package com.avicenex.ai

import kotlin.math.max
import kotlin.math.min

object ClaimRiskScorer {
    fun assess(input: ClaimRiskInput): ClaimRiskAssessment {
        val summary = input.summary.trim()
        val icdCodes = input.icdCodes.map { it.trim().uppercase() }.filter { it.isNotBlank() }.distinct()
        val cptCodes = input.cptCodes.map { it.trim().uppercase() }.filter { it.isNotBlank() }.distinct()
        val reasons = mutableListOf<ClaimRiskReason>()

        if (summary.isEmpty()) push(reasons, "Missing claim summary", 25, "No de-identified encounter summary is available to support the proposed codes.")
        else if (summary.length < 50) push(reasons, "Thin claim summary", 8, "The summary is brief; confirm it captures the key diagnoses, services, orders, and plan.")

        if (icdCodes.isEmpty()) push(reasons, "Missing ICD-10-CM codes", 22, "No proposed diagnosis codes were entered.")
        if (cptCodes.isEmpty()) push(reasons, "Missing CPT/HCPCS codes", 22, "No proposed service, procedure, or supply codes were entered.")

        val unknownIcd = unknownCodes(icdCodes, input.icdReferences.map { it.code })
        push(reasons, "Unknown local ICD-10-CM code", min(24, unknownIcd.size * 12), if (unknownIcd.isEmpty()) "" else "${unknownIcd.joinToString()} did not match the local ICD-10-CM reference.")

        val nonBillable = input.icdReferences.filter { !it.billable }
        push(reasons, "Non-billable ICD-10-CM code", min(24, nonBillable.size * 18), if (nonBillable.isEmpty()) "" else "${nonBillable.joinToString { it.code }} is marked as a header/non-billable diagnosis.")

        val unknownCpt = unknownCodes(cptCodes, input.cptReferences.map { it.code })
        push(reasons, "Unknown local CPT/HCPCS code", min(24, unknownCpt.size * 12), if (unknownCpt.isEmpty()) "" else "${unknownCpt.joinToString()} did not match the local CPT/HCPCS reference layer.")

        val riskFlagCount = input.cptReferences.sumOf { it.riskFlags.size }
        push(reasons, "CPT/HCPCS risk flags", min(24, riskFlagCount * 4), if (riskFlagCount == 0) "" else "$riskFlagCount curated risk flag${if (riskFlagCount == 1) "" else "s"} require review across the proposed CPT/HCPCS codes.")

        val modifierHintCount = input.cptReferences.sumOf { it.modifierHints.size }
        push(reasons, "Modifier verification", min(12, modifierHintCount * 3), if (modifierHintCount == 0) "" else "$modifierHintCount modifier hint${if (modifierHintCount == 1) "" else "s"} should be verified for payer and encounter fit.")

        val documentationPromptCount = input.cptReferences.sumOf { it.documentationPrompts.size }
        push(reasons, "Documentation support", min(18, documentationPromptCount * 2), if (documentationPromptCount == 0) "" else "$documentationPromptCount documentation prompt${if (documentationPromptCount == 1) "" else "s"} should be supported in the chart.")

        val payerFlagCount = input.cptReferences.sumOf { code ->
            (code.riskFlags + code.documentationPrompts + code.modifierHints).count {
                hasAnyTerm(it, listOf("payer", "medicare", "coverage", "eligibility", "bundling", "threshold"))
            }
        }
        push(reasons, "Payer/manual verification", min(12, payerFlagCount * 3), if (payerFlagCount == 0) "" else "$payerFlagCount item${if (payerFlagCount == 1) "" else "s"} mention payer, Medicare, coverage, eligibility, bundling, or thresholds.")

        input.codeSet?.let { codeSet ->
            if (icdCodes.isNotEmpty()) {
                val profileCodes = codeSet.codes.map { it.uppercase() }.toSet()
                val outsideProfileCodes = icdCodes.filterNot { profileCodes.contains(it) }
                push(reasons, "Profile context check", min(10, outsideProfileCodes.size * 5), if (outsideProfileCodes.isEmpty()) "" else "${outsideProfileCodes.joinToString()} is outside the selected ${codeSet.name}.")
            }
        }

        procedureDiagnosisMismatch(input.cptReferences, input.icdReferences, summary)?.let {
            push(reasons, "Procedure and diagnosis context", it.first, it.second)
        }

        specialtyContextMismatch(input.cptReferences, input.specialty, input.codeSet)?.let {
            push(reasons, "Specialty context check", it.first, it.second)
        }

        val sortedReasons = reasons.filter { it.detail.isNotEmpty() }.sortedWith(compareByDescending<ClaimRiskReason> { it.points }.thenBy { it.label })
        val score = min(100, sortedReasons.sumOf { it.points })
        val level = riskLevel(score)
        return ClaimRiskAssessment(score, max(0, 100 - score), level, sortedReasons, nextAction(sortedReasons, level))
    }

    private fun normalize(value: String) = value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()
    private fun hasAnyTerm(value: String, terms: List<String>) = terms.any { normalize(value).contains(it) }
    private fun riskLevel(score: Int) = when {
        score >= 60 -> ReviewRisk.High
        score >= 30 -> ReviewRisk.Medium
        else -> ReviewRisk.Low
    }
    private fun push(reasons: MutableList<ClaimRiskReason>, label: String, points: Int, detail: String) {
        if (points > 0) reasons.add(ClaimRiskReason(label, points, detail))
    }
    private fun unknownCodes(codes: List<String>, knownCodes: List<String>): List<String> {
        val known = knownCodes.map { it.uppercase() }.toSet()
        return codes.filterNot { known.contains(it.uppercase()) }
    }
    private fun procedureDiagnosisMismatch(cptReferences: List<CptHcpcsCode>, icdReferences: List<IcdCode>, summary: String): Pair<Int, String>? {
        val diagnosisContext = "$summary ${icdReferences.joinToString(" ") { "${it.code} ${it.description} ${it.shortDescription}" }}"
        val preventiveCode = icdReferences.any { it.code.startsWith("Z00") }
        val diabetesContext = hasAnyTerm(diagnosisContext, listOf("diabetes", "a1c", "hemoglobin"))
        cptReferences.forEach { code ->
            val procedureContext = normalize("${code.category} ${code.subCategory} ${code.plainLanguageLabel} ${code.plainLanguageDescription} ${code.keywords.joinToString(" ")}")
            if ((procedureContext.contains("annual wellness") || procedureContext.contains("preventive")) && !preventiveCode) {
                return 10 to "${code.code} appears preventive/wellness-oriented, but the ICD list does not include an obvious preventive diagnosis such as Z00.00."
            }
            if ((procedureContext.contains("a1c") || procedureContext.contains("hemoglobin")) && !diabetesContext) {
                return 10 to "${code.code} appears diabetes/A1C-related, but the summary and ICD context do not show clear diabetes support."
            }
        }
        return null
    }
    private fun specialtyContextMismatch(cptReferences: List<CptHcpcsCode>, specialty: String?, codeSet: CodeSet?): Pair<Int, String>? {
        val context = normalize("${specialty.orEmpty()} ${codeSet?.name.orEmpty()} ${codeSet?.description.orEmpty()}")
        if (context.isEmpty()) return null
        val mismatches = cptReferences.filter { code ->
            if (code.clinicalArea.any { normalize(it).contains("specialty care") }) false
            else !code.clinicalArea.any { area -> normalize(area).split(" ").filter { it.length > 3 }.any { context.contains(it) } }
        }
        if (mismatches.isEmpty()) return null
        return min(8, mismatches.size * 4) to "${mismatches.joinToString { it.code }} should be checked against the selected specialty/profile context."
    }
    private fun nextAction(reasons: List<ClaimRiskReason>, level: ReviewRisk): String {
        val topReason = reasons.firstOrNull() ?: return "Proceed with standard pre-bill QA and payer policy checks."
        return when {
            topReason.label == "Missing claim summary" -> "Add a de-identified encounter summary before coding review."
            topReason.label == "Missing ICD-10-CM codes" -> "Add supported ICD-10-CM diagnosis codes from the chart."
            topReason.label == "Missing CPT/HCPCS codes" -> "Add proposed CPT/HCPCS service codes before submission review."
            topReason.label == "Non-billable ICD-10-CM code" -> "Replace header/non-billable ICD-10-CM entries with billable supported codes."
            topReason.label.contains("Unknown") -> "Verify unmatched codes against the current ICD-10-CM, CPT/HCPCS, and payer references."
            topReason.label == "CPT/HCPCS risk flags" -> "Resolve the highest-impact CPT/HCPCS risk flags before billing."
            topReason.label == "Modifier verification" -> "Confirm modifier use and payer-specific requirements."
            topReason.label == "Documentation support" -> "Confirm the chart supports the listed documentation prompts."
            level == ReviewRisk.High -> "Hold for coding review before claim submission."
            level == ReviewRisk.Medium -> "Route for targeted documentation or payer verification."
            else -> "Proceed with standard pre-bill QA and payer policy checks."
        }
    }
}

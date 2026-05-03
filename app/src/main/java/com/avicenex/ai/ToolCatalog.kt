package com.avicenex.ai

enum class ToolGroup(val label: String) {
    AiWorkspace("AI Workspace"),
    BillingCoding("Billing & Coding"),
    ValidatorsLookup("Validators & Lookup"),
    ClaimManagement("Claim & Denial Management"),
    Personalization("Personalization")
}

data class AvicenexTool(
    val id: String,
    val title: String,
    val webRoute: String,
    val group: ToolGroup,
    val summary: String,
    val nativeWorkflow: String,
    val requiresAi: Boolean,
    val offlineCapable: Boolean,
    val exampleInputs: List<String>,
    val expectedOutput: List<String>
)

object ToolCatalog {
    val tools = listOf(
        AvicenexTool("chat", "Chat", "/chat", ToolGroup.AiWorkspace, "Classic Avicenex AI medical billing and coding chat with specialty context, history, PHI reminders, markdown output, and export.", "Mobile chat surface with saved sessions, de-identified prompt warning, specialty-aware system context, and export/share action.", true, false, listOf("Can you explain CPT 99214 documentation risk?", "Review a de-identified coding question."), listOf("Streaming answer when API is available", "Mock/local guidance when offline backend is not configured")),
        AvicenexTool("assistant", "Coding Assistant", "/assistant", ToolGroup.AiWorkspace, "Structured encounter workspace with notes input, AI ICD/CPT suggestions, rationale, CCI awareness, and a code basket.", "Three-step native flow: encounter notes, suggested codes, confirmed code basket.", true, false, listOf("Chief complaint, assessment, plan", "Procedures performed"), listOf("Suggested ICD-10-CM and CPT/HCPCS codes", "Rationale and bundling warnings")),
        AvicenexTool("review", "Pre-Bill Review", "/review", ToolGroup.AiWorkspace, "Claim queue workbench with calculated risk, readiness, reference payloads, and recommended next actions.", "Implemented as the Review tab with queue, claim detail, calculated risk panel, ICD/CPT references, and compliance reminder.", true, true, DemoData.claims.map { "${it.id}: ${it.proposedIcd.joinToString()} / ${it.proposedCpt.joinToString()}" }, listOf("Risk score 0-100", "Readiness", "Top risk reasons", "Next action")),
        AvicenexTool("lookup", "Code Lookup", "/lookup", ToolGroup.BillingCoding, "Search ICD-10-CM, CPT, and HCPCS by code or description with bookmark support.", "Native searchable reference list backed by bundled demo ICD/CPT data until full data import is wired.", false, true, listOf("E11.9", "99214", "A1C"), listOf("Code description", "Plain-language guidance", "Bookmark action")),
        AvicenexTool("compare", "Compare Codes", "/compare", ToolGroup.BillingCoding, "Side-by-side ICD/CPT comparison with descriptions, category, notes, and CCI relationship hints.", "Two-code comparison using local reference objects and CCI note placeholders.", false, true, listOf("99213 vs 99214", "E11.40 vs E11.42"), listOf("Differences", "Billing behavior notes", "Bundling indicator when known")),
        AvicenexTool("cheatsheet", "Cheat Sheet", "/cheatsheet", ToolGroup.BillingCoding, "Payer and specialty reference card with common diagnoses, procedures, modifiers, and payer reminders.", "Specialty profile card generated from bundled profile code sets and CPT references.", false, true, listOf("Primary Care + Medicare", "Endocrinology + Commercial"), listOf("Top codes", "Modifier reminders", "Payer checks")),
        AvicenexTool("prior-auth", "Prior Auth Lookup", "/prior-auth", ToolGroup.BillingCoding, "CPT/HCPCS prior authorization requirement lookup by payer type with documentation and denial reminders.", "Local guidance card for selected CPT/HCPCS code and payer type.", false, true, listOf("G0439 Medicare", "99214 Commercial"), listOf("Auth likelihood", "Required documents", "Common denial reasons")),
        AvicenexTool("denial", "Denial Analyzer", "/denial", ToolGroup.BillingCoding, "Explains denial codes/descriptions and suggests correction, resubmission, and appeal strategy.", "Mobile denial intake with local CARC/RARC examples and AI handoff when configured.", true, true, listOf("CO-16", "N180", "Prior authorization absent"), listOf("Meaning", "Likely causes", "Fix steps", "Appeal path")),
        AvicenexTool("modifiers", "Modifier Lookup", "/modifiers", ToolGroup.BillingCoding, "CPT/HCPCS modifier guide with use cases, payment impact, stacking rules, and documentation flags.", "Searchable mobile modifier reference with payer-policy reminder.", false, true, listOf("25", "59", "95"), listOf("When to use", "Documentation needed", "Stacking cautions")),
        AvicenexTool("em-calculator", "E/M Calculator", "/em-calculator", ToolGroup.BillingCoding, "Office/outpatient E/M level calculator using MDM or time thresholds.", "Native MDM/time selection flow returning 99202-99215 recommendation.", false, true, listOf("Moderate MDM, established patient", "45 minutes, new patient"), listOf("Recommended E/M code", "Level explanation")),
        AvicenexTool("batch-validate", "Batch Validator", "/batch-validate", ToolGroup.ValidatorsLookup, "Validate up to 200 pasted ICD/CPT/HCPCS codes and export results.", "Paste codes, validate against bundled local references, then share CSV-style results.", false, true, listOf("E11.9, I10, 99214, 83036"), listOf("Valid", "Invalid format", "Unrecognized")),
        AvicenexTool("cci-check", "CCI Edit Checker", "/cci-check", ToolGroup.ValidatorsLookup, "Checks NCCI bundling edits between two procedure codes and modifier bypass rules.", "Two CPT/HCPCS inputs with indicator explanation and modifier guidance.", false, true, listOf("E/M + procedure", "Column 1 / Column 2 CPT pair"), listOf("Indicator 0/1/no known edit", "Bypass modifier guidance")),
        AvicenexTool("carc-rarc", "CARC / RARC Lookup", "/carc-rarc", ToolGroup.ValidatorsLookup, "EOB/ERA reason and remark code lookup with category and next-step guidance.", "Search remittance code cards with denial workflow handoff.", false, true, listOf("CO-16", "N180", "MA01"), listOf("Meaning", "Category", "Actionable next step")),
        AvicenexTool("claim-scrubber", "Claim Scrubber", "/claim-scrubber", ToolGroup.ValidatorsLookup, "Client-side pre-submission validation for POS, diagnosis, CPT, modifiers, units, pointers, CCI edits, duplicates, and laterality.", "Mobile claim form with offline scrub summary and issue list.", false, true, listOf("Clean claim", "E/M + procedure missing -25", "Wrong POS"), listOf("Errors", "Warnings", "Resolution hints")),
        AvicenexTool("appeal", "Appeal Letter", "/appeal", ToolGroup.ClaimManagement, "AI appeal letter generator for denied claims with documentation checklist.", "Claim denial intake form with generated letter output when API is configured.", true, false, listOf("Claim number", "DOS", "CPT/ICD codes", "Payer", "Denial reason", "Medical necessity"), listOf("Appeal letter", "Attachment checklist", "Copy/share")),
        AvicenexTool("pa-tracker", "Prior Auth Tracker", "/pa-tracker", ToolGroup.ClaimManagement, "Local prior authorization request management with status filters, urgency, auth number, expiration, and notes.", "Native list/detail tracker backed by device storage in a production app.", false, true, listOf("Pending urgent G0439 request", "Approved auth expiring in 7 days"), listOf("Status board", "Expiration alert", "Search/filter")),
        AvicenexTool("bookmarks", "Bookmarks", "/bookmarks", ToolGroup.Personalization, "Saved ICD/CPT/HCPCS codes from lookup and workflow references.", "Saved-code list for frequently used references.", false, true, listOf("E11.9", "99214", "G0439"), listOf("Saved references", "Quick access")),
        AvicenexTool("profiles", "Specialty Profiles", "/profiles", ToolGroup.Personalization, "Specialty/provider code set context that steers AI prompts and code lookup priority.", "Implemented as Profiles tab with bundled specialty code sets.", false, true, DemoData.codeSets.map { it.name }, listOf("Selected specialty", "Favorite codes", "Prompt context"))
    )

    fun toolsFor(group: ToolGroup) = tools.filter { it.group == group }
}

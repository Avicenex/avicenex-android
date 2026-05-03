package com.avicenex.ai

object DemoData {
    val claims = listOf(
        ReviewClaim(
            id = "CLM-1048",
            owner = "Billing",
            specialty = "Primary Care",
            encounter = "Established follow-up",
            summary = "Diabetes and hypertension follow-up with medication review, A1C ordered, and proposed level 4 E/M.",
            proposedIcd = listOf("E11.9", "I10"),
            proposedCpt = listOf("99214", "83036"),
            profileId = "primary-care-demo",
            status = "Needs documentation"
        ),
        ReviewClaim(
            id = "CLM-1051",
            owner = "MA queue",
            specialty = "Primary Care",
            encounter = "Medicare wellness",
            summary = "Annual wellness visit with problem-focused hypertension medication adjustment on the same date.",
            proposedIcd = listOf("Z00.00", "I10"),
            proposedCpt = listOf("G0439", "99213"),
            profileId = "primary-care-demo",
            status = "Needs coding review"
        ),
        ReviewClaim(
            id = "CLM-1056",
            owner = "Coding",
            specialty = "Endocrinology",
            encounter = "Diabetes complication review",
            summary = "Type 2 diabetes follow-up with neuropathy assessment and medication management.",
            proposedIcd = listOf("E11.40", "E11.42"),
            proposedCpt = listOf("99214"),
            profileId = "endocrinology-demo",
            status = "Payer check needed"
        )
    )

    val codeSets = listOf(
        CodeSet("primary-care-demo", "Primary Care Demo Set", "specialty", "Common ICD-10 references for primary care follow-up, wellness, and chronic condition documentation.", listOf("E11.9", "E11.40", "E11.42", "I10", "J45.909", "M54.50", "Z00.00")),
        CodeSet("endocrinology-demo", "Endocrinology Demo Set", "specialty", "Common ICD-10 references for diabetes and endocrine-focused billing/coding conversations.", listOf("E11.9", "E11.40", "E11.42")),
        CodeSet("urgent-care-demo", "Urgent Care Demo Set", "specialty", "Common ICD-10 references for urgent care style symptoms and episodic visits.", listOf("J45.909", "M54.50", "R07.9", "I10"))
    )

    val icdCodes = listOf(
        IcdCode("E11.9", "Type 2 diabetes mellitus without complications", "Type 2 diabetes without complications", true),
        IcdCode("E11.40", "Type 2 diabetes mellitus with diabetic neuropathy, unspecified", "Type 2 diabetes with neuropathy", true),
        IcdCode("E11.42", "Type 2 diabetes mellitus with diabetic polyneuropathy", "Type 2 diabetes with diabetic polyneuropathy", true),
        IcdCode("I10", "Essential hypertension", "Essential hypertension", true),
        IcdCode("Z00.00", "Encounter for general adult medical examination without abnormal findings", "Adult medical exam without abnormal findings", true),
        IcdCode("J45.909", "Unspecified asthma, uncomplicated", "Unspecified asthma, uncomplicated", true),
        IcdCode("M54.50", "Low back pain, unspecified", "Low back pain", true),
        IcdCode("R07.9", "Chest pain, unspecified", "Chest pain", true)
    )

    val cptCodes = listOf(
        CptHcpcsCode(
            code = "99214",
            codeSystem = "CPT",
            category = "Evaluation and Management",
            subCategory = "Office or outpatient visit",
            plainLanguageLabel = "Established patient office visit, moderate complexity",
            plainLanguageDescription = "Established patient evaluation and management visit commonly associated with moderate medical decision making or a longer time-based encounter.",
            clinicalArea = listOf("Primary Care", "Specialty Care", "Endocrinology", "Cardiology"),
            commonSettings = listOf("Office", "Outpatient clinic", "Telehealth when payer allows"),
            documentationPrompts = listOf("Problems addressed and status", "Medication management", "Data ordered or reviewed", "Risk of complications or management", "Total time when used"),
            modifierHints = listOf("25", "95"),
            riskFlags = listOf("Moderate risk or data must be documented when MDM supports the level.", "Payer downcoding risk increases when assessment and plan are thin."),
            keywords = listOf("established patient", "level 4 office visit", "moderate mdm", "chronic disease", "medication management", "99214")
        ),
        CptHcpcsCode(
            code = "99213",
            codeSystem = "CPT",
            category = "Evaluation and Management",
            subCategory = "Office or outpatient visit",
            plainLanguageLabel = "Established patient office visit, low complexity",
            plainLanguageDescription = "Established patient evaluation and management visit commonly associated with low medical decision making or a moderate short time-based encounter.",
            clinicalArea = listOf("Primary Care", "Specialty Care", "Behavioral Health"),
            commonSettings = listOf("Office", "Outpatient clinic", "Telehealth when payer allows"),
            documentationPrompts = listOf("Established patient status", "Condition status", "Medication changes or management", "Tests ordered or reviewed", "Follow-up plan"),
            modifierHints = listOf("25", "95"),
            riskFlags = listOf("Support E/M level using MDM or time.", "Check telehealth modifier and place-of-service rules by payer."),
            keywords = listOf("established patient", "level 3 office visit", "follow-up", "low mdm", "medication refill", "e/m", "99213")
        ),
        CptHcpcsCode(
            code = "83036",
            codeSystem = "CPT",
            category = "Pathology and Laboratory",
            subCategory = "Chemistry",
            plainLanguageLabel = "Hemoglobin A1C test",
            plainLanguageDescription = "Lab test commonly used to monitor average blood glucose control for diabetes care.",
            clinicalArea = listOf("Primary Care", "Endocrinology"),
            commonSettings = listOf("Office", "Outpatient lab"),
            documentationPrompts = listOf("Diabetes or glycemic monitoring indication", "Order linkage", "Result follow-up plan"),
            modifierHints = emptyList(),
            riskFlags = listOf("Confirm medical necessity and diagnosis support for payer policy."),
            keywords = listOf("a1c", "hemoglobin", "diabetes", "lab", "83036")
        ),
        CptHcpcsCode(
            code = "G0439",
            codeSystem = "HCPCS",
            category = "Preventive / Medicare",
            subCategory = "Annual wellness visit",
            plainLanguageLabel = "Subsequent Medicare annual wellness visit",
            plainLanguageDescription = "Subsequent Medicare annual wellness visit workflow for eligible beneficiaries.",
            clinicalArea = listOf("Primary Care", "Geriatrics"),
            commonSettings = listOf("Office", "Outpatient clinic"),
            documentationPrompts = listOf("Eligibility timing", "Updated health risk assessment", "Updated prevention plan", "Cognitive and functional review when required", "Screening schedule"),
            modifierHints = listOf("25"),
            riskFlags = listOf("Confirm prior AWV timing.", "Problem-oriented E/M on the same day requires separately identifiable documentation."),
            keywords = listOf("annual wellness visit", "awv", "subsequent awv", "medicare wellness", "g0439")
        )
    )
}

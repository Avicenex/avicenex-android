package com.avicenex.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaimRiskScorerTest {
    @Test
    fun demoClaimsProduceCalculatedAssessments() {
        val assessments = DemoData.claims.map { claim ->
            ClaimRiskScorer.assess(
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

        assertEquals(3, assessments.size)
        assertTrue(assessments.all { it.reasons.isNotEmpty() })
        assertTrue(assessments.all { it.score in 0..100 })
        assertTrue(assessments.all { it.readiness == 100 - it.score })
    }

    @Test
    fun missingCodesDriveHighRiskNextAction() {
        val assessment = ClaimRiskScorer.assess(
            ClaimRiskInput(
                summary = "",
                icdCodes = emptyList(),
                cptCodes = emptyList(),
                specialty = null,
                codeSet = null,
                icdReferences = emptyList(),
                cptReferences = emptyList()
            )
        )

        assertEquals(ReviewRisk.High, assessment.level)
        assertEquals(69, assessment.score)
        assertEquals("Add a de-identified encounter summary before coding review.", assessment.nextAction)
    }
}

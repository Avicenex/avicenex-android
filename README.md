# Avicenex AI Android

Native Jetpack Compose MVP for Avicenex AI mobile workflows.

The repo was empty when scaffolded, so this project provides an Android app structure with:

- Pre-bill review queue
- Claim detail with calculated risk panel
- ICD-10-CM and CPT/HCPCS reference sections
- Workflow assistant entry points
- Full original web tool catalog: Chat, Coding Assistant, Pre-Bill Review, Code Lookup, Compare Codes, Cheat Sheet, Prior Auth Lookup, Denial Analyzer, Modifier Lookup, Appeal Letter, E/M Calculator, Batch Validator, CCI Edit Checker, CARC/RARC Lookup, Prior Auth Tracker, Claim Scrubber, Bookmarks, and Profiles
- Specialty/profile code sets

The bundled demo data is derived from the Avicenex web repo reference files. CPT/HCPCS labels remain Avicenex-authored plain-language content, and the compliance reminder stays visible in the review and assistant workflows.

## Verify

```sh
./gradlew test
./gradlew assembleDebug
```

This workspace does not currently include a Gradle wrapper. Install or generate the wrapper before running the commands above.

# Regulatory Standards Catalog

This document defines the core principles of the regulations Coldwater operates under. Use this to verify ADRs and ACs.

## 🇨🇦 PIPEDA (Personal Information Protection and Electronic Documents Act)
*   **Principle 4.4 (Limiting Collection)**: PII must only be collected if necessary. *Coldwater implementation: Sovereign Signal Extraction.*
*   **Principle 4.5 (Limiting Use, Disclosure, and Retention)**: Data must not be used for purposes other than those for which it was collected.
*   **Principle 4.7 (Safeguards)**: PII must be protected by security safeguards. *Coldwater implementation: Device-side AES-GCM vault.*

## 🇪🇺 GDPR (General Data Protection Regulation)
*   **Article 5(1)(c) (Data Minimization)**: Adequate, relevant and limited to what is necessary.
*   **Article 17 (Right to Erasure)**: Users must be able to delete their data. *Coldwater implementation: Cryptographic Shredding (key deletion).*
*   **Article 25 (Privacy by Design)**: Data protection through technology design.

## ⚖️ Law 25 (Quebec)
*   **Default Privacy**: Privacy settings must be at the highest level by default.
*   **Impact Assessment**: Mandatory for any project involving PII.

## 🤖 AIDA (Artificial Intelligence and Data Act - Proposed)
*   **Transparency**: AI-generated trust insights must be explainable.
*   **Bias Mitigation**: Continuous auditing for discriminatory patterns in trust scoring.

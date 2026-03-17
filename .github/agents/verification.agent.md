---
name: Verification Agent
description: Cross-check documentation against actual source code to identify gaps, missing business rules, incomplete validations, and broken cross-domain references
---

# Verification Agent

## Role
Validate documentation completeness and consistency against source code.

## Mandatory Skill-First Rule
- Always use `state-management` for lifecycle updates.
- Always load the language skill selected by state language.
- Do not embed language-specific parsing patterns or framework examples in this agent file.
- Use skill guidance to inspect repositories/DAOs, validations, and state transitions.

## Skill Routing
- `cobol` -> `cobol-analysis`
- `java` -> `java-analysis`
- `typescript` or `javascript` -> `typescript-analysis`
- `vbnet` -> `vbnet-analysis`
- `python` -> `python-analysis` (if available)
- `dotnet` -> `dotnet-analysis` (if available)

## Inputs
- `docs/[MODULE_NAME]-state.json`
- All documentation artifacts
- Source code

## Required Outputs
- `docs/verification/gap-report.md`
- `docs/verification/table-usage-matrix.md`
- `docs/verification/cross-domain-dependencies.md`

## Workflow
1. Verify coordination phase is complete.
2. Set verification phase to `in-progress`.
3. Load language skill.
4. Build table usage matrix and cross-domain dependency report.
5. Verify query-rule coverage, validation coverage, and state transitions.
6. Produce severity-ranked gap report and remediation prompts.
7. Set verification phase to `complete` or `remediation-needed`.

## Severity Model
- `critical`: missing core business rule coverage.
- `high`: undocumented cross-domain dependency.
- `medium`: partial validation/state transition coverage.
- `low`: minor documentation drift.

## Handoff
- If critical/high gaps exist, hand off to targeted remediation runs of discovery/business/technical agents.


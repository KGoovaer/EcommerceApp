---
name: Documentation Pipeline
description: Full documentation pipeline — planning → discovery → business → technical → coordination → verification
on:
  workflow_dispatch:
    inputs:
      module_name:
        description: "Module name for docs folder prefix (e.g. EcommerceApp)"
        required: false
        default: "EcommerceApp"
      start_phase:
        description: "Phase to start from (planning | discovery | business | technical | coordination | verification)"
        required: false
        default: "planning"
      skip_commit:
        description: "Set to 'true' to skip the final git commit"
        required: false
        default: "false"
permissions:
  contents: read
  issues: read
  pull-requests: read
tools:
  bash: true
  github:
    toolsets:
      - default
safe-outputs:
  create-pull-request:
    base-branch: main
    draft: false
    allowed-files:
      - docs/**
---

# Documentation Pipeline Orchestrator

You are a documentation pipeline orchestrator for the **EcommerceApp** Java/J2EE codebase.

Your job is to run six documentation agents **in strict sequence**, writing all output to the `docs/` folder, updating the shared state file after each phase, and committing progress to the repository.

The six phases are:
1. **planning** — detect language, analyse structure, create `docs/$MODULE_NAME-state.json` and `docs/documentation-plan.md`
2. **discovery** — load `java-analysis` skill, produce `docs/discovered-flows.md`, `docs/discovered-domain-concepts.md`, `docs/discovered-components.md`
3. **business** — transform discoveries into `docs/business/` artefacts (use-cases, business processes, BUREQs)
4. **technical** — derive `docs/functional/` artefacts (FUREQs, NFUREQs, functional flows, integration docs)
5. **coordination** — produce `docs/index.md`, `docs/system-overview.md`, traceability matrices in `docs/traceability/`, domain catalogue in `docs/domain/`
6. **verification** — cross-check all docs against source code, write `docs/verification/gap-report.md`, `docs/verification/table-usage-matrix.md`, `docs/verification/cross-domain-dependencies.md`

---

## Setup

Read the input values:
- `MODULE_NAME` = the `module_name` input (default `EcommerceApp`)
- `START_PHASE` = the `start_phase` input (default `planning`)
- `SKIP_COMMIT` = the `skip_commit` input (default `false`)

Create the `docs/` directory if it does not exist.

---

## Agent & Skill Files

Before each phase, read the matching files from the repository:

| Phase | Agent file | Skill file(s) |
|---|---|---|
| planning | `.github/agents/planning-agent.md` | `.github/skills/state-management/SKILL.md` |
| discovery | `.github/agents/discovery.agent.md` | `.github/skills/java-analysis/SKILL.md` + `.github/skills/state-management/SKILL.md` |
| business | `.github/agents/business-documenter.agent.md` | `.github/skills/state-management/SKILL.md` |
| technical | `.github/agents/technical-documenter.agent.md` | `.github/skills/java-analysis/SKILL.md` + `.github/skills/state-management/SKILL.md` |
| coordination | `.github/agents/doc-coordinator.agent.md` | `.github/skills/state-management/SKILL.md` |
| verification | `.github/agents/verification.agent.md` | `.github/skills/java-analysis/SKILL.md` + `.github/skills/state-management/SKILL.md` |

Read ALL of each file's content before you begin the phase — do not rely on memory of their content.

---

## Execution Rules

- Always check `START_PHASE` before beginning. Skip any phase that comes **before** `START_PHASE` in the ordering above.
- After each phase completes, update `docs/$MODULE_NAME-state.json` to mark that phase as `complete` (follow exactly the instructions in `.github/skills/state-management/SKILL.md`).
- If the state file already marks a phase as `complete` AND `START_PHASE` is `planning`, skip that phase automatically.
- Do **not** halt between phases — proceed immediately to the next.
- Write every output file completely; do not truncate.
- Use relative paths inside the `docs/` folder for all cross-references and links.

---

## Phase 1 — Planning

Read `.github/agents/planning-agent.md` and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the planning agent:
- Detect programming language from the repository (look for `pom.xml`, `.java` files, `web.xml`).
- Analyse the top-level directory structure.
- Create `docs/$MODULE_NAME-state.json` with at minimum:
  - `module_name`, `language`, `detected_framework`, `current_phase`, `phase_history`, `artifact_inventory`
- Write `docs/documentation-plan.md` covering all six phases with estimated artefact list.
- Mark planning phase `complete` in the state file.

---

## Phase 2 — Discovery

Read `.github/agents/discovery.agent.md`, `.github/skills/java-analysis/SKILL.md`, and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the discovery agent using the Java analysis skill patterns:
- Identify all servlet entry points (`@WebServlet` annotations) and map them to URLs.
- Trace each URL → Servlet → DAO calls → entity usage.
- Identify all JSP pages and their direct DAO/scriptlet calls.
- Extract domain entities from `com.entity.*`.
- Map the DAO layer (`DAO.java` – `DAO5.java`) and their SQL operations.
- Document the cookie-based auth flow.

Write:
- `docs/discovered-flows.md` — every request flow with entry point, components touched, data accessed
- `docs/discovered-domain-concepts.md` — domain entities and relationships
- `docs/discovered-components.md` — component inventory with dependencies

Mark discovery phase `complete` in the state file.

---

## Phase 3 — Business Documentation

Read `.github/agents/business-documenter.agent.md` and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the business documenter agent:
- For each discovered flow, derive one or more use cases.
- Write use cases to `docs/business/use-cases/UC_NNN.md` (actors, preconditions, main flow, alternatives, postconditions).
- Group flows into business processes; write to `docs/business/processes/BP_NNN.md`.
- Write `docs/business/index.md` as a navigation page.
- Every use case must have at least one BUREQ (Business Requirement).

Mark business phase `complete` in the state file.

---

## Phase 4 — Technical Documentation

Read `.github/agents/technical-documenter.agent.md`, `.github/skills/java-analysis/SKILL.md`, and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the technical documenter agent:
- Derive functional requirements (FUREQ) from BUREQs and UCs; write to `docs/functional/requirements/FUREQ_NNN.md`.
- Derive non-functional requirements (NFUREQ); write to `docs/functional/requirements/NFUREQ_NNN.md`.
- Write technical flows to `docs/functional/flows/FF_NNN.md` — each must reference concrete source files and line numbers.
- Write integration documentation to `docs/functional/integration/` covering the SQLite connection, file upload, and cookie auth.
- Write `docs/functional/index.md`.

Mark technical phase `complete` in the state file.

---

## Phase 5 — Documentation Coordination

Read `.github/agents/doc-coordinator.agent.md` and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the coordinator agent:
- Write `docs/index.md` — top-level navigation linking to all sections.
- Write `docs/system-overview.md` — architecture summary with component diagram (Mermaid).
- Write `docs/domain/domain-concepts-catalog.md` — all domain entities with descriptions and relationships.
- Write `docs/traceability/requirement-matrix.md` — BUREQ → UC → FUREQ mapping table.
- Write `docs/traceability/flow-to-component-map.md` — discovered flow → servlets/DAOs/JSPs.
- Write `docs/traceability/id-registry.md` — master ID list.
- Validate that all IDs referenced in documents exist in the registry.

Mark coordination phase `complete` in the state file.

---

## Phase 6 — Verification

Read `.github/agents/verification.agent.md`, `.github/skills/java-analysis/SKILL.md`, and `.github/skills/state-management/SKILL.md` in full.

Follow every instruction in the verification agent:
- For each DAO method found in source code, check it is referenced in at least one FUREQ or functional flow.
- For each servlet, check it appears in at least one discovered flow and UC.
- Identify any servlet that modifies data but has no auth check documented.
- Write `docs/verification/gap-report.md` — list all undocumented components, missing rules, and broken references.
- Write `docs/verification/table-usage-matrix.md` — which DAOs touch which DB operations (SELECT/INSERT/UPDATE/DELETE) per servlet.
- Write `docs/verification/cross-domain-dependencies.md` — dependencies between servlet groups and DAO groups.

Mark verification phase `complete` in the state file.

---

## Finalisation

After all phases complete (or after the last non-skipped phase):

1. Update `docs/$MODULE_NAME-state.json`:
   - Set `current_phase` to `complete`
   - Set `pipeline_completed_at` to the current ISO-8601 timestamp

2. Unless `SKIP_COMMIT` is `true`, output all written files to the `safe-outputs` pull-request payload so that gh-aw creates a PR with every `docs/` file you have written during this run.

3. Print a summary of what was created:
   - Number of files written per phase
   - Any phases skipped
   - Any gaps or warnings from the verification phase

## Usage

```bash
# Run the full pipeline from scratch
gh workflow run doc-pipeline.yml

# Resume from a specific phase (e.g., after fixing something in business docs)
gh workflow run doc-pipeline.yml -f start_phase=technical

# Dry run — don't commit
gh workflow run doc-pipeline.yml -f skip_commit=true
```

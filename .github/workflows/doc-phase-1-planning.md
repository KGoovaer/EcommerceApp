---
name: Doc Pipeline — 1 Planning
description: Documentation pipeline phase 1 of 6 — language detection, state file creation, and documentation plan
on:
  workflow_dispatch:
    inputs:
      module_name:
        description: "Module name (default: EcommerceApp)"
        required: false
        default: "EcommerceApp"
timeout-minutes: 60
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
    base-branch: master
    draft: true
    allowed-files:
      - docs/**
---

# Phase 1 — Planning

You are the **Planning Agent**. This is the first of six sequential documentation phases for the EcommerceApp codebase.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/planning-agent.md`
2. `.github/skills/state-management/SKILL.md`

Follow every instruction in those files exactly.

## Your specific task

Analyse the EcommerceApp repository structure and produce two output files:

**`docs/EcommerceApp-state.json`** — state file with at minimum these fields:
- `module_name`: `"EcommerceApp"`
- `language`: detected language (should be `"java"`)
- `detected_framework`: detected framework (should be `"servlet-jsp"`)
- `current_phase`: `"planning"`
- `phase_history`: array with planning entry marked `complete`
- `artifact_inventory`: list of expected output artefacts across all 6 phases

**`docs/documentation-plan.md`** — documentation plan covering all six phases:
1. Planning — state file, plan
2. Discovery — flows, domain concepts, components
3. Business — use cases (`UC_*.md`), business processes (`BP_*.md`), BUREQs
4. Technical — functional requirements (`FUREQ_*.md`, `NFUREQ_*.md`), flows (`FF_*.md`), integration docs
5. Coordination — index, system overview, traceability matrices, domain catalogue
6. Verification — gap report, table usage matrix, cross-domain dependencies

## Detection hints

- `EcommerceApp/pom.xml` — Maven, Servlet 3.0, SQLite
- `EcommerceApp/src/main/java/com/servlet/` — 19+ servlet classes with `@WebServlet`
- `EcommerceApp/src/main/java/com/dao/` — DAO.java through DAO5.java (plain JDBC)
- `EcommerceApp/src/main/java/com/entity/` — 14 entity beans
- `EcommerceApp/src/main/webapp/*.jsp` — 60+ JSP pages

## Output

Mark planning phase `complete` in `docs/EcommerceApp-state.json` before finishing.

Both files will be included in the documentation pipeline pull request. When creating the pull request, use the title **`docs: EcommerceApp documentation pipeline`** — this exact title is how phases 2–6 locate the correct PR and branch.

## Handoff

Once you have pushed the two output files and the pull request is created, the pipeline continues automatically — phase 2 (Discovery) is triggered by the `pull_request: opened` event on the new PR. No further action is needed.

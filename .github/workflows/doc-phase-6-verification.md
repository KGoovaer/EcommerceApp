---
name: Doc Pipeline — 6 Verification
description: Documentation pipeline phase 6 of 6 — cross-check all documentation against source code
on:
  pull_request:
    types: [opened, synchronize]
    branches: [master]
    paths:
      - docs/EcommerceApp-state.json
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
  push-to-pull-request-branch:
    branch: "*"
    allowed-files:
      - docs/**
---

# Phase 6 — Verification

You are the **Verification Agent**. This is the final phase (6 of 6) of the documentation pipeline.

## Guard check

First, run:

```bash
if [ -n "${GITHUB_HEAD_REF:-}" ]; then
  BRANCH="$GITHUB_HEAD_REF"
else
  BRANCH=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json headRefName --jq '.[0].headRefName' 2>/dev/null || echo "")
fi
echo "Syncing docs from branch: ${BRANCH}"
[ -n "$BRANCH" ] && git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ || echo "Warning: could not sync docs"
```

Read `docs/EcommerceApp-state.json`. Abort if:
- Coordination phase is not marked `complete` → print "Phase 5 (coordination) is not complete — aborting." and stop.
- Verification phase is already `complete` or `in-progress` → print "Phase 6 (verification) already done or running — aborting." and stop.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/verification.agent.md`
2. `.github/skills/java-analysis/SKILL.md`
3. `.github/skills/state-management/SKILL.md`
4. `docs/traceability/id-registry.md`
5. `docs/traceability/requirement-matrix.md`
6. `docs/traceability/flow-to-component-map.md`
7. `docs/functional/index.md`

Follow every instruction in those files exactly.

## Your specific task

Cross-check all generated documentation against the actual source code.

### Servlet coverage check

For each `.java` file in `EcommerceApp/src/main/java/com/servlet/`:
- Verify it appears in at least one entry in `docs/traceability/flow-to-component-map.md`
- Verify it is referenced in at least one use case in `docs/business/use-cases/`
- Check whether it performs data-modifying operations (INSERT/UPDATE/DELETE via DAO) without checking an auth cookie — flag as a security gap if so

### DAO method coverage check

For each public method in `DAO.java`, `DAO2.java`, `DAO3.java`, `DAO4.java`, `DAO5.java`:
- Verify it is referenced in at least one FUREQ or functional flow (`docs/functional/flows/` or `docs/functional/requirements/`)
- Record any DAO methods with no documentation reference

### ID consistency check

Read `docs/traceability/id-registry.md` and scan all `.md` files under `docs/` for ID references (patterns like `BUREQ-\d+`, `UC_\d+`, `FUREQ-\d+`, `FF-\d+`, `BP-\d+`). Report any referenced IDs not in the registry.

## Output files

**`docs/verification/gap-report.md`**:
- Section: Undocumented servlets (list with file name and reason)
- Section: Undocumented DAO methods (list with class, method name)
- Section: Security gaps (servlets that write data without auth check, not already noted in NFUREQs)
- Section: Broken ID references (ID, file where referenced, reason it's broken)
- Section: Summary — counts of gaps found, overall documentation coverage % estimate

**`docs/verification/table-usage-matrix.md`** — table with rows = servlet classes, columns = DAO methods. Cell = operation type (SELECT / INSERT / UPDATE / DELETE / —). Infer from DAO method names and the flows in `docs/traceability/flow-to-component-map.md`.

**`docs/verification/cross-domain-dependencies.md`** — table showing which servlet groups depend on which DAO groups:
- Group servlets by functional area (product browsing, cart, orders, auth, admin)
- Group DAOs by the entities they manage
- Mark dependencies

## Finalisation

Update `docs/EcommerceApp-state.json`:
- Mark verification phase `complete`
- Set `current_phase` to `"complete"`
- Set `pipeline_completed_at` to current ISO-8601 timestamp
- Add all three verification files to `artifact_inventory`

Update `docs/index.md`: add links to the three verification files under a "Verification" section.

Print a final summary:
- Total files written across all phases (read `artifact_inventory` from state file)
- Number of gaps found (from gap-report)
- Overall pipeline status: COMPLETE

All output files will be pushed to the documentation pipeline PR.

## Handoff

After pushing verification outputs, evaluate the gap report:

- **If the gap report contains Critical or High severity gaps**: dispatch `doc-phase-2-discovery` to run targeted remediation. Pass `module_name` as usual. The discovery agent will re-run on the flows identified in the gap report.
- **If no Critical or High gaps are found**: the pipeline is complete. Print a final summary: total artifacts, gap count, severity breakdown, and overall status `COMPLETE`.

- **If Critical or High gaps are found**: document them in the gap report only. Do not re-trigger phases — a human should review and re-run the pipeline manually.

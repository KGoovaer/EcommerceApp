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
      pr_number:
        description: "PR number to push docs to"
        required: false
        default: ""
      branch:
        description: "PR head branch to push docs to"
        required: false
        default: ""
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
    labels: [ai-docs-requested]
    allowed-files:
      - docs/**
  dispatch-workflow:
    workflows: [doc-phase-2-discovery]
    max: 1
---

# Phase 1 — Planning

You are the **Planning Agent**. This is the first of six sequential documentation phases for the EcommerceApp codebase.

## Guard check

Run this to resolve the PR context you will push docs to:

```bash
PR_NUMBER="${{ inputs.pr_number }}"
BRANCH="${{ inputs.branch }}"

if [ -z "$PR_NUMBER" ] || [ -z "$BRANCH" ]; then
  echo "ERROR: pr_number and branch inputs are required for phase 1." >&2
  exit 1
fi

echo "PR_NUMBER=${PR_NUMBER}"
echo "BRANCH=${BRANCH}"
git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ 2>/dev/null || true
```

Abort if planning phase is already `complete` or `in-progress` in `docs/EcommerceApp-state.json` (if the file already exists) → print "Phase 1 (planning) already done or running — aborting." and stop.

**Important**: When calling `push_to_pull_request_branch`, always pass `pull_request_number: <PR_NUMBER>` using the number printed above.

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

Both files (`docs/EcommerceApp-state.json` and `docs/documentation-plan.md`) will be pushed to the existing PR branch `<BRANCH>` (do **not** create a new pull request — one already exists).

## Handoff

Once you have pushed the two output files, immediately dispatch the next phase, passing the PR context forward:

```
dispatch_workflow("doc-phase-2-discovery", inputs: {module_name: "EcommerceApp", pr_number: <PR_NUMBER>, branch: "<BRANCH>"})
```

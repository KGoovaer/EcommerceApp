---
name: Doc Pipeline — 4 Technical
description: Documentation pipeline phase 4 of 6 — derive functional and technical requirements from business docs
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

# Phase 4 — Technical Documentation

You are the **Technical Documenter Agent**. This is phase 4 of 6 in the documentation pipeline.

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
- Business phase is not marked `complete` → print "Phase 3 (business) is not complete — aborting." and stop.
- Technical phase is already `complete` or `in-progress` → print "Phase 4 (technical) already done or running — aborting." and stop.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/technical-documenter.agent.md`
2. `.github/skills/java-analysis/SKILL.md`
3. `.github/skills/state-management/SKILL.md`
4. `docs/business/index.md`
5. A representative sample of `docs/business/use-cases/` (all UC files)
6. `docs/discovered-components.md`
7. `docs/discovered-flows.md`

Follow every instruction in those files exactly.

## Your specific task

Convert business documentation into functional and technical documentation.

### Functional requirements (FUREQ)

For each BUREQ identified in `docs/business/index.md`, derive one or more functional requirements. Write each to `docs/functional/requirements/FUREQ_NNN.md`:
- **ID** (e.g. `FUREQ-001`)
- **Title**
- **Traces to**: BUREQ ID(s) and UC ID(s)
- **Description**: implementation-level statement of what the system must do
- **Acceptance criteria**: testable conditions
- **Involved components**: servlet(s), DAO(s), entity(ies)

### Non-functional requirements (NFUREQ)

Derive system-level NFRs. Write each to `docs/functional/requirements/NFUREQ_NNN.md`:
- Security (cookie auth, no session, no CSRF protection — document as-is)
- Data integrity (PreparedStatement usage)
- File upload constraints (extension validation, path restrictions)
- Concurrency note (static Connection — not thread-safe)

### Technical flows

For each entry in `docs/discovered-flows.md`, write a technical flow file at `docs/functional/flows/FF_NNN.md`:
- **ID** and **Title**
- **Trigger**: HTTP method + URL
- **Sequence**: ordered steps that reference concrete source file names and method names
- **Data flow**: input parameters → DAO query → entity → JSP output
- **Auth check**: which cookie is validated and how
- **Error path**: what happens on failure (redirect target)

### Integration documentation

Write these three files:

**`docs/functional/integration/database.md`** — SQLite connection via `DBConnect.getConn()`, connection lifecycle, thread-safety warning, tables inferred from DAO SQL.

**`docs/functional/integration/file-upload.md`** — `MyUtilities.UploadFile()` behaviour, accepted extensions, hardcoded upload path configuration, path traversal risk note.

**`docs/functional/integration/auth-cookies.md`** — `cname` (customer) and `tname` (admin) cookies, maxAge values, flash-cookie pattern (maxAge=10), how servlets read/write them.

### Index

Write `docs/functional/index.md`:
- Table of all FUREQs (ID, title, BUREQ traces)
- Table of all NFUREQs (ID, title)
- Table of all technical flows (FF ID, title, servlet, URL)

## State update

Update `docs/EcommerceApp-state.json`: mark technical phase `complete` and add all new files to `artifact_inventory`.

All output files will be pushed to the documentation pipeline PR.

## Handoff

## Handoff

Once you have pushed all technical documentation outputs to the PR, the pipeline continues automatically — phase 5 (Coordination) is triggered by the `pull_request: synchronize` event. No further action is needed.

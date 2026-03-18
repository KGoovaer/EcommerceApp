---
name: Doc Pipeline — 5 Coordination
description: Documentation pipeline phase 5 of 6 — build indexes, traceability matrices, and system overview
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
  push-to-pull-request-branch:
    branch: "*"
    allowed-files:
      - docs/**
  dispatch-workflow:
    workflows: [doc-phase-6-verification]
    max: 1
---

# Phase 5 — Documentation Coordination

You are the **Documentation Coordinator Agent**. This is phase 5 of 6 in the documentation pipeline.

## Guard check

First, run:

```bash
BRANCH=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json headRefName --jq '.[0].headRefName' 2>/dev/null)
PR_NUM=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json number --jq '.[0].number' 2>/dev/null)
echo "Pipeline PR: #${PR_NUM} on branch ${BRANCH}"
[ -n "$BRANCH" ] && git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ || echo "Warning: could not fetch docs from branch ${BRANCH}"
```

Note the PR number from the output above — you will need to pass it as `pull_request_number` when using the push tool later.

Read `docs/EcommerceApp-state.json`. If technical phase is not marked `complete`, print "Phase 4 (technical) is not complete — aborting." and stop immediately.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/doc-coordinator.agent.md`
2. `.github/skills/state-management/SKILL.md`
3. `docs/EcommerceApp-state.json`
4. `docs/business/index.md`
5. `docs/functional/index.md`
6. `docs/discovered-components.md`

Follow every instruction in those files exactly.

## Your specific task

Build the cross-cutting navigation, overview, and traceability layer.

### Top-level entry point

**`docs/index.md`** — the main documentation landing page:
- Project description (EcommerceApp — Java/J2EE online electronics shop)
- Documentation status: phases completed, files generated, date
- Navigation table linking to every section:
  - Discovery (`discovered-flows.md`, `discovered-domain-concepts.md`, `discovered-components.md`)
  - Business (`business/index.md`)
  - Functional (`functional/index.md`)
  - Domain (`domain/domain-concepts-catalog.md`)
  - Traceability (`traceability/requirement-matrix.md`, etc.)
  - Verification (link — to be added by phase 6)

### System overview

**`docs/system-overview.md`**:
- Architecture summary: Browser → JSP/Servlet → DAO → SQLite
- Mermaid component diagram showing: Browser, JSP layer, Servlet layer, DAO layer, DBConnect, SQLite
- Key design decisions: cookie auth (no session), static Connection, PreparedStatement SQL
- Technology stack table: Java, Servlet 3.0, JSP, SQLite, Maven, Tomcat 8+
- Security baseline note: known limitations (plaintext passwords, no CSRF, forgeable cookies)

### Domain concepts catalogue

**`docs/domain/domain-concepts-catalog.md`** — for each entity class (`brand`, `cart`, `category`, `contactus`, `customer`, `laptop`, `mobile`, `order_details`, `orders`, `Product`, `tv`, `usermaster`, `viewlist`, `watch`):
- Description in business terms
- Key fields
- Relationships to other entities
- Which DAO(s) manage it
- Whether it has guest, customer, and/or admin visibility

### Traceability matrices

**`docs/traceability/requirement-matrix.md`** — table: BUREQ ID | UC ID | FUREQ ID | Status

**`docs/traceability/flow-to-component-map.md`** — table: Flow (discovered-flows entry) | Servlet | URL | DAO(s) | Entities | JSP(s)

**`docs/traceability/id-registry.md`** — master list of all IDs in use:
- All BUREQ-NNN IDs
- All UC_NNN IDs
- All FUREQ-NNN and NFUREQ-NNN IDs
- All FF_NNN IDs
- All BP_NNN IDs

Validate: every ID referenced in any document must appear in this registry. List any broken references as warnings at the end of the file.

## State update

Update `docs/EcommerceApp-state.json`: mark coordination phase `complete` and add all new files to `artifact_inventory`.

All output files will be pushed to the documentation pipeline PR. Pass `pull_request_number: <PR_NUM>` (the number from the guard check) when using the push tool.

## Handoff

After pushing all coordination outputs, immediately dispatch the next phase:

```
dispatch_workflow("doc-phase-6-verification", inputs: {module_name: "<same module_name input>"})
```

Pass the same `module_name` value that was provided to this workflow.

---
name: Doc Pipeline — 2 Discovery
description: Documentation pipeline phase 2 of 6 — Java code analysis, servlet/DAO/entity discovery
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
    workflows: [doc-phase-3-business]
    max: 1
---

# Phase 2 — Discovery

You are the **Discovery Agent**. This is phase 2 of 6 in the documentation pipeline.

## Guard check

First, run:

```bash
BRANCH=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json headRefName --jq '.[0].headRefName' 2>/dev/null)
PR_NUM=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json number --jq '.[0].number' 2>/dev/null)
echo "Pipeline PR: #${PR_NUM} on branch ${BRANCH}"
[ -n "$BRANCH" ] && git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ || echo "Warning: could not fetch docs from branch ${BRANCH}"
```

Note the PR number from the output above — you will need to pass it as `pull_request_number` when using the push tool later.

Read `docs/EcommerceApp-state.json`. If it does not exist, or if planning phase is not marked `complete`, print "Phase 1 (planning) is not complete — aborting." and stop immediately.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/discovery.agent.md`
2. `.github/skills/java-analysis/SKILL.md`
3. `.github/skills/state-management/SKILL.md`
4. `docs/EcommerceApp-state.json`

Follow every instruction in those files exactly.

## Your specific task

Analyse the EcommerceApp source code and produce three discovery documents.

### Servlets to analyse

Read every `.java` file in `EcommerceApp/src/main/java/com/servlet/`. For each servlet:
- Extract the `@WebServlet` URL pattern
- Identify which HTTP methods it handles (`doGet` / `doPost`)
- Identify which DAO classes and methods it calls
- Identify which entity classes it uses
- Identify whether it reads/writes auth cookies (`cname`, `tname`)

### DAOs to analyse

Read these files completely:
- `EcommerceApp/src/main/java/com/dao/DAO.java`
- `EcommerceApp/src/main/java/com/dao/DAO2.java`
- `EcommerceApp/src/main/java/com/dao/DAO3.java`
- `EcommerceApp/src/main/java/com/dao/DAO4.java`
- `EcommerceApp/src/main/java/com/dao/DAO5.java`

For each DAO method: name, SQL operation type (SELECT/INSERT/UPDATE/DELETE), tables touched, parameters, return type.

### Entities to analyse

Read all files in `EcommerceApp/src/main/java/com/entity/`. List each entity's fields.

### JSP pages

List all `.jsp` files in `EcommerceApp/src/main/webapp/`. Group them by the tripling pattern (guest / customer `c` / admin `a` suffix).

## Output files

**`docs/discovered-flows.md`** — one section per servlet URL. Each section: URL, HTTP method, auth required (yes/no), servlets involved, DAOs called, entities used, JSPs rendered, brief description of the flow.

**`docs/discovered-domain-concepts.md`** — one section per entity class. Fields, relationships to other entities, which DAOs manage it.

**`docs/discovered-components.md`** — a component inventory table:
- Servlets: name, URL, purpose, auth
- DAOs: name, methods, tables
- Entities: name, fields
- JSPs: name, variant (guest/customer/admin), linked servlet/DAO

## State update

Update `docs/EcommerceApp-state.json`: mark discovery phase `complete` and add the three output files to `artifact_inventory`.

All output files will be pushed to the documentation pipeline PR. Pass `pull_request_number: <PR_NUM>` (the number from the guard check) when using the push tool.

## Handoff

After pushing all discovery outputs, immediately dispatch the next phase:

```
dispatch_workflow("doc-phase-3-business", inputs: {module_name: "<same module_name input>"})
```

Pass the same `module_name` value that was provided to this workflow.

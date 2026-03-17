---
name: Doc Pipeline — 3 Business
description: Documentation pipeline phase 3 of 6 — transform discoveries into business use cases and processes
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
    workflows: [doc-phase-4-technical]
    max: 1
---

# Phase 3 — Business Documentation

You are the **Business Documenter Agent**. This is phase 3 of 6 in the documentation pipeline.

## Guard check

First, run:

```bash
git fetch origin docs/pipeline-EcommerceApp 2>/dev/null && git checkout FETCH_HEAD -- docs/ 2>/dev/null || true
```

Read `docs/EcommerceApp-state.json`. If discovery phase is not marked `complete`, print "Phase 2 (discovery) is not complete — aborting." and stop immediately.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/business-documenter.agent.md`
2. `.github/skills/state-management/SKILL.md`
3. `docs/discovered-flows.md`
4. `docs/discovered-domain-concepts.md`
5. `docs/discovered-components.md`

Follow every instruction in those files exactly.

## Your specific task

Transform the discovery artefacts into stakeholder-friendly business documentation.

### Use cases

For each distinct user action in `docs/discovered-flows.md`, write one use case file at `docs/business/use-cases/UC_NNN.md` (NNN = zero-padded number, e.g. `UC_001.md`).

Each use case file must contain:
- **ID** and **Title**
- **Actors** (e.g. Guest, Customer, Admin)
- **Preconditions**
- **Main flow** (numbered steps)
- **Alternative flows** (what happens on errors or edge cases)
- **Postconditions**
- **Business Requirements (BUREQs)** — at least one testable statement per UC, formatted as `BUREQ-NNN: <statement>`

### Business processes

Group related use cases into business processes. Write each process to `docs/business/processes/BP_NNN.md`:
- **ID** and **Title**
- **Description** in business language
- **Included Use Cases** (list UC IDs)
- **Business Rules** — constraints or conditions that apply to this process

Suggested process groupings (adjust based on discovered flows):
- Product Browsing (guest and customer views)
- Shopping Cart Management
- Order Placement and Payment
- Customer Account Management
- Admin Product Management
- Admin Order Management
- Admin Customer Management

### Index

Write `docs/business/index.md`:
- Table of all use cases (ID, title, actors, process)
- Table of all business processes (ID, title, UC count)
- Table of all BUREQs (ID, statement, UC reference)

## State update

Update `docs/EcommerceApp-state.json`: mark business phase `complete` and add all new files to `artifact_inventory`.

All output files will be pushed to branch `docs/pipeline-EcommerceApp`.

## Handoff

After pushing all business documentation outputs, immediately dispatch the next phase:

```
dispatch_workflow("doc-phase-4-technical", inputs: {module_name: "<same module_name input>"})
```

Pass the same `module_name` value that was provided to this workflow.

---
description: Transform technical discoveries into business documentation
on:
  workflow_dispatch:
    inputs:
      pr_number:
        description: "PR number to push documentation to"
        required: true
        type: string
      branch:
        description: "PR branch name"
        required: true
        type: string
      scope:
        description: "Scoped instruction for targeted re-run from verification"
        required: false
        type: string
permissions:
  contents: read
  pull-requests: read
  issues: read
  actions: read
checkout:
  fetch: ["*"]
  fetch-depth: 0
tools:
  github:
    toolsets: [default]
safe-outputs:
  push-to-pull-request-branch:
    target: "*"
    labels: [ai-docs]
  dispatch-workflow:
    workflows: [doc-technical-documenter]
    max: 1
timeout-minutes: 30
---

# Business Documenter — Documentation Pipeline

You are running as part of an automated documentation pipeline inside GitHub Actions.

## Context

- PR number: `${{ github.event.inputs.pr_number }}`
- Branch: `${{ github.event.inputs.branch }}`
- Scoped instruction: `${{ github.event.inputs.scope }}`

## Instructions

1. **Check out the PR branch** to access previous artifacts:
   ```bash
   git checkout ${{ github.event.inputs.branch }}
   ```

2. **Read your agent instructions** from `.github/agents/business-documenter.agent.md` — follow the role, responsibilities, and quality gates defined there.

3. **Read the state file** (`docs/*-state.json`) and verify the discovery phase is complete.

4. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.

5. **If a scoped instruction is provided**, focus on the specified areas only. Otherwise, process all discovered artifacts.

6. **Execute business documentation** following your agent instructions:
   - Read discovery artifacts:
     - `docs/discovered-flows.md`
     - `docs/discovered-domain-concepts.md`
     - `docs/discovered-components.md`
   - Create use cases: `docs/business/use-cases/UC_*.md`
   - Create business processes: `docs/business/processes/BP_*.md`
   - Create business overview: `docs/business/overview/*.md`
   - Create business index: `docs/business/index.md`
   - Every use case must have: actors, preconditions, main flow, alternative flows
   - Every BUREQ must be testable and linked to use cases

7. **Update the state file** — set business phase to `complete`, advance `currentPhase` to `technical`.

8. **Push artifacts** via `push_to_pull_request_branch` (target PR: `${{ github.event.inputs.pr_number }}`).

9. **Dispatch the next agent**: `doc-technical-documenter` with inputs:
   ```json
   { "pr_number": "${{ github.event.inputs.pr_number }}", "branch": "${{ github.event.inputs.branch }}" }
   ```

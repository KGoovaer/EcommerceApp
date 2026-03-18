---
description: Create functional and technical documentation from business requirements
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
    workflows: [doc-coordinator]
    max: 1
timeout-minutes: 30
---

# Technical Documenter — Documentation Pipeline

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

2. **Read your agent instructions** from `.github/agents/technical-documenter.agent.md` — follow the role, responsibilities, and quality gates defined there.

3. **Read the state file** (`docs/*-state.json`) and verify the business phase is complete.

4. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.

5. **Load the language skill** based on the state file language (e.g., `.github/skills/java-analysis/SKILL.md`).

6. **If a scoped instruction is provided**, focus on the specified areas. Otherwise, process all business documentation.

7. **Execute technical documentation** following your agent instructions:
   - Read business docs: `docs/business/use-cases/UC_*.md`, `docs/business/processes/BP_*.md`
   - Derive FUREQs from BUREQs: `docs/functional/requirements/FUREQ_*.md`
   - Document NFUREQs: `docs/functional/requirements/NFUREQ_*.md`
   - Document functional flows: `docs/functional/flows/FF_*.md`
   - Document integration points: `docs/functional/integration/*.md`
   - Create functional index: `docs/functional/index.md`
   - Every FUREQ must trace to at least one BUREQ and UC
   - Every technical flow must reference concrete code locations

8. **Update the state file** — set technical phase to `complete`, advance `currentPhase` to `coordination`.

9. **Push artifacts** via `push_to_pull_request_branch` (target PR: `${{ github.event.inputs.pr_number }}`).

10. **Dispatch the next agent**: `doc-coordinator` with inputs:
   ```json
   { "pr_number": "${{ github.event.inputs.pr_number }}", "branch": "${{ github.event.inputs.branch }}" }
   ```

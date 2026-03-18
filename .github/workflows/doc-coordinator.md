---
description: Coordinate documentation structure, consistency, and traceability
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
    workflows: [doc-verification-agent]
    max: 1
timeout-minutes: 30
---

# Documentation Coordinator — Documentation Pipeline

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

2. **Read your agent instructions** from `.github/agents/doc-coordinator.agent.md` — follow the role, responsibilities, and quality gates defined there.

3. **Read the state file** (`docs/*-state.json`) and verify the technical phase is complete.

4. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.

5. **If a scoped instruction is provided**, focus on the specified structure/consistency areas. Otherwise, coordinate all documentation.

6. **Execute coordination** following your agent instructions:
   - Validate directory structure and artifact presence
   - Create master landing page: `docs/index.md`
   - Create system overview: `docs/system-overview.md`
   - Build domain concepts catalog: `docs/domain/domain-concepts-catalog.md`
   - Build traceability matrix: `docs/traceability/requirement-matrix.md`
   - Build flow-to-component map: `docs/traceability/flow-to-component-map.md`
   - Build ID registry: `docs/traceability/id-registry.md`
   - Validate all cross-references and report broken links
   - Ensure IDs are unique and consistently referenced
   - Verify traceability chain: BUREQ → UC → FUREQ → flow/component

7. **Update the state file** — set coordination phase to `complete`, advance `currentPhase` to `verification`.

8. **Push artifacts** via `push_to_pull_request_branch` (target PR: `${{ github.event.inputs.pr_number }}`).

9. **Dispatch the next agent**: `doc-verification-agent` with inputs:
   ```json
   { "pr_number": "${{ github.event.inputs.pr_number }}", "branch": "${{ github.event.inputs.branch }}" }
   ```

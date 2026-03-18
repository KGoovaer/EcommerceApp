---
description: Discover flows, components, and domain concepts from source code
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
    workflows: [doc-business-documenter]
    max: 1
timeout-minutes: 45
---

# Discovery Agent — Documentation Pipeline

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

2. **Read your agent instructions** from `.github/agents/discovery.agent.md` — follow the role, responsibilities, and quality gates defined there.

3. **Read the state file** (`docs/*-state.json`) to get the module name, language, and current phase.

4. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.

5. **Load the language skill** based on the detected language in the state file (e.g., `.github/skills/java-analysis/SKILL.md` for Java).

6. **If a scoped instruction is provided** (`${{ github.event.inputs.scope }}`), focus your discovery on the specified areas only. Otherwise, perform full discovery as described in your agent instructions.

7. **Execute discovery** following your agent instructions and the loaded language skill:
   - Detect entry points (servlets, controllers, scheduled jobs, event handlers)
   - Trace execution flows with triggers, paths, decisions, integrations, and outcomes
   - Extract domain concepts from the codebase
   - Build a component inventory with responsibilities and dependencies
   - Perform deep data access query analysis — document ALL filtering conditions, JOINs, aggregations
   - Map cross-domain table usage
   - Output to:
     - `docs/discovered-flows.md`
     - `docs/discovered-domain-concepts.md`
     - `docs/discovered-components.md`

8. **Update the state file** — set discovery phase to `complete`, advance `currentPhase` to `business`.

9. **Push artifacts** to the PR branch via `push_to_pull_request_branch` (target PR: `${{ github.event.inputs.pr_number }}`).

10. **Dispatch the next agent**: `doc-business-documenter` with inputs:
   ```json
   { "pr_number": "${{ github.event.inputs.pr_number }}", "branch": "${{ github.event.inputs.branch }}" }
   ```

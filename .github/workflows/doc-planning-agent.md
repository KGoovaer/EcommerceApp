---
description: Analyze codebase, detect language, create state file and documentation plan
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
    workflows: [doc-discovery-agent]
    max: 1
timeout-minutes: 30
---

# Planning Agent — Documentation Pipeline

You are the first agent in an automated documentation generation pipeline running inside a GitHub Actions workflow.

## Context

- PR number: `${{ github.event.inputs.pr_number }}`
- Branch: `${{ github.event.inputs.branch }}`
- Scoped instruction: `${{ github.event.inputs.scope }}`

## Instructions

1. **Read your detailed agent instructions** from `.github/agents/planning-agent.md` — follow them precisely.
2. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.
3. **If dispatched for re-run** (workflow_dispatch), check out the PR branch first:
   ```bash
   git checkout <branch>
   ```
   Then follow the scoped instruction if one was provided.
4. **Execute the planning workflow** as described in your agent instructions:
   - Detect the programming language from the codebase
   - Analyze the project structure (source directories, packages, servlets, JSPs, etc.)
   - Identify business domains and group related functionality
   - Create the state file at `docs/[MODULE_NAME]-state.json`
   - Generate the documentation plan at `docs/documentation-plan.md`
   - Define batch boundaries for the discovery phase
   - Define completeness criteria for the verification agent

5. **Push your artifacts** to the PR branch using the `push_to_pull_request_branch` safe output. Specify the PR number you determined above.

6. **Dispatch the next agent** by calling `dispatch_workflow` with:
   - `workflow_name`: `doc-discovery-agent`
   - `inputs`: `{ "pr_number": "<PR_NUMBER>", "branch": "<BRANCH>" }`

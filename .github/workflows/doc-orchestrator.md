---
description: Start the documentation pipeline when a PR is labeled ai-docs-requested
on:
  pull_request:
    types: [labeled]
if: "${{ github.event.label.name == 'ai-docs-requested' }}"
permissions:
  contents: read
  pull-requests: read
  issues: read
  actions: read
safe-outputs:
  dispatch-workflow:
    workflows: [doc-planning-agent]
    max: 1
  add-labels:
    allowed: [ai-docs]
timeout-minutes: 5
---

# Documentation Pipeline Orchestrator

A pull request has been labeled `ai-docs-requested`, requesting AI-powered documentation generation.

## Your Task

1. **Add the `ai-docs` label** to PR #${{ github.event.pull_request.number }}. This label is required for downstream agents to push documentation artifacts to the PR branch.
2. **Dispatch the `doc-planning-agent` workflow** to begin the documentation pipeline. Pass these inputs:
   - `pr_number`: `${{ github.event.pull_request.number }}`
   - `branch`: Use the GitHub tool to look up PR #${{ github.event.pull_request.number }} and get the head branch name.

The pipeline proceeds automatically through these phases:
planning → discovery → business documentation → technical documentation → coordination → verification

Do not perform any code analysis yourself. Your only job is to add the label and start the chain.

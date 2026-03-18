---
description: Verify documentation against source code and re-dispatch agents for gaps
on:
  workflow_dispatch:
    inputs:
      pr_number:
        description: "PR number to verify documentation for"
        required: true
        type: string
      branch:
        description: "PR branch name"
        required: true
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
    workflows: [doc-discovery-agent, doc-business-documenter, doc-technical-documenter, doc-coordinator]
    max: 3
  add-comment:
    target: "*"
    max: 1
  add-labels:
    allowed: [docs-verified, docs-incomplete, docs-retry-1, docs-retry-2, docs-retry-3]
    max: 3
  remove-labels:
    allowed: [docs-incomplete, ai-docs-requested]
    max: 2
  create-agent-session:
  missing-data:
    create-issue: true
    labels: [docs-missing-data]
timeout-minutes: 45
---

# Verification Agent — Documentation Pipeline

You are the final quality gate in the documentation pipeline. Cross-check all documentation against the actual source code, identify gaps, and either approve the documentation or re-dispatch agents to fix incomplete areas.

## Context

- PR number: `${{ github.event.inputs.pr_number }}`
- Branch: `${{ github.event.inputs.branch }}`

## Loop Guard — Check Before Running

Before performing verification, check the PR labels for retry count:

1. Use the GitHub tool to read labels on PR #`${{ github.event.inputs.pr_number }}`
2. **If the PR has label `docs-retry-3`** — this is the final attempt. Do NOT re-dispatch any agents. Instead:
   - Use the `missing_data` safe output to report permanent gaps that could not be resolved
   - Add a comment to the PR listing all unresolved gaps with severity
   - Add the `docs-incomplete` label
   - Exit with `noop`
3. **If the PR has `docs-retry-2`** and you need to re-dispatch → add `docs-retry-3`
4. **If the PR has `docs-retry-1`** and you need to re-dispatch → add `docs-retry-2`
5. **If NO retry labels exist** and you need to re-dispatch → add `docs-retry-1`

## Instructions

1. **Check out the PR branch**:
   ```bash
   git checkout ${{ github.event.inputs.branch }}
   ```

2. **Read the state file** (`docs/*-state.json`) and verify the coordination phase is complete.

3. **Read your agent instructions** from `.github/agents/verification.agent.md` — follow the role, severity model, and verification tasks defined there.

4. **Read the state management skill** from `.github/skills/state-management/SKILL.md`.

5. **Load the language skill** based on the state file language (e.g., `.github/skills/java-analysis/SKILL.md`).

6. **Execute verification** following your agent instructions:
   - Build table usage matrix: map every database table to flows that read/write it
   - Repository query deep analysis: compare WHERE conditions against documented business rules
   - Validation completeness check: enumerate all validations, verify each has documented outcomes
   - Cross-domain dependency verification: verify bidirectional documentation links
   - Entity state machine verification: map all status transitions, verify all are documented
   - Generate consolidated gap report with severity classification (critical, high, medium, low)

7. **Push verification artifacts** to the PR branch:
   - `docs/verification/gap-report.md`
   - `docs/verification/table-usage-matrix.md`
   - `docs/verification/cross-domain-dependencies.md`

8. **Evaluate results and take action:**

---

### If Verification PASSES (no critical or high gaps)

- Add a comment to PR #`${{ github.event.inputs.pr_number }}`:
  > ✅ Documentation verification passed. All critical areas are covered.
- Add the `docs-verified` label
- Remove the `docs-incomplete` label if present
- Update the state file: set verification phase to `complete`
- Exit with `noop` — the documentation pipeline is complete

### If Verification FAILS (critical or high gaps found)

- Add a comment to the PR with a structured gap report:
  - Which agent produced incomplete documentation (discovery, business-documenter, technical-documenter, doc-coordinator)
  - Specific modules, flows, or requirements that are incomplete
  - Severity of each gap (critical / high / medium / low)
- Add the `docs-incomplete` label
- Add the appropriate retry label (`docs-retry-1`, `docs-retry-2`, or `docs-retry-3`)
- **Re-dispatch the earliest agent in the chain that has gaps.** Because each agent dispatches the next, fixing upstream cascades downstream automatically. Provide a `scope` input describing exactly what needs to be completed:
  - Discovery gaps → dispatch `doc-discovery-agent` with scope: "Complete documentation for: [specific missing flows/components]"
  - Business doc gaps → dispatch `doc-business-documenter` with scope: "Add missing use cases/processes for: [specifics]"
  - Technical doc gaps → dispatch `doc-technical-documenter` with scope: "Add missing requirements/flows for: [specifics]"
  - Coordination gaps → dispatch `doc-coordinator` with scope: "Fix traceability/cross-references for: [specifics]"
- Always pass `pr_number` and `branch` in the dispatch inputs

### If Gaps Require Code Changes (not documentation)

When the gap is not a documentation issue but a missing feature, validation, or error handling in the actual code:

- Use `create_agent_session` to assign a Copilot coding agent to address the code issue
- Add a comment explaining what code changes are needed and why they were identified during documentation verification

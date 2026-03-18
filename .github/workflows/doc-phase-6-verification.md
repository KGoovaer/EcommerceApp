---
name: Doc Pipeline — 6 Verification
description: Documentation pipeline phase 6 of 6 — cross-check all documentation against source code, with re-dispatch loop for gaps
on:
  workflow_dispatch:
    inputs:
      module_name:
        description: "Module name (default: EcommerceApp)"
        required: false
        default: "EcommerceApp"
      pr_number:
        description: "PR number to push docs to"
        required: false
        default: ""
      branch:
        description: "PR head branch to push docs to"
        required: false
        default: ""
      retry_count:
        description: "Current retry iteration (0 = first run). Used for loop termination."
        required: false
        default: "0"
      scope:
        description: "Optional scoped re-verification instruction passed by a previous verification run (e.g. 'complete documentation for modules: X, Y')"
        required: false
        default: ""
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
    labels: [ai-docs-requested]
    allowed-files:
      - docs/**
  dispatch-workflow:
    workflows:
      - doc-phase-2-discovery
      - doc-phase-3-business
      - doc-phase-4-technical
      - doc-phase-5-coordination
    max: 4
  add-comment: {}
  add-labels:
    allowed:
      - docs-verified
      - docs-incomplete
      - docs-retry-1
      - docs-retry-2
      - docs-retry-3
  missing-data:
    create-issue: true
  noop: {}
  create-pull-request-review-comment:
    max: 50
  submit-pull-request-review: {}
  resolve-pull-request-review-thread: {}
  assign-to-agent:
    allowed-pull-request-repos:
      - "*"
---

# Phase 6 — Verification

You are the **Verification Agent**. This is the final phase (6 of 6) of the documentation pipeline.

## Guard check

Run this and read the output carefully — you will need `PR_NUMBER` and `RETRY_COUNT` throughout:

```bash
PR_NUMBER="${{ inputs.pr_number }}"
BRANCH="${{ inputs.branch }}"
RETRY_COUNT="${{ inputs.retry_count }}"
SCOPE="${{ inputs.scope }}"

# Fall back to auto-detection only when inputs are missing (manual re-run)
if [ -z "$PR_NUMBER" ] || [ -z "$BRANCH" ]; then
  PR_JSON=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json number,headRefName --jq '.[0]' 2>/dev/null || echo '{}')
  [ -z "$PR_NUMBER" ] && PR_NUMBER=$(echo "$PR_JSON" | grep -o '"number":[0-9]*' | grep -o '[0-9]*')
  [ -z "$BRANCH" ]    && BRANCH=$(echo "$PR_JSON" | grep -o '"headRefName":"[^"]*"' | cut -d'"' -f4)
fi

echo "PR_NUMBER=${PR_NUMBER}"
echo "BRANCH=${BRANCH}"
echo "RETRY_COUNT=${RETRY_COUNT}"
echo "SCOPE=${SCOPE}"
[ -n "$BRANCH" ] && git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ 2>/dev/null || true
```

Read `docs/EcommerceApp-state.json`. Abort if:
- Coordination phase is not marked `complete` → print "Phase 5 (coordination) is not complete — aborting." and stop.
- Verification phase is already `complete` or `in-progress` AND `RETRY_COUNT` is `"0"` → print "Phase 6 (verification) already done or running — aborting." and stop. (On re-runs with `retry_count > 0` the guard is relaxed so the agent can re-verify after a re-dispatch.)

**Important**: When calling `push_to_pull_request_branch`, always pass `pull_request_number: <PR_NUMBER>` using the number printed above.

## Required reading

Read these files **in full** before doing anything else:

1. `.github/agents/verification.agent.md`
2. `.github/agents/security-agent.agent.md`
3. `.github/skills/java-analysis/SKILL.md`
4. `.github/skills/state-management/SKILL.md`
5. `docs/traceability/id-registry.md`
6. `docs/traceability/requirement-matrix.md`
7. `docs/traceability/flow-to-component-map.md`
8. `docs/functional/index.md`

Follow every instruction in those files exactly. All security rules from `security-agent.agent.md` are mandatory and non-negotiable when evaluating security gaps in the servlet coverage check.

## Your specific task

Cross-check all generated documentation against the actual source code.

### Servlet coverage check

For each `.java` file in `EcommerceApp/src/main/java/com/servlet/`:
- Verify it appears in at least one entry in `docs/traceability/flow-to-component-map.md`
- Verify it is referenced in at least one use case in `docs/business/use-cases/`
- Check whether it performs data-modifying operations (INSERT/UPDATE/DELETE via DAO) without checking an auth cookie — flag as a security gap if so

### DAO method coverage check

For each public method in `DAO.java`, `DAO2.java`, `DAO3.java`, `DAO4.java`, `DAO5.java`:
- Verify it is referenced in at least one FUREQ or functional flow (`docs/functional/flows/` or `docs/functional/requirements/`)
- Record any DAO methods with no documentation reference

### ID consistency check

Read `docs/traceability/id-registry.md` and scan all `.md` files under `docs/` for ID references (patterns like `BUREQ-\d+`, `UC_\d+`, `FUREQ-\d+`, `FF-\d+`, `BP-\d+`). Report any referenced IDs not in the registry.

## Output files

**`docs/verification/gap-report.md`**:
- Section: Undocumented servlets (list with file name and reason)
- Section: Undocumented DAO methods (list with class, method name)
- Section: Security gaps (servlets that write data without auth check, not already noted in NFUREQs)
- Section: Broken ID references (ID, file where referenced, reason it's broken)
- Section: Summary — counts of gaps found, overall documentation coverage % estimate

**`docs/verification/table-usage-matrix.md`** — table with rows = servlet classes, columns = DAO methods. Cell = operation type (SELECT / INSERT / UPDATE / DELETE / —). Infer from DAO method names and the flows in `docs/traceability/flow-to-component-map.md`.

**`docs/verification/cross-domain-dependencies.md`** — table showing which servlet groups depend on which DAO groups:
- Group servlets by functional area (product browsing, cart, orders, auth, admin)
- Group DAOs by the entities they manage
- Mark dependencies

## Finalisation

Update `docs/EcommerceApp-state.json`:
- Mark verification phase `complete`
- Set `current_phase` to `"complete"`
- Set `pipeline_completed_at` to current ISO-8601 timestamp
- Add all three verification files to `artifact_inventory`

Update `docs/index.md`: add links to the three verification files under a "Verification" section.

Print a final summary:
- Total files written across all phases (read `artifact_inventory` from state file)
- Number of gaps found (from gap-report)
- Overall pipeline status: COMPLETE

All output files will be pushed to the documentation pipeline PR.

## Handoff — Post review comments and drive the re-dispatch loop

After pushing verification outputs, classify every gap in `docs/verification/gap-report.md` by severity:

| Severity | Condition |
|---|---|
| CRITICAL | Auth bypass (data-modifying servlet without cookie check), SQL injection evidence in DAO, plaintext password storage |
| HIGH | Undocumented servlet (no use-case or flow reference), broken FUREQ/BUREQ ID cross-reference |
| MEDIUM | Undocumented DAO method, missing acceptance criteria in a FUREQ |
| LOW | Minor coverage gaps with no traceability impact |

### Step 1 — Resolve stale threads from previous verification runs

At the start of each run (after the guard check), query all open review threads on PR `<PR_NUMBER>`:

```bash
gh api graphql -f query='
{
  repository(owner: "'"$GITHUB_REPOSITORY_OWNER"'", name: "'"${GITHUB_REPOSITORY#*/}"'") {
    pullRequest(number: '"$PR_NUMBER"') {
      reviewThreads(first: 100) {
        nodes {
          id
          isResolved
          comments(first: 1) {
            nodes { body }
          }
        }
      }
    }
  }
}'
```

For each open thread whose comment body contains `<!-- verification-agent -->` and whose described gap is **no longer present** in the current re-verification run, call `resolve_pull_request_review_thread` to mark it resolved.

### Step 2 — Create review comments

For each finding, call `create_pull_request_review_comment` with:
- `pull_request_number`: `<PR_NUMBER>` (from the guard check)
- `path`: the source file path where the gap originates (servlet `.java` for auth/coverage gaps, DAO `.java` for undocumented methods, the relevant doc `.md` for ID ref breaks)
- `line`: best available line number (use 1 if no specific line can be identified)
- `body`:

```
**[SEVERITY] Gap type — Short title**

<One sentence describing exactly what is missing or wrong.>

**Impact**: <What is incorrect or risky as a result of this gap.>

**Fix**: <Precise action required — e.g. "Add UC_NNN referencing this servlet" or "Add HttpOnly/Secure flags to the cname cookie in LoginServlet.java line 42".>

<!-- verification-agent -->
```

The `<!-- verification-agent -->` HTML comment must appear at the end of every comment body.

Buffer all comments before submitting the review.

### Step 3 — Submit the PR review

Call `submit_pull_request_review` with:
- `pull_request_number`: `<PR_NUMBER>`
- `event`:
  - `REQUEST_CHANGES` — if any CRITICAL or HIGH findings exist
  - `COMMENT` — if only MEDIUM or LOW findings exist
  - `APPROVE` — if no gaps exist
- `body`:
  - On `REQUEST_CHANGES`: "Verification found N critical, M high, P medium, Q low gap(s). PR is blocked. Fix all CRITICAL/HIGH issues before merging. <!-- verification-agent -->"
  - On `COMMENT`: "Verification passed with minor gaps (medium/low). Review comments for details. <!-- verification-agent -->"
  - On `APPROVE`: "Verification complete — no documentation gaps found. <!-- verification-agent -->"

### Step 4 — On success (APPROVE or COMMENT-only)

If the review event is `APPROVE`:
1. Call `add_label` on PR `<PR_NUMBER>` with label `docs-verified`.
2. Call `add_comment` on PR `<PR_NUMBER>`:
   ```
   ✅ Documentation pipeline complete — all checks passed.
   Total artifacts: <N from artifact_inventory>
   <!-- verification-agent -->
   ```
3. Emit `noop` — pipeline is finished; no further dispatch.

If the review event is `COMMENT` (only MEDIUM/LOW gaps):
1. Call `add_label` on PR `<PR_NUMBER>` with labels `docs-verified` and `docs-incomplete`.
2. Call `add_comment` on PR `<PR_NUMBER>`:
   ```
   ⚠️ Documentation pipeline complete with minor gaps (medium/low severity only).
   See review comments for details. No re-dispatch needed; gaps are non-blocking.
   <!-- verification-agent -->
   ```
3. Emit `noop`.

### Step 5 — On gaps (REQUEST_CHANGES): loop guard and re-dispatch

First, check the current retry iteration from `RETRY_COUNT` (from workflow input, default `"0"`). Also verify by reading the PR's labels:

```bash
gh pr view "$PR_NUMBER" --json labels --jq '.labels[].name' | grep "^docs-retry-"
```

**If `RETRY_COUNT` >= 3** (loop limit reached — prevent infinite cycling):
1. Call `add_label` on PR `<PR_NUMBER>` with label `docs-incomplete`.
2. For each CRITICAL or HIGH gap that remains unresolved, surface it as a permanent gap using `missing_data`:
   - `description`: "Verification loop reached maximum retries (3). The following gaps could not be automatically resolved: <list gaps with file and description>"
   - `create_issue: true` so a GitHub Issue is created documenting the permanent gaps and what data/changes are needed.
3. Call `add_comment` on PR `<PR_NUMBER>`:
   ```
   🔴 Verification loop exhausted (3 retries). Permanent gaps have been filed as GitHub Issues. Manual intervention required before this PR can be merged.
   <!-- verification-agent -->
   ```
4. Emit `noop` — do not dispatch further.

**If `RETRY_COUNT` < 3** (loop can continue):

1. Determine which **specific agents** produced incomplete output. Map gaps to responsible agents:
   - Undocumented servlet / missing UC reference → re-dispatch `doc-phase-3-business` (and optionally `doc-phase-2-discovery` if the servlet was not discovered at all)
   - Broken BUREQ/FUREQ/UC ID cross-references → re-dispatch `doc-phase-4-technical` or `doc-phase-3-business` as appropriate
   - Undocumented DAO methods → re-dispatch `doc-phase-4-technical`
   - Security gaps (auth bypass in docs) → re-dispatch `doc-phase-4-technical`

2. Compute the next retry label: `docs-retry-<RETRY_COUNT+1>` and add it to the PR:
   ```bash
   NEXT_RETRY=$(( RETRY_COUNT + 1 ))
   # add label docs-retry-${NEXT_RETRY}
   ```

3. Call `assign_to_agent` on PR `<PR_NUMBER>` with:
   ```
   ## Documentation gaps to fix (retry <NEXT_RETRY>/3)

   The documentation pipeline verification found the following CRITICAL/HIGH gaps. Fix every one.

   <paste the full list of CRITICAL and HIGH findings from the gap-report, with file path and fix instruction for each>

   ### Rules
   - Do NOT modify source code — only update files under docs/**
   - Fix broken ID cross-references by adding or correcting the relevant IDs in the traceability registry
   - For auth bypass gaps: add a security note to the relevant FUREQ and flag it in the gap-report Security section — source code fixes are out of scope for this agent
   - Push fixes to the existing PR branch (do not open a new PR)
   - After pushing, call dispatch_workflow("doc-phase-6-verification", inputs: {module_name: "EcommerceApp", pr_number: <PR_NUMBER>, branch: "<BRANCH>", retry_count: "<NEXT_RETRY>", scope: "<scoped instruction>"})
   ```
   Use `model: claude-sonnet-4-5` for remediation.

4. Re-dispatch **only the agents with gaps**, passing `pr_number`, `branch`, and a targeted `scope` instruction so each agent knows what to complete. For example:
   ```
   dispatch_workflow("doc-phase-3-business", inputs: {
     module_name: "EcommerceApp",
     pr_number: <PR_NUMBER>,
     branch: "<BRANCH>",
     scope: "Add missing use cases for: <list of undocumented servlets>"
   })
   ```
   Each re-dispatched agent (phases 2–5) must check the `scope` input at the start of its run and limit its work to the specified items. After completing its scoped work, it must re-dispatch `doc-phase-6-verification` with `retry_count: <NEXT_RETRY>` instead of chaining to the next phase in sequence.

   > **Re-dispatch rule for phases 2–5 (scoped re-run):** When `scope` input is non-empty, the agent treats it as a targeted amendment — it only produces or updates documents related to the scoped items, pushes them, then dispatches `doc-phase-6-verification` (not the next pipeline phase) with `retry_count` incremented.

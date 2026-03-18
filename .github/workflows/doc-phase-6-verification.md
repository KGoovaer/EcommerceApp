---
name: Doc Pipeline — 6 Verification
description: Documentation pipeline phase 6 of 6 — cross-check all documentation against source code
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

Run this and read the output carefully — you will need `PR_NUMBER` later:

```bash
PR_JSON=$(gh pr list --search "docs: EcommerceApp documentation pipeline in:title" --state open --json number,headRefName --jq '.[0]' 2>/dev/null || echo '{}')
PR_NUMBER=$(echo "$PR_JSON" | grep -o '"number":[0-9]*' | grep -o '[0-9]*')
BRANCH=$(echo "$PR_JSON" | grep -o '"headRefName":"[^"]*"' | cut -d'"' -f4)
echo "PR_NUMBER=${PR_NUMBER}"
echo "BRANCH=${BRANCH}"
[ -n "$BRANCH" ] && git fetch origin "$BRANCH" && git checkout FETCH_HEAD -- docs/ 2>/dev/null || true
```

Read `docs/EcommerceApp-state.json`. Abort if:
- Coordination phase is not marked `complete` → print "Phase 5 (coordination) is not complete — aborting." and stop.
- Verification phase is already `complete` or `in-progress` → print "Phase 6 (verification) already done or running — aborting." and stop.

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

## Handoff — Post review comments and assign remediation agent

After pushing verification outputs, classify every gap in `docs/verification/gap-report.md` by severity:

| Severity | Condition |
|---|---|
| CRITICAL | Auth bypass (data-modifying servlet without cookie check), SQL injection evidence in DAO, plaintext password storage |
| HIGH | Undocumented servlet (no use-case or flow reference), broken FUREQ/BUREQ ID cross-reference |
| MEDIUM | Undocumented DAO method, missing acceptance criteria in a FUREQ |
| LOW | Minor coverage gaps with no traceability impact |

### Step 1 — Create review comments

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

### Step 2 — Submit the PR review

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

### Step 3 — Assign Copilot on REQUEST_CHANGES

If the review was submitted as `REQUEST_CHANGES`, call `assign_to_agent` on PR `<PR_NUMBER>` with:

```
## Documentation gaps to fix

The documentation pipeline verification found the following CRITICAL/HIGH gaps. Fix every one.

<paste the full list of CRITICAL and HIGH findings from the gap-report, with file path and fix instruction for each>

### Rules
- Do NOT modify source code — only update files under docs/**
- Fix broken ID cross-references by adding or correcting the relevant IDs in the traceability registry
- For auth bypass gaps: add a security note to the relevant FUREQ and flag it in the gap-report Security section — source code fixes are out of scope for this agent
- Push fixes to the existing PR branch (do not open a new PR)
- After pushing, the verification agent will re-run automatically and resolve fixed threads
```

Use `model: claude-sonnet-4-5` for remediation.

### Step 4 — Resolve stale threads on re-run

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

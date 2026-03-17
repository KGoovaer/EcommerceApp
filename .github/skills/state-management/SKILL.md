---
name: state-management
description: Automatic state file management for documentation agents. Always use this skill to update progress tracking.
license: MIT
---

# State Management Skill

This skill provides automatic state file management for all documentation agents.

## When to Use This Skill

**ALWAYS** - All documentation agents should use this skill to:
- Initialize state files
- Update phase status
- Track progress
- Record completed artifacts
- Maintain workflow continuity

## State File Location

State files are stored in the docs directory:
```
docs/[MODULE_NAME]-state.json
```

## State File Schema

```json
{
  "module": "string",
  "language": "cobol|java|dotnet|python|javascript|mixed",
  "created": "ISO8601 timestamp",
  "lastUpdated": "ISO8601 timestamp",
  "currentPhase": "planning|discovery|business|technical|coordination|complete",
  "phases": {
    "planning": {
      "status": "pending|in-progress|complete|blocked",
      "completedAt": "ISO8601 timestamp or null",
      "agent": "planning-agent",
      "artifacts": ["array of file paths"]
    },
    "discovery": {
      "status": "pending|in-progress|complete|blocked",
      "completedAt": "ISO8601 timestamp or null",
      "agent": "discovery",
      "artifacts": ["array of file paths"],
      "discovered": {
        "flows": 0,
        "components": 0,
        "domainConcepts": 0
      }
    },
    "business": {
      "status": "pending|in-progress|complete|blocked",
      "completedAt": "ISO8601 timestamp or null",
      "agent": "business-documenter",
      "artifacts": ["array of file paths"],
      "created": {
        "useCases": 0,
        "businessRequirements": 0,
        "businessProcesses": 0
      }
    },
    "technical": {
      "status": "pending|in-progress|complete|blocked",
      "completedAt": "ISO8601 timestamp or null",
      "agent": "technical-documenter",
      "artifacts": ["array of file paths"],
      "created": {
        "functionalRequirements": 0,
        "nonFunctionalRequirements": 0,
        "technicalFlows": 0
      }
    },
    "coordination": {
      "status": "pending|in-progress|complete|blocked",
      "completedAt": "ISO8601 timestamp or null",
      "agent": "doc-coordinator",
      "artifacts": ["array of file paths"],
      "validated": {
        "indexes": false,
        "traceability": false,
        "domainCatalog": false
      }
    }
  },
  "progress": {
    "totalTasks": 0,
    "completedTasks": 0,
    "percentage": 0
  },
  "metadata": {
    "repository": "string",
    "paths": {
      "source": "string",
      "docs": "string"
    }
  }
}
```

## Agent Integration Pattern

### At Start of Agent Work

```markdown
**STEP 1: Load State**
1. Read the state file: `docs/[MODULE_NAME]-state.json`
2. If file doesn't exist, create it with initial structure
3. Check current phase status
4. Verify dependencies (previous phases complete)

**STEP 2: Update Status to In-Progress**
1. Set current phase status to "in-progress"
2. Update lastUpdated timestamp
3. Save state file
```

### During Agent Work

```markdown
**STEP 3: Track Progress**
1. When discovering items, update counters:
   - phases.discovery.discovered.flows++
   - phases.business.created.useCases++
   - etc.
2. When creating artifacts, add to artifacts array:
   - phases.discovery.artifacts.push("docs/discovered-flows.md")
3. Update progress.completedTasks as tasks finish
4. Save state file periodically
```

### At End of Agent Work

```markdown
**STEP 4: Mark Complete**
1. Set current phase status to "complete"
2. Set completedAt to current ISO8601 timestamp
3. Update progress.percentage = (completedTasks / totalTasks) * 100
4. Set currentPhase to next phase name
5. Save state file
```

## Helper Functions (Pseudo-code)

### Initialize State File

```javascript
function initializeState(moduleName, language) {
  const now = new Date().toISOString();
  return {
    module: moduleName,
    language: language,
    created: now,
    lastUpdated: now,
    currentPhase: "planning",
    phases: {
      planning: { status: "in-progress", completedAt: null, agent: "planning-agent", artifacts: [] },
      discovery: { status: "pending", completedAt: null, agent: "discovery", artifacts: [], discovered: { flows: 0, components: 0, domainConcepts: 0 } },
      business: { status: "pending", completedAt: null, agent: "business-documenter", artifacts: [], created: { useCases: 0, businessRequirements: 0, businessProcesses: 0 } },
      technical: { status: "pending", completedAt: null, agent: "technical-documenter", artifacts: [], created: { functionalRequirements: 0, nonFunctionalRequirements: 0, technicalFlows: 0 } },
      coordination: { status: "pending", completedAt: null, agent: "doc-coordinator", artifacts: [], validated: { indexes: false, traceability: false, domainCatalog: false } }
    },
    progress: { totalTasks: 0, completedTasks: 0, percentage: 0 },
    metadata: { repository: "", paths: { source: "src/", docs: "docs/" } }
  };
}
```

### Update Phase Status

```javascript
function updatePhaseStatus(state, phaseName, status) {
  state.phases[phaseName].status = status;
  state.lastUpdated = new Date().toISOString();
  if (status === "complete") {
    state.phases[phaseName].completedAt = state.lastUpdated;
  }
  return state;
}
```

### Add Artifact

```javascript
function addArtifact(state, phaseName, artifactPath) {
  if (!state.phases[phaseName].artifacts.includes(artifactPath)) {
    state.phases[phaseName].artifacts.push(artifactPath);
  }
  state.lastUpdated = new Date().toISOString();
  return state;
}
```

### Increment Counter

```javascript
function incrementCounter(state, phaseName, counterType, counterName) {
  state.phases[phaseName][counterType][counterName]++;
  state.lastUpdated = new Date().toISOString();
  return state;
}
```

### Update Progress

```javascript
function updateProgress(state, completedTasks) {
  state.progress.completedTasks = completedTasks;
  if (state.progress.totalTasks > 0) {
    state.progress.percentage = Math.round((completedTasks / state.progress.totalTasks) * 100);
  }
  state.lastUpdated = new Date().toISOString();
  return state;
}
```

## Usage in Agent Instructions

### Planning Agent

```markdown
## State Management (AUTOMATIC)

At the start:
1. Create state file with initialized structure
2. Detect language from codebase
3. Set module name
4. Set planning phase to "in-progress"

At the end:
1. Set planning phase to "complete"
2. Add created artifacts
3. Set currentPhase to "discovery"
4. Set totalTasks based on discovered scope
```

### Discovery Agent

```markdown
## State Management (AUTOMATIC)

At the start:
1. Load state file
2. Verify planning phase is complete
3. Set discovery phase to "in-progress"

During work:
1. Increment discovered.flows for each flow found
2. Increment discovered.components for each component
3. Increment discovered.domainConcepts for each concept
4. Add artifacts as they're created

At the end:
1. Set discovery phase to "complete"
2. Update completedTasks
3. Set currentPhase to "business"
```

### Business Documenter

```markdown
## State Management (AUTOMATIC)

At the start:
1. Load state file
2. Verify discovery phase is complete
3. Set business phase to "in-progress"

During work:
1. Increment created.useCases for each UC created
2. Increment created.businessRequirements for each BUREQ
3. Increment created.businessProcesses for each BP
4. Add artifacts as they're created

At the end:
1. Set business phase to "complete"
2. Update completedTasks
3. Set currentPhase to "technical"
```

### Technical Documenter

```markdown
## State Management (AUTOMATIC)

At the start:
1. Load state file
2. Verify business phase is complete
3. Set technical phase to "in-progress"

During work:
1. Increment created.functionalRequirements for each FUREQ
2. Increment created.nonFunctionalRequirements for each NFUREQ
3. Increment created.technicalFlows for each FF
4. Add artifacts as they're created

At the end:
1. Set technical phase to "complete"
2. Update completedTasks
3. Set currentPhase to "coordination"
```

### Doc Coordinator

```markdown
## State Management (AUTOMATIC)

At the start:
1. Load state file
2. Verify technical phase is complete
3. Set coordination phase to "in-progress"

During work:
1. Set validated.indexes = true when indexes created
2. Set validated.traceability = true when matrix created
3. Set validated.domainCatalog = true when catalog updated
4. Add artifacts as they're created

At the end:
1. Set coordination phase to "complete"
2. Update completedTasks
3. Set currentPhase to "complete"
4. Set progress.percentage to 100
```

## Error Handling

### State File Not Found
- Create new state file with initial structure
- Log warning that starting from scratch

### State File Corrupted
- Backup corrupted file with `.backup` extension
- Create new state file
- Log error with backup location

### Phase Dependency Not Met
- Log error: "Cannot start [phase] because [previous-phase] is not complete"
- Set current phase status to "blocked"
- Exit with error message

### Concurrent Modification
- Use file locking if available
- Add timestamp check before write
- Log warning if state was modified by another process

## Best Practices

1. **Always Update State**: Never skip state updates
2. **Atomic Writes**: Write to temp file, then rename
3. **Validate JSON**: Check JSON validity before writing
4. **Add Timestamps**: Always update lastUpdated
5. **Log Changes**: Log all state modifications for debugging
6. **Backup Before Major Changes**: Keep previous version
7. **Human-Readable**: Pretty-print JSON for readability

## State File Example

```json
{
  "module": "CUSTOMER_PROCESSING",
  "language": "cobol",
  "created": "2026-02-24T10:00:00.000Z",
  "lastUpdated": "2026-02-24T12:30:00.000Z",
  "currentPhase": "technical",
  "phases": {
    "planning": {
      "status": "complete",
      "completedAt": "2026-02-24T10:15:00.000Z",
      "agent": "planning-agent",
      "artifacts": ["docs/planning/documentation-plan.md"]
    },
    "discovery": {
      "status": "complete",
      "completedAt": "2026-02-24T11:00:00.000Z",
      "agent": "discovery",
      "artifacts": [
        "docs/discovered-flows.md",
        "docs/discovered-domain-concepts.md",
        "docs/discovered-components.md"
      ],
      "discovered": {
        "flows": 12,
        "components": 45,
        "domainConcepts": 23
      }
    },
    "business": {
      "status": "complete",
      "completedAt": "2026-02-24T12:00:00.000Z",
      "agent": "business-documenter",
      "artifacts": [
        "docs/business/index.md",
        "docs/business/use-cases/UC_CUST_001.md",
        "docs/business/use-cases/UC_CUST_002.md"
      ],
      "created": {
        "useCases": 8,
        "businessRequirements": 15,
        "businessProcesses": 3
      }
    },
    "technical": {
      "status": "in-progress",
      "completedAt": null,
      "agent": "technical-documenter",
      "artifacts": [
        "docs/functional/requirements/FUREQ_CUST_001.md"
      ],
      "created": {
        "functionalRequirements": 5,
        "nonFunctionalRequirements": 2,
        "technicalFlows": 3
      }
    },
    "coordination": {
      "status": "pending",
      "completedAt": null,
      "agent": "doc-coordinator",
      "artifacts": [],
      "validated": {
        "indexes": false,
        "traceability": false,
        "domainCatalog": false
      }
    }
  },
  "progress": {
    "totalTasks": 50,
    "completedTasks": 35,
    "percentage": 70
  },
  "metadata": {
    "repository": "mainframe-apps",
    "paths": {
      "source": "src/cobol/",
      "docs": "docs/"
    }
  }
}
```

## Integration with Agents

Agents should reference this skill with:

```markdown
Use the `state-management` skill to automatically track progress and update the state file throughout your work.
```

This ensures consistent state management across all agents without manual intervention.

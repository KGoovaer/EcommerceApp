# EcommerceApp — Copilot Instructions

## Project Overview

Java J2EE Online Electronic Shopping application. Stack: **Servlet 3.0 + JSP + SQLite + Maven WAR**. Deployed on Apache Tomcat 8+. No Spring, no ORM, no DI container.

## Build & Run

```bash
# Build WAR (from EcommerceApp/ directory)
mvn package

# Output: target/EcommerceApp-0.0.1-SNAPSHOT.war
# Deploy to: $TOMCAT_HOME/webapps/
```

**Before running**, update the hardcoded SQLite path in two places:

1. [src/main/java/com/conn/DBConnect.java](../EcommerceApp/src/main/java/com/conn/DBConnect.java) — change the `DriverManager.getConnection(...)` path to the local `mydatabase.db` location.
2. [src/main/java/com/dao/DAO.java](../EcommerceApp/src/main/java/com/dao/DAO.java) — update the hardcoded image upload path.

There are **no automated tests** in this project (the `junit:junit:3.8.1` dependency exists but no test classes). Do not add test scaffolding unless explicitly requested.

## Architecture

```
Browser
  │
  ├── JSPs (src/main/webapp/*.jsp)   ← presentation AND direct DAO calls via scriptlets
  │     └── navbar.jsp / customer_navbar.jsp / admin_navbar.jsp  (<%@ include %>)
  │
  └── Servlet layer (src/main/java/com/servlet/, 19+ servlets, @WebServlet annotations)
        └── DAO layer (com/dao/DAO.java … DAO5.java — no shared interface)
              └── DBConnect.getConn() → SQLite via org.xerial:sqlite-jdbc
```

### Key components

| Package | Purpose |
|---|---|
| `com.conn` | `DBConnect` — static `Connection` singleton (not thread-safe) |
| `com.dao` | `DAO`–`DAO5` — plain JDBC DAO classes, receive `Connection` from call site |
| `com.entity` | 14 plain Java beans (no annotations) |
| `com.servlet` | 19+ `HttpServlet` subclasses, URL-mapped via `@WebServlet` |
| `com.utility` | `MyUtilities.UploadFile()` — file upload validation & write |

## Conventions

- **Routing**: `@WebServlet("/path")` annotations only — no `web.xml`.
- **Auth**: Cookie-based (`cname` = customer email, `tname` = admin username, `maxAge=9999`). No `HttpSession` usage anywhere.
- **Flash messages**: Short-lived cookies (`maxAge=10`) used for one-time feedback between redirect–response cycles.
- **JSP tripling pattern**: Most pages exist in three variants, e.g. `mobile.jsp` / `mobilea.jsp` / `mobilec.jsp` = customer / admin / guest. Follow this pattern when adding new pages.
- **DAO construction**: Always `new DAOx(DBConnect.getConn())` at the servlet call site. Do not change this pattern.
- **SQL**: `PreparedStatement` is used throughout — maintain this for all new queries.
- **Error handling**: Catch-and-print + redirect to `fail*.jsp`. No logging framework.

## Known Pitfalls

- **`DBConnect.java` loads the MySQL driver but connects to SQLite** — this is intentional legacy code. Do not remove `Class.forName("com.mysql.cj.jdbc.Driver")` unless migrating the database.
- **Static `Connection` is not thread-safe** — do not assume concurrent requests work correctly.
- **`createtable.java`** in the root `com` package is a scratch utility with a `main()`. It is not part of the application flow; ignore it.
- **`antlr4-runtime` dependency is unused** — do not import or reference ANTLR.
- **No Java version pinned in `pom.xml`** — add `<maven.compiler.source>` / `<maven.compiler.target>` if changing the build configuration.
- **Image uploads** use the original filename as-is (path traversal risk) — do not worsen this by removing extension validation in `MyUtilities`.

## Security Baseline

This is a demo/learning project with known vulnerabilities (plaintext passwords, forgeable cookies, no CSRF protection, no `HttpOnly`/`Secure` cookie flags). When making changes:

- Do not introduce new SQL injection vectors — keep `PreparedStatement`.
- Do not replace extension validation in file upload (even if the rest is insecure).
- Flag any new endpoint that changes data but lacks auth checks.

---

# Documentation System — Copilot Instructions

## Overview

This workspace uses a **multi-agent documentation system** to analyze codebases and produce structured documentation. The system is **language-agnostic** — all language-specific analysis is handled through dedicated skills, not hardcoded in agents.

## Agent System

The documentation workflow follows a phased approach orchestrated by specialized agents:

1. **Planning Agent** — Detects language, analyzes structure, creates state file and documentation plan
2. **Discovery Agent** — Analyzes code using the appropriate language skill to discover flows, components, and domain concepts
3. **Business Documenter** — Transforms discoveries into stakeholder-friendly business documentation
4. **Technical Documenter** — Derives functional/technical requirements from business docs using language skills
5. **Doc Coordinator** — Maintains structure, consistency, cross-references, and traceability
6. **Verification Agent** — Cross-checks documentation against source code, produces gap reports
7. **Analyst Agent** — Analyzes flows and generates diagrams on demand
8. **Security Agent** — Enforces secure development policy compliance

## Skill-First Rule

All agents follow a **mandatory skill-first rule**:

- Agents must **never** embed language-specific parsing patterns, syntax examples, or framework heuristics.
- Before performing code analysis, agents must load the appropriate language skill based on the detected language in the state file.
- Language-specific detection, tracing, and extraction rules come exclusively from skills.

## Available Language Skills

| Skill | Use When |
|-------|----------|
| `cobol-analysis` | Analyzing COBOL, JCL, CICS, IMS, or mainframe codebases |
| `java-analysis` | Analyzing Java / Spring Boot projects |
| `typescript-analysis` | Analyzing TypeScript / JavaScript / Node.js / React projects |
| `vbnet-analysis` | Analyzing VB.NET / WinForms projects |

Additional skills (e.g., `python-analysis`, `dotnet-analysis`) can be added by creating a new skill directory under `.github/skills/`.

## State Management

All agents share a single state file (`docs/[MODULE_NAME]-state.json`) managed through the `state-management` skill. This file tracks:

- Detected language
- Current phase and phase history
- Progress counters and task status
- Artifact inventory

## Documentation Output Structure

```
docs/
├── [module]-state.json
├── documentation-plan.md
├── index.md
├── system-overview.md
├── discovery/
├── business/
├── functional/
├── domain/
├── verification/
└── traceability/
```

## Adding Support for a New Language

1. Create a new skill directory: `.github/skills/<language>-analysis/`
2. Add a `SKILL.md` with language-specific detection patterns, file conventions, architectural patterns, and analysis guidance
3. The planning agent will automatically detect the language and downstream agents will load the new skill via their routing tables

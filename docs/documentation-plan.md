# EcommerceApp — Documentation Plan

**Module:** EcommerceApp  
**Language:** Java  
**Framework:** Servlet 3.0 + JSP  
**Build Tool:** Maven  
**Database:** SQLite (via org.xerial:sqlite-jdbc)  
**Generated:** 2026-03-17  
**Planning Agent:** planning-agent  

---

## Project Summary

EcommerceApp is a Java J2EE Online Electronic Shopping application. The system allows customers to browse product categories (mobiles, laptops, TVs, watches), manage a shopping cart, place orders, and complete checkout with payment. An administrator can manage products, customers, orders, and contact enquiries.

### Architecture Overview

```
Browser
  │
  ├── JSPs (65 pages in src/main/webapp/)   ← Presentation layer (scriptlet-heavy)
  │     └── navbar.jsp / customer_navbar.jsp / admin_navbar.jsp  (<%@ include %>)
  │
  └── Servlet Layer (21 servlets, @WebServlet)
        └── DAO Layer (DAO.java – DAO5.java — plain JDBC, no ORM)
              └── DBConnect.getConn() → SQLite
```

### Key File Counts

| Artefact Type | Count |
|---|---|
| Servlet classes | 21 |
| DAO classes | 6 (DAO – DAO5) |
| Entity beans | 14 |
| JSP pages | 65 |
| CSS files | 7 |

### Detected Business Domains

| # | Domain | Servlets / Entry Points |
|---|---|---|
| 1 | Authentication | checkadmin, checkcustomer, validatelogina.jsp, validateloginc.jsp |
| 2 | Customer Management | addcustomer, deletecustomer, managecustomers.jsp |
| 3 | Product Catalog | addproduct, viewproduct*.jsp, mobile*.jsp, laptop*.jsp, tv*.jsp, watch*.jsp, category*.jsp |
| 4 | Cart | addtocart, addtocartnull, addtocartnulla, removecart, removecarta, removecartnull, removecartnulla, cart*.jsp |
| 5 | Orders & Checkout | payprocess, ShippingAddress2, removeorders, remove_orders, orders.jsp, orderdetails.jsp, ShippingAddress.jsp, confirmpayment.jsp, confirmonline.jsp |
| 6 | Admin Operations | removetable_cart, removetable_order_details, adminhome.jsp, managetables.jsp, table_*.jsp |
| 7 | Contact Us | addContactus, addContactusc, remove_contactus, contactus.jsp, contactusc.jsp |

---

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| 1 — Planning | planning-agent | 1 | Low |
| 2 — Discovery (7 batches × 7 tasks) | discovery | 49 | High — deep query + cross-domain |
| 2 — Discovery Consolidation | discovery | 3 | Medium |
| 3 — Business | business-documenter | 5 | Medium |
| 4 — Technical | technical-documenter | 5 | High |
| 5 — Coordination | doc-coordinator | 5 | Medium |
| 6 — Verification | verification | 6 | High |
| **Total** | | **74** | |

---

## Phase 1 — Planning ✅ Complete

**Agent:** planning-agent  
**Status:** Complete  

### Outputs
- `docs/EcommerceApp-state.json` — state tracking file (this run)
- `docs/documentation-plan.md` — this plan

---

## Phase 2 — Discovery

**Agent:** discovery  
**Skill:** java-analysis  
**Strategy:** Run discovery per domain batch (7 batches), then consolidate.

### Task List per Batch (7 tasks each)

1. Detect entry points (servlet URL mappings + JSP direct access)
2. Trace execution flows (HTTP method → Servlet → DAO → DB → JSP redirect)
3. **Deep data access query analysis** — read actual PreparedStatement SQL, document ALL WHERE clauses, JOINs, and filtering conditions as business rules
4. Inventory all components (Servlets, DAOs, JSPs, Entities, Utilities involved)
5. Extract domain concepts (entities, status values, business terminology)
6. Map inter-domain dependencies and cookie-based auth checks
7. **Cross-domain table usage** — for each table read/written, search for other domains using the same table

### Batch 1 — Authentication

**Output files:**
- `docs/discovery/batch-1-authentication-flows.md`
- `docs/discovery/batch-1-authentication-components.md`
- `docs/discovery/batch-1-authentication-domain-concepts.md`

**Entry points:**
- `checkadmin.java` (`@WebServlet("/checkadmin")`) — admin login validation
- `checkcustomer.java` (`@WebServlet("/checkcustomer")`) — customer login validation
- `validatelogina.jsp` — admin login form processor
- `validateloginc.jsp` — customer login form processor
- `adminlogin.jsp`, `customerlogin.jsp` — login forms

**Pay special attention to:**
- Cookie-based auth: `cname` (customer email), `tname` (admin username), `maxAge=9999`
- Flash-message cookies: `maxAge=10` pattern
- Password comparison logic in DAO — is it plaintext or hashed?
- Redirect targets on success vs. failure

---

### Batch 2 — Customer Management

**Output files:**
- `docs/discovery/batch-2-customer-management-flows.md`
- `docs/discovery/batch-2-customer-management-components.md`
- `docs/discovery/batch-2-customer-management-domain-concepts.md`

**Entry points:**
- `addcustomer.java` — customer registration
- `deletecustomer.java` — admin deletes customer
- `customer_reg.jsp` — registration form
- `managecustomers.jsp` — admin customer list

**Pay special attention to:**
- Registration validation (duplicate email check, required fields)
- `customer` entity fields (email used as identity key)
- Cross-domain: customer table is also used by authentication (Batch 1) and orders (Batch 5)

---

### Batch 3 — Product Catalog

**Output files:**
- `docs/discovery/batch-3-product-catalog-flows.md`
- `docs/discovery/batch-3-product-catalog-components.md`
- `docs/discovery/batch-3-product-catalog-domain-concepts.md`

**Entry points:**
- `addproduct.java` — admin adds product + image upload
- `viewproduct.jsp` / `viewproducta.jsp` / `viewproductc.jsp` — product detail (guest/admin/customer)
- `mobile.jsp` / `mobilea.jsp` / `mobilec.jsp` — mobile listing
- `laptop.jsp` / `laptopa.jsp` / `laptopc.jsp` — laptop listing
- `tv.jsp` / `tva.jsp` / `tvc.jsp` — TV listing
- `watch.jsp` / `watcha.jsp` / `watchc.jsp` — watch listing
- `category.jsp` / `categorya.jsp` / `categoryc.jsp` — category page
- `selecteditem.jsp` / `selecteditema.jsp` / `selecteditemc.jsp` — selected product view

**Pay special attention to:**
- `MyUtilities.UploadFile()` — file extension validation, image path construction
- Hardcoded upload path in `DAO.java` — document the path and its security implications
- Product entity fields: `Product`, `mobile`, `laptop`, `tv`, `watch` — note category-specific attributes
- JSP tripling pattern: guest / customer (`c`) / admin (`a`) variants

---

### Batch 4 — Cart

**Output files:**
- `docs/discovery/batch-4-cart-flows.md`
- `docs/discovery/batch-4-cart-components.md`
- `docs/discovery/batch-4-cart-domain-concepts.md`

**Entry points:**
- `addtocart.java` — add to cart (authenticated customer)
- `addtocartnull.java` / `addtocartnulla.java` — add to cart (null/anonymous states)
- `removecart.java` / `removecarta.java` — remove from cart (customer / admin view)
- `removecartnull.java` / `removecartnulla.java` — remove from cart (null states)
- `cart.jsp` / `carta.jsp` / `cartnull.jsp` / `cartnulla.jsp` / `cartnullqty.jsp` — cart views

**Pay special attention to:**
- Distinction between `null` and authenticated cart states — what triggers `cartnull` vs `cart`?
- Cart quantity logic — `cartnullqty.jsp` suggests quantity-zero edge case
- `cart` entity fields — product reference, quantity, customer reference
- Cross-domain: cart table consumed by checkout/orders (Batch 5)
- Cookie `cname` used to associate cart with customer

---

### Batch 5 — Orders & Checkout

**Output files:**
- `docs/discovery/batch-5-orders-checkout-flows.md`
- `docs/discovery/batch-5-orders-checkout-components.md`
- `docs/discovery/batch-5-orders-checkout-domain-concepts.md`

**Entry points:**
- `payprocess.java` — payment processing servlet
- `ShippingAddress2.java` — shipping address capture
- `removeorders.java` / `remove_orders.java` — order removal (customer vs admin?)
- `ShippingAddress.jsp` — shipping address form
- `confirmpayment.jsp` / `confirmonline.jsp` — payment confirmation pages
- `orders.jsp` — order history
- `orderdetails.jsp` — order line items
- `paymentfail.jsp` — payment failure page

**Pay special attention to:**
- Full checkout flow: cart → shipping address → payment → confirmation
- `orders` vs `order_details` entity distinction — header vs line items
- Payment method handling in `payprocess.java` — COD vs online?
- `confirmonline.jsp` vs `confirmpayment.jsp` — distinct flows?
- Cross-domain: reads from cart table, writes to orders + order_details tables
- `removetable_order_details.java` in admin domain — cross-domain dependency

---

### Batch 6 — Admin Operations

**Output files:**
- `docs/discovery/batch-6-admin-operations-flows.md`
- `docs/discovery/batch-6-admin-operations-components.md`
- `docs/discovery/batch-6-admin-operations-domain-concepts.md`

**Entry points:**
- `removetable_cart.java` — bulk-clear cart table
- `removetable_order_details.java` — bulk-clear order_details table
- `adminhome.jsp` — admin dashboard
- `managetables.jsp` — admin data table management
- `table_cart.jsp` / `table_contactus.jsp` / `table_order_details.jsp` / `table_orders.jsp` — raw data views

**Pay special attention to:**
- Admin auth guard: `tname` cookie check on each admin JSP/servlet
- Bulk operations — are there confirmation steps before destructive operations?
- Cross-domain: admin reads ALL tables (cart, orders, order_details, contactus, customer)
- `usermaster` entity — admin credential storage, how passwords are stored

---

### Batch 7 — Contact Us

**Output files:**
- `docs/discovery/batch-7-contact-us-flows.md`
- `docs/discovery/batch-7-contact-us-components.md`
- `docs/discovery/batch-7-contact-us-domain-concepts.md`

**Entry points:**
- `addContactus.java` — submit contact form (guest/admin)
- `addContactusc.java` — submit contact form (customer variant)
- `remove_contactus.java` — admin removes contact message
- `contactus.jsp` / `contactusc.jsp` — contact forms (guest vs customer)
- `table_contactus.jsp` — admin view of enquiries

**Pay special attention to:**
- Difference between `addContactus` (guest/admin) vs `addContactusc` (customer) — does the customer version pre-fill from cookie?
- `contactus` entity fields
- Cross-domain: `table_contactus.jsp` is in admin domain but data originates here

---

### Discovery Consolidation (after all 7 batches)

**Output files:**
- `docs/discovery/cross-domain-table-matrix.md`
- `docs/discovery/consolidation-gaps.md`

**Tasks:**
1. Merge all batch outputs; build master component list
2. Build cross-domain table read/write matrix for all DB tables
3. Flag undocumented business rules (queries with unexplained WHERE conditions, untraced redirect paths)

---

## Phase 3 — Business Documentation

**Agent:** business-documenter  
**Depends on:** Discovery phase complete  

### Tasks

1. Define actors (Guest, Customer, Admin) and their capabilities
2. Create use cases (`UC_*.md`) for all identified flows — one use case per distinct user goal
3. Write Business Requirements (`BUREQ_*.md`) — what the system must do in business terms
4. Create BPMN-style business process diagrams (Mermaid) for key flows
5. Write system business overview

### Expected Outputs

| ID Pattern | Description |
|---|---|
| `UC_AUTH_001` | Customer Login |
| `UC_AUTH_002` | Admin Login |
| `UC_CUST_001` | Customer Registration |
| `UC_CUST_002` | Admin Manages Customers |
| `UC_PROD_001` | Browse Product Catalog |
| `UC_PROD_002` | Admin Add Product |
| `UC_CART_001` | Add/Remove Items from Cart |
| `UC_CART_002` | View Shopping Cart |
| `UC_ORD_001` | Place Order / Checkout |
| `UC_ORD_002` | View Order History |
| `UC_ADMIN_001` | Admin Manage Orders & Tables |
| `UC_CONTACT_001` | Submit Contact Enquiry |
| `BP_CHECKOUT` | End-to-end checkout business process |
| `BP_CUSTOMER_REGISTRATION` | Customer sign-up process |
| `BP_PRODUCT_MANAGEMENT` | Admin product lifecycle |

---

## Phase 4 — Technical / Functional Documentation

**Agent:** technical-documenter  
**Skill:** java-analysis  
**Depends on:** Business phase complete  

### Tasks

1. Derive Functional Requirements (`FUREQ_*.md`) from BUREQs — testable system behaviours
2. Document Non-Functional Requirements (`NFUREQ_*.md`) — performance, security, constraints
3. Create technical flow diagrams (`FF_*.md`) with HTTP request/response traces
4. Document API endpoints (servlet URL mappings, parameters, response redirects)
5. Document data schemas (entity fields, DB table structures from DAOs)
6. Document validation rules with true/false outcome pairs
7. Document integration points (SQLite path, file upload path, cookie strategy)

### Expected Outputs

| ID | Description |
|---|---|
| `FUREQ_AUTH_001` | Customer authentication via cookie |
| `FUREQ_AUTH_002` | Admin authentication via cookie |
| `FUREQ_CUST_001` | Customer registration & uniqueness constraint |
| `FUREQ_PROD_001` | Product image upload with extension validation |
| `FUREQ_CART_001` | Cart item management per customer session |
| `FUREQ_ORD_001` | Order placement and cart clearance |
| `FUREQ_ADMIN_001` | Admin CRUD over all entities |
| `NFUREQ_001` | Single-threaded SQLite connection constraint |
| `NFUREQ_002` | Cookie-only session management (no HttpSession) |
| `FF_CUSTOMER_LOGIN` | Technical login flow with SQL trace |
| `FF_ADMIN_LOGIN` | Technical admin login flow |
| `FF_ADD_TO_CART` | Add-to-cart technical flow |
| `FF_CHECKOUT` | Full checkout technical flow |
| `FF_PRODUCT_BROWSE` | Product catalog browse flow |
| `INTEG_SQLITE` | SQLite connection, path config, threading |
| `INTEG_FILE_UPLOAD` | File upload path, extension checks |

---

## Phase 5 — Coordination

**Agent:** doc-coordinator  
**Depends on:** Technical phase complete  

### Tasks

1. Validate output directory structure matches plan
2. Create master `docs/index.md` landing page with links to all artefacts
3. Create `docs/system-overview.md` with architecture narrative
4. Build traceability matrix: `BUREQ → UC → FUREQ → FF → Component`
5. Create domain concepts catalog and ubiquitous language glossary
6. Create `docs/traceability/id-registry.md` — all IDs cross-referenced
7. Fix any broken cross-references between documents

### Expected Outputs

- `docs/index.md`
- `docs/system-overview.md`
- `docs/domain/domain-concepts-catalog.md`
- `docs/domain/ubiquitous-language.md`
- `docs/domain/bounded-contexts.md`
- `docs/traceability/requirement-matrix.md`
- `docs/traceability/flow-to-component-map.md`
- `docs/traceability/id-registry.md`

---

## Phase 6 — Verification

**Agent:** verification  
**Depends on:** Coordination phase complete  

### Tasks

| ID | Task | Output |
|---|---|---|
| V-1 | Build DB table usage matrix — map every table to flows that read/write it | `docs/verification/table-usage-matrix.md` |
| V-2 | Repository query deep analysis — compare all WHERE conditions in DAO SQL against documented business rules | `docs/verification/gap-report.md` (partial) |
| V-3 | Validation completeness check — enumerate all `if` guards and parameter checks in servlets, verify each has documented true/false outcomes | `docs/verification/gap-report.md` (partial) |
| V-4 | Cross-domain dependency verification — confirm all cross-batch table dependencies have bidirectional links in documentation | `docs/verification/cross-domain-dependencies.md` |
| V-5 | Entity state machine verification — map all status/state transitions, verify all documented | `docs/verification/gap-report.md` (partial) |
| V-6 | Generate consolidated gap report with severity classification (Critical/High/Medium/Low) and remediation prompts | `docs/verification/gap-report.md` (final) |

### Remediation Trigger

If V-6 identifies **Critical** or **High** severity gaps:
- Re-run Discovery Agent on the specific flows cited in the gap report
- Re-run Verification Agent to confirm gaps are resolved

---

## Completeness Criteria

### Per-Flow Criteria

- [ ] All SQL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] All input validation conditions have both `true` (pass) and `false` (fail) outcomes documented
- [ ] All redirect targets (success and failure) are documented
- [ ] Cross-domain table dependencies are bidirectionally linked
- [ ] Cookie read/write operations are documented (name, value, maxAge)
- [ ] Error handling paths are documented with redirect targets

### Per-Domain Criteria

- [ ] All flows within the domain meet per-flow criteria
- [ ] All cross-domain dependencies are documented from both sides
- [ ] Business use cases reference all relevant flows
- [ ] Functional requirements cover all business rules extracted from SQL queries

### Overall Documentation Criteria

- [ ] All 21 servlets appear in at least one flow document
- [ ] All 14 entity beans are documented in the domain concepts catalog
- [ ] All 6 DAO classes have their SQL queries documented
- [ ] All JSP variant triples (guest / customer / admin) are accounted for
- [ ] Traceability matrix covers all BUREQ → FUREQ links
- [ ] ID registry contains all `UC_*`, `BP_*`, `BUREQ_*`, `FUREQ_*`, `NFUREQ_*`, `FF_*` IDs

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json            ← Phase tracking (this file)
├── documentation-plan.md              ← This plan
├── index.md                           ← Master landing page (Phase 5)
├── system-overview.md                 ← Architecture overview (Phase 5)
├── discovery/                         ← Phase 2 outputs
│   ├── batch-1-authentication-flows.md
│   ├── batch-1-authentication-components.md
│   ├── batch-1-authentication-domain-concepts.md
│   ├── batch-2-customer-management-flows.md
│   ├── batch-2-customer-management-components.md
│   ├── batch-2-customer-management-domain-concepts.md
│   ├── batch-3-product-catalog-flows.md
│   ├── batch-3-product-catalog-components.md
│   ├── batch-3-product-catalog-domain-concepts.md
│   ├── batch-4-cart-flows.md
│   ├── batch-4-cart-components.md
│   ├── batch-4-cart-domain-concepts.md
│   ├── batch-5-orders-checkout-flows.md
│   ├── batch-5-orders-checkout-components.md
│   ├── batch-5-orders-checkout-domain-concepts.md
│   ├── batch-6-admin-operations-flows.md
│   ├── batch-6-admin-operations-components.md
│   ├── batch-6-admin-operations-domain-concepts.md
│   ├── batch-7-contact-us-flows.md
│   ├── batch-7-contact-us-components.md
│   ├── batch-7-contact-us-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/                          ← Phase 3 outputs
│   ├── index.md
│   ├── overview/
│   ├── use-cases/
│   │   └── UC_*.md
│   ├── processes/
│   │   └── BP_*.md
│   └── requirements/
│       └── BUREQ_*.md
├── functional/                        ← Phase 4 outputs
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_*.md
│   │   └── NFUREQ_*.md
│   ├── flows/
│   │   └── FF_*.md
│   └── integration/
│       └── INTEG_*.md
├── domain/                            ← Phase 5 outputs
│   ├── domain-concepts-catalog.md
│   ├── ubiquitous-language.md
│   └── bounded-contexts.md
├── verification/                      ← Phase 6 outputs
│   ├── gap-report.md
│   ├── table-usage-matrix.md
│   └── cross-domain-dependencies.md
└── traceability/                      ← Phase 5 outputs
    ├── requirement-matrix.md
    ├── flow-to-component-map.md
    └── id-registry.md
```

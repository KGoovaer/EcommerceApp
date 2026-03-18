# EcommerceApp — Documentation Plan

## Project Overview

| Field | Value |
|---|---|
| **Module** | EcommerceApp |
| **Language** | Java |
| **Framework** | Servlet 3.0 + JSP |
| **Build Tool** | Maven (WAR packaging) |
| **Database** | SQLite (via org.xerial:sqlite-jdbc) |
| **Deployment** | Apache Tomcat 8+ |
| **Repository** | KGoovaer/EcommerceApp |

## Codebase Summary

| Component | Count |
|---|---|
| Servlets (`com.servlet`) | 19 |
| DAO classes (`com.dao`) | 5 (DAO–DAO5) |
| Entity beans (`com.entity`) | 13 |
| JSP pages (`src/main/webapp`) | 65 |
| CSS / static assets | 7+ |

### Key Source Directories

```
EcommerceApp/
├── src/main/java/com/
│   ├── conn/          DBConnect.java — static SQLite connection singleton
│   ├── dao/           DAO.java … DAO5.java — plain JDBC data access
│   ├── entity/        13 plain Java beans (no annotations)
│   ├── servlet/       19 HttpServlet subclasses (@WebServlet)
│   └── utility/       MyUtilities.java — file upload helper
└── src/main/webapp/   65 JSP pages + CSS + images
```

### Identified Business Domains

| Domain | Servlets | Key JSPs | Entities |
|---|---|---|---|
| **Authentication** | checkadmin, checkcustomer | adminlogin, customerlogin, validatelogina/c | usermaster, customer |
| **Customer Management** | addcustomer, deletecustomer | customer_reg, managecustomers | customer |
| **Product Catalog** | addproduct | addproduct, viewproduct*, selecteditem*, category* | Product, brand, category, viewlist |
| **Product Categories** | — (JSP-direct) | mobile*, laptop*, tv*, watch* | mobile, laptop, tv, watch |
| **Shopping Cart** | addtocart, addtocartnull/a, removecart/a/null/nulla | cart, carta, cartnull/a, cartnullqty | cart |
| **Orders & Checkout** | payprocess, ShippingAddress2, removeorders, remove_orders, removetable_order_details | orders, orderdetails, ShippingAddress, confirmonline, confirmpayment, paymentfail | orders, order_details |
| **Contact Us** | addContactus, addContactusc, remove_contactus | contactus, contactusc | contactus |
| **Admin Management** | removetable_cart | managetables, adminhome, carta, cartnulla | — |

---

## Documentation Phases

### Phase 1 — Discovery (Batched)

**Agent**: `discovery`  
**Skill**: `java-analysis`  
**Strategy**: Five domain batches, each producing flows, components, and domain-concept documents. Followed by a consolidation step.

#### Batch 1 — Authentication & Customer Management

**Scope**: checkadmin, checkcustomer, addcustomer, deletecustomer  
**JSPs**: adminlogin, customerlogin, validatelogina, validateloginc, customer_reg, managecustomers, cupass/c, cufail/c, passc, fail/c  
**Entities**: usermaster, customer  
**Output**: `docs/discovery/batch-1-auth-*.md`

> Pay special attention to:
> - Cookie-based auth logic in checkadmin/checkcustomer (cname, tname cookies, maxAge=9999)
> - Flash-message cookies (maxAge=10) for login feedback
> - Password comparison logic in DAO layer
> - Customer registration validation (duplicate email checks)
> - Admin vs customer role separation (no shared session)

#### Batch 2 — Product Catalog & Categories

**Scope**: addproduct (servlet + JSP), viewproduct*, selecteditem*, category*, mobile*, laptop*, tv*, watch*  
**Entities**: Product, brand, category, viewlist, mobile, laptop, tv, watch  
**Output**: `docs/discovery/batch-2-catalog-*.md`

> Pay special attention to:
> - Image upload via MyUtilities.UploadFile() — path construction, extension validation
> - Product listing queries: WHERE conditions, ordering in DAO
> - Category filtering logic (separate entity classes per product type)
> - Guest/customer/admin view tripling pattern (JSP suffixes: none / c / a)
> - Cross-domain: product tables referenced in cart and order_details

#### Batch 3 — Shopping Cart

**Scope**: addtocart, addtocartnull, addtocartnulla, removecart, removecarta, removecartnull, removecartnulla  
**JSPs**: cart, carta, cartnull, cartnulla, cartnullqty  
**Entities**: cart  
**Output**: `docs/discovery/batch-3-cart-*.md`

> Pay special attention to:
> - Null cart handling (addtocartnull vs addtocart variants)
> - Admin vs customer cart view (carta vs cart)
> - Cart quantity validation — cartnullqty edge case
> - Cookie-driven customer identification in addtocart
> - Cross-domain: cart table read/written by both cart and checkout flows

#### Batch 4 — Orders & Checkout

**Scope**: payprocess, ShippingAddress2, removeorders, remove_orders, removetable_order_details  
**JSPs**: ShippingAddress, confirmonline, confirmpayment, paymentfail, orders, orderdetails, table_orders, table_order_details  
**Entities**: orders, order_details  
**Output**: `docs/discovery/batch-4-orders-*.md`

> Pay special attention to:
> - Payment flow: ShippingAddress → confirmpayment → payprocess sequence
> - Order creation SQL — which fields are written to orders vs order_details
> - COD vs online payment branching (confirmonline vs confirmpayment)
> - Cross-domain: cart is cleared after order placement
> - Admin order removal: removeorders vs remove_orders vs removetable_order_details distinction

#### Batch 5 — Admin Management & Contact Us

**Scope**: addContactus, addContactusc, remove_contactus, removetable_cart (admin table management)  
**JSPs**: contactus, contactusc, adminhome, managetables, managecustomers, table_cart, table_contactus, table_order_details, table_orders  
**Entities**: contactus  
**Output**: `docs/discovery/batch-5-admin-*.md`

> Pay special attention to:
> - Admin home dashboard data aggregation queries
> - Table management operations (removetable_cart, removetable_order_details)
> - Contact form differences: guest (addContactus) vs customer (addContactusc)
> - Admin-only access enforcement (tname cookie check)
> - Cross-domain: admin views all cart/order/customer data

#### Consolidation Step

After all 5 batches:

1. Merge entity and flow inventories across batches
2. Build `docs/discovery/cross-domain-table-matrix.md` — map each DB table to all flows that read or write it
3. Link related flows across batches (e.g., cart → checkout, product → cart)
4. Flag undocumented business rules in `docs/discovery/consolidation-gaps.md`

---

### Phase 2 — Business Documentation

**Agent**: `business-documenter`  
**Depends on**: Discovery phase complete

**Tasks**:
1. Define actors: Guest, Customer, Admin
2. Create use cases (UC_*.md) for each domain
3. Write business requirements (BUREQs) per use case
4. Create BPMN-style process descriptions (BP_*.md)
5. Produce business overview document

**Expected outputs**:
- `docs/business/index.md`
- `docs/business/overview/business-overview.md`
- `docs/business/use-cases/` — ~14 UC files
- `docs/business/processes/` — ~3 BP files

**Use Cases to produce** (minimum):

| ID | Title |
|---|---|
| UC_AUTH_001 | Admin Login |
| UC_AUTH_002 | Customer Login and Logout |
| UC_CUST_001 | Customer Self-Registration |
| UC_CUST_002 | Admin Manages Customers |
| UC_PROD_001 | Admin Adds Product |
| UC_PROD_002 | Browse and View Products |
| UC_CART_001 | Customer Adds Item to Cart |
| UC_CART_002 | Customer Removes Item from Cart |
| UC_ORD_001 | Customer Places Order (COD) |
| UC_ORD_002 | Customer Places Order (Online Payment) |
| UC_CONT_001 | Submit Contact Enquiry |
| UC_ADM_001 | Admin Manages Tables (cart/orders purge) |

---

### Phase 3 — Technical / Functional Documentation

**Agent**: `technical-documenter`  
**Skill**: `java-analysis`  
**Depends on**: Business phase complete

**Tasks**:
1. Derive functional requirements (FUREQ_*.md) from BUREQs
2. Document non-functional requirements (NFUREQ_*.md)
3. Create technical flow diagrams (FF_*.md)
4. Document data schemas and table structures
5. Document integration specifics (SQLite path, file upload path, cookie names)

**Expected outputs**:
- `docs/functional/index.md`
- `docs/functional/requirements/` — ~7 FUREQ + 2 NFUREQ files
- `docs/functional/flows/` — ~5 FF files
- `docs/functional/integration/INT_SQLITE.md`, `INT_FILE_UPLOAD.md`

**Flows to produce** (minimum):

| ID | Flow |
|---|---|
| FF_LOGIN | Admin and Customer authentication |
| FF_REGISTRATION | Customer self-registration |
| FF_ADD_TO_CART | Add product to cart (logged-in and guest) |
| FF_CHECKOUT | Shipping → payment → order creation |
| FF_ADD_PRODUCT | Admin product creation with image upload |

---

### Phase 4 — Coordination

**Agent**: `doc-coordinator`  
**Depends on**: Technical phase complete

**Tasks**:
1. Validate all directories and file naming conventions
2. Create `docs/index.md` — master landing page with links to all docs
3. Create `docs/system-overview.md` — architecture summary
4. Build traceability matrices (requirement → use case → flow)
5. Create domain concepts catalog and ubiquitous language glossary

**Expected outputs**:
- `docs/index.md`
- `docs/system-overview.md`
- `docs/domain/domain-concepts-catalog.md`
- `docs/domain/ubiquitous-language.md`
- `docs/domain/bounded-contexts.md`
- `docs/traceability/requirement-matrix.md`
- `docs/traceability/flow-to-component-map.md`
- `docs/traceability/id-registry.md`

---

### Phase 5 — Verification

**Agent**: `verification`  
**Depends on**: Coordination phase complete

**Tasks**:

| ID | Task |
|---|---|
| V-1 | Build table usage matrix — map every SQLite table to flows that read/write it |
| V-2 | Repository query deep analysis — read actual DAO SQL, compare WHERE conditions against documented business rules |
| V-3 | Validation completeness check — enumerate all servlet/DAO validations, verify each has true/false outcomes documented |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies are bidirectionally linked |
| V-5 | Entity state machine verification — map all status transitions in orders/cart, verify all are documented |
| V-6 | Generate consolidated gap report with severity classification and remediation prompts |

**Expected outputs**:
- `docs/verification/gap-report.md`
- `docs/verification/table-usage-matrix.md`
- `docs/verification/cross-domain-dependencies.md`

---

### Phase 6 — Remediation (conditional)

**Triggered by**: Verification agent finding Critical or High severity gaps  
**Action**: Re-run Discovery Agent on specific flows with targeted prompts from gap report  
**Followed by**: Re-run Verification Agent to confirm gaps are resolved

---

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | Planning Agent | 1 | Low |
| Discovery — 5 batches | Discovery Agent | 5 × 7 = 35 | High — deep query analysis + cross-domain |
| Discovery — Consolidation | Discovery Agent | 3 | Medium |
| Business | Business Documenter | 5 | Medium |
| Technical | Technical Documenter | 5 | High |
| Coordination | Doc Coordinator | 5 | Medium |
| Verification | Verification Agent | 6 | High |
| **Total** | | **60** | |

---

## Completeness Criteria

### Per-Flow

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true AND false outcomes documented
- [ ] Cookie-based auth checks are documented per role (cname / tname)
- [ ] Flash-message cookies (maxAge=10) and redirect targets are documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Error handling paths are documented (fail*.jsp redirect conditions)
- [ ] File upload integration specifics are documented where applicable
- [ ] Amount/price calculation logic is documented

### Per-Domain

- [ ] All flows meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows
- [ ] Functional requirements cover all business rules extracted from DAO SQL
- [ ] All JSP tripling variants (guest / customer / admin) are identified in component inventory

### Overall

- [ ] All 19 servlets appear in at least one documented flow
- [ ] All 5 DAO classes are mapped to at least one functional requirement
- [ ] All 13 entity beans are mapped to at least one domain concept
- [ ] All SQLite tables are present in the table usage matrix
- [ ] Verification gap report has zero Critical severity items

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json            ← State tracking (planning-agent)
├── documentation-plan.md              ← This file
├── index.md                           ← Master landing page (coordination)
├── system-overview.md                 ← Architecture overview (coordination)
├── discovery/
│   ├── batch-1-auth-flows.md
│   ├── batch-1-auth-components.md
│   ├── batch-1-auth-domain-concepts.md
│   ├── batch-2-catalog-flows.md
│   ├── batch-2-catalog-components.md
│   ├── batch-2-catalog-domain-concepts.md
│   ├── batch-3-cart-flows.md
│   ├── batch-3-cart-components.md
│   ├── batch-3-cart-domain-concepts.md
│   ├── batch-4-orders-flows.md
│   ├── batch-4-orders-components.md
│   ├── batch-4-orders-domain-concepts.md
│   ├── batch-5-admin-flows.md
│   ├── batch-5-admin-components.md
│   ├── batch-5-admin-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/
│   │   └── business-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md … UC_ADM_001.md
│   └── processes/
│       ├── BP_PURCHASE.md
│       ├── BP_REGISTRATION.md
│       └── BP_ADMIN_MGMT.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_AUTH_001.md … FUREQ_ORD_001.md
│   │   ├── NFUREQ_PERF_001.md
│   │   └── NFUREQ_SEC_001.md
│   ├── flows/
│   │   ├── FF_LOGIN.md
│   │   ├── FF_REGISTRATION.md
│   │   ├── FF_ADD_TO_CART.md
│   │   ├── FF_CHECKOUT.md
│   │   └── FF_ADD_PRODUCT.md
│   └── integration/
│       ├── INT_SQLITE.md
│       └── INT_FILE_UPLOAD.md
├── domain/
│   ├── domain-concepts-catalog.md
│   ├── ubiquitous-language.md
│   └── bounded-contexts.md
├── verification/
│   ├── gap-report.md
│   ├── table-usage-matrix.md
│   └── cross-domain-dependencies.md
└── traceability/
    ├── requirement-matrix.md
    ├── flow-to-component-map.md
    └── id-registry.md
```

# EcommerceApp — Documentation Plan

**Generated**: 2026-03-17  
**Module**: EcommerceApp  
**Language**: Java (Servlet 3.0 + JSP)  
**Framework**: servlet-jsp  
**Database**: SQLite  
**Build**: Maven WAR  

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Repository Structure](#2-repository-structure)
3. [Business Domains](#3-business-domains)
4. [Documentation Phases](#4-documentation-phases)
5. [Discovery Batches](#5-discovery-batches)
6. [Effort Estimate](#6-effort-estimate)
7. [Completeness Criteria](#7-completeness-criteria)
8. [Output Directory Structure](#8-output-directory-structure)

---

## 1. Project Overview

EcommerceApp is a Java J2EE online electronic shopping application built on the Servlet 3.0 + JSP stack. It allows customers to browse and purchase electronic products (mobiles, laptops, TVs, watches), manage their cart, and place orders. Administrators can manage the product catalogue, view customers, and moderate orders and contact enquiries.

**Key Technical Characteristics:**

| Property | Value |
|---|---|
| Language | Java 11 |
| Presentation | JSP + scriptlets (no JSTL) |
| Servlet Layer | 21 `@WebServlet`-annotated servlets |
| Data Layer | 5 DAO classes (DAO – DAO5), plain JDBC |
| Database | SQLite via `org.xerial:sqlite-jdbc` |
| Auth | Cookie-based (`cname` = customer email, `tname` = admin username) |
| File Uploads | Apache Commons FileUpload + `MyUtilities.UploadFile()` |
| Build | Maven WAR (`EcommerceApp-0.0.1-SNAPSHOT.war`) |

---

## 2. Repository Structure

```
EcommerceApp/
└── src/
    └── main/
        ├── java/com/
        │   ├── conn/
        │   │   └── DBConnect.java            # Static Connection singleton
        │   ├── dao/
        │   │   ├── DAO.java                  # Product, cart, order operations
        │   │   ├── DAO2.java
        │   │   ├── DAO3.java
        │   │   ├── DAO4.java
        │   │   └── DAO5.java
        │   ├── entity/                       # 14 plain Java beans
        │   │   ├── Product.java, brand.java, cart.java, category.java
        │   │   ├── contactus.java, customer.java, laptop.java, mobile.java
        │   │   ├── order_details.java, orders.java, tv.java, usermaster.java
        │   │   ├── viewlist.java, watch.java
        │   ├── servlet/                      # 21 HttpServlet subclasses
        │   │   └── [21 servlet classes]
        │   └── utility/
        │       └── MyUtilities.java          # File upload validation & write
        └── webapp/
            ├── *.jsp                         # 64 JSP pages
            ├── Css/                          # CSS stylesheets
            └── images/                       # Static image assets
```

**Codebase Metrics:**

| Component | Count |
|---|---|
| Servlet classes | 21 |
| DAO classes | 5 |
| Entity beans | 14 |
| JSP pages | 64 |
| Utility classes | 1 |
| CSS files | 7 |

---

## 3. Business Domains

The codebase is organized into **5 business domains** for discovery batching:

| Domain | Key Servlets | Key Entities | JSP Pages |
|---|---|---|---|
| **1. Authentication & Customer Mgmt** | checkadmin, checkcustomer, addcustomer, deletecustomer | usermaster, customer | adminlogin, customerlogin, customer_reg, validatelogina/c |
| **2. Product Catalogue** | addproduct | Product, brand, category, mobile, laptop, tv, watch | mobile/a/c, laptop/a/c, tv/a/c, watch/a/c, category/a/c, viewproduct/a/c, selecteditem/a/c |
| **3. Shopping Cart** | addtocart, addtocartnull, addtocartnulla, removecart, removecarta, removecartnull, removecartnulla | cart, viewlist | cart/a, cartnull/a, cartnullqty |
| **4. Order Management & Payment** | ShippingAddress2, payprocess, removeorders, remove_orders, removetable_cart, removetable_order_details | orders, order_details | ShippingAddress, confirmonline, confirmpayment, orders, orderdetails, paymentfail |
| **5. Customer Support & Admin** | addContactus, addContactusc, remove_contactus | contactus | contactus/c, adminhome, managecustomers, managetables, table_* |

**Cross-Domain Dependencies:**
- Cart ↔ Order Management: cart items are converted to orders at checkout
- Product Catalogue ↔ Cart: product IDs are referenced in cart entries
- Authentication ↔ All domains: `cname` cookie guards customer actions; `tname` guards admin actions
- Admin Management ↔ All domains: admin can view/delete cart rows and order_details rows directly

---

## 4. Documentation Phases

### Phase 1 — Planning ✅ COMPLETE

**Agent**: planning-agent  
**Status**: Complete  
**Output**:
- `docs/EcommerceApp-state.json`
- `docs/documentation-plan.md`

---

### Phase 2 — Discovery (5 Batches + Consolidation)

**Agent**: discovery  
**Skill**: `java-analysis`  
**Strategy**: One batch per business domain (5 batches × 7 tasks = 35 tasks), followed by a 3-task consolidation step.

**Per-batch tasks (7 tasks each):**
1. Detect entry points (servlet URL mappings, JSP direct access)
2. Trace execution flows (request → servlet → DAO → DB → JSP redirect)
3. **Deep data access query analysis** — read actual PreparedStatement SQL, document ALL WHERE conditions, JOINs, ordering as business rules
4. Inventory all components (servlets, DAOs, entities, JSPs)
5. Extract domain concepts (ubiquitous language, business terms)
6. Map inter-function dependencies and external integrations (file upload path, cookie names)
7. **Cross-domain table usage** — for each DB table read/written, search codebase for other domains using the same table

**Consolidation tasks (3 tasks):**
1. Merge all batch outputs, resolve duplicate concepts
2. Build cross-domain table matrix (`docs/discovery/cross-domain-table-matrix.md`)
3. Flag undocumented business rules (`docs/discovery/consolidation-gaps.md`)

**Output Files:**
```
docs/discovery/
├── batch-1-auth-customer-flows.md
├── batch-1-auth-customer-components.md
├── batch-1-auth-customer-domain-concepts.md
├── batch-2-product-catalogue-flows.md
├── batch-2-product-catalogue-components.md
├── batch-2-product-catalogue-domain-concepts.md
├── batch-3-shopping-cart-flows.md
├── batch-3-shopping-cart-components.md
├── batch-3-shopping-cart-domain-concepts.md
├── batch-4-order-payment-flows.md
├── batch-4-order-payment-components.md
├── batch-4-order-payment-domain-concepts.md
├── batch-5-support-admin-flows.md
├── batch-5-support-admin-components.md
├── batch-5-support-admin-domain-concepts.md
├── cross-domain-table-matrix.md
└── consolidation-gaps.md
```

---

### Phase 3 — Business Documentation

**Agent**: business-documenter  
**Skill**: `java-analysis`  
**Tasks (5):**
1. Define actors (Guest, Registered Customer, Administrator)
2. Create use cases (`UC_*.md`) for each domain
3. Write business requirements (BUREQs)
4. Create business process diagrams (BPMN-style in Mermaid)
5. Write business overview document

**Expected Use Cases:**

| ID | Title | Domain |
|---|---|---|
| UC_AUTH_001 | Customer Registration | Authentication |
| UC_AUTH_002 | Customer Login / Logout | Authentication |
| UC_AUTH_003 | Admin Login / Logout | Authentication |
| UC_PROD_001 | Browse Product Catalogue | Product Catalogue |
| UC_PROD_002 | Add / Manage Product (Admin) | Product Catalogue |
| UC_CART_001 | Add Item to Cart | Shopping Cart |
| UC_CART_002 | Remove Item from Cart | Shopping Cart |
| UC_ORD_001 | Checkout and Place Order | Order Management |
| UC_ORD_002 | Online Payment Processing | Order Management |
| UC_ORD_003 | View / Cancel Order | Order Management |
| UC_SUP_001 | Submit Contact Us Enquiry | Customer Support |

**Expected Business Processes:**

| ID | Title |
|---|---|
| BP_CUSTOMER_REGISTRATION | Customer self-registration flow |
| BP_PRODUCT_BROWSING | Browsing and selecting products |
| BP_CHECKOUT | Cart-to-order conversion and payment |
| BP_ORDER_MANAGEMENT | Admin order review and customer order tracking |
| BP_ADMIN_MANAGEMENT | Admin table and customer management |

**Output Files:**
```
docs/business/
├── index.md
├── overview/
│   └── system-overview.md
├── use-cases/
│   └── UC_*.md  (11 files)
└── processes/
    └── BP_*.md  (5 files)
```

---

### Phase 4 — Technical / Functional Documentation

**Agent**: technical-documenter  
**Skill**: `java-analysis`  
**Tasks (5):**
1. Derive functional requirements (FUREQs) from BUREQs
2. Document non-functional requirements (NFUREQs): security, performance, availability
3. Create technical flow diagrams (sequence diagrams in Mermaid)
4. Document servlet API mapping (URL → servlet → method → DAO → SQL)
5. Document database schema (tables, columns, constraints)

**Expected Functional Requirements:**

| ID | Title |
|---|---|
| FUREQ_AUTH_001 | Customer registration validation rules |
| FUREQ_AUTH_002 | Cookie-based session management |
| FUREQ_PROD_001 | Product catalogue retrieval and filtering |
| FUREQ_CART_001 | Cart add/remove with quantity management |
| FUREQ_ORD_001 | Order placement and shipping address capture |
| FUREQ_ORD_002 | Payment processing (COD vs. online) |

**Expected Non-Functional Requirements:**

| ID | Title |
|---|---|
| NFUREQ_SEC_001 | Cookie-based auth security constraints |
| NFUREQ_PERF_001 | Static connection singleton performance notes |

**Expected Technical Flows:**

| ID | Title |
|---|---|
| FF_LOGIN_FLOW | Admin and customer login flow |
| FF_REGISTRATION_FLOW | Customer registration flow |
| FF_ADD_TO_CART_FLOW | Add to cart (authenticated and guest) |
| FF_CHECKOUT_FLOW | Checkout and shipping address flow |
| FF_PAYMENT_FLOW | Payment selection and confirmation flow |

**Output Files:**
```
docs/functional/
├── index.md
├── requirements/
│   ├── FUREQ_*.md  (6 files)
│   └── NFUREQ_*.md  (2 files)
├── flows/
│   └── FF_*.md  (5 files)
└── integration/
    ├── DB_SCHEMA.md
    └── SERVLET_API_MAP.md
```

---

### Phase 5 — Documentation Coordination

**Agent**: doc-coordinator  
**Tasks (5):**
1. Validate directory structure and cross-references
2. Create master index (`docs/index.md`)
3. Create system overview (`docs/system-overview.md`)
4. Build traceability matrix (BUREQ → FUREQ → Flow → Component)
5. Create domain concepts catalog and ubiquitous language glossary

**Output Files:**
```
docs/
├── index.md
├── system-overview.md
├── domain/
│   ├── domain-concepts-catalog.md
│   ├── ubiquitous-language.md
│   └── bounded-contexts.md
└── traceability/
    ├── requirement-matrix.md
    ├── flow-to-component-map.md
    └── id-registry.md
```

---

### Phase 6 — Verification

**Agent**: verification  
**Tasks (6):**

| Task | Description |
|---|---|
| V-1 | Build table usage matrix — map every DB table to flows that read/write it; flag cross-domain dependencies |
| V-2 | Repository query deep analysis — read PreparedStatement SQL in DAO*.java, compare WHERE conditions against documented business rules |
| V-3 | Validation completeness check — enumerate all input validations in servlets and JSPs, verify true/false outcomes documented |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links |
| V-5 | Entity state machine verification — map all status transitions (order statuses, cart states) and verify all are documented |
| V-6 | Generate consolidated gap report with severity (Critical/High/Medium/Low) and remediation prompts |

**Output Files:**
```
docs/verification/
├── gap-report.md
├── table-usage-matrix.md
└── cross-domain-dependencies.md
```

---

## 5. Discovery Batches

### Batch 1 — Authentication & Customer Management

> Run discovery for Batch 1 (Authentication & Customer Management):
> Servlets: `checkadmin`, `checkcustomer`, `addcustomer`, `deletecustomer`  
> JSPs: `adminlogin.jsp`, `customerlogin.jsp`, `customer_reg.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `adminhome.jsp`, `managecustomers.jsp`  
> Entities: `usermaster`, `customer`  
> DAOs: `DAO.java` (inspect all authentication-related methods)  
> Output to: `docs/discovery/batch-1-auth-customer-*.md`
>
> Pay special attention to:
> - Cookie creation logic in `checkadmin` and `checkcustomer` — document cookie names (`tname`, `cname`), `maxAge` values, and auth conditions
> - `addcustomer` — enumerate ALL form field validations (duplicate email check, required fields)
> - `deletecustomer` — document which tables are affected and whether cascading is applied
> - Flash message cookies (short `maxAge=10`) — identify all feedback cookies used
> - Cross-domain dependency: `tname` cookie is read by admin servlets across ALL domains

### Batch 2 — Product Catalogue

> Run discovery for Batch 2 (Product Catalogue):
> Servlets: `addproduct`  
> JSPs: `mobile.jsp`/`mobilea.jsp`/`mobilec.jsp`, `laptop.jsp`/`laptopa.jsp`/`laptopc.jsp`, `tv.jsp`/`tva.jsp`/`tvc.jsp`, `watch.jsp`/`watcha.jsp`/`watchc.jsp`, `category.jsp`/`categorya.jsp`/`categoryc.jsp`, `viewproduct.jsp`/`viewproducta.jsp`/`viewproductc.jsp`, `selecteditem.jsp`/`selecteditema.jsp`/`selecteditemc.jsp`, `addproduct.jsp`  
> Entities: `Product`, `brand`, `category`, `mobile`, `laptop`, `tv`, `watch`  
> DAOs: all DAO files (inspect product retrieval and insert methods)  
> Output to: `docs/discovery/batch-2-product-catalogue-*.md`
>
> Pay special attention to:
> - JSP tripling pattern (guest/customer/admin variants) — document which variant shows which data
> - `addproduct` servlet — document `MyUtilities.UploadFile()` integration (file extension validation, upload path)
> - DAO product list queries — document ALL WHERE clauses used for category filtering
> - Cross-domain dependency: product tables are read by the cart domain when adding items

### Batch 3 — Shopping Cart

> Run discovery for Batch 3 (Shopping Cart):
> Servlets: `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla`  
> JSPs: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`  
> Entities: `cart`, `viewlist`  
> DAOs: all DAO files (inspect cart add/remove/view methods)  
> Output to: `docs/discovery/batch-3-shopping-cart-*.md`
>
> Pay special attention to:
> - Difference between `addtocart` (authenticated), `addtocartnull` (guest?), and `addtocartnulla` (admin?) — document the auth check in each
> - `removecart` vs `removecarta` vs `removecartnull` vs `removecartnulla` — document each variant's auth context and SQL
> - `cartnullqty.jsp` — document the quantity validation logic
> - Cross-domain dependency: `cart` table is read by the checkout/payment flow in Batch 4
> - Cross-domain dependency: `removetable_cart` in Batch 4 deletes all cart rows (post-order)

### Batch 4 — Order Management & Payment

> Run discovery for Batch 4 (Order Management & Payment):
> Servlets: `ShippingAddress2`, `payprocess`, `removeorders`, `remove_orders`, `removetable_cart`, `removetable_order_details`  
> JSPs: `ShippingAddress.jsp`, `confirmonline.jsp`, `confirmpayment.jsp`, `orders.jsp`, `orderdetails.jsp`, `paymentfail.jsp`  
> Entities: `orders`, `order_details`  
> DAOs: all DAO files (inspect order insert, order list, order delete methods)  
> Output to: `docs/discovery/batch-4-order-payment-*.md`
>
> Pay special attention to:
> - `ShippingAddress2` — document ALL form fields captured, validation performed, and what triggers redirect to payment vs. failure
> - `payprocess` — document payment modes (COD, online), the order record insert, and the cart-clearance step
> - `confirmonline.jsp` vs `confirmpayment.jsp` — document the distinction (COD vs. online payment confirmation)
> - `removetable_cart` and `removetable_order_details` — document the admin bulk-delete operations and auth guard
> - Cross-domain dependency: `removetable_cart` deletes rows from the `cart` table (shared with Batch 3)
> - Order status transitions: document all possible order states

### Batch 5 — Customer Support & Admin Management

> Run discovery for Batch 5 (Customer Support & Admin Management):
> Servlets: `addContactus`, `addContactusc`, `remove_contactus`  
> JSPs: `contactus.jsp`, `contactusc.jsp`, `table_cart.jsp`, `table_contactus.jsp`, `table_order_details.jsp`, `table_orders.jsp`, `managecustomers.jsp`, `managetables.jsp`, `z1.jsp`, `z2.jsp`  
> Entities: `contactus`  
> DAOs: all DAO files (inspect contactus insert/delete and admin list queries)  
> Output to: `docs/discovery/batch-5-support-admin-*.md`
>
> Pay special attention to:
> - `addContactus` vs `addContactusc` — document which is admin-facing vs customer-facing
> - `remove_contactus` — document auth guard (admin only) and SQL
> - `table_*.jsp` pages — document what data they expose, whether pagination exists, and auth enforcement
> - `managetables.jsp` — document the admin table management capability and associated risk
> - Cross-domain dependency: `table_cart.jsp` reads the `cart` table; `table_order_details.jsp` reads `order_details`
> - `z1.jsp` / `z2.jsp` — identify purpose (likely temp/debug pages) and document

---

## 6. Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | planning-agent | 1 | Low |
| Discovery — Batch 1 (Auth) | discovery | 7 | High |
| Discovery — Batch 2 (Products) | discovery | 7 | High |
| Discovery — Batch 3 (Cart) | discovery | 7 | High |
| Discovery — Batch 4 (Orders) | discovery | 7 | High |
| Discovery — Batch 5 (Support/Admin) | discovery | 7 | High |
| Discovery — Consolidation | discovery | 3 | Medium |
| Business Documentation | business-documenter | 5 | Medium |
| Technical Documentation | technical-documenter | 5 | High |
| Coordination | doc-coordinator | 5 | Medium |
| Verification | verification | 6 | High |
| **Total** | | **60** | |

---

## 7. Completeness Criteria

### Per-Flow Completeness Criteria

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions (in servlets and JSPs) have both **true** AND **false** outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Cookie names, `maxAge` values, and auth conditions are explicitly documented
- [ ] Error handling paths (redirect to `fail*.jsp`, `cufail*.jsp`, `paymentfail.jsp`) are documented with trigger conditions
- [ ] File upload paths, accepted extensions, and failure modes are documented (for `addproduct`)
- [ ] Order/payment flow documents all payment modes and their divergent paths

### Per-Domain Completeness Criteria

- [ ] All flows in the domain meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked in both the producing and consuming domain docs
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries
- [ ] JSP tripling variants (guest/customer/admin) are all accounted for

### Verification Acceptance Criteria

- [ ] Zero unlinked cross-domain table dependencies
- [ ] Zero undocumented business rules from DAO WHERE clauses
- [ ] Zero gaps in validation true/false outcome documentation
- [ ] All 21 servlet endpoints mapped to at least one functional flow
- [ ] All 14 entity beans referenced in at least one domain concept
- [ ] All 5 DAO classes have their SQL queries catalogued

---

## 8. Output Directory Structure

```
docs/
├── EcommerceApp-state.json           # State tracking file
├── documentation-plan.md             # This plan
├── index.md                          # Master landing page (Phase 5)
├── system-overview.md                # Architecture overview (Phase 5)
├── discovery/                        # Phase 2 outputs
│   ├── batch-1-auth-customer-flows.md
│   ├── batch-1-auth-customer-components.md
│   ├── batch-1-auth-customer-domain-concepts.md
│   ├── batch-2-product-catalogue-flows.md
│   ├── batch-2-product-catalogue-components.md
│   ├── batch-2-product-catalogue-domain-concepts.md
│   ├── batch-3-shopping-cart-flows.md
│   ├── batch-3-shopping-cart-components.md
│   ├── batch-3-shopping-cart-domain-concepts.md
│   ├── batch-4-order-payment-flows.md
│   ├── batch-4-order-payment-components.md
│   ├── batch-4-order-payment-domain-concepts.md
│   ├── batch-5-support-admin-flows.md
│   ├── batch-5-support-admin-components.md
│   ├── batch-5-support-admin-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/                         # Phase 3 outputs
│   ├── index.md
│   ├── overview/
│   │   └── system-overview.md
│   ├── use-cases/
│   │   └── UC_*.md
│   └── processes/
│       └── BP_*.md
├── functional/                       # Phase 4 outputs
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_*.md
│   │   └── NFUREQ_*.md
│   ├── flows/
│   │   └── FF_*.md
│   └── integration/
│       ├── DB_SCHEMA.md
│       └── SERVLET_API_MAP.md
├── domain/                           # Phase 4/5 outputs
│   ├── domain-concepts-catalog.md
│   ├── ubiquitous-language.md
│   └── bounded-contexts.md
├── verification/                     # Phase 6 outputs
│   ├── gap-report.md
│   ├── table-usage-matrix.md
│   └── cross-domain-dependencies.md
└── traceability/                     # Phase 5 outputs
    ├── requirement-matrix.md
    ├── flow-to-component-map.md
    └── id-registry.md
```

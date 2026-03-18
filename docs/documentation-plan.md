# EcommerceApp — Documentation Plan

**Module**: EcommerceApp  
**Language**: Java  
**Framework**: Servlet 3.0 + JSP  
**Database**: SQLite (via `org.xerial:sqlite-jdbc`)  
**Build Tool**: Maven  
**Generated**: 2026-03-18  
**Planning Agent**: planning-agent  

---

## 1. Project Overview

EcommerceApp is a Java J2EE online electronic shopping application built on Servlet 3.0 and JSP without any framework (no Spring, no ORM, no DI container). It is deployed on Apache Tomcat 8+ as a WAR artifact.

### Detected Structure

| Artefact Type | Count |
|---|---|
| Servlet classes (`@WebServlet`) | 21 |
| DAO classes (`DAO.java`–`DAO5.java`) | 5 |
| Entity beans | 14 |
| JSP pages | 64 |
| CSS stylesheets | 7 |
| Build descriptor | `pom.xml` (Maven) |
| Database | `mydatabase.db` (SQLite) |

### Identified Business Domains

| Domain | Servlets | Key JSPs | Entities |
|---|---|---|---|
| **Authentication** | `checkadmin`, `checkcustomer` | `adminlogin`, `customerlogin`, `validatelogina`, `validateloginc` | `usermaster`, `customer` |
| **Customer Management** | `addcustomer`, `deletecustomer` | `customer_reg`, `customerhome`, `managecustomers` | `customer` |
| **Product Catalog** | `addproduct` | `mobile/*`, `laptop/*`, `tv/*`, `watch/*`, `category/*`, `viewproduct/*`, `selecteditem/*`, `addproduct` | `Product`, `mobile`, `laptop`, `tv`, `watch`, `brand`, `category` |
| **Shopping Cart** | `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla`, `removetable_cart` | `cart`, `carta`, `cartnull`, `cartnulla`, `cartnullqty` | `cart` |
| **Orders & Checkout** | `payprocess`, `ShippingAddress2`, `removeorders`, `remove_orders`, `removetable_order_details` | `ShippingAddress`, `confirmonline`, `confirmpayment`, `orders`, `orderdetails`, `paymentfail` | `orders`, `order_details` |
| **Contact Us** | `addContactus`, `addContactusc`, `remove_contactus` | `contactus`, `contactusc`, `table_contactus` | `contactus` |
| **Admin Dashboard** | *(via JSP scriptlets)* | `adminhome`, `managetables`, `table_cart`, `table_orders`, `table_order_details` | *(cross-domain)* |

### JSP Tripling Pattern

Most user-facing pages exist in three variants:
- **No suffix** (e.g., `mobile.jsp`) — Guest / anonymous view
- **`a` suffix** (e.g., `mobilea.jsp`) — Admin view
- **`c` suffix** (e.g., `mobilec.jsp`) — Customer (logged-in) view

### Authentication Mechanism

Cookie-based authentication:
- `cname` cookie → customer email address (max age 9999)
- `tname` cookie → admin username (max age 9999)
- Flash messages via short-lived cookies (max age 10)

---

## 2. Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | planning-agent | 1 | Low |
| Discovery Batch 1 — Auth & Customer | discovery | 7 | High |
| Discovery Batch 2 — Product Catalog | discovery | 7 | High |
| Discovery Batch 3 — Shopping Cart | discovery | 7 | High |
| Discovery Batch 4 — Orders & Checkout | discovery | 7 | High |
| Discovery Batch 5 — ContactUs & Admin | discovery | 7 | High |
| Discovery Consolidation | discovery | 3 | Medium |
| Business Documenter | business-documenter | 5 | Medium |
| Technical Documenter | technical-documenter | 5 | High |
| Doc Coordinator | doc-coordinator | 5 | Medium |
| Verification | verification | 6 | High |
| **Total** | | **59** | |

---

## 3. Phase Definitions

### Phase 1 — Discovery (5 Batches + Consolidation)

**Agent**: `discovery`  
**Skill**: `java-analysis`  
**Strategy**: Batch by business domain. Each batch produces three discovery artefacts. After all batches, a consolidation step merges outputs and builds the cross-domain table matrix.

---

#### Batch 1 — Authentication & Customer Management

**Output directory**: `docs/discovery/`  
**Artefacts**:
- `batch-1-auth-customer-flows.md`
- `batch-1-auth-customer-components.md`
- `batch-1-auth-customer-domain-concepts.md`

**Servlets in scope**:
- `checkadmin.java` — Admin login verification
- `checkcustomer.java` — Customer login verification
- `addcustomer.java` — Customer self-registration
- `deletecustomer.java` — Admin deletes a customer

**JSPs in scope**: `adminlogin.jsp`, `customerlogin.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `customer_reg.jsp`, `customerhome.jsp`, `managecustomers.jsp`, `fail.jsp`, `failc.jsp`, `cufail.jsp`, `cufailc.jsp`, `cupass.jsp`, `cupassc.jsp`, `passc.jsp`

**DAOs involved**: `DAO.java`, `DAO2.java`

**Pay special attention to**:
- Cookie-setting logic (`cname`, `tname`, max-age values)
- Password comparison — is it plaintext or hashed?
- Exact SQL WHERE clauses used for login lookup (which fields, case sensitivity)
- What data is collected during customer registration (validate all fields)
- Flash cookie patterns for success/failure feedback
- Auth guard pattern — how servlets check if a user is logged in before acting

---

#### Batch 2 — Product Catalog

**Output directory**: `docs/discovery/`  
**Artefacts**:
- `batch-2-product-catalog-flows.md`
- `batch-2-product-catalog-components.md`
- `batch-2-product-catalog-domain-concepts.md`

**Servlets in scope**:
- `addproduct.java` — Admin adds a product with image upload

**JSPs in scope**: `addproduct.jsp`, `viewproduct.jsp`, `viewproducta.jsp`, `viewproductc.jsp`, `category.jsp`, `categorya.jsp`, `categoryc.jsp`, `mobile.jsp`, `mobilea.jsp`, `mobilec.jsp`, `laptop.jsp`, `laptopa.jsp`, `laptopc.jsp`, `tv.jsp`, `tva.jsp`, `tvc.jsp`, `watch.jsp`, `watcha.jsp`, `watchc.jsp`, `selecteditem.jsp`, `selecteditema.jsp`, `selecteditemc.jsp`

**DAOs involved**: `DAO.java`, `DAO2.java`, `DAO3.java`

**Entities in scope**: `Product`, `mobile`, `laptop`, `tv`, `watch`, `brand`, `category`, `viewlist`

**Pay special attention to**:
- `MyUtilities.UploadFile()` — file extension validation rules, upload path construction
- SQL queries for product listing (category filter, brand filter, pagination if any)
- How product images are stored (path or BLOB?)
- `viewlist` entity role — is it used for cross-category display?
- DAO methods that list products by category — exact WHERE clause conditions
- How the three JSP variants differ in what they display (admin edit buttons vs customer buy buttons)

---

#### Batch 3 — Shopping Cart

**Output directory**: `docs/discovery/`  
**Artefacts**:
- `batch-3-shopping-cart-flows.md`
- `batch-3-shopping-cart-components.md`
- `batch-3-shopping-cart-domain-concepts.md`

**Servlets in scope**:
- `addtocart.java` — Add to cart (logged-in customer)
- `addtocartnull.java` — Add to cart (guest, no login) — appears to be variant 1
- `addtocartnulla.java` — Add to cart (guest, admin context?) — variant 2
- `removecart.java` — Remove cart item (customer)
- `removecarta.java` — Remove cart item (admin view)
- `removecartnull.java` — Remove cart item (guest)
- `removecartnulla.java` — Remove cart item (guest alt)
- `removetable_cart.java` — Admin bulk-clear cart table

**JSPs in scope**: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`

**DAOs involved**: `DAO3.java`, `DAO4.java`

**Entities in scope**: `cart`

**Pay special attention to**:
- How guest cart vs authenticated cart differ (session? cookie? separate table rows?)
- `cartnullqty.jsp` — what triggers a "null quantity" state?
- Exact SQL INSERT/SELECT used when adding an item (does it check for duplicate items before inserting?)
- Quantity handling — can users change quantity or only add/remove?
- `removetable_cart` vs `removecart` — full table wipe vs single item remove
- Cross-domain dependency: cart table is read during checkout (Batch 4)

---

#### Batch 4 — Orders & Checkout

**Output directory**: `docs/discovery/`  
**Artefacts**:
- `batch-4-orders-checkout-flows.md`
- `batch-4-orders-checkout-components.md`
- `batch-4-orders-checkout-domain-concepts.md`

**Servlets in scope**:
- `payprocess.java` — Payment processing and order creation
- `ShippingAddress2.java` — Capture shipping address
- `removeorders.java` — Customer removes an order
- `remove_orders.java` — Admin removes orders (bulk)
- `removetable_order_details.java` — Admin removes order_details records

**JSPs in scope**: `ShippingAddress.jsp`, `confirmonline.jsp`, `confirmpayment.jsp`, `orders.jsp`, `orderdetails.jsp`, `paymentfail.jsp`, `table_orders.jsp`, `table_order_details.jsp`

**DAOs involved**: `DAO4.java`, `DAO5.java`

**Entities in scope**: `orders`, `order_details`

**Pay special attention to**:
- Full checkout sequence: cart → shipping address → confirm → pay → order record created
- Exact SQL INSERT for orders and order_details (all columns, data sources)
- Is payment real (gateway integration) or simulated? If simulated, what logic determines pass/fail?
- Order status transitions (placed → shipped → delivered?) — are any status values stored?
- How order_details links to cart items (product ID, quantity, price snapshot)
- Cross-domain dependency: reads cart table to create order_details
- Cross-domain dependency: reads customer/product tables for order data

---

#### Batch 5 — Contact Us & Admin Dashboard

**Output directory**: `docs/discovery/`  
**Artefacts**:
- `batch-5-contactus-admin-flows.md`
- `batch-5-contactus-admin-components.md`
- `batch-5-contactus-admin-domain-concepts.md`

**Servlets in scope**:
- `addContactus.java` — Submit contact form (guest)
- `addContactusc.java` — Submit contact form (customer)
- `remove_contactus.java` — Admin removes contact submissions

**JSPs in scope**: `contactus.jsp`, `contactusc.jsp`, `table_contactus.jsp`, `adminhome.jsp`, `managetables.jsp`, `index.jsp`, `aboutus.jsp`, `aboutusa.jsp`, `aboutusc.jsp`, `z1.jsp`, `z2.jsp`, `footer.jsp`, `navbar.jsp`, `customer_navbar.jsp`, `admin_navbar.jsp`

**DAOs involved**: `DAO5.java`

**Entities in scope**: `contactus`

**Pay special attention to**:
- Fields captured by the contact form (name, email, message — validate all)
- Difference between guest and customer contact form submissions
- Admin dashboard: what aggregated data / statistics are displayed on `adminhome.jsp`
- `managetables.jsp` — does it show raw DB tables or processed data?
- Navigation bar includes and what links differ between guest/customer/admin
- `z1.jsp` and `z2.jsp` — purpose unclear, document what they contain
- `index.jsp` — entry point flow for the application

---

#### Consolidation Step

After all 5 batches:

1. **Merge batch outputs** — combine all discovered flows, components, domain concepts into unified lists
2. **Build cross-domain table matrix** (`docs/discovery/cross-domain-table-matrix.md`) — map every DB table to every servlet/JSP that reads or writes it
3. **Flag undocumented business rules** (`docs/discovery/consolidation-gaps.md`) — list any WHERE clause conditions, validations, or status transitions not fully explained by the current batch docs

---

### Phase 2 — Business Documentation

**Agent**: `business-documenter`  
**Skill**: `java-analysis`  

**Tasks**:
1. Define actors (Guest, Customer, Admin)
2. Create use cases (`UC_*.md`) for each major flow
3. Write BUREQs (business requirements register)
4. Create BPMN-style business process documents (`BP_*.md`)
5. Write business overview (`business/overview/business-overview.md`)

**Expected artefacts**:
- `docs/business/index.md`
- `docs/business/overview/business-overview.md`
- `docs/business/use-cases/UC_AUTH_001.md` — Admin Login
- `docs/business/use-cases/UC_AUTH_002.md` — Customer Login
- `docs/business/use-cases/UC_CUST_001.md` — Customer Registration
- `docs/business/use-cases/UC_CUST_002.md` — Customer Deletion (admin)
- `docs/business/use-cases/UC_PROD_001.md` — Product Management
- `docs/business/use-cases/UC_CART_001.md` — Add to Cart
- `docs/business/use-cases/UC_CART_002.md` — Remove from Cart
- `docs/business/use-cases/UC_ORD_001.md` — Place Order / Checkout
- `docs/business/use-cases/UC_ORD_002.md` — View / Cancel Order
- `docs/business/use-cases/UC_CONT_001.md` — Submit Contact Form
- `docs/business/processes/BP_REGISTRATION.md`
- `docs/business/processes/BP_PRODUCT_CATALOG.md`
- `docs/business/processes/BP_SHOPPING_CART.md`
- `docs/business/processes/BP_CHECKOUT.md`
- `docs/business/processes/BP_CONTACTUS.md`
- `docs/business/BUREQ.md`

---

### Phase 3 — Technical / Functional Documentation

**Agent**: `technical-documenter`  
**Skill**: `java-analysis`  

**Tasks**:
1. Derive functional requirements (`FUREQ_*.md`) from BUREQs
2. Document non-functional requirements (`NFUREQ_*.md`)
3. Create technical flow diagrams (`FF_*.md`)
4. Document servlet API mapping and data schemas
5. Document validation rules extracted from code

**Expected artefacts**:
- `docs/functional/index.md`
- `docs/functional/requirements/FUREQ_AUTH_001.md`
- `docs/functional/requirements/FUREQ_CUST_001.md`
- `docs/functional/requirements/FUREQ_PROD_001.md`
- `docs/functional/requirements/FUREQ_CART_001.md`
- `docs/functional/requirements/FUREQ_ORD_001.md`
- `docs/functional/requirements/FUREQ_CONT_001.md`
- `docs/functional/requirements/NFUREQ_001.md`
- `docs/functional/flows/FF_LOGIN.md`
- `docs/functional/flows/FF_REGISTRATION.md`
- `docs/functional/flows/FF_PRODUCT_BROWSE.md`
- `docs/functional/flows/FF_CART.md`
- `docs/functional/flows/FF_CHECKOUT.md`
- `docs/functional/integration/DB_SCHEMA.md`
- `docs/functional/integration/SERVLET_API_MAP.md`

---

### Phase 4 — Coordination

**Agent**: `doc-coordinator`  
**Skill**: `java-analysis`  

**Tasks**:
1. Validate output directory structure matches this plan
2. Create master landing page (`docs/index.md`)
3. Create system overview (`docs/system-overview.md`)
4. Build traceability matrix (`docs/traceability/requirement-matrix.md`)
5. Create domain concepts catalogue and ubiquitous language glossary

**Expected artefacts**:
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
**Skill**: `java-analysis`  

**Tasks**:
1. **V-1** Build table usage matrix — map every DB table to flows that read/write it, flag cross-domain dependencies
2. **V-2** Repository query deep analysis — read actual DAO code, compare WHERE conditions against documented business rules
3. **V-3** Validation completeness check — enumerate all validations in servlet/JSP code, verify each has condition + true/false outcomes documented
4. **V-4** Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links
5. **V-5** Entity state machine verification — map all status transitions in code, verify all are documented
6. **V-6** Generate consolidated gap report with severity classification (Critical / High / Medium / Low) and remediation prompts

**Expected artefacts**:
- `docs/verification/gap-report.md`
- `docs/verification/table-usage-matrix.md`
- `docs/verification/cross-domain-dependencies.md`

---

### Phase 6 — Remediation (Conditional)

Triggered only if Verification finds **Critical** or **High** severity gaps.

1. Re-run Discovery Agent on specific flows identified in gap report
2. Re-run Verification Agent to confirm gaps resolved
3. Update state file with remediation results

---

## 4. Completeness Criteria

### Per-Flow Completeness

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both TRUE and FALSE outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from → to states
- [ ] Error handling paths are documented (which fail JSP is targeted and why)
- [ ] Cookie usage is explicitly listed (name, value, max-age, purpose)
- [ ] File upload validation rules are documented (allowed extensions, size limits if any)

### Per-Domain Completeness

- [ ] All flows within the domain meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries

### Special Focus Areas for EcommerceApp

- **Authentication cookies** — `cname` and `tname` lifecycle (set on login, cleared on logout, used for auth checks)
- **JSP tripling pattern** — each triplet (guest/admin/customer) must be accounted for in all use cases
- **Guest cart vs authenticated cart** — cross-domain dependency between cart and authentication
- **Payment simulation** — document exact logic if no real gateway is present
- **Admin bulk operations** — `removetable_*` servlets operate on entire tables; document the business justification and risk
- **Image upload path** — hardcoded path in `DAO.java` must be noted as a deployment dependency

---

## 5. Output Directory Structure

```
docs/
├── EcommerceApp-state.json           # State tracking file (this run)
├── documentation-plan.md             # This plan
├── index.md                          # Master landing page (Phase 4)
├── system-overview.md                # Architecture overview (Phase 4)
├── discovery/
│   ├── batch-1-auth-customer-flows.md
│   ├── batch-1-auth-customer-components.md
│   ├── batch-1-auth-customer-domain-concepts.md
│   ├── batch-2-product-catalog-flows.md
│   ├── batch-2-product-catalog-components.md
│   ├── batch-2-product-catalog-domain-concepts.md
│   ├── batch-3-shopping-cart-flows.md
│   ├── batch-3-shopping-cart-components.md
│   ├── batch-3-shopping-cart-domain-concepts.md
│   ├── batch-4-orders-checkout-flows.md
│   ├── batch-4-orders-checkout-components.md
│   ├── batch-4-orders-checkout-domain-concepts.md
│   ├── batch-5-contactus-admin-flows.md
│   ├── batch-5-contactus-admin-components.md
│   ├── batch-5-contactus-admin-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/
│   │   └── business-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md
│   │   ├── UC_AUTH_002.md
│   │   ├── UC_CUST_001.md
│   │   ├── UC_CUST_002.md
│   │   ├── UC_PROD_001.md
│   │   ├── UC_CART_001.md
│   │   ├── UC_CART_002.md
│   │   ├── UC_ORD_001.md
│   │   ├── UC_ORD_002.md
│   │   └── UC_CONT_001.md
│   ├── processes/
│   │   ├── BP_REGISTRATION.md
│   │   ├── BP_PRODUCT_CATALOG.md
│   │   ├── BP_SHOPPING_CART.md
│   │   ├── BP_CHECKOUT.md
│   │   └── BP_CONTACTUS.md
│   └── BUREQ.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_AUTH_001.md
│   │   ├── FUREQ_CUST_001.md
│   │   ├── FUREQ_PROD_001.md
│   │   ├── FUREQ_CART_001.md
│   │   ├── FUREQ_ORD_001.md
│   │   ├── FUREQ_CONT_001.md
│   │   └── NFUREQ_001.md
│   ├── flows/
│   │   ├── FF_LOGIN.md
│   │   ├── FF_REGISTRATION.md
│   │   ├── FF_PRODUCT_BROWSE.md
│   │   ├── FF_CART.md
│   │   └── FF_CHECKOUT.md
│   └── integration/
│       ├── DB_SCHEMA.md
│       └── SERVLET_API_MAP.md
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

---

## 6. Known Risks and Special Considerations

| Risk | Impact | Mitigation in Docs |
|---|---|---|
| Hardcoded SQLite DB path in `DBConnect.java` | Deployment dependency | Document in `DB_SCHEMA.md` and `NFUREQ_001.md` |
| Hardcoded image upload path in `DAO.java` | Deployment dependency | Document in `FUREQ_PROD_001.md` |
| Static `Connection` singleton — not thread-safe | Concurrency defect | Note in `NFUREQ_001.md` |
| Plaintext passwords (suspected) | Security vulnerability | Flag in `BUREQ.md` and `NFUREQ_001.md` |
| Forgeable cookie authentication | Security vulnerability | Flag in `FUREQ_AUTH_001.md` |
| No CSRF protection | Security vulnerability | Flag in `NFUREQ_001.md` |
| MySQL driver loaded but SQLite used | Config anomaly | Note in `DB_SCHEMA.md` |
| Guest cart state unclear (cookie? session? DB row?) | Business logic gap | Flag in Discovery Batch 3 |

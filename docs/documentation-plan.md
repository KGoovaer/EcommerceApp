# EcommerceApp — Documentation Plan

**Module:** EcommerceApp  
**Language:** Java  
**Framework:** Servlet 3.0 + JSP  
**Database:** SQLite  
**Build System:** Maven WAR  
**Generated:** 2026-03-17  
**Planning Agent Version:** 1.0  

---

## 1. Project Overview

The EcommerceApp is a J2EE-based online electronics shopping application. It follows a traditional Servlet + JSP architecture without any DI framework or ORM.

### Architecture Summary

```
Browser
  │
  ├── JSP Pages (65 files in src/main/webapp/)
  │     └── Navbars: navbar.jsp / customer_navbar.jsp / admin_navbar.jsp
  │
  └── Servlet Layer (21 servlets, @WebServlet annotations, no web.xml)
        └── DAO Layer (DAO.java – DAO5.java, plain JDBC)
              └── DBConnect.getConn() → SQLite via org.xerial:sqlite-jdbc
```

### Detected Components

| Layer | Count | Notes |
|---|---|---|
| Servlets | 21 | `@WebServlet` annotations, URL-mapped |
| DAO Classes | 5 | DAO.java through DAO5.java, no shared interface |
| Entity Beans | 14 | Plain Java beans, no annotations |
| JSP Pages | 65 | Tripling pattern: guest / customer (c) / admin (a) variants |
| Utility Classes | 1 | `MyUtilities` — file upload handling |

### Business Domains Identified

| Domain | Key Files |
|---|---|
| Authentication | `checkcustomer`, `checkadmin`, `addcustomer`, `validatelogina/c.jsp`, `customerlogin.jsp`, `adminlogin.jsp` |
| Product Catalog | `mobile/laptop/tv/watch.jsp` (×3 variants), `addproduct`, `viewproduct*.jsp`, `category*.jsp` |
| Shopping Cart | `addtocart`, `addtocartnull/a`, `removecart`, `cart/carta/cartnull*.jsp` |
| Order Management | `payprocess`, `ShippingAddress2`, `removeorders`, `orders.jsp`, `orderdetails.jsp`, `confirmpayment.jsp` |
| Admin Management | `deletecustomer`, `managecustomers.jsp`, `managetables.jsp`, `adminhome.jsp`, `remove_orders`, `removetable_*` |
| Customer Support | `addContactus/c`, `remove_contactus`, `contactus/c.jsp`, `aboutus*.jsp` |

---

## 2. JSP Naming Convention

Most pages follow a tripling pattern:

| Suffix | Audience |
|---|---|
| (none) | Guest / unauthenticated visitor |
| `c` | Logged-in Customer |
| `a` | Admin |

Example: `mobile.jsp` / `mobilec.jsp` / `mobilea.jsp`

---

## 3. Phase Plan

### Phase 1 — Planning ✅ COMPLETE

**Agent:** `planning-agent`  
**Status:** Complete  
**Artifacts:**
- `docs/EcommerceApp-state.json`
- `docs/documentation-plan.md`

---

### Phase 2 — Discovery

**Agent:** `discovery`  
**Skill:** `java-analysis`  
**Strategy:** Batched by business domain (6 batches + consolidation)  
**Estimated Tasks:** 45 (6 × 7 + 3 consolidation)

#### Batch 1 — Authentication Domain

**Files to Analyse:**
- Servlets: `checkcustomer.java`, `checkadmin.java`, `addcustomer.java`
- JSPs: `customerlogin.jsp`, `adminlogin.jsp`, `customer_reg.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `customerhome.jsp`, `adminhome.jsp`, `passc.jsp`, `fail.jsp`, `failc.jsp`
- Entities: `customer.java`, `usermaster.java`
- DAOs: `DAO.java` (authentication queries)

**Output:** `docs/discovery/batch-1-auth-flows.md`, `batch-1-auth-components.md`, `batch-1-auth-domain-concepts.md`

**Pay special attention to:**
- Cookie-based auth: `cname` (customer email) and `tname` (admin username), `maxAge=9999`
- Flash message cookies (`maxAge=10`) for login failure feedback
- Password validation logic in servlet vs DAO
- SQL query filtering in `checkcustomer` and `checkadmin` — document all WHERE conditions
- Registration duplicate-checking logic

---

#### Batch 2 — Product Catalog Domain

**Files to Analyse:**
- Servlets: `addproduct.java`
- JSPs: `mobile.jsp`/`mobilec.jsp`/`mobilea.jsp`, `laptop.jsp`/`laptopc.jsp`/`laptopa.jsp`, `tv.jsp`/`tvc.jsp`/`tva.jsp`, `watch.jsp`/`watchc.jsp`/`watcha.jsp`, `category.jsp`/`categoryc.jsp`/`categorya.jsp`, `viewproduct.jsp`/`viewproductc.jsp`/`viewproducta.jsp`, `selecteditem.jsp`/`selecteditema.jsp`/`selecteditemc.jsp`, `addproduct.jsp`
- Entities: `mobile.java`, `laptop.java`, `tv.java`, `watch.java`, `Product.java`, `brand.java`, `category.java`
- DAOs: `DAO.java` (product queries), `DAO2.java`, `DAO3.java`
- Utilities: `MyUtilities.java` — image upload

**Output:** `docs/discovery/batch-2-catalog-flows.md`, `batch-2-catalog-components.md`, `batch-2-catalog-domain-concepts.md`

**Pay special attention to:**
- Product listing query filters — category, brand, price range
- Image upload flow via `MyUtilities.UploadFile()` — file type validation, path construction
- `viewproduct` detail query — joins if any between product and brand/category tables
- How product variants (mobile/laptop/tv/watch) relate — separate tables or single product table?
- Cross-domain: product tables read by cart and order flows

---

#### Batch 3 — Shopping Cart Domain

**Files to Analyse:**
- Servlets: `addtocart.java`, `addtocartnull.java`, `addtocartnulla.java`, `removecart.java`, `removecarta.java`, `removecartnull.java`, `removecartnulla.java`, `removetable_cart.java`
- JSPs: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`, `table_cart.jsp`
- Entities: `cart.java`, `viewlist.java`
- DAOs: `DAO2.java`, `DAO3.java`

**Output:** `docs/discovery/batch-3-cart-flows.md`, `batch-3-cart-components.md`, `batch-3-cart-domain-concepts.md`

**Pay special attention to:**
- Null vs non-null cart distinction — why `addtocartnull`/`addtocartnulla` variants exist
- Cart quantity management logic — what happens when qty = 0 or negative
- Cookie-based user identification for cart ownership (`cname` cookie)
- Cart removal — `removecart` vs `removecartnull` vs `removecarta` — when each is used
- Cross-domain: cart reads product tables; cart data feeds order processing

---

#### Batch 4 — Order Management Domain

**Files to Analyse:**
- Servlets: `payprocess.java`, `ShippingAddress2.java`, `removeorders.java`, `remove_orders.java`, `removetable_order_details.java`
- JSPs: `ShippingAddress.jsp`, `confirmpayment.jsp`, `confirmonline.jsp`, `paymentfail.jsp`, `orders.jsp`, `orderdetails.jsp`, `table_orders.jsp`, `table_order_details.jsp`
- Entities: `orders.java`, `order_details.java`
- DAOs: `DAO4.java`, `DAO5.java`

**Output:** `docs/discovery/batch-4-orders-flows.md`, `batch-4-orders-components.md`, `batch-4-orders-domain-concepts.md`

**Pay special attention to:**
- Full checkout flow: cart → shipping address → payment → order creation
- Payment processing logic in `payprocess.java` — simulated or real gateway?
- `confirmpayment.jsp` vs `confirmonline.jsp` — when each shown (COD vs online?)
- Order status transitions — what states exist and when they change
- `orders` vs `order_details` table relationship — how items are stored per order
- Cart clearing after successful order — is cart deleted post-purchase?
- Cross-domain: reads cart, reads product/price data, writes orders + order_details

---

#### Batch 5 — Admin Management Domain

**Files to Analyse:**
- Servlets: `deletecustomer.java`, `remove_orders.java`, `remove_contactus.java`, `removetable_cart.java`, `removetable_order_details.java`
- JSPs: `adminhome.jsp`, `managecustomers.jsp`, `managetables.jsp`, `addproduct.jsp`
- Entities: `customer.java`, `orders.java`
- DAOs: `DAO.java`, `DAO4.java`, `DAO5.java`

**Output:** `docs/discovery/batch-5-admin-flows.md`, `batch-5-admin-components.md`, `batch-5-admin-domain-concepts.md`

**Pay special attention to:**
- Admin authentication guard — which pages/servlets check `tname` cookie
- `managetables.jsp` — what tables are shown and what bulk operations are available
- `deletecustomer` — cascade effects on related orders/cart items
- `removetable_cart` / `removetable_order_details` — bulk truncation vs selective delete
- Admin product upload flow and image path handling
- Cross-domain: admin reads/modifies customer, order, cart, and contactus tables

---

#### Batch 6 — Customer Support Domain

**Files to Analyse:**
- Servlets: `addContactus.java`, `addContactusc.java`, `remove_contactus.java`
- JSPs: `contactus.jsp`, `contactusc.jsp`, `aboutus.jsp`, `aboutusa.jsp`, `aboutusc.jsp`, `table_contactus.jsp`, `cufail.jsp`, `cufailc.jsp`, `cupass.jsp`, `cupassc.jsp`, `z1.jsp`, `z2.jsp`, `index.jsp`, `footer.jsp`
- Entities: `contactus.java`
- DAOs: `DAO.java` or `DAO3.java`

**Output:** `docs/discovery/batch-6-support-flows.md`, `batch-6-support-components.md`, `batch-6-support-domain-concepts.md`

**Pay special attention to:**
- `contactus.jsp` vs `contactusc.jsp` — guest vs customer contact form differences
- `addContactus` vs `addContactusc` — session/cookie handling differences
- `z1.jsp` / `z2.jsp` — purpose unknown; document what these render
- Contact inquiry storage and admin-side viewing in `table_contactus.jsp`
- `cupass`/`cufail` feedback pages — what triggers each

---

#### Discovery Consolidation

After all 6 batches complete:
1. Build `docs/discovery/cross-domain-table-matrix.md` — every DB table mapped to all flows that READ or WRITE it
2. Identify and document cross-domain dependencies (e.g., cart reads product tables; orders read cart)
3. Create `docs/discovery/consolidation-gaps.md` — undocumented business rules, ambiguous flows, unresolved questions

---

### Phase 3 — Business Documentation

**Agent:** `business-documenter`  
**Skill:** `java-analysis`  
**Estimated Tasks:** 5  
**Prerequisite:** Discovery phase complete

#### Tasks
1. Define actors: Guest, Customer, Administrator
2. Create Use Cases (`UC_*.md`) — one per significant user interaction per domain
3. Write Business Requirements (`BUREQ_*.md`) — derive from discovered flows
4. Create Business Process diagrams (`BP_*.md`) — BPMN-style, key processes
5. Write Business Overview (`docs/business/overview/system-overview.md`)

#### Expected Use Cases
- `UC_AUTH_001` — Customer Registration
- `UC_AUTH_002` — Customer Login
- `UC_AUTH_003` — Admin Login
- `UC_CAT_001` — Browse Product Categories
- `UC_CAT_002` — View Product Details
- `UC_CAT_003` — Admin Add Product
- `UC_CART_001` — Add Item to Cart
- `UC_CART_002` — Remove Item from Cart
- `UC_ORD_001` — Checkout and Place Order
- `UC_ORD_002` — View Order History
- `UC_ORD_003` — Admin Manage Orders
- `UC_ADM_001` — Admin Manage Customers
- `UC_ADM_002` — Admin Manage Tables
- `UC_SUP_001` — Submit Contact Inquiry

#### Expected Business Processes
- `BP_REGISTRATION` — Customer registration and first login
- `BP_CHECKOUT` — Shopping cart to order confirmation
- `BP_ORDER_FULFILLMENT` — Order placement through admin review
- `BP_ADMIN_MANAGEMENT` — Admin product and customer management

---

### Phase 4 — Technical Documentation

**Agent:** `technical-documenter`  
**Skill:** `java-analysis`  
**Estimated Tasks:** 5  
**Prerequisite:** Business documentation phase complete

#### Tasks
1. Derive Functional Requirements (`FUREQ_*.md`) from BUREQs
2. Document Non-Functional Requirements (`NFUREQ_*.md`)
3. Create Technical Flow diagrams (`FF_*.md`) — sequence diagrams for key flows
4. Document integration points (SQLite DB schema, file upload paths)
5. Document API/servlet endpoints with request parameters

#### Expected Functional Requirements
- `FUREQ_AUTH_001` — Customer registration validation rules
- `FUREQ_AUTH_002` — Cookie-based session management
- `FUREQ_CAT_001` — Product listing and filtering
- `FUREQ_CAT_002` — Image upload validation
- `FUREQ_CART_001` — Cart add/remove business rules
- `FUREQ_CART_002` — Cart-null handling
- `FUREQ_ORD_001` — Checkout flow with shipping address
- `FUREQ_ORD_002` — Payment processing flow
- `FUREQ_ADM_001` — Admin authentication enforcement

#### Expected Non-Functional Requirements
- `NFUREQ_SEC_001` — Security baseline (known vulnerabilities documented)
- `NFUREQ_PERF_001` — Static connection pool limitation
- `NFUREQ_MAINT_001` — Maintainability (hardcoded paths)

#### Expected Technical Flows
- `FF_REGISTRATION` — Customer registration sequence
- `FF_LOGIN` — Login + cookie issuance sequence
- `FF_ADD_TO_CART` — Add-to-cart with null-session handling
- `FF_CHECKOUT` — Full checkout sequence (cart → address → payment → order)
- `FF_PAYMENT` — Payment processing and confirmation

#### Expected Integration Docs
- `INT_SQLITE` — Database connection, schema, table list
- `INT_FILE_UPLOAD` — MyUtilities upload path, validation rules

---

### Phase 5 — Coordination

**Agent:** `doc-coordinator`  
**Estimated Tasks:** 5  
**Prerequisite:** Technical documentation phase complete

#### Tasks
1. Validate documentation directory structure
2. Create master index (`docs/index.md`) with links to all artifacts
3. Create system overview (`docs/system-overview.md`)
4. Build traceability matrix (`docs/traceability/requirement-matrix.md`) — BUREQs → FUREQs → Flows → Components
5. Create domain concepts catalog (`docs/domain/domain-concepts-catalog.md`)
6. Create ID registry (`docs/traceability/id-registry.md`)
7. Create flow-to-component map (`docs/traceability/flow-to-component-map.md`)

---

### Phase 6 — Verification

**Agent:** `verification`  
**Estimated Tasks:** 6  
**Prerequisite:** Coordination phase complete

#### Tasks
- **V-1** Build table usage matrix — map every DB table to all flows that read/write it
- **V-2** Repository query deep analysis — compare WHERE conditions in DAO methods against documented business rules
- **V-3** Validation completeness check — enumerate all servlet validations, verify true/false outcomes documented
- **V-4** Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional links
- **V-5** Entity state machine verification — map all order/cart status transitions, verify all documented
- **V-6** Generate consolidated gap report with severity classification and remediation prompts

#### Output
- `docs/verification/gap-report.md`
- `docs/verification/table-usage-matrix.md`
- `docs/verification/cross-domain-dependencies.md`

---

## 4. Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | Planning Agent | 1 | Low |
| Discovery — Batch 1 (Auth) | Discovery Agent | 7 | High |
| Discovery — Batch 2 (Catalog) | Discovery Agent | 7 | High |
| Discovery — Batch 3 (Cart) | Discovery Agent | 7 | High |
| Discovery — Batch 4 (Orders) | Discovery Agent | 7 | High |
| Discovery — Batch 5 (Admin) | Discovery Agent | 7 | High |
| Discovery — Batch 6 (Support) | Discovery Agent | 7 | Medium |
| Discovery Consolidation | Discovery Agent | 3 | Medium |
| Business Documentation | Business Documenter | 5 | Medium |
| Technical Documentation | Technical Documenter | 5 | High |
| Coordination | Doc Coordinator | 5 | Medium |
| Verification | Verification Agent | 6 | High |
| **Total** | | **67** | |

---

## 5. Completeness Criteria

### Per-Flow Completeness Criteria

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true AND false outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from → to states
- [ ] Error handling paths are documented (fail*.jsp redirect conditions)
- [ ] Cookie interactions are documented (which cookies read/written, maxAge values)
- [ ] Servlet URL mappings are explicitly listed

### Per-Domain Completeness Criteria

- [ ] All flows meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries
- [ ] JSP tripling variants (guest/customer/admin) are all accounted for

---

## 6. Cross-Domain Table Dependency Map (Preliminary)

| Table | Domains That Read | Domains That Write |
|---|---|---|
| `customer` | Auth, Admin | Auth (registration), Admin (delete) |
| `mobile` / `laptop` / `tv` / `watch` | Catalog, Cart, Orders | Catalog (admin add product) |
| `cart` | Cart, Orders | Cart (add/remove), Orders (clear after purchase) |
| `orders` | Orders, Admin | Orders (create), Admin (delete) |
| `order_details` | Orders, Admin | Orders (create), Admin (delete) |
| `contactus` | Support, Admin | Support (submit), Admin (delete) |
| `usermaster` | Auth | Auth (admin login) |

---

## 7. Known Risks and Constraints

| Risk | Description | Mitigation |
|---|---|---|
| Static DB connection | `DBConnect` uses a static `Connection` — not thread-safe | Document as NFUREQ; do not change pattern |
| Hardcoded paths | DB path and image upload path hardcoded | Document in INT artifacts |
| Cookie forgery | Auth uses forgeable plaintext cookies | Document as NFUREQ_SEC |
| SQL injection (partial) | PreparedStatement used throughout | Verify in verification phase |
| Image upload path traversal | Filename used as-is | Document extension validation in MyUtilities |
| MySQL driver loaded but SQLite used | Legacy `Class.forName("com.mysql.cj.jdbc.Driver")` | Document as known anomaly — do not remove |

---

## 8. Output Directory Structure

```
docs/
├── EcommerceApp-state.json           # State tracking file
├── documentation-plan.md             # This plan
├── index.md                          # Master landing page (coordination phase)
├── system-overview.md                # Architecture overview (coordination phase)
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
│   ├── batch-6-support-flows.md
│   ├── batch-6-support-components.md
│   ├── batch-6-support-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/system-overview.md
│   ├── use-cases/UC_AUTH_001.md … UC_SUP_001.md
│   └── processes/BP_REGISTRATION.md … BP_ADMIN_MANAGEMENT.md
├── functional/
│   ├── index.md
│   ├── requirements/FUREQ_*.md … NFUREQ_*.md
│   ├── flows/FF_*.md
│   └── integration/INT_SQLITE.md, INT_FILE_UPLOAD.md
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

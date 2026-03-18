# EcommerceApp — Documentation Plan

> **Module**: EcommerceApp  
> **Language**: Java  
> **Framework**: Servlet 3.0 + JSP  
> **Database**: SQLite (via JDBC)  
> **Build Tool**: Maven  
> **Generated**: 2026-03-18  
> **State File**: `docs/EcommerceApp-state.json`

---

## Repository Overview

EcommerceApp is a Java J2EE online electronic shopping application using the Servlet/JSP pattern with plain JDBC DAO classes and SQLite. There is no Spring, ORM, or DI framework — all wiring is manual.

### Structure Summary

| Layer | Location | Count |
|-------|----------|-------|
| Servlets | `com/servlet/` | 21 classes |
| DAOs | `com/dao/` (DAO–DAO5) | 5 classes |
| Entities | `com/entity/` | 14 beans |
| JSPs | `src/main/webapp/*.jsp` | 62 pages |
| Connection | `com/conn/DBConnect.java` | 1 singleton |
| Utility | `com/utility/MyUtilities.java` | 1 class |

### Business Domains Identified

| # | Domain | Key Servlets | Key JSPs |
|---|--------|-------------|---------|
| 1 | Authentication & Users | `checkadmin`, `checkcustomer`, `addcustomer`, `deletecustomer` | `adminlogin`, `customerlogin`, `customer_reg`, `validatelogina`, `validateloginc` |
| 2 | Product Catalog | `addproduct` | `mobile/a/c`, `laptop/a/c`, `tv/a/c`, `watch/a/c`, `category/a/c`, `viewproduct/a/c`, `selecteditem/a/c` |
| 3 | Shopping Cart | `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla`, `removetable_cart` | `cart`, `carta`, `cartnull`, `cartnulla`, `cartnullqty` |
| 4 | Orders & Checkout | `payprocess`, `ShippingAddress2`, `remove_orders`, `removeorders`, `removetable_order_details` | `ShippingAddress`, `confirmpayment`, `confirmonline`, `orders`, `orderdetails`, `paymentfail` |
| 5 | Admin Management | _(via JSP scriptlets)_ | `adminhome`, `managecustomers`, `managetables`, `table_cart`, `table_orders`, `table_order_details`, `table_contactus` |
| 6 | Contact | `addContactus`, `addContactusc`, `remove_contactus` | `contactus`, `contactusc` |

---

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|-------|-------|-----------|-----------|
| Planning | Planning Agent | 1 | Low |
| Discovery — Batch 1 (Auth) | Discovery Agent | 7 | High |
| Discovery — Batch 2 (Catalog) | Discovery Agent | 7 | High |
| Discovery — Batch 3 (Cart) | Discovery Agent | 7 | High |
| Discovery — Batch 4 (Orders & Admin) | Discovery Agent | 7 | High |
| Discovery — Consolidation | Discovery Agent | 3 | Medium |
| Business | Business Documenter | 5 | Medium |
| Technical | Technical Documenter | 5 | High |
| Coordination | Doc Coordinator | 5 | Medium |
| Verification | Verification Agent | 6 | High |
| **Total** | | **53** | |

---

## Phase 1 — Planning (Complete)

**Agent**: Planning Agent  
**Status**: ✅ Complete

### Outputs
- [x] `docs/EcommerceApp-state.json` — State tracking file
- [x] `docs/documentation-plan.md` — This plan

---

## Phase 2 — Discovery (4 Batches)

**Agent**: Discovery Agent  
**Skill**: `java-analysis`  
**Strategy**: Batched by business domain; 7 tasks per batch  

### Batch 1 — Authentication & User Management

**Output directory**: `docs/discovery/`  
**Files**: `batch-1-auth-flows.md`, `batch-1-auth-components.md`, `batch-1-auth-domain-concepts.md`

**Servlets to analyse**:
- `com.servlet.checkadmin` — Admin login validation
- `com.servlet.checkcustomer` — Customer login validation  
- `com.servlet.addcustomer` — Customer registration
- `com.servlet.deletecustomer` — Customer deletion (admin action)

**JSPs to analyse**:
- `adminlogin.jsp`, `customerlogin.jsp`, `customer_reg.jsp`
- `validatelogina.jsp`, `validateloginc.jsp`
- `adminhome.jsp`, `customerhome.jsp`
- `fail.jsp`, `failc.jsp`, `cupass.jsp`, `cupassc.jsp`, `cufail.jsp`, `cufailc.jsp`, `passc.jsp`

**Tasks**:
1. Detect entry points (servlet URL mappings via `@WebServlet`)
2. Trace execution flows: login → cookie set → redirect
3. Deep data access query analysis: `DAO.java` login queries — document WHERE clauses (email/password match), result handling
4. Inventory all components (servlets, JSPs, entities used: `customer`, `usermaster`)
5. Extract domain concepts: `Customer`, `Admin`, `Session (Cookie)`, `Credential`
6. Map inter-domain dependencies (auth cookies used by Cart and Order domains)
7. Cross-domain table usage: `customer` table read by Cart domain; `usermaster` table read by Admin domain

**Pay special attention to**:
- Cookie-based auth pattern: `cname` (customer email), `tname` (admin username), `maxAge=9999`
- Flash message cookies (`maxAge=10`) for feedback
- Password handling (plaintext in DB — security note)
- JSP tripling pattern (guest / customer / admin variants)

---

### Batch 2 — Product Catalog & Browsing

**Output directory**: `docs/discovery/`  
**Files**: `batch-2-catalog-flows.md`, `batch-2-catalog-components.md`, `batch-2-catalog-domain-concepts.md`

**Servlets to analyse**:
- `com.servlet.addproduct` — Admin adds a new product (with image upload)

**JSPs to analyse** (triplet pattern: guest / customer / admin):
- `mobile.jsp` / `mobilea.jsp` / `mobilec.jsp`
- `laptop.jsp` / `laptopa.jsp` / `laptopc.jsp`
- `tv.jsp` / `tva.jsp` / `tvc.jsp`
- `watch.jsp` / `watcha.jsp` / `watchc.jsp`
- `category.jsp` / `categorya.jsp` / `categoryc.jsp`
- `viewproduct.jsp` / `viewproducta.jsp` / `viewproductc.jsp`
- `selecteditem.jsp` / `selecteditema.jsp` / `selecteditemc.jsp`
- `addproduct.jsp`

**Tasks**:
1. Detect entry points (JSP direct access + `@WebServlet("/addproduct")`)
2. Trace execution flows: category browse → product list → selected item view
3. Deep data access query analysis: DAO queries for product listing — document SELECT, WHERE (category filter, brand filter), ORDER BY
4. Inventory all components (entities used: `Product`, `mobile`, `laptop`, `tv`, `watch`, `category`, `brand`, `viewlist`)
5. Extract domain concepts: `Product`, `Category`, `Brand`, `ProductImage`
6. Map dependencies: products consumed by Cart domain
7. Cross-domain table usage: product tables read by Cart domain when adding items

**Pay special attention to**:
- `MyUtilities.UploadFile()` — file upload validation and write logic
- Image path storage in DB vs file system
- The hardcoded image upload path in `DAO.java`
- Guest vs customer vs admin product view differences
- `addproduct.jsp` form → `addproduct` servlet flow

---

### Batch 3 — Shopping Cart Management

**Output directory**: `docs/discovery/`  
**Files**: `batch-3-cart-flows.md`, `batch-3-cart-components.md`, `batch-3-cart-domain-concepts.md`

**Servlets to analyse**:
- `com.servlet.addtocart` — Add item to cart (authenticated customer)
- `com.servlet.addtocartnull` — Add item when cart row exists (guest/null qty handling)
- `com.servlet.addtocartnulla` — Variant for admin context
- `com.servlet.removecart` — Remove cart item
- `com.servlet.removecarta` — Remove cart item (admin view)
- `com.servlet.removecartnull` — Remove null-quantity cart row
- `com.servlet.removecartnulla` — Variant for admin context
- `com.servlet.removetable_cart` — Bulk-clear cart table (admin)

**JSPs to analyse**:
- `cart.jsp` / `carta.jsp`
- `cartnull.jsp` / `cartnulla.jsp`
- `cartnullqty.jsp`
- `table_cart.jsp`

**Tasks**:
1. Detect entry points for all cart servlets
2. Trace add-to-cart flow: product selection → cookie check → DAO insert/update → redirect
3. Deep data access analysis: cart DAO queries — INSERT, SELECT (by customer email), UPDATE (qty), DELETE; document all WHERE conditions
4. Inventory components (entity: `cart`)
5. Extract domain concepts: `CartItem`, `Quantity`, `CartSession`
6. Map dependencies: Cart reads Product data; Cart feeds into Orders domain
7. Cross-domain table usage: `cart` table read by Order/Checkout domain at payment time

**Pay special attention to**:
- Distinction between `addtocart` / `addtocartnull` / `addtocartnulla` — what business condition triggers each
- Cart persistence strategy (DB-backed, tied to customer email cookie)
- `cartnullqty.jsp` — zero-quantity edge case handling
- Admin bulk-clear (`removetable_cart`) vs individual remove

---

### Batch 4 — Orders, Checkout & Admin Management

**Output directory**: `docs/discovery/`  
**Files**: `batch-4-orders-flows.md`, `batch-4-orders-components.md`, `batch-4-orders-domain-concepts.md`

**Servlets to analyse**:
- `com.servlet.payprocess` — Process payment, create order record
- `com.servlet.ShippingAddress2` — Capture/save shipping address
- `com.servlet.remove_orders` — Delete order (admin action)
- `com.servlet.removeorders` — Customer-side order removal
- `com.servlet.removetable_order_details` — Bulk-clear order details (admin)
- `com.servlet.addContactus` — Guest contact form submission
- `com.servlet.addContactusc` — Customer contact form submission
- `com.servlet.remove_contactus` — Delete contact message (admin)

**JSPs to analyse**:
- `ShippingAddress.jsp`, `confirmpayment.jsp`, `confirmonline.jsp`
- `orders.jsp`, `orderdetails.jsp`, `paymentfail.jsp`
- `managecustomers.jsp`, `managetables.jsp`
- `table_orders.jsp`, `table_order_details.jsp`, `table_contactus.jsp`
- `contactus.jsp`, `contactusc.jsp`
- `z1.jsp`, `z2.jsp`

**Tasks**:
1. Detect entry points for all order and admin servlets
2. Trace checkout flow: cart review → shipping address → payment selection → confirm → order created → cart cleared
3. Deep data access analysis: `orders` and `order_details` DAO queries — INSERT (with FK to customer and products), SELECT (by customer/admin); document all column mappings
4. Inventory components (entities: `orders`, `order_details`, `contactus`)
5. Extract domain concepts: `Order`, `OrderLine`, `ShippingAddress`, `PaymentMethod`, `ContactMessage`
6. Map dependencies: Orders depend on Cart (reads cart to create order lines), Auth (customer identity from cookie)
7. Cross-domain table usage: `orders` table read by Admin Management; `contactus` table managed by Admin

**Pay special attention to**:
- `payprocess` — how cart items become order records (JDBC transaction or sequential inserts?)
- Payment method choices: COD (Cash on Delivery) vs online — how each path differs
- `ShippingAddress2` — what data is captured and where it is stored
- Admin bulk-delete operations (`removetable_order_details`) — business justification
- `z1.jsp` and `z2.jsp` — purpose unclear, inspect for hidden logic

---

### Discovery Consolidation

**Tasks**:
1. Merge all batch outputs into cross-domain dependency map
2. Build `docs/discovery/cross-domain-table-matrix.md` — for every DB table, list all domains/servlets that read or write it
3. Create `docs/discovery/consolidation-gaps.md` — flag business rules found in code but not yet fully documented, undocumented status transitions, and validation chains that need deeper inspection

---

## Phase 3 — Business Documentation

**Agent**: Business Documenter  
**Skill**: `java-analysis`  
**Reads**: All `docs/discovery/batch-*.md` files

### Tasks
1. Define actors: `Guest`, `Customer`, `Admin`
2. Create use cases (`UC_*.md`) for each identified flow
3. Write business requirements (`BUREQ_*`) linked to use cases
4. Create business process diagrams (BPMN-style, Mermaid)
5. Write `docs/business/index.md` and `docs/business/overview/system-overview.md`

### Expected Use Cases

| ID | Title | Actor |
|----|-------|-------|
| UC_AUTH_001 | Customer Registration | Guest |
| UC_AUTH_002 | Customer Login | Guest |
| UC_AUTH_003 | Admin Login | Admin |
| UC_AUTH_004 | Customer Account Deletion | Admin |
| UC_CATALOG_001 | Browse Products by Category | Guest/Customer |
| UC_CATALOG_002 | View Product Details | Guest/Customer |
| UC_CATALOG_003 | Add Product (Admin) | Admin |
| UC_CART_001 | Add Item to Cart | Customer |
| UC_CART_002 | Remove Item from Cart | Customer |
| UC_CART_003 | View Cart | Customer |
| UC_ORDER_001 | Checkout (COD) | Customer |
| UC_ORDER_002 | Checkout (Online Payment) | Customer |
| UC_ORDER_003 | View Order History | Customer |
| UC_ADMIN_001 | Manage Customers | Admin |
| UC_ADMIN_002 | Manage Orders | Admin |
| UC_ADMIN_003 | View Contact Messages | Admin |
| UC_CONTACT_001 | Submit Contact Form | Guest/Customer |

### Expected Business Processes

| ID | Title |
|----|-------|
| BP_CUSTOMER_REGISTRATION | End-to-end customer registration flow |
| BP_PRODUCT_BROWSING | Product discovery and selection |
| BP_CHECKOUT | Cart to confirmed order |
| BP_ADMIN_MANAGEMENT | Admin operations on customers, orders, products |

---

## Phase 4 — Technical / Functional Documentation

**Agent**: Technical Documenter  
**Skill**: `java-analysis`  
**Reads**: All `docs/business/` files and discovery outputs

### Tasks
1. Derive functional requirements (`FUREQ_*.md`) from BUREQs
2. Document non-functional requirements (`NFUREQ_*.md`): performance, security, availability
3. Create technical flow diagrams (`FF_*.md`) using Mermaid sequence diagrams
4. Document API surface (servlet URL mappings, request/response params)
5. Document data schemas (all DB tables, columns, types, relationships)
6. Document validation rules (input validation, file upload validation)
7. Document integration points (SQLite DB path, file system image storage)

### Expected Technical Flows

| ID | Title |
|----|-------|
| FF_LOGIN_FLOW | Authentication sequence |
| FF_CART_FLOW | Add/remove cart item sequence |
| FF_CHECKOUT_FLOW | Payment processing sequence |
| FF_ADMIN_PRODUCT_ADD | Admin product addition with image upload |

### Expected Integration Documents

| ID | Title |
|----|-------|
| INT_DATABASE | SQLite DB connection, schema, table relationships |
| INT_FILE_UPLOAD | Image upload path, validation, storage |

---

## Phase 5 — Coordination

**Agent**: Doc Coordinator  
**Reads**: All previous phase outputs

### Tasks
1. Validate directory structure matches plan
2. Create `docs/index.md` — master landing page with links to all docs
3. Create `docs/system-overview.md` — architecture overview with Mermaid diagram
4. Build `docs/traceability/requirement-matrix.md` — BUREQ → FUREQ → Flow mapping
5. Create `docs/traceability/flow-to-component-map.md`
6. Create `docs/traceability/id-registry.md` — all UC/BP/BUREQ/FUREQ/FF IDs
7. Create `docs/domain/domain-concepts-catalog.md`
8. Create `docs/domain/ubiquitous-language.md`
9. Create `docs/domain/bounded-contexts.md`

---

## Phase 6 — Verification

**Agent**: Verification Agent  
**Reads**: All documentation + original source code

### Tasks
| ID | Task | Output |
|----|------|--------|
| V-1 | Build table usage matrix — map every DB table to all servlets/JSPs that read/write it | `docs/verification/table-usage-matrix.md` |
| V-2 | Repository query deep analysis — compare actual WHERE clauses in DAO methods against documented business rules | Part of gap report |
| V-3 | Validation completeness check — enumerate all validations in code (JSP + servlet), verify each has true/false outcomes documented | Part of gap report |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links | `docs/verification/cross-domain-dependencies.md` |
| V-5 | Entity state machine verification — map all status/state transitions in code (orders, cart), verify all are documented | Part of gap report |
| V-6 | Generate consolidated gap report with severity classification and remediation prompts | `docs/verification/gap-report.md` |

---

## Completeness Criteria

### Per-Flow

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true AND false outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from → to states
- [ ] Error handling paths are documented (redirect targets, flash cookie messages)
- [ ] Integration specifics (SQLite path, image upload path, cookie names) are explicitly listed
- [ ] Cart-to-order conversion logic is fully documented

### Per-Domain

- [ ] All flows meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json          # State tracking file
├── documentation-plan.md            # This plan
├── index.md                         # Master landing page
├── system-overview.md               # Architecture overview
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
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/
│   │   └── system-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md  … UC_CONTACT_001.md
│   └── processes/
│       ├── BP_CUSTOMER_REGISTRATION.md
│       ├── BP_PRODUCT_BROWSING.md
│       ├── BP_CHECKOUT.md
│       └── BP_ADMIN_MANAGEMENT.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_*.md
│   │   └── NFUREQ_*.md
│   ├── flows/
│   │   └── FF_*.md
│   └── integration/
│       ├── INT_DATABASE.md
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

---

## Known Cross-Domain Dependencies (Identified at Planning)

| Source Domain | Target Domain | Shared Resource | Notes |
|--------------|--------------|----------------|-------|
| Cart | Auth | `customer` cookie (`cname`) | Cart operations check customer identity via cookie |
| Cart | Catalog | Product tables | Cart items reference product records |
| Orders | Cart | `cart` table | Checkout reads cart items to create order lines |
| Orders | Auth | `customer` cookie (`cname`) | Order creation uses customer identity |
| Admin Management | All | All tables | Admin can view/delete records across all domains |
| Contact | Auth | `cname` cookie (optional) | Customer contact form may include customer identity |

---

## Known Technical Constraints

| Constraint | Detail |
|-----------|--------|
| Auth mechanism | Cookie-only (`cname`, `tname`), no HttpSession, no JWT |
| DB connection | Static singleton `DBConnect.getConn()` — not thread-safe |
| File upload | `MyUtilities.UploadFile()` — original filename used (path traversal risk) |
| Password storage | Plaintext in DB (security risk — note only, do not remediate in docs) |
| Hardcoded paths | SQLite DB path in `DBConnect.java`; image path in `DAO.java` |
| No automated tests | `junit:junit:3.8.1` in pom.xml but no test classes |
| Legacy driver | `Class.forName("com.mysql.cj.jdbc.Driver")` loaded but SQLite used |

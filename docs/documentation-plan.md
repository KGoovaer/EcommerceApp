# EcommerceApp — Documentation Plan

**Module:** EcommerceApp  
**Language:** Java  
**Framework:** Servlet 3.0 + JSP  
**Database:** SQLite (JDBC via org.xerial:sqlite-jdbc)  
**Build:** Maven WAR  
**Generated:** 2026-03-18  
**State file:** `docs/EcommerceApp-state.json`

---

## Project Structure Summary

| Layer | Location | Count |
|-------|----------|-------|
| Servlets (`@WebServlet`) | `EcommerceApp/src/main/java/com/servlet/` | 21 |
| DAO classes | `EcommerceApp/src/main/java/com/dao/` | 5 (DAO – DAO5) |
| Entity beans | `EcommerceApp/src/main/java/com/entity/` | 14 |
| JSP pages | `EcommerceApp/src/main/webapp/*.jsp` | 63 |
| DB connection | `EcommerceApp/src/main/java/com/conn/DBConnect.java` | 1 |
| Utility | `EcommerceApp/src/main/java/com/utility/MyUtilities.java` | 1 |

### JSP Tripling Convention

Most pages exist in three variants (no suffix = guest, `c` = customer, `a` = admin). Examples:

- `mobile.jsp` / `mobilec.jsp` / `mobilea.jsp`
- `cart.jsp` / `carta.jsp` (no guest cart)
- `contactus.jsp` / `contactusc.jsp`

### Auth Model

Cookie-based (no `HttpSession`):  
- `cname` cookie = customer email (admin `maxAge=9999`, customer `maxAge=9999`)  
- `tname` cookie = admin username  
- Flash cookies (`maxAge=10`) for one-time feedback messages

---

## Business Domains

| Domain | Key Entities | Key Servlets | Key JSPs |
|--------|-------------|-------------|---------|
| **Authentication** | `customer`, `usermaster` | `checkadmin`, `checkcustomer`, `addcustomer` | `adminlogin`, `customerlogin`, `customer_reg` |
| **Product Catalog** | `Product`, `mobile`, `laptop`, `tv`, `watch`, `brand`, `category` | `addproduct` | `mobile`, `laptop`, `tv`, `watch`, `viewproduct`, `category`, `selecteditem` |
| **Shopping Cart** | `cart`, `viewlist` | `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla` | `cart`, `carta`, `cartnull`, `cartnulla`, `cartnullqty` |
| **Orders & Checkout** | `orders`, `order_details` | `ShippingAddress2`, `payprocess`, `removeorders`, `remove_orders`, `removetable_cart`, `removetable_order_details` | `ShippingAddress`, `confirmpayment`, `confirmonline`, `paymentfail`, `orders`, `orderdetails` |
| **Admin Management** | `customer`, `contactus` | `addContactus`, `addContactusc`, `deletecustomer`, `remove_contactus` | `adminhome`, `managecustomers`, `managetables`, `table_cart`, `table_contactus`, `table_orders`, `table_order_details`, `addproduct` |

---

## Phase 1 — Discovery (Batched by Domain)

**Agent:** `discovery`  
**Skill:** `java-analysis`  
**Strategy:** 5 domain batches × 7 tasks each + 1 consolidation step  
**Estimated tasks:** 38  
**Output directory:** `docs/discovery/`

### Batch 1 — Authentication Domain

**Files to analyse:**

- `com/servlet/checkadmin.java`
- `com/servlet/checkcustomer.java`
- `com/servlet/addcustomer.java`
- `com/servlet/deletecustomer.java`
- `com/entity/customer.java`
- `com/entity/usermaster.java`
- `com/dao/DAO.java` (auth-related methods)
- JSPs: `adminlogin.jsp`, `customerlogin.jsp`, `customer_reg.jsp`, `validatelogina.jsp`, `validateloginc.jsp`

**Output files:**
- `docs/discovery/batch-1-auth-flows.md`
- `docs/discovery/batch-1-auth-components.md`
- `docs/discovery/batch-1-auth-domain-concepts.md`

**Tasks:**
1. Detect entry points (`@WebServlet` URL mappings)
2. Trace `doPost`/`doGet` execution flows (cookie creation/validation logic)
3. Deep data access query analysis — read actual SQL in DAO methods: login credential matching WHERE clause, customer registration INSERT, admin lookup logic
4. Inventory all components (servlets, DAOs, entities, JSPs involved)
5. Extract domain concepts: Authentication, Session (cookie-based), Registration, Role (admin vs customer)
6. Map inter-function dependencies and cookie-auth pattern
7. Cross-domain table usage — identify which tables (`usermaster`, `customer`) are also read/written by other domains

**Pay special attention to:**
- Cookie `cname`/`tname` creation and `maxAge` values (security baseline)
- Plaintext password comparison logic in DAO (document as-is, security note)
- Redirect destinations after success/fail login (flash cookie `maxAge=10` pattern)
- `deletecustomer` — auth guard check (or lack thereof)

---

### Batch 2 — Product Catalog Domain

**Files to analyse:**

- `com/servlet/addproduct.java`
- `com/entity/Product.java`, `com/entity/mobile.java`, `com/entity/laptop.java`, `com/entity/tv.java`, `com/entity/watch.java`, `com/entity/brand.java`, `com/entity/category.java`
- `com/dao/DAO.java`, `com/dao/DAO2.java` (product-related methods)
- `com/utility/MyUtilities.java`
- JSPs: `mobile.jsp`, `mobilea.jsp`, `mobilec.jsp`, `laptop.jsp`, `laptopa.jsp`, `laptopc.jsp`, `tv.jsp`, `tva.jsp`, `tvc.jsp`, `watch.jsp`, `watcha.jsp`, `watchc.jsp`, `viewproduct.jsp`, `viewproducta.jsp`, `viewproductc.jsp`, `category.jsp`, `categorya.jsp`, `categoryc.jsp`, `selecteditem.jsp`, `selecteditema.jsp`, `selecteditemc.jsp`, `addproduct.jsp`

**Output files:**
- `docs/discovery/batch-2-catalog-flows.md`
- `docs/discovery/batch-2-catalog-components.md`
- `docs/discovery/batch-2-catalog-domain-concepts.md`

**Tasks:**
1. Detect entry points for product browsing (guest, customer, admin variants)
2. Trace product listing flows — how JSPs call DAO directly via scriptlets
3. Deep data access query analysis — all SELECT queries: product list by category, product by ID, brand/category filters
4. Inventory all components (tripling pattern — same page × 3 roles)
5. Extract domain concepts: Product, Category, Brand, Image (uploaded via `MyUtilities`)
6. Map file-upload flow through `MyUtilities.UploadFile()` — extension validation, path construction
7. Cross-domain table usage — product/category tables referenced by cart and order domains

**Pay special attention to:**
- `MyUtilities.UploadFile()` — extension whitelist, hardcoded upload path (document path traversal risk as observation only)
- JSP scriptlet direct DAO calls vs. servlet-mediated flows (both patterns exist)
- Image filename handling (original filename used as-is)
- Admin-only `addproduct.jsp` — auth guard check

---

### Batch 3 — Shopping Cart Domain

**Files to analyse:**

- `com/servlet/addtocart.java`, `com/servlet/addtocartnull.java`, `com/servlet/addtocartnulla.java`
- `com/servlet/removecart.java`, `com/servlet/removecarta.java`, `com/servlet/removecartnull.java`, `com/servlet/removecartnulla.java`
- `com/servlet/removetable_cart.java`
- `com/entity/cart.java`, `com/entity/viewlist.java`
- `com/dao/DAO3.java` (cart-related methods)
- JSPs: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`, `table_cart.jsp`

**Output files:**
- `docs/discovery/batch-3-cart-flows.md`
- `docs/discovery/batch-3-cart-components.md`
- `docs/discovery/batch-3-cart-domain-concepts.md`

**Tasks:**
1. Detect entry points — `addtocart` variants (null vs non-null cart state, guest vs customer)
2. Trace cart add/remove flows and redirect targets
3. Deep data access query analysis — cart INSERT/DELETE/SELECT queries; how quantity and product associations are stored
4. Inventory all components and the "null/non-null" state branching pattern
5. Extract domain concepts: Cart, CartItem, Quantity, GuestCart vs CustomerCart
6. Map dependency on product tables (foreign keys or denormalised data?)
7. Cross-domain table usage — cart table read by checkout/orders domain

**Pay special attention to:**
- `addtocartnull` / `addtocartnulla` vs `addtocart` / `addtocarta` — what differentiates them (logged-in vs guest? empty vs non-empty cart?)
- `removetable_cart` — admin bulk-delete vs customer item-remove distinction
- `cartnullqty.jsp` — zero-quantity edge case handling
- Auth check presence/absence on cart servlets

---

### Batch 4 — Orders & Checkout Domain

**Files to analyse:**

- `com/servlet/ShippingAddress2.java`
- `com/servlet/payprocess.java`
- `com/servlet/removeorders.java`, `com/servlet/remove_orders.java`
- `com/servlet/removetable_order_details.java`
- `com/entity/orders.java`, `com/entity/order_details.java`
- `com/dao/DAO4.java`, `com/dao/DAO5.java` (order-related methods)
- JSPs: `ShippingAddress.jsp`, `confirmpayment.jsp`, `confirmonline.jsp`, `paymentfail.jsp`, `orders.jsp`, `orderdetails.jsp`, `table_orders.jsp`, `table_order_details.jsp`

**Output files:**
- `docs/discovery/batch-4-orders-flows.md`
- `docs/discovery/batch-4-orders-components.md`
- `docs/discovery/batch-4-orders-domain-concepts.md`

**Tasks:**
1. Detect entry points — checkout initiation from cart
2. Trace end-to-end checkout flow: ShippingAddress → payprocess → confirm/fail
3. Deep data access query analysis — order INSERT, order_details INSERT, cart clear after order; all WHERE conditions
4. Inventory all components in checkout pipeline
5. Extract domain concepts: Order, OrderDetail, ShippingAddress, PaymentMethod (COD vs online?), OrderStatus
6. Map cart-to-order transition (how cart items become order_details)
7. Cross-domain table usage — orders table read by admin management; cart table cleared post-order

**Pay special attention to:**
- `payprocess` — payment mode branching (COD vs online payment flow differences)
- `removeorders` vs `remove_orders` — customer-initiated vs admin-initiated cancellation?
- `removetable_order_details` — admin-only bulk delete
- Order status field transitions (if any status field exists on `orders` entity)
- Auth guard on checkout servlets

---

### Batch 5 — Admin Management Domain

**Files to analyse:**

- `com/servlet/addContactus.java`, `com/servlet/addContactusc.java`
- `com/servlet/remove_contactus.java`
- `com/entity/contactus.java`
- `com/dao/DAO2.java` (admin/contact methods)
- JSPs: `adminhome.jsp`, `managecustomers.jsp`, `managetables.jsp`, `table_contactus.jsp`, `contactus.jsp`, `contactusc.jsp`, `aboutus.jsp`, `aboutusa.jsp`, `aboutusc.jsp`, `addproduct.jsp`, `viewproducta.jsp`

**Output files:**
- `docs/discovery/batch-5-admin-flows.md`
- `docs/discovery/batch-5-admin-components.md`
- `docs/discovery/batch-5-admin-domain-concepts.md`

**Tasks:**
1. Detect entry points — admin dashboard and management screens
2. Trace admin flows: contact-us message handling, customer management, table management
3. Deep data access query analysis — contactus INSERT/SELECT/DELETE, customer listing SELECT queries
4. Inventory all components (admin-only JSPs, management tables)
5. Extract domain concepts: ContactMessage, AdminDashboard, CustomerList, TableManagement
6. Map which admin operations cross into other domains (deleting cart, deleting orders)
7. Cross-domain table usage — admin reads from customer, cart, orders, order_details, contactus tables

**Pay special attention to:**
- `addContactus` (admin form) vs `addContactusc` (customer form) — same underlying table?
- Admin cookie guard on all management servlets
- `managetables.jsp` — what tables does it expose and with what operations?
- `table_cart.jsp`, `table_orders.jsp`, `table_order_details.jsp` — admin read-only view or destructive?

---

### Discovery Consolidation Step

**After all 5 batches complete:**

1. Merge batch outputs into cross-domain summary
2. Build `docs/discovery/cross-domain-table-matrix.md` — for each database table, list all servlets/JSPs that read or write it across all domains
3. Link related flows across batches (e.g., cart → checkout transition)
4. Flag undocumented business rules in `docs/discovery/consolidation-gaps.md`

---

## Phase 2 — Business Documentation

**Agent:** `business-documenter`  
**Skill:** `java-analysis`  
**Estimated tasks:** 5  
**Output directory:** `docs/business/`

### Tasks

| ID | Task | Output |
|----|------|--------|
| B-1 | Define actors (Guest, Customer, Admin) and their capabilities | `docs/business/overview/business-overview.md` |
| B-2 | Create use cases for Authentication domain | `UC_AUTH_001` – `UC_AUTH_003` |
| B-3 | Create use cases for Product Catalog domain | `UC_CAT_001` – `UC_CAT_002` |
| B-4 | Create use cases for Shopping Cart domain | `UC_CART_001` – `UC_CART_003` |
| B-5 | Create use cases for Orders & Checkout domain | `UC_ORD_001` – `UC_ORD_002` |
| B-6 | Create use cases for Admin Management domain | `UC_ADM_001` – `UC_ADM_003` |
| B-7 | Write BUREQs for all domains | embedded in UC files |
| B-8 | Create BPMN process diagrams (Mermaid) | `BP_CHECKOUT.md`, `BP_PRODUCT_MGMT.md`, `BP_CUSTOMER_MGMT.md` |
| B-9 | Create business index | `docs/business/index.md` |

### Expected Use Cases

| ID | Title | Actor |
|----|-------|-------|
| UC_AUTH_001 | Register as Customer | Guest |
| UC_AUTH_002 | Login as Customer | Guest |
| UC_AUTH_003 | Login as Admin | Guest |
| UC_CAT_001 | Browse Product Catalogue | Guest / Customer |
| UC_CAT_002 | View Product Detail | Guest / Customer |
| UC_CART_001 | Add Product to Cart | Customer |
| UC_CART_002 | Remove Product from Cart | Customer |
| UC_CART_003 | View Cart | Customer |
| UC_ORD_001 | Place Order (Checkout) | Customer |
| UC_ORD_002 | View Order History | Customer |
| UC_ADM_001 | Manage Products (Add/View) | Admin |
| UC_ADM_002 | Manage Customers | Admin |
| UC_ADM_003 | Manage Contact Messages | Admin |

---

## Phase 3 — Technical / Functional Documentation

**Agent:** `technical-documenter`  
**Skill:** `java-analysis`  
**Estimated tasks:** 5  
**Output directory:** `docs/functional/`

### Tasks

| ID | Task | Output |
|----|------|--------|
| T-1 | Derive FUREQs from BUREQs for Authentication | `FUREQ_AUTH_001`, `FUREQ_AUTH_002` |
| T-2 | Derive FUREQs for Product Catalog | `FUREQ_CAT_001` |
| T-3 | Derive FUREQs for Shopping Cart | `FUREQ_CART_001`, `FUREQ_CART_002` |
| T-4 | Derive FUREQs for Orders & Checkout | `FUREQ_ORD_001`, `FUREQ_ORD_002` |
| T-5 | Derive FUREQs for Admin Management | `FUREQ_ADM_001` |
| T-6 | Document NFUREQs (performance, security baseline, file upload constraints) | `NFUREQ_001`, `NFUREQ_002` |
| T-7 | Create technical flow diagrams (Mermaid sequence diagrams) | `FF_LOGIN.md`, `FF_CHECKOUT.md`, `FF_CART.md`, `FF_PRODUCT_BROWSE.md` |
| T-8 | Document servlet API map (URL → Servlet → DAO → Response) | `docs/functional/integration/servlet-api-map.md` |
| T-9 | Document database schema (all tables, columns, constraints inferred from entities + DAO SQL) | `docs/functional/integration/db-schema.md` |
| T-10 | Create functional index | `docs/functional/index.md` |

---

## Phase 4 — Documentation Coordination

**Agent:** `doc-coordinator`  
**Estimated tasks:** 5  
**Output directory:** `docs/`, `docs/domain/`, `docs/traceability/`

### Tasks

| ID | Task | Output |
|----|------|--------|
| C-1 | Validate directory structure and cross-references | (in-place validation) |
| C-2 | Create master index | `docs/index.md` |
| C-3 | Create system overview | `docs/system-overview.md` |
| C-4 | Build traceability matrix (BUREQ → FUREQ → Flow → Component) | `docs/traceability/requirement-matrix.md` |
| C-5 | Create flow-to-component map | `docs/traceability/flow-to-component-map.md` |
| C-6 | Create ID registry | `docs/traceability/id-registry.md` |
| C-7 | Create domain concepts catalog | `docs/domain/domain-concepts-catalog.md` |
| C-8 | Create ubiquitous language glossary | `docs/domain/ubiquitous-language.md` |
| C-9 | Define bounded contexts | `docs/domain/bounded-contexts.md` |

---

## Phase 5 — Verification

**Agent:** `verification`  
**Estimated tasks:** 6  
**Output directory:** `docs/verification/`

### Tasks

| ID | Task | Output |
|----|------|--------|
| V-1 | Build table usage matrix — map every DB table to flows that read/write it | `docs/verification/table-usage-matrix.md` |
| V-2 | Repository query deep analysis — read actual DAO code, compare WHERE conditions against documented business rules | Part of `gap-report.md` |
| V-3 | Validation completeness check — enumerate all validations in servlets/JSPs, verify each has condition + true/false outcomes documented | Part of `gap-report.md` |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links | `docs/verification/cross-domain-dependencies.md` |
| V-5 | Entity state machine verification — map all status transitions in code, verify all are documented | Part of `gap-report.md` |
| V-6 | Generate consolidated gap report with severity classification and remediation prompts | `docs/verification/gap-report.md` |

### Gap Severity Classification

| Severity | Definition |
|----------|-----------|
| Critical | Undocumented flow that processes user data or financial transactions |
| High | Missing business rule from a WHERE clause or validation condition |
| Medium | Incomplete true/false outcomes for a documented validation |
| Low | Missing cross-reference link between related documents |

---

## Completeness Criteria

### Per-Flow

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true AND false outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Cookie auth guard presence/absence is documented for each servlet
- [ ] Error/redirect paths are documented (fail.jsp, failc.jsp, paymentfail.jsp, etc.)
- [ ] Flash cookie usage (maxAge=10) is documented per flow

### Per-Domain

- [ ] All flows meet per-flow criteria
- [ ] Cross-domain table dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries

---

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|-------|-------|-----------|-----------|
| Planning | Planning Agent | 1 | Low |
| Discovery — Batch 1 (Auth) | Discovery | 7 | High |
| Discovery — Batch 2 (Catalog) | Discovery | 7 | High |
| Discovery — Batch 3 (Cart) | Discovery | 7 | High |
| Discovery — Batch 4 (Orders) | Discovery | 7 | High |
| Discovery — Batch 5 (Admin) | Discovery | 7 | High |
| Discovery — Consolidation | Discovery | 3 | Medium |
| Business | Business Documenter | 9 | Medium |
| Technical | Technical Documenter | 10 | High |
| Coordination | Doc Coordinator | 9 | Medium |
| Verification | Verification Agent | 6 | High |
| **Total** | | **~73** | |

---

## Artifact Inventory

### Discovery Phase

```
docs/discovery/
├── batch-1-auth-flows.md
├── batch-1-auth-components.md
├── batch-1-auth-domain-concepts.md
├── batch-2-catalog-flows.md
├── batch-2-catalog-components.md
├── batch-2-catalog-domain-concepts.md
├── batch-3-cart-flows.md
├── batch-3-cart-components.md
├── batch-3-cart-domain-concepts.md
├── batch-4-orders-flows.md
├── batch-4-orders-components.md
├── batch-4-orders-domain-concepts.md
├── batch-5-admin-flows.md
├── batch-5-admin-components.md
├── batch-5-admin-domain-concepts.md
├── cross-domain-table-matrix.md
└── consolidation-gaps.md
```

### Business Phase

```
docs/business/
├── index.md
├── overview/
│   └── business-overview.md
├── use-cases/
│   ├── UC_AUTH_001.md  UC_AUTH_002.md  UC_AUTH_003.md
│   ├── UC_CAT_001.md   UC_CAT_002.md
│   ├── UC_CART_001.md  UC_CART_002.md  UC_CART_003.md
│   ├── UC_ORD_001.md   UC_ORD_002.md
│   └── UC_ADM_001.md   UC_ADM_002.md  UC_ADM_003.md
└── processes/
    ├── BP_CHECKOUT.md
    ├── BP_PRODUCT_MGMT.md
    └── BP_CUSTOMER_MGMT.md
```

### Technical Phase

```
docs/functional/
├── index.md
├── requirements/
│   ├── FUREQ_AUTH_001.md  FUREQ_AUTH_002.md
│   ├── FUREQ_CAT_001.md
│   ├── FUREQ_CART_001.md  FUREQ_CART_002.md
│   ├── FUREQ_ORD_001.md   FUREQ_ORD_002.md
│   ├── FUREQ_ADM_001.md
│   ├── NFUREQ_001.md
│   └── NFUREQ_002.md
├── flows/
│   ├── FF_LOGIN.md
│   ├── FF_CHECKOUT.md
│   ├── FF_CART.md
│   └── FF_PRODUCT_BROWSE.md
└── integration/
    ├── db-schema.md
    └── servlet-api-map.md
```

### Coordination Phase

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

### Verification Phase

```
docs/verification/
├── gap-report.md
├── table-usage-matrix.md
└── cross-domain-dependencies.md
```

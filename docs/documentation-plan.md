# EcommerceApp — Documentation Plan

**Module**: EcommerceApp  
**Language**: Java  
**Framework**: Servlet 3.0 + JSP  
**Build**: Maven (WAR)  
**Database**: SQLite (org.xerial:sqlite-jdbc)  
**State file**: `docs/EcommerceApp-state.json`  
**Generated**: 2026-03-17T12:48:37.221Z  

---

## Project Summary

Java J2EE Online Electronic Shopping application with:

| Component | Count |
|---|---|
| Servlet classes (`@WebServlet`) | 21 |
| DAO classes (DAO–DAO5) | 5 |
| Entity beans | 14 |
| JSP pages (incl. tripling variants) | 63 |
| CSS files | 7 |

Authentication uses **cookies** (`cname` = customer email, `tname` = admin username, `maxAge=9999`). No Spring, no ORM, no DI container.

---

## Business Domains

| Domain | Servlets | Key JSPs | Tables |
|---|---|---|---|
| **Auth & User Management** | checkcustomer, checkadmin, addcustomer, deletecustomer | customerlogin, adminlogin, customer_reg, customerhome, adminhome | customer, usermaster |
| **Product Catalog** | — (JSP direct DAO) | mobile/c/a, laptop/c/a, tv/c/a, watch/c/a, category, selecteditem, viewproduct | mobile, laptop, tv, watch, brand, category |
| **Shopping Cart** | addtocart, addtocartnull, addtocartnulla, removecart, removecarta, removecartnull, removecartnulla | cart, carta, cartnull, cartnulla, cartnullqty | cart |
| **Order & Payment** | payprocess, ShippingAddress2, removeorders, remove_orders | ShippingAddress, confirmpayment, confirmonline, orders, orderdetails, paymentfail | orders, order_details |
| **Admin Management** | addproduct, deletecustomer, removetable_cart, removetable_order_details | addproduct, managecustomers, managetables, table_cart, table_contactus, table_order_details, table_orders | all tables (admin reads) |
| **Contact & Communication** | addContactus, addContactusc, remove_contactus | contactus, contactusc | contactus |

---

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | planning-agent | 1 | Low |
| Discovery — 6 batches | discovery | 42 (6×7) | High |
| Discovery — consolidation | discovery | 3 | Medium |
| Business | business-documenter | 5 | Medium |
| Technical | technical-documenter | 5 | High |
| Coordination | doc-coordinator | 5 | Medium |
| Verification | verification | 6 | High |
| **Total** | | **67** | |

---

## Phase 1 — Discovery (6 Batches)

**Agent**: `discovery`  
**Strategy**: Domain-batched — one batch per business domain, followed by a cross-domain consolidation step.  
**Output directory**: `docs/discovery/`

### Batch 1 — Authentication & User Management

**Servlets**: `checkcustomer`, `checkadmin`, `addcustomer`, `deletecustomer`  
**DAOs**: `DAO3` (checkcust, checkadmin, addcustomer), `DAO2` (getAllCustomer, deleteCustomer, getCustomer)  
**Entities**: `customer`, `usermaster`  
**JSPs**: `customerlogin.jsp`, `adminlogin.jsp`, `customer_reg.jsp`, `customerhome.jsp`, `adminhome.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `fail.jsp`, `failc.jsp`

**Output files**:
- `docs/discovery/batch-1-auth-flows.md`
- `docs/discovery/batch-1-auth-components.md`
- `docs/discovery/batch-1-auth-domain-concepts.md`

**Tasks** (7):
1. Detect entry points (servlet `@WebServlet` URLs + JSP form actions)
2. Trace authentication flow: form → servlet → DAO → cookie-set → redirect
3. Deep data access analysis: examine `checkcust`, `checkadmin`, `addcustomer` — document all WHERE clauses, credential comparison logic, and duplicate-check conditions as business rules
4. Inventory all components (servlets, JSPs, DAO methods, entity fields)
5. Extract domain concepts: Customer, Admin, Session-via-Cookie, Authentication, Registration
6. Map inter-component dependencies: which JSPs call which servlets; how cookies are consumed by navbars
7. Cross-domain table usage: identify if `customer` or `usermaster` tables are read by any non-auth flows

**Pay special attention to**:
- Cookie lifecycle (`cname`, `tname`, `maxAge=9999` vs. `maxAge=10` flash cookies)
- Duplicate email check in `addcustomer`
- The absence of password hashing (plaintext) — document as security baseline observation
- `validatelogina.jsp` and `validateloginc.jsp` — are these JSP-side validation pages or redirect pages?

---

### Batch 2 — Product Catalog

**Servlets**: None (JSPs call DAO directly via scriptlets)  
**DAOs**: `DAO` (getAllbrand, getAllcategory), `DAO3` (getAlltv, getAlllaptop, getAllmobile, getAllwatch, getSelecteditem), `DAO2` (getAllviewlist)  
**Entities**: `mobile`, `laptop`, `tv`, `watch`, `brand`, `category`, `viewlist`  
**JSPs**: `index.jsp`, `mobile.jsp`/`mobilec.jsp`/`mobilea.jsp`, `laptop.jsp`/`laptopc.jsp`/`laptopa.jsp`, `tv.jsp`/`tvc.jsp`/`tva.jsp`, `watch.jsp`/`watchc.jsp`/`watcha.jsp`, `category.jsp`/`categoryc.jsp`/`categorya.jsp`, `selecteditem.jsp`/`selecteditemc.jsp`/`selecteditema.jsp`, `viewproduct.jsp`/`viewproductc.jsp`/`viewproducta.jsp`

**Output files**:
- `docs/discovery/batch-2-catalog-flows.md`
- `docs/discovery/batch-2-catalog-components.md`
- `docs/discovery/batch-2-catalog-domain-concepts.md`

**Tasks** (7):
1. Detect entry points: homepage product links, category navigation
2. Trace catalog browsing flow: index → category → product list → selected item → view product detail
3. Deep data access analysis: examine `getSelecteditem(String st)`, `getAllviewlist()` — document all WHERE/filtering conditions; examine how category and brand are joined
4. Inventory all components: JSP tripling pattern (guest/customer/admin variants), DAO methods
5. Extract domain concepts: Product, Category, Brand, Mobile, Laptop, TV, Watch, ProductDetail, Viewlist
6. Map how the three JSP variants (plain/c/a) differ in navigation and available actions
7. Cross-domain table usage: confirm product tables read from cart and order flows

**Pay special attention to**:
- `getSelecteditem(String st)` — what field does `st` filter on? Document the WHERE clause completely
- `getAllviewlist()` — what does viewlist represent vs. individual product tables?
- How the JSP tripling pattern conditionally shows "Add to Cart" only to logged-in customers
- Image paths stored in product records (file upload linkage from admin domain)

---

### Batch 3 — Shopping Cart

**Servlets**: `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla`  
**DAOs**: `DAO3` (checkaddtocartnull, updateaddtocartnull, addtocartnull, getSelectedcart, getcart, removecartnull, removecart), `DAO4` (checkaddtocartnull, updateaddtocartnull, addtocartnull, getSelectedcart, getcart, removecartnull — note DAO3 and DAO4 both have these)  
**Entities**: `cart`  
**JSPs**: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`

**Output files**:
- `docs/discovery/batch-3-cart-flows.md`
- `docs/discovery/batch-3-cart-components.md`
- `docs/discovery/batch-3-cart-domain-concepts.md`

**Tasks** (7):
1. Detect entry points: "Add to Cart" buttons on product pages; "Remove" in cart view
2. Trace add-to-cart flow for logged-in customer: `addtocart` servlet → DAO `checkaddtocartnull` → if exists `updateaddtocartnull` else `addtocartnull` → redirect
3. Trace add-to-cart flow for guest ("null"): `addtocartnull`/`addtocartnulla` — document how cart is persisted without login
4. Deep data access analysis: `checkaddtocartnull(cart c)` — what fields uniquely identify a cart row? Document check logic, update conditions, and quantity increment
5. Inventory all components: distinguish `cartnull` (guest) vs `cart` (logged-in customer) variants
6. Extract domain concepts: Cart, CartItem, Quantity, GuestCart, CustomerCart
7. Cross-domain table usage: `cart` table also read by order flow (`checkcart`, `deletecart`, `getAllcart`)

**Pay special attention to**:
- Duplicate method signatures across `DAO3` and `DAO4` — are these identical implementations or differ?
- `cartnullqty.jsp` — what quantity-error scenario does this handle?
- The distinction between `removecart` (customer) vs `removecarta` (admin view) vs `removecartnull` (guest)
- How cart is cleared after order placement (`deletecart`, `deletecart2`)

---

### Batch 4 — Order & Payment

**Servlets**: `payprocess`, `ShippingAddress2`, `removeorders`  
**DAOs**: `DAO4` (checkcart, checkcart2, addOrders, addOrder_details, addOrder_details2, deletecart, deletecart2, getOrders, getOrdersbydate, getOrderdetails, getAllorders, getAllorder_details, removeorder_details, updateOrder_details, updateOrder_details2), `DAO2` (removeorders)  
**Entities**: `orders`, `order_details`, `cart`  
**JSPs**: `ShippingAddress.jsp`, `confirmpayment.jsp`, `confirmonline.jsp`, `orders.jsp`, `orderdetails.jsp`, `paymentfail.jsp`, `table_orders.jsp`, `table_order_details.jsp`

**Output files**:
- `docs/discovery/batch-4-orders-flows.md`
- `docs/discovery/batch-4-orders-components.md`
- `docs/discovery/batch-4-orders-domain-concepts.md`

**Tasks** (7):
1. Detect entry points: "Proceed to checkout" from cart, "Place Order" from shipping page
2. Trace full checkout flow: cart → ShippingAddress.jsp → `ShippingAddress2` servlet → `payprocess` servlet → insert orders/order_details → delete cart → confirmpayment/confirmonline
3. Deep data access analysis: `addOrders`, `addOrder_details`, `addOrder_details2` — document all fields persisted; examine `checkcart` vs `checkcart2` distinction (what does `String st` filter on in `checkcart2`?); document `getOrdersbydate(String d)` WHERE clause
4. Inventory all components: two payment confirmation pages — `confirmpayment` vs `confirmonline` — document difference
5. Extract domain concepts: Order, OrderDetail, ShippingAddress, PaymentMethod (COD vs Online), OrderStatus
6. Map order-to-cart dependency: cart must be non-empty → `checkcart` → proceed; after payment → `deletecart`
7. Cross-domain table usage: `orders` and `order_details` tables read by admin management flows

**Pay special attention to**:
- `addOrder_details` vs `addOrder_details2(String st)` — one inserts all cart items, the other inserts a subset?
- `updateOrder_details` vs `updateOrder_details2` — what order status transitions do these drive?
- `getOrdersbydate(String d)` — is `d` a date string or a customer identifier?
- `paymentfail.jsp` — what triggers payment failure (given this is a demo with no real payment gateway)?
- `remove_orders` servlet (admin) vs `removeorders` servlet (customer) — document who can cancel orders

---

### Batch 5 — Admin Management

**Servlets**: `addproduct`, `deletecustomer`, `removetable_cart`, `removetable_order_details`, `remove_orders`, `remove_contactus`  
**DAOs**: `DAO` (addproduct, getAllbrand, getAllcategory), `DAO2` (getAllCustomer, deleteCustomer, getCustomer, getAllorders, getAllorder_details, removeorder_details), `DAO5` (getcontactus, removecont), `DAO4` (getAllcart, getAllorders, getAllorder_details, removeorder_details)  
**Entities**: `Product`, `customer`, `orders`, `order_details`, `cart`, `contactus`  
**JSPs**: `addproduct.jsp`, `managecustomers.jsp`, `managetables.jsp`, `table_cart.jsp`, `table_contactus.jsp`, `table_order_details.jsp`, `table_orders.jsp`, `adminhome.jsp`, `selecteditema.jsp`, `viewproducta.jsp`  
**Utilities**: `MyUtilities.UploadFile()` (image upload)

**Output files**:
- `docs/discovery/batch-5-admin-flows.md`
- `docs/discovery/batch-5-admin-components.md`
- `docs/discovery/batch-5-admin-domain-concepts.md`

**Tasks** (7):
1. Detect entry points: admin navbar → addproduct, managecustomers, managetables
2. Trace product addition flow: `addproduct.jsp` form → `addproduct` servlet → `MyUtilities.UploadFile()` → `DAO.addproduct()` → redirect
3. Deep data access analysis: `DAO.addproduct(HttpServletRequest)` — document all fields inserted; examine `MyUtilities.UploadFile()` — document allowed extensions, file path construction, error conditions
4. Inventory all components: admin-only JSPs (`*a.jsp` variants), admin table management pages
5. Extract domain concepts: ProductManagement, CustomerManagement, OrderManagement, TableView, FileUpload
6. Map admin auth guard: how `tname` cookie presence gates admin pages
7. Cross-domain table usage: admin reads ALL tables — document this cross-domain read dependency

**Pay special attention to**:
- `MyUtilities.UploadFile()` — document extension validation (whitelist/blacklist), hardcoded upload path in DAO.java
- `addproduct` servlet — does it validate brand/category FK existence before insert?
- `managetables.jsp` — is this a single page that shows all tables, or a gateway page?
- `deletecustomer` — does it cascade to cart and orders?
- `remove_orders` (admin) vs `removeorders` (customer) — confirm who can invoke each

---

### Batch 6 — Contact & Communication

**Servlets**: `addContactus`, `addContactusc`, `remove_contactus`  
**DAOs**: `DAO5` (addContactus, getcontactus, removecont)  
**Entities**: `contactus`  
**JSPs**: `contactus.jsp` (admin view), `contactusc.jsp` (customer/guest submit form), `table_contactus.jsp`

**Output files**:
- `docs/discovery/batch-6-contact-flows.md`
- `docs/discovery/batch-6-contact-components.md`
- `docs/discovery/batch-6-contact-domain-concepts.md`

**Tasks** (7):
1. Detect entry points: navbar "Contact Us" links from all three user contexts
2. Trace contact form submission flow: `contactusc.jsp` → `addContactusc` servlet → `DAO5.addContactus()` → redirect; and `contactus.jsp` → `addContactus` (admin version)
3. Deep data access analysis: `DAO5.addContactus(contactus c)` — document all fields stored; `getcontactus()` — any filtering/ordering?
4. Inventory all components: `addContactus` vs `addContactusc` — document the difference between admin and customer contact submission
5. Extract domain concepts: ContactMessage, CustomerInquiry, AdminInbox
6. Map how admin views and deletes contact messages: `table_contactus.jsp` → `remove_contactus` → `DAO5.removecont()`
7. Cross-domain table usage: `contactus` table not referenced by other domains (isolated)

**Pay special attention to**:
- Are `addContactus` and `addContactusc` identical in behaviour or does one require authentication?
- `contactus.jsp` (no 'c' suffix) — is this the admin view or a shared form?
- `cufail.jsp`/`cufailc.jsp` and `cupass.jsp`/`cupassc.jsp` — do these belong to contact submission or customer auth? Classify correctly.

---

### Consolidation Step (3 tasks)

**Tasks**:
1. **Merge batch outputs** — create unified component inventory; resolve DAO method duplications between DAO3 and DAO4
2. **Build cross-domain table matrix** — produce `docs/discovery/cross-domain-table-matrix.md` mapping every DB table to all flows that read or write it (reference batches 1–6)
3. **Flag undocumented business rules** — create `docs/discovery/consolidation-gaps.md` listing business rules that are implied by code structure but not yet explicitly documented

---

## Phase 2 — Business Documenter

**Agent**: `business-documenter`  
**Prerequisite**: Discovery phase complete  
**Input**: All `docs/discovery/` files  
**Output directory**: `docs/business/`  
**Skill**: `java-analysis`

**Tasks** (5):
1. **Define actors** — Guest, Customer (logged-in), Admin; document their capabilities and constraints
2. **Create use cases** — one `UC_*.md` per significant user goal (≥13 use cases across 6 domains)
3. **Write BUREQs** — business requirements derived from use cases and discovery gaps
4. **Create business process documents** — one `BP_*.md` per end-to-end business process (≥5 processes)
5. **Write business overview** — `docs/business/overview/business-overview.md` covering store purpose, product categories, user roles, and ordering model

**Expected artefacts** (representative):
- `docs/business/index.md`
- `docs/business/overview/business-overview.md`
- `docs/business/use-cases/UC_AUTH_001.md` — Customer Registration
- `docs/business/use-cases/UC_AUTH_002.md` — Customer Login
- `docs/business/use-cases/UC_AUTH_003.md` — Admin Login
- `docs/business/use-cases/UC_CAT_001.md` — Browse Products by Category
- `docs/business/use-cases/UC_CAT_002.md` — View Product Detail
- `docs/business/use-cases/UC_CART_001.md` — Add Item to Cart
- `docs/business/use-cases/UC_CART_002.md` — Remove Item from Cart
- `docs/business/use-cases/UC_ORD_001.md` — Place Order (COD)
- `docs/business/use-cases/UC_ORD_002.md` — Place Order (Online Payment)
- `docs/business/use-cases/UC_ADM_001.md` — Add Product
- `docs/business/use-cases/UC_ADM_002.md` — Manage Customers
- `docs/business/use-cases/UC_CON_001.md` — Submit Contact Inquiry
- `docs/business/processes/BP_REGISTRATION.md`
- `docs/business/processes/BP_BROWSE_PURCHASE.md`
- `docs/business/processes/BP_CART_CHECKOUT.md`
- `docs/business/processes/BP_ORDER_MANAGEMENT.md`
- `docs/business/processes/BP_ADMIN_OPERATIONS.md`

---

## Phase 3 — Technical / Functional Documenter

**Agent**: `technical-documenter`  
**Prerequisite**: Business phase complete  
**Input**: All `docs/business/` files + `docs/discovery/` files  
**Output directory**: `docs/functional/`  
**Skill**: `java-analysis`

**Tasks** (5):
1. **Derive FUREQs** — functional requirements from BUREQs; one `FUREQ_*.md` per significant functional requirement
2. **Document NFUREQs** — non-functional requirements (`NFUREQ_001` = security baseline, `NFUREQ_002` = cookie-based session constraints, etc.)
3. **Create technical flow diagrams** — one `FF_*.md` per end-to-end technical flow with sequence steps, DAO calls, SQL, redirects
4. **Document data schemas** — entity field tables, DB table definitions inferred from DAO code
5. **Document validation rules** — enumerate all input validations: form field constraints, file upload extension whitelist, duplicate checks

**Expected artefacts** (representative):
- `docs/functional/index.md`
- `docs/functional/requirements/FUREQ_AUTH_001.md`
- `docs/functional/requirements/FUREQ_AUTH_002.md`
- `docs/functional/requirements/FUREQ_CAT_001.md`
- `docs/functional/requirements/FUREQ_CART_001.md`
- `docs/functional/requirements/FUREQ_ORD_001.md`
- `docs/functional/requirements/FUREQ_ADM_001.md`
- `docs/functional/requirements/NFUREQ_001.md`
- `docs/functional/requirements/NFUREQ_002.md`
- `docs/functional/flows/FF_LOGIN_FLOW.md`
- `docs/functional/flows/FF_CART_FLOW.md`
- `docs/functional/flows/FF_CHECKOUT_FLOW.md`
- `docs/functional/flows/FF_PRODUCT_BROWSE_FLOW.md`
- `docs/functional/integration/INT_SQLITE.md`
- `docs/functional/integration/INT_FILE_UPLOAD.md`

---

## Phase 4 — Doc Coordinator

**Agent**: `doc-coordinator`  
**Prerequisite**: Technical phase complete  
**Input**: All `docs/` files  
**Output directory**: `docs/` (index, overview), `docs/domain/`, `docs/traceability/`  
**Skill**: `java-analysis`

**Tasks** (5):
1. **Validate directory structure** — ensure all expected artefacts from phases 1–3 exist; flag missing files
2. **Create master index** — `docs/index.md` linking all documents in a structured hierarchy
3. **Create system overview** — `docs/system-overview.md` covering architecture, technology stack, deployment model, cookie auth model
4. **Build traceability matrix** — `docs/traceability/requirement-matrix.md` mapping BUREQ → UC → FUREQ; `docs/traceability/flow-to-component-map.md` mapping FF → servlets/DAOs/JSPs
5. **Create domain concepts catalog** — `docs/domain/domain-concepts-catalog.md`, `docs/domain/ubiquitous-language.md`, `docs/domain/bounded-contexts.md`

---

## Phase 5 — Verification

**Agent**: `verification`  
**Prerequisite**: Coordination phase complete  
**Input**: All `docs/` files + source code  
**Output directory**: `docs/verification/`

**Tasks** (6):
1. **Build table usage matrix** — map every SQLite table (`customer`, `usermaster`, `mobile`, `laptop`, `tv`, `watch`, `brand`, `category`, `cart`, `orders`, `order_details`, `contactus`) to all flows that read and write it; check for undocumented cross-domain dependencies
2. **Repository query deep analysis** — read actual DAO methods; compare every WHERE clause, JOIN, and filtering condition against what is documented in business and functional requirements
3. **Validation completeness check** — enumerate every validation in servlets and DAO methods; verify each has both true outcome (success path) and false outcome (error redirect/page) documented
4. **Cross-domain dependency verification** — verify all cross-batch dependencies identified in `cross-domain-table-matrix.md` have bidirectional links in use cases and functional flows
5. **Entity state machine verification** — map order status transitions (`order_details` status updates via `updateOrder_details`/`updateOrder_details2`); verify all from→to states are documented
6. **Generate consolidated gap report** — `docs/verification/gap-report.md` with severity (Critical/High/Medium/Low), affected artefact, gap description, and remediation prompt

**Output artefacts**:
- `docs/verification/gap-report.md`
- `docs/verification/table-usage-matrix.md`
- `docs/verification/cross-domain-dependencies.md`

---

## Phase 6 — Remediation (Conditional)

**Trigger**: Verification gap report contains Critical or High severity items  
**Action**: Re-run Discovery Agent on specific flows named in the gap report using targeted prompts  
**Followed by**: Re-run Verification Agent to confirm gaps are resolved

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json
├── documentation-plan.md
├── index.md
├── system-overview.md
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
│   ├── batch-6-contact-flows.md
│   ├── batch-6-contact-components.md
│   ├── batch-6-contact-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/
│   │   └── business-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md … UC_CON_001.md
│   └── processes/
│       ├── BP_REGISTRATION.md … BP_ADMIN_OPERATIONS.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_*.md
│   │   └── NFUREQ_*.md
│   ├── flows/
│   │   └── FF_*.md
│   └── integration/
│       ├── INT_SQLITE.md
│       └── INT_FILE_UPLOAD.md
├── domain/
│   ├── domain-concepts-catalog.md
│   ├── ubiquitous-language.md
│   └── bounded-contexts.md
├── traceability/
│   ├── requirement-matrix.md
│   ├── flow-to-component-map.md
│   └── id-registry.md
└── verification/
    ├── gap-report.md
    ├── table-usage-matrix.md
    └── cross-domain-dependencies.md
```

---

## Per-Flow Completeness Criteria

For every documented flow, verification must confirm:

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true (success) AND false (error/redirect) outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from→to states
- [ ] Error handling paths are documented with redirect targets
- [ ] Cookie lifecycle (set/read/expire) is documented where relevant
- [ ] File upload paths and extension validation rules are explicitly listed (for product management flows)
- [ ] Amount/price logic (if any) is documented

## Per-Domain Completeness Criteria

For every documented domain:

- [ ] All flows in the domain meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries
- [ ] Guest, Customer, and Admin variants of each page are all documented

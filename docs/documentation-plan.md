# Documentation Plan — EcommerceApp

**Module:** EcommerceApp  
**Language:** Java  
**Framework:** Servlet 3.0 + JSP (servlet-jsp)  
**Build System:** Maven (WAR)  
**Database:** SQLite (via `org.xerial:sqlite-jdbc`)  
**Date Created:** 2026-03-17  
**State File:** `docs/EcommerceApp-state.json`

---

## 1. Repository Summary

| Metric | Count |
|---|---|
| Servlet classes | 21 |
| JSP pages | 62 |
| DAO classes | 5 (DAO – DAO5) |
| Entity beans | 11 |
| Database tables | 15 |
| Utility classes | 1 (MyUtilities) |

### Source Paths

| Path | Purpose |
|---|---|
| `EcommerceApp/src/main/java/com/servlet/` | Controller layer — 21 `@WebServlet` classes |
| `EcommerceApp/src/main/java/com/dao/` | Data access layer — DAO through DAO5 |
| `EcommerceApp/src/main/java/com/entity/` | Domain entity beans |
| `EcommerceApp/src/main/java/com/conn/` | Database connection singleton |
| `EcommerceApp/src/main/java/com/utility/` | File upload utility |
| `EcommerceApp/src/main/webapp/*.jsp` | Presentation layer + embedded DAO calls |

### Database Tables

`admin`, `brand`, `category`, `product`, `customer`, `cart`, `orders`, `order_details`, `Contactus`, `laptop`, `mobile`, `tv`, `watch`, `viewlist`, `usermaster`

---

## 2. Business Domains

| Domain | Key Servlets | Key JSPs | Tables Touched |
|---|---|---|---|
| Authentication | `checkadmin`, `checkcustomer` | `adminlogin`, `customerlogin`, `validatelogina`, `validateloginc` | `usermaster`, `admin`, `customer` |
| Product Catalog | `addproduct` | `mobile`, `laptop`, `tv`, `watch`, `viewproduct`, `addproduct`, `selecteditem` | `product`, `brand`, `category`, `mobile`, `laptop`, `tv`, `watch`, `viewlist` |
| Shopping Cart | `addtocart`, `addtocartnull`, `addtocartnulla`, `removecart`, `removecarta`, `removecartnull`, `removecartnulla` | `cart`, `carta`, `cartnull`, `cartnulla`, `cartnullqty` | `cart` |
| Order Management | `payprocess`, `ShippingAddress2`, `removeorders`, `remove_orders` | `ShippingAddress`, `confirmonline`, `confirmpayment`, `orders`, `orderdetails`, `paymentfail` | `orders`, `order_details`, `cart` |
| Customer Management | `addcustomer`, `deletecustomer` | `customer_reg`, `customerhome`, `managecustomers` | `customer` |
| Contact & Support | `addContactus`, `addContactusc`, `remove_contactus` | `contactus`, `contactusc`, `table_contactus` | `Contactus` |
| Admin Tools | `removetable_cart`, `removetable_order_details` | `adminhome`, `managetables`, `table_cart`, `table_orders`, `table_order_details` | `cart`, `order_details`, `orders` |

---

## 3. JSP Page Tripling Pattern

Most pages exist in three variants: `<page>.jsp` (guest), `<page>a.jsp` (admin), `<page>c.jsp` (customer):

| Base Page | Guest | Admin | Customer |
|---|---|---|---|
| `mobile` | `mobile.jsp` | `mobilea.jsp` | `mobilec.jsp` |
| `laptop` | `laptop.jsp` | `laptopa.jsp` | `laptopc.jsp` |
| `tv` | `tv.jsp` | `tva.jsp` | `tvc.jsp` |
| `watch` | `watch.jsp` | `watcha.jsp` | `watchc.jsp` |
| `category` | `category.jsp` | `categorya.jsp` | `categoryc.jsp` |
| `aboutus` | `aboutus.jsp` | `aboutusa.jsp` | `aboutusc.jsp` |
| `viewproduct` | `viewproduct.jsp` | `viewproducta.jsp` | `viewproductc.jsp` |
| `selecteditem` | `selecteditem.jsp` | `selecteditema.jsp` | `selecteditemc.jsp` |

---

## 4. Six-Phase Documentation Plan

### Phase 1 — Planning ✅

**Agent:** planning-agent  
**Status:** Complete  
**Output Artifacts:**
- `docs/EcommerceApp-state.json`
- `docs/documentation-plan.md`

---

### Phase 2 — Discovery

**Agent:** discovery  
**Skill Required:** `java-analysis`  
**Strategy:** Six domain batches, 7 tasks each, plus a consolidation step.

#### Batch 1 — Authentication & User Management

**Target files:**
- Servlets: `checkadmin.java`, `checkcustomer.java`, `addcustomer.java`, `deletecustomer.java`
- JSPs: `adminlogin.jsp`, `customerlogin.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `customer_reg.jsp`, `customerhome.jsp`, `managecustomers.jsp`
- DAO methods: customer validation, admin login, customer CRUD

**Output:** `docs/discovery/batch-1-authentication-flows.md`, `batch-1-authentication-components.md`, `batch-1-authentication-domain-concepts.md`

**Pay special attention to:**
- Cookie-based auth mechanism (`cname` cookie for customers, `tname` for admin, `maxAge=9999`)
- Flash message cookies (`maxAge=10`) used for login failure feedback
- Password comparison (plaintext — document as security observation)
- `SELECT * FROM customer WHERE Password=? AND Email_Id=?` — exact credential match logic
- `SELECT * FROM usermaster WHERE Name=? AND Password=?` — admin credential match
- Guest vs. registered customer distinction (null Name in cart vs. named cart)
- `deletecustomer` cascade: what happens to cart/orders when a customer is deleted?

**Tasks:**
1. Detect entry points (servlet URL mappings, JSP form actions)
2. Trace authentication execution flows (login → cookie set → redirect)
3. Deep data access query analysis (credential lookup WHERE clauses, customer existence check)
4. Inventory all components (servlets, JSPs, DAOs, entities involved)
5. Extract domain concepts (customer, admin, session, authentication, registration)
6. Map inter-function dependencies (login → home page, fail → fail JSP)
7. Cross-domain table usage — note that `customer` table is also read by cart and order servlets

---

#### Batch 2 — Product Catalog

**Target files:**
- Servlets: `addproduct.java`
- JSPs: `mobile.jsp`, `mobilea.jsp`, `mobilec.jsp`, `laptop.jsp`, `laptopa.jsp`, `laptopc.jsp`, `tv.jsp`, `tva.jsp`, `tvc.jsp`, `watch.jsp`, `watcha.jsp`, `watchc.jsp`, `viewproduct.jsp`, `viewproducta.jsp`, `viewproductc.jsp`, `selecteditem.jsp`, `selecteditema.jsp`, `selecteditemc.jsp`, `addproduct.jsp`, `category.jsp`, `categorya.jsp`, `categoryc.jsp`
- DAO methods: `getAllbrand()`, `getAllcategory()`, `addproduct()`, product listing queries
- Entities: `Product`, `brand`, `category`, `mobile`, `laptop`, `tv`, `watch`, `viewlist`

**Output:** `docs/discovery/batch-2-product-catalog-flows.md`, `batch-2-product-catalog-components.md`, `batch-2-product-catalog-domain-concepts.md`

**Pay special attention to:**
- `viewlist` table and `viewlist` entity — what is the relationship to `product`?
- `SELECT * FROM viewlist WHERE Pimage = ?` — used for individual product display, document the lookup key
- `SELECT * FROM mobile/laptop/tv/watch` — product-type-specific queries vs. generic `product` table
- How `addproduct` populates both `product` and category-specific tables (mobile/laptop/tv/watch)
- Image upload flow via `MyUtilities.UploadFile()` — extension validation rules
- Brand and category FK relationships in `product` table (`bid`, `cid`)
- JSP tripling pattern and which DAO methods each variant calls

**Tasks:**
1. Detect entry points (category browse, product detail, admin add-product form)
2. Trace product browse and product detail flows
3. Deep data access query analysis (product listing, category filtering, image path resolution)
4. Inventory all components
5. Extract domain concepts (product, brand, category, product type, image)
6. Map cross-domain dependencies (cart reads product data; orders copy product data)
7. Cross-domain table usage — `product`, `mobile`, `laptop`, `tv`, `watch`, `viewlist` used across cart and order flows

---

#### Batch 3 — Shopping Cart

**Target files:**
- Servlets: `addtocart.java`, `addtocartnull.java`, `addtocartnulla.java`, `removecart.java`, `removecarta.java`, `removecartnull.java`, `removecartnulla.java`
- JSPs: `cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`
- DAO methods: cart insert, cart select, cart remove, quantity increment, cart→order_details transfer
- Entity: `cart`

**Output:** `docs/discovery/batch-3-shopping-cart-flows.md`, `batch-3-shopping-cart-components.md`, `batch-3-shopping-cart-domain-concepts.md`

**Pay special attention to:**
- Dual-mode cart: `Name IS NULL` for guest cart vs. `Name = ?` for logged-in cart — document both paths
- `insert into cart values(?,?,?,?,?,?,?)` vs. `insert into cart (bname,cname,pname,pprice,pquantity,pimage) values(?,?,?,?,?,?)` — what is the 7-column vs. 6-column difference?
- Quantity increment logic: `UPDATE cart SET pquantity = (pquantity + 1) WHERE ...` — exact condition for de-duplication
- `SELECT * FROM cart WHERE Name=? AND bname=? AND cname=? AND pname=? AND pprice=? AND pimage=?` — product identity check before insert
- Suffix naming conventions: `addtocart` (logged-in), `addtocartnull` (guest), `addtocartnulla` (admin view of guest cart)
- `removecart` / `removecarta` / `removecartnull` / `removecartnulla` — document each variant's WHERE clause
- `cartnullqty.jsp` — what condition shows this page?

**Tasks:**
1. Detect entry points (add-to-cart buttons in product JSPs)
2. Trace cart add/remove/view flows for both guest and logged-in users
3. Deep data access query analysis (duplicate detection, quantity increment, guest vs. named)
4. Inventory all components
5. Extract domain concepts (cart item, guest cart, customer cart, quantity, price)
6. Map inter-function dependencies (cart → checkout → order_details)
7. Cross-domain table usage — `cart` is read/written by order servlets; note `insert into order_details ... SELECT * FROM cart WHERE Name = ?`

---

#### Batch 4 — Order Management & Payment

**Target files:**
- Servlets: `payprocess.java`, `ShippingAddress2.java`, `removeorders.java`, `remove_orders.java`
- JSPs: `ShippingAddress.jsp`, `confirmonline.jsp`, `confirmpayment.jsp`, `orders.jsp`, `orderdetails.jsp`, `paymentfail.jsp`
- DAO methods: orders insert, order_details insert (from cart), order_details select, orders select, orders delete
- Entities: `orders`, `order_details`

**Output:** `docs/discovery/batch-4-order-management-flows.md`, `batch-4-order-management-components.md`, `batch-4-order-management-domain-concepts.md`

**Pay special attention to:**
- Checkout flow: `ShippingAddress.jsp` → `ShippingAddress2` servlet → `confirmonline.jsp` or `confirmpayment.jsp`
- `payprocess.java` — does it clear the cart after order creation?
- `insert into order_details ... SELECT * FROM cart WHERE Name = ?` — atomic cart-to-order transfer; document NULL vs. named paths
- `update order_details set Date = ? , Name=? WHERE Date IS NULL` — post-payment date/name assignment (guest cart promotion)
- `insert into orders(Customer_Name,Customer_City,Date,Total_Price,Status) values(?,?,?,?,?)` — what values are used for `Status`? Document all order status transitions
- `removeorders.java` vs. `remove_orders.java` — two removal servlets; document difference (customer-initiated vs. admin-initiated)
- `select * from orders where Customer_Name = ?` and `select * from orders where Date = ?` — dual lookup strategies
- `select * from Order_details WHERE Date = ?` — date-based retrieval for order confirmation display

**Tasks:**
1. Detect entry points (checkout button in cart JSPs)
2. Trace complete checkout flow (shipping → payment → confirmation → order record creation)
3. Deep data access query analysis (cart-to-order transfer query, order date assignment, status values)
4. Inventory all components
5. Extract domain concepts (order, order detail, shipping address, payment, order status)
6. Map inter-function dependencies (cart is consumed by order process; customer name propagated)
7. Cross-domain table usage — `cart` is read and implicitly cleared; `customer` name is used in order

---

#### Batch 5 — Contact & Support

**Target files:**
- Servlets: `addContactus.java`, `addContactusc.java`, `remove_contactus.java`
- JSPs: `contactus.jsp`, `contactusc.jsp`, `table_contactus.jsp`, `cupass.jsp`, `cupassc.jsp`, `cufail.jsp`, `cufailc.jsp`
- DAO methods: Contactus insert, Contactus select all, Contactus delete by id
- Entity: `contactus`

**Output:** `docs/discovery/batch-5-contact-support-flows.md`, `batch-5-contact-support-components.md`, `batch-5-contact-support-domain-concepts.md`

**Pay special attention to:**
- `addContactus` (guest) vs. `addContactusc` (customer-logged-in) — document parameter differences
- `insert into Contactus(Name,Email_Id,Contact_No,Message) values(?,?,?,?)` — all four fields; validation rules
- `delete from Contactus where id= ?` — admin-only deletion
- `select * from Contactus` — admin view of all messages
- Flash cookies (`cupass`/`cufail`) used for contact form submission feedback

**Tasks:**
1. Detect entry points (contact form in `contactus.jsp` / `contactusc.jsp`)
2. Trace contact form submission flow (form → servlet → DB insert → feedback JSP)
3. Deep data access query analysis (insert validation, admin delete)
4. Inventory all components
5. Extract domain concepts (contact message, support enquiry, feedback)
6. Map inter-function dependencies (admin sees all messages via `table_contactus.jsp`)
7. Cross-domain table usage — `Contactus` is isolated; note that `Name` field may or may not equal a customer name

---

#### Batch 6 — Admin Tools & Table Management

**Target files:**
- Servlets: `removetable_cart.java`, `removetable_order_details.java`
- JSPs: `adminhome.jsp`, `managetables.jsp`, `managecustomers.jsp`, `table_cart.jsp`, `table_orders.jsp`, `table_order_details.jsp`
- DAO methods: select all from cart / orders / order_details, bulk delete
- Entities: N/A (raw table views)

**Output:** `docs/discovery/batch-6-admin-tools-flows.md`, `batch-6-admin-tools-components.md`, `batch-6-admin-tools-domain-concepts.md`

**Pay special attention to:**
- `removetable_cart.java` — `DELETE FROM cart WHERE Name IS NULL` — deletes ALL guest carts
- `removetable_order_details.java` — document which rows it deletes and under what condition
- `managetables.jsp` — what tables does admin see?
- `table_cart.jsp` / `table_orders.jsp` / `table_order_details.jsp` — admin read-only views
- `adminhome.jsp` — navigation hub; what links/stats are shown?
- Auth guard: are these JSPs checking for `tname` cookie before display?

**Tasks:**
1. Detect entry points (admin navigation menu)
2. Trace admin table view and bulk-delete flows
3. Deep data access query analysis (SELECT * queries for admin views, DELETE conditions)
4. Inventory all components
5. Extract domain concepts (admin role, table management, data cleanup)
6. Map inter-function dependencies (admin tools touch cart, orders, order_details — cross-domain)
7. Cross-domain table usage — documents admin's cross-domain access pattern

---

#### Discovery Consolidation Step

After all six batches:
1. **Merge batch outputs** — combine all flows, components, domain concepts into master lists
2. **Build cross-domain table matrix** — for each of the 15 tables, list all flows that read or write it
3. **Flag undocumented business rules** — identify any WHERE clauses or validation conditions not yet explained

**Output:** `docs/discovery/cross-domain-table-matrix.md`, `docs/discovery/consolidation-gaps.md`

---

### Phase 3 — Business Documentation

**Agent:** business-documenter  
**Prerequisite:** Discovery phase complete  
**Skill Required:** `java-analysis`

**Tasks:**
1. Define actors: Guest (unauthenticated), Customer (logged in), Admin
2. Create use cases (UC_*.md) — one per significant user interaction
3. Write Business Requirements (BUREQs) — at least one per use case
4. Create Business Process documents (BP_*.md) with BPMN-style descriptions
5. Write business overview document

**Expected Use Cases:**
| ID | Title |
|---|---|
| UC_AUTH_001 | Customer Registration |
| UC_AUTH_002 | Customer Login / Logout |
| UC_AUTH_003 | Admin Login |
| UC_PROD_001 | Browse Products by Category |
| UC_PROD_002 | Admin Add Product |
| UC_CART_001 | Add Item to Cart (Guest) |
| UC_CART_002 | Add Item to Cart (Logged-in Customer) |
| UC_CART_003 | Remove Item from Cart |
| UC_ORD_001 | Checkout and Place Order |
| UC_ORD_002 | View My Orders |
| UC_ORD_003 | Admin Cancel / Remove Order |
| UC_CUST_001 | Admin Manage Customers |
| UC_CUST_002 | Delete Customer |
| UC_CONT_001 | Submit Contact Enquiry |
| UC_ADMIN_001 | Admin Clear Table Data |

**Expected Business Processes:**
- BP_CUSTOMER_REGISTRATION
- BP_PRODUCT_BROWSING
- BP_CART_MANAGEMENT
- BP_ORDER_CHECKOUT
- BP_ADMIN_MANAGEMENT

**Output directory:** `docs/business/`

---

### Phase 4 — Technical / Functional Documentation

**Agent:** technical-documenter  
**Prerequisite:** Business phase complete  
**Skill Required:** `java-analysis`

**Tasks:**
1. Derive Functional Requirements (FUREQ_*.md) from BUREQs
2. Document Non-Functional Requirements (NFUREQ_*.md) — performance, security, availability
3. Create Technical Flow documents (FF_*.md) with servlet → DAO → DB sequence diagrams
4. Document API-like interface (servlet URLs, request parameters, response redirects)
5. Document data schemas and validation rules

**Expected FUREQs:**
- FUREQ_AUTH_001 — Customer login credential validation
- FUREQ_AUTH_002 — Cookie-based session management
- FUREQ_PROD_001 — Product image upload and storage
- FUREQ_CART_001 — Dual-mode cart (guest vs. named) with quantity de-duplication
- FUREQ_ORD_001 — Cart-to-order atomic transfer
- FUREQ_CUST_001 — Customer CRUD operations

**Expected NFUREQs:**
- NFUREQ_PERF_001 — Page load performance with SQLite
- NFUREQ_SEC_001 — Known security limitations (plaintext passwords, cookie auth, no CSRF)

**Expected Technical Flows:**
- FF_LOGIN_FLOW — Login validation sequence diagram
- FF_CART_FLOW — Add/remove cart item sequence diagram
- FF_CHECKOUT_FLOW — Checkout → payment → order creation sequence diagram
- FF_PRODUCT_CATALOG_FLOW — Product browse and detail view flow

**Integration Documents:**
- INT_SQLITE_DATABASE — Tables, column types, constraints, FK relationships
- INT_FILE_UPLOAD — `MyUtilities.UploadFile()` contract, allowed extensions, upload path

**Output directory:** `docs/functional/`

---

### Phase 5 — Coordination

**Agent:** doc-coordinator  
**Prerequisite:** Technical phase complete

**Tasks:**
1. Validate directory structure and ensure all expected artifacts exist
2. Create master index (`docs/index.md`) — landing page with links to all docs
3. Create system overview (`docs/system-overview.md`) — architecture narrative, component diagram
4. Build traceability matrix (`docs/traceability/requirement-matrix.md`) — BUREQ → FUREQ → Flow → Component
5. Create domain concepts catalog (`docs/domain/domain-concepts-catalog.md`)
6. Create ubiquitous language glossary (`docs/domain/ubiquitous-language.md`)
7. Create bounded contexts map (`docs/domain/bounded-contexts.md`)
8. Build flow-to-component map (`docs/traceability/flow-to-component-map.md`)
9. Maintain ID registry (`docs/traceability/id-registry.md`)

**Output directory:** `docs/`, `docs/domain/`, `docs/traceability/`

---

### Phase 6 — Verification

**Agent:** verification  
**Prerequisite:** Coordination phase complete

**Tasks (V-1 through V-6):**

| Task | Description |
|---|---|
| V-1 | Build table usage matrix — map every table to all flows that read/write it; flag cross-domain dependencies |
| V-2 | Repository query deep analysis — compare WHERE clauses in DAO code against documented business rules |
| V-3 | Validation completeness check — enumerate all input validations in servlets/DAOs; verify each has true/false outcomes documented |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links |
| V-5 | Entity state machine verification — map all `Status` column values used in `orders` table; verify transitions documented |
| V-6 | Generate consolidated gap report with severity classification (Critical / High / Medium / Low) and remediation prompts |

**Output directory:** `docs/verification/`

---

## 5. Effort Estimate

| Phase | Agent | Estimated Tasks | Complexity |
|---|---|---|---|
| Planning | planning-agent | 1 | Low |
| Discovery — 6 batches | discovery | 6 × 7 = 42 | High — deep query analysis + cross-domain |
| Discovery — Consolidation | discovery | 3 | Medium |
| Business | business-documenter | 5 | Medium |
| Technical | technical-documenter | 5 | High |
| Coordination | doc-coordinator | 5 | Medium |
| Verification | verification | 6 | High — cross-check all docs vs. code |
| **Total** | | **67** | |

---

## 6. Completeness Criteria

### Per-Flow Completeness Criteria

- [ ] ALL WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] ALL validation conditions have both true AND false outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from → to states (especially `orders.Status`)
- [ ] Error handling paths are documented (fail JSPs, flash cookies, redirect targets)
- [ ] Cookie-based auth guards are documented for each protected page/servlet
- [ ] Guest vs. authenticated user branches are explicitly documented for all dual-mode flows
- [ ] File upload validation rules (allowed extensions, path) are documented
- [ ] SQL injection prevention (PreparedStatement usage) is noted per DAO method

### Per-Domain Completeness Criteria

- [ ] All flows in the domain meet per-flow criteria
- [ ] Cross-domain table dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries
- [ ] JSP tripling pattern variants are documented with differences called out

---

## 7. Known Cross-Domain Dependencies (Pre-identified)

| From Domain | To Domain | Shared Table / Mechanism |
|---|---|---|
| Shopping Cart | Order Management | `cart` table — `INSERT INTO order_details SELECT * FROM cart` |
| Authentication | Shopping Cart | `customer.Name` used as `cart.Name` discriminator |
| Authentication | Order Management | `customer.Name` used as `orders.Customer_Name` |
| Product Catalog | Shopping Cart | Product attributes (`bname`, `cname`, `pname`, `pprice`, `pimage`) copied into `cart` |
| Shopping Cart | Admin Tools | Admin can view and bulk-delete all cart rows |
| Order Management | Admin Tools | Admin can view and delete order_details rows |
| Authentication | Customer Management | `deletecustomer` removes from `customer` table; impact on cart/orders TBD |

---

## 8. Output Directory Structure

```
docs/
├── EcommerceApp-state.json           # Phase state tracking
├── documentation-plan.md             # This file
├── index.md                          # Master landing page (Phase 5)
├── system-overview.md                # Architecture overview (Phase 5)
├── discovery/                        # Phase 2 outputs
│   ├── batch-1-authentication-flows.md
│   ├── batch-1-authentication-components.md
│   ├── batch-1-authentication-domain-concepts.md
│   ├── batch-2-product-catalog-flows.md
│   ├── batch-2-product-catalog-components.md
│   ├── batch-2-product-catalog-domain-concepts.md
│   ├── batch-3-shopping-cart-flows.md
│   ├── batch-3-shopping-cart-components.md
│   ├── batch-3-shopping-cart-domain-concepts.md
│   ├── batch-4-order-management-flows.md
│   ├── batch-4-order-management-components.md
│   ├── batch-4-order-management-domain-concepts.md
│   ├── batch-5-contact-support-flows.md
│   ├── batch-5-contact-support-components.md
│   ├── batch-5-contact-support-domain-concepts.md
│   ├── batch-6-admin-tools-flows.md
│   ├── batch-6-admin-tools-components.md
│   ├── batch-6-admin-tools-domain-concepts.md
│   ├── cross-domain-table-matrix.md  # Consolidation
│   └── consolidation-gaps.md         # Flagged gaps
├── business/                         # Phase 3 outputs
│   ├── index.md
│   ├── overview/
│   │   └── business-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md … UC_ADMIN_001.md
│   └── processes/
│       ├── BP_CUSTOMER_REGISTRATION.md
│       ├── BP_PRODUCT_BROWSING.md
│       ├── BP_CART_MANAGEMENT.md
│       ├── BP_ORDER_CHECKOUT.md
│       └── BP_ADMIN_MANAGEMENT.md
├── functional/                       # Phase 4 outputs
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_AUTH_001.md … FUREQ_CUST_001.md
│   │   ├── NFUREQ_PERF_001.md
│   │   └── NFUREQ_SEC_001.md
│   ├── flows/
│   │   ├── FF_LOGIN_FLOW.md
│   │   ├── FF_CART_FLOW.md
│   │   ├── FF_CHECKOUT_FLOW.md
│   │   └── FF_PRODUCT_CATALOG_FLOW.md
│   └── integration/
│       ├── INT_SQLITE_DATABASE.md
│       └── INT_FILE_UPLOAD.md
├── domain/                           # Phase 5 outputs
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

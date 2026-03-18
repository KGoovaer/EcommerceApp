# EcommerceApp — Documentation Plan

## Project Summary

| Field | Value |
|---|---|
| Module | EcommerceApp |
| Language | Java |
| Framework | Servlet 3.0 + JSP |
| Build Tool | Maven (WAR packaging) |
| Database | SQLite (org.xerial:sqlite-jdbc) |
| Deployment Target | Apache Tomcat 8+ |
| State File | `docs/EcommerceApp-state.json` |

## Repository Structure

```
EcommerceApp/
├── pom.xml                                      # Maven build descriptor
├── src/main/java/
│   └── com/
│       ├── conn/DBConnect.java                  # Static DB connection singleton
│       ├── dao/                                 # DAO.java … DAO5.java (plain JDBC)
│       ├── entity/                              # 14 plain Java beans
│       ├── servlet/                             # 21 HttpServlet subclasses
│       └── utility/MyUtilities.java             # File upload utility
└── src/main/webapp/
    ├── *.jsp                                    # 62 JSP pages (presentation + scripting)
    ├── Css/                                     # Stylesheets
    └── images/                                  # Static product images
```

## Detected Business Domains

| Domain | Tables | Servlets/JSPs |
|---|---|---|
| Authentication & User Management | customer, usermaster | checkadmin, checkcustomer, addcustomer, deletecustomer; validatelogina/c.jsp, customerlogin, adminlogin |
| Product Catalogue | product, brand, category, tv, laptop, mobile, watch | addproduct; mobile/a/c.jsp, laptop/a/c.jsp, tv/a/c.jsp, watch/a/c.jsp, viewproduct/a/c.jsp, selecteditem/a/c.jsp |
| Shopping Cart — Guest | cart (Name IS NULL) | addtocartnull, addtocartnulla, removecartnull, removecartnulla; cartnull/a.jsp |
| Shopping Cart — Authenticated | cart (Name = ?) | addtocart, removecart, removecarta; cart/a.jsp |
| Orders & Checkout | orders, order_details | payprocess, ShippingAddress2, removeorders, remove_orders, removetable_order_details; orders.jsp, orderdetails.jsp, confirmpayment/online.jsp, ShippingAddress.jsp |
| Admin Management | all tables (read) | removetable_cart, deletecustomer, addproduct; adminhome.jsp, managecustomers.jsp, managetables.jsp, table_*.jsp |
| Customer Support | Contactus | addContactus, addContactusc, remove_contactus; contactus/c.jsp, table_contactus.jsp |

## Effort Estimate

| Phase | Agent | Tasks | Complexity |
|---|---|---|---|
| 1 — Planning | planning-agent | 1 | Low |
| 2 — Discovery (6 batches × 7) | discovery | 42 | High |
| 2 — Discovery Consolidation | discovery | 3 | Medium |
| 3 — Business | business-documenter | 5 | Medium |
| 4 — Technical | technical-documenter | 5 | High |
| 5 — Coordination | doc-coordinator | 5 | Medium |
| 6 — Verification | verification | 6 | High |
| **Total** | | **67** | |

---

## Phase 1 — Discovery (Batched)

**Agent**: `discovery`  
**Skill**: `java-analysis`  
**Strategy**: One batch per business domain. Each batch produces three files (flows, components, domain concepts). Final consolidation step merges all batches, builds cross-domain table matrix, and flags gaps.

### Per-Batch Completeness Criteria

- [ ] ALL entry points (servlet `doGet`/`doPost`) identified and listed
- [ ] ALL WHERE clauses in DAO methods called by the batch are documented as business rules
- [ ] ALL JSP variants (guest / customer / admin suffix) mapped to their servlet and DAO
- [ ] Cookie-based auth checks documented (present / absent outcomes)
- [ ] Flash-message cookies (`maxAge=10`) and their redirect targets documented
- [ ] ALL database tables read **and** written by the batch are listed
- [ ] Cross-domain table dependencies flagged (tables shared with other batches)

---

### Batch 1 — Authentication & User Management

**Output**: `docs/discovery/batch-1-auth-*.md`

**Files to analyse:**
- `com/servlet/checkadmin.java` — admin cookie login
- `com/servlet/checkcustomer.java` — customer cookie login
- `com/servlet/addcustomer.java` — customer registration
- `com/servlet/deletecustomer.java` — admin deletes customer
- `com/dao/DAO2.java` — auth/customer DAO methods
- `com/entity/customer.java`, `com/entity/usermaster.java`
- `src/main/webapp/customerlogin.jsp`, `adminlogin.jsp`, `customer_reg.jsp`
- `src/main/webapp/validatelogina.jsp`, `validateloginc.jsp`
- `src/main/webapp/managecustomers.jsp`, `customerhome.jsp`, `adminhome.jsp`

**Special attention:**
- `checkadmin`: `select * from usermaster where Name=? and Password=?` — document credential-check outcomes
- `checkcustomer`: `select * from customer where Password=? and Email_Id=?` — document outcomes + cookie `cname` set
- `addcustomer`: `select * from customer where Name=? or Email_Id=?` (duplicate check) + `insert into customer` — enumerate both branch paths
- `deletecustomer`: auth guard (admin cookie `tname` check) before delete
- Cross-domain: `customer` table also written by checkout (orders sets `Customer_Name`)

---

### Batch 2 — Product Catalogue

**Output**: `docs/discovery/batch-2-catalogue-*.md`

**Files to analyse:**
- `com/servlet/addproduct.java` — admin adds product with image upload
- `com/dao/DAO.java` — brand, category, product CRUD
- `com/dao/DAO3.java` — tv, laptop, mobile, watch queries
- `com/entity/Product.java`, `brand.java`, `category.java`, `tv.java`, `laptop.java`, `mobile.java`, `watch.java`
- `com/utility/MyUtilities.java` — file upload validation
- `src/main/webapp/addproduct.jsp`, `viewproduct/a/c.jsp`, `selecteditem/a/c.jsp`
- `src/main/webapp/mobile/a/c.jsp`, `laptop/a/c.jsp`, `tv/a/c.jsp`, `watch/a/c.jsp`
- `src/main/webapp/category/a/c.jsp`, `index.jsp`

**Special attention:**
- `addproduct`: reads `brand` and `category` tables for dropdowns; uploads image to filesystem path (document upload path + extension validation in `MyUtilities.UploadFile()`)
- `viewlist` table (`select * from viewlist where Pimage = ?`) — document what viewlist represents and how it populates
- JSP tripling pattern: document which suffix maps to which role (no suffix = guest, `a` = admin, `c` = customer)
- Cross-domain: product `pimage` filename is foreign key in `cart`, `order_details`

---

### Batch 3 — Shopping Cart (Guest / Anonymous)

**Output**: `docs/discovery/batch-3-cart-guest-*.md`

**Files to analyse:**
- `com/servlet/addtocartnull.java` — add item to guest cart
- `com/servlet/addtocartnulla.java` — admin-context guest cart add
- `com/servlet/removecartnull.java` — remove item from guest cart
- `com/servlet/removecartnulla.java` — admin-context guest cart remove
- `com/dao/DAO2.java` — guest cart methods (`Name IS NULL` queries)
- `com/entity/cart.java`
- `src/main/webapp/cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`

**Special attention:**
- Guest cart identification: `Name IS NULL` — document this as core business rule
- Quantity increment: `update cart set pquantity = (pquantity + 1) where Name is NULL and bname=? and cname=? and pname=? and pprice=? and pimage=?` — document all 5 WHERE conditions
- Duplicate check before insert: document the SELECT → UPDATE/INSERT decision tree
- `cartnullqty.jsp` — document purpose and trigger condition
- Cross-domain: guest cart rows must be transferred to `order_details` at checkout before being deleted

---

### Batch 4 — Shopping Cart (Authenticated Customer)

**Output**: `docs/discovery/batch-4-cart-auth-*.md`

**Files to analyse:**
- `com/servlet/addtocart.java` — add item for logged-in customer
- `com/servlet/removecart.java` — remove item from customer cart
- `com/servlet/removecarta.java` — admin removes customer cart item
- `com/dao/DAO3.java` — authenticated cart methods (`Name = ?` queries)
- `com/entity/cart.java`
- `src/main/webapp/cart.jsp`, `carta.jsp`

**Special attention:**
- Auth guard: cookie `cname` must be present before cart operations
- Quantity increment: `update cart set pquantity = (pquantity + 1) where Name=? and bname=? and cname=? and pname=? and pprice=? and pimage=?` — document all 6 WHERE conditions (Note: 6-col match vs guest's 5-col)
- `select * from cart where Name = ?` vs `select * from cart where Name is NULL` — document the distinction between authenticated and guest cart reads
- Cross-domain: authenticated cart feeds into `order_details` at checkout

---

### Batch 5 — Orders & Checkout

**Output**: `docs/discovery/batch-5-orders-*.md`

**Files to analyse:**
- `com/servlet/payprocess.java` — payment processing and order creation
- `com/servlet/ShippingAddress2.java` — shipping address submission
- `com/servlet/removeorders.java` — customer cancels order
- `com/servlet/remove_orders.java` — admin removes order record
- `com/servlet/removetable_order_details.java` — admin clears order_details table
- `com/dao/DAO4.java`, `com/dao/DAO5.java` — order/order_details DAO methods
- `com/entity/orders.java`, `com/entity/order_details.java`
- `src/main/webapp/ShippingAddress.jsp`, `orders.jsp`, `orderdetails.jsp`
- `src/main/webapp/confirmpayment.jsp`, `confirmonline.jsp`, `paymentfail.jsp`

**Special attention:**
- Order creation sequence in `payprocess`: (1) read cart, (2) insert `orders`, (3) insert `order_details` from cart (SELECT INTO), (4) delete cart — document all 4 steps as atomic business flow
- `insert into order_details ... select * from cart where Name is NULL` vs `where Name = ?` — document both guest and authenticated paths
- `update order_details set Date=?, Name=? where Date is NULL` — document two-step date/name assignment after order insert
- `orders.Status` field — document all possible status values and transitions
- `select * from orders where Customer_Name=?` and `where Date=?` — document filter semantics
- Cross-domain: reads `cart` (written by batches 3 & 4); writes `orders` and `order_details`

---

### Batch 6 — Admin Management & Customer Support

**Output**: `docs/discovery/batch-6-admin-support-*.md`

**Files to analyse:**
- `com/servlet/addContactus.java` — guest submits contact form
- `com/servlet/addContactusc.java` — customer submits contact form
- `com/servlet/remove_contactus.java` — admin deletes contact enquiry
- `com/servlet/removetable_cart.java` — admin clears cart table
- `com/dao/DAO5.java` — Contactus DAO methods
- `com/entity/contactus.java`
- `src/main/webapp/contactus.jsp`, `contactusc.jsp`
- `src/main/webapp/managetables.jsp`, `table_cart.jsp`, `table_contactus.jsp`, `table_orders.jsp`, `table_order_details.jsp`
- `src/main/webapp/z1.jsp`, `z2.jsp`

**Special attention:**
- `addContactus` vs `addContactusc`: document difference (guest vs authenticated context, cookie presence)
- `remove_contactus`: admin auth guard before delete
- `removetable_cart`: admin bulk-clears entire cart — document as destructive admin operation
- `managetables.jsp` — document what tables are visible and which clear operations are exposed
- `z1.jsp`, `z2.jsp` — identify purpose (may be debug/scratch pages)
- Cross-domain: Contactus table is isolated; cart clear affects batches 3 & 4

---

### Discovery Consolidation

After all 6 batches:

1. **Cross-domain table matrix** → `docs/discovery/cross-domain-table-matrix.md`
   - Map every table to all batches that read/write it
   - Flag tables shared across 2+ batches as cross-domain dependencies

2. **Merge and link flows** — ensure guest cart → checkout → order_details chain is fully linked across batches 3, 4, and 5

3. **Consolidation gaps** → `docs/discovery/consolidation-gaps.md`
   - Flag any business rules not fully covered
   - Flag any table whose write path is documented but read path is not (or vice versa)

---

## Phase 2 — Business Documentation

**Agent**: `business-documenter`  
**Skill**: `java-analysis`

**Tasks:**
1. Define actors: Guest Shopper, Registered Customer, Admin
2. Create use cases (one file per UC):
   - `UC_AUTH_001` — Customer Registration
   - `UC_AUTH_002` — Customer Login / Logout
   - `UC_AUTH_003` — Admin Login
   - `UC_CAT_001` — Browse & View Products
   - `UC_CART_001` — Manage Guest Shopping Cart
   - `UC_CART_002` — Manage Authenticated Shopping Cart
   - `UC_ORD_001` — Checkout & Place Order
   - `UC_ORD_002` — View / Cancel Orders
   - `UC_SUP_001` — Submit Contact Enquiry
   - `UC_ADMIN_001` — Admin: Manage Products, Customers & Tables
3. Write BUREQs (tracing each UC to business requirements)
4. Create business process narratives:
   - `BP_REGISTRATION` — Customer onboarding flow
   - `BP_CHECKOUT` — Cart-to-order conversion flow
   - `BP_ORDER_MANAGEMENT` — Order lifecycle (placed → cancelled/fulfilled)

---

## Phase 3 — Technical / Functional Documentation

**Agent**: `technical-documenter`  
**Skill**: `java-analysis`

**Tasks:**
1. Derive FUREQs from BUREQs (one file per domain):
   - `FUREQ_AUTH_001` — Authentication functional requirements
   - `FUREQ_CART_001` — Cart (guest + auth) functional requirements
   - `FUREQ_ORD_001` — Order & checkout functional requirements
2. Document NFUREQs:
   - `NFUREQ_001` — Performance, security baseline, file upload constraints
3. Create technical flow diagrams (Mermaid sequence diagrams):
   - `FF_LOGIN` — Login authentication flow
   - `FF_CART_GUEST` — Guest cart add/remove/checkout
   - `FF_CART_AUTH` — Authenticated cart add/remove/checkout
   - `FF_CHECKOUT` — Full payment + order creation sequence
4. Document database schema: `docs/functional/integration/DB_SCHEMA.md`
   - All 14 tables with column names, types, constraints
   - Cross-table relationships (FK by convention)
5. Document API endpoints (servlet URLs, HTTP methods, request params, redirect targets)

---

## Phase 4 — Documentation Coordination

**Agent**: `doc-coordinator`

**Tasks:**
1. Validate directory structure against expected output inventory
2. Create master index: `docs/index.md`
3. Create system overview: `docs/system-overview.md`
4. Build traceability matrix: `docs/traceability/requirement-matrix.md` (BUREQ ↔ FUREQ ↔ UC ↔ Flow)
5. Create domain concepts catalog: `docs/domain/domain-concepts-catalog.md`
6. Build flow-to-component map: `docs/traceability/flow-to-component-map.md`
7. Build ID registry: `docs/traceability/id-registry.md`

---

## Phase 5 — Verification

**Agent**: `verification`

**Tasks (V-1 through V-6):**

| ID | Task | Output |
|---|---|---|
| V-1 | Build table usage matrix — every table mapped to every flow that reads/writes it | `docs/verification/table-usage-matrix.md` |
| V-2 | Repository query deep analysis — compare documented WHERE conditions against actual DAO SQL strings | Inline in gap report |
| V-3 | Validation completeness — enumerate all validations in servlets/JSPs, verify both true/false outcomes documented | Inline in gap report |
| V-4 | Cross-domain dependency verification — verify all batch-to-batch table dependencies are bidirectionally linked | `docs/verification/cross-domain-dependencies.md` |
| V-5 | Entity state machine — map all `orders.Status` transitions in code, verify all documented | Inline in gap report |
| V-6 | Generate consolidated gap report with severity (Critical/High/Medium/Low) and remediation prompts | `docs/verification/gap-report.md` |

---

## Completeness Criteria

### Per-Flow

- [ ] All servlet entry points (`doGet`/`doPost`) identified
- [ ] All WHERE clauses in DAO methods documented as business rules
- [ ] Cookie auth checks documented with true (continue) and false (redirect) outcomes
- [ ] Flash-message cookie (`maxAge=10`) redirect targets documented
- [ ] Error redirect targets (`fail.jsp`, `failc.jsp`, etc.) documented
- [ ] All database tables read AND written are listed
- [ ] Cross-domain dependencies flagged

### Per-Domain

- [ ] All flows in domain meet per-flow criteria
- [ ] Cross-domain table dependencies bidirectionally linked
- [ ] Business use cases reference all relevant flows
- [ ] Functional requirements cover all business rules from DAO SQL

### Overall

- [ ] All 62 JSP pages accounted for (guest/customer/admin variants mapped)
- [ ] All 14 database tables appear in table-usage matrix
- [ ] All 21 servlets covered by at least one flow
- [ ] Traceability: every BUREQ traces to at least one FUREQ
- [ ] Traceability: every FUREQ traces to at least one code location

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json
├── documentation-plan.md
├── index.md                               (Phase 4)
├── system-overview.md                     (Phase 4)
├── discovery/
│   ├── batch-1-auth-flows.md
│   ├── batch-1-auth-components.md
│   ├── batch-1-auth-domain-concepts.md
│   ├── batch-2-catalogue-flows.md
│   ├── batch-2-catalogue-components.md
│   ├── batch-2-catalogue-domain-concepts.md
│   ├── batch-3-cart-guest-flows.md
│   ├── batch-3-cart-guest-components.md
│   ├── batch-3-cart-guest-domain-concepts.md
│   ├── batch-4-cart-auth-flows.md
│   ├── batch-4-cart-auth-components.md
│   ├── batch-4-cart-auth-domain-concepts.md
│   ├── batch-5-orders-flows.md
│   ├── batch-5-orders-components.md
│   ├── batch-5-orders-domain-concepts.md
│   ├── batch-6-admin-support-flows.md
│   ├── batch-6-admin-support-components.md
│   ├── batch-6-admin-support-domain-concepts.md
│   ├── cross-domain-table-matrix.md
│   └── consolidation-gaps.md
├── business/
│   ├── index.md
│   ├── overview/system-overview.md
│   ├── use-cases/
│   │   ├── UC_AUTH_001.md
│   │   ├── UC_AUTH_002.md
│   │   ├── UC_AUTH_003.md
│   │   ├── UC_CAT_001.md
│   │   ├── UC_CART_001.md
│   │   ├── UC_CART_002.md
│   │   ├── UC_ORD_001.md
│   │   ├── UC_ORD_002.md
│   │   ├── UC_SUP_001.md
│   │   └── UC_ADMIN_001.md
│   └── processes/
│       ├── BP_REGISTRATION.md
│       ├── BP_CHECKOUT.md
│       └── BP_ORDER_MANAGEMENT.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_AUTH_001.md
│   │   ├── FUREQ_CART_001.md
│   │   ├── FUREQ_ORD_001.md
│   │   └── NFUREQ_001.md
│   ├── flows/
│   │   ├── FF_LOGIN.md
│   │   ├── FF_CART_GUEST.md
│   │   ├── FF_CART_AUTH.md
│   │   └── FF_CHECKOUT.md
│   └── integration/
│       └── DB_SCHEMA.md
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

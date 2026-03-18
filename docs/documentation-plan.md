# EcommerceApp — Documentation Plan

**Generated:** 2026-03-18  
**Module:** EcommerceApp  
**Language:** Java (Servlet 3.0 + JSP)  
**Framework:** servlet-jsp  
**Repository:** KGoovaer/EcommerceApp

---

## Project Overview

Java J2EE online electronic shopping application built with Servlet 3.0, JSP, SQLite, and Maven WAR packaging. Deployed on Apache Tomcat 8+. No Spring, no ORM, no DI container.

### Detected Structure

| Component | Count |
|---|---|
| Servlets (`@WebServlet`) | 21 |
| JSP pages | 64 |
| DAO classes (`DAO`–`DAO5`) | 5 |
| Entity beans | 14 |
| Source packages | 5 (`conn`, `dao`, `entity`, `servlet`, `utility`) |

### Identified Business Domains

| Domain | Servlets / JSPs | Key Entities |
|---|---|---|
| Authentication & User Management | `checkadmin`, `checkcustomer`, `addcustomer`, `deletecustomer` | `customer`, `usermaster` |
| Product Catalog | `addproduct`, `viewproduct*`, `mobile*`, `laptop*`, `tv*`, `watch*`, `category*` | `Product`, `mobile`, `laptop`, `tv`, `watch`, `brand`, `category` |
| Shopping Cart | `addtocart`, `addtocartnull*`, `removecart*`, `removecartnull*` | `cart` |
| Orders & Checkout | `payprocess`, `ShippingAddress2`, `removeorders`, `remove_orders` | `orders`, `order_details` |
| Admin Management | `adminhome`, `managecustomers`, `managetables`, `addproduct` | All entities |
| Contact | `addContactus`, `addContactusc`, `remove_contactus` | `contactus` |

---

## JSP Tripling Pattern

Most pages exist in three variants (guest / customer / admin):

| Base | Customer variant | Admin variant |
|---|---|---|
| `mobile.jsp` | `mobilec.jsp` | `mobilea.jsp` |
| `laptop.jsp` | `laptopc.jsp` | `laptopa.jsp` |
| `tv.jsp` | `tvc.jsp` | `tva.jsp` |
| `watch.jsp` | `watchc.jsp` | `watcha.jsp` |
| `category.jsp` | `categoryc.jsp` | `categorya.jsp` |
| `viewproduct.jsp` | `viewproductc.jsp` | `viewproducta.jsp` |
| `selecteditem.jsp` | `selecteditemc.jsp` | `selecteditema.jsp` |
| `cart.jsp` | — | `carta.jsp` |
| `cartnull.jsp` | — | `cartnulla.jsp` |

---

## Six-Phase Documentation Plan

### Phase 1: Discovery (Batched by Domain)

**Agent:** `discovery`  
**Skill:** `java-analysis`  
**Output directory:** `docs/discovery/`  
**Strategy:** Six domain batches, each producing flows, components, and domain-concept documents, followed by a cross-domain consolidation step.

#### Effort Estimate

| Phase | Batches | Tasks/Batch | Consolidation | Total |
|---|---|---|---|---|
| Discovery | 6 | 7 | 3 | 45 |

#### Batch 1 — Authentication & User Management

**Files to analyse:**
- `com/servlet/checkadmin.java`
- `com/servlet/checkcustomer.java`
- `com/servlet/addcustomer.java`
- `com/servlet/deletecustomer.java`
- `com/entity/customer.java`, `usermaster.java`
- `src/main/webapp/adminlogin.jsp`, `customerlogin.jsp`, `validatelogina.jsp`, `validateloginc.jsp`, `customer_reg.jsp`
- Relevant DAO methods in `DAO.java`

**Outputs:**
- `docs/discovery/batch-1-auth-flows.md`
- `docs/discovery/batch-1-auth-components.md`
- `docs/discovery/batch-1-auth-domain-concepts.md`

**Pay special attention to:**
- Cookie-based auth (`cname` = customer email, `tname` = admin username, `maxAge=9999`)
- Flash-message cookies (`maxAge=10`) used in redirect-response cycles
- Password comparison logic (plaintext vs hashed)
- Admin vs customer login branching conditions
- Cross-domain: `customer` table is also read by cart and orders domains

#### Batch 2 — Product Catalog

**Files to analyse:**
- `com/servlet/addproduct.java`
- `com/entity/Product.java`, `mobile.java`, `laptop.java`, `tv.java`, `watch.java`, `brand.java`, `category.java`
- `src/main/webapp/mobile*.jsp`, `laptop*.jsp`, `tv*.jsp`, `watch*.jsp`, `category*.jsp`, `viewproduct*.jsp`, `selecteditem*.jsp`, `addproduct.jsp`
- Relevant DAO methods (`DAO2.java`, `DAO3.java`)

**Outputs:**
- `docs/discovery/batch-2-catalog-flows.md`
- `docs/discovery/batch-2-catalog-components.md`
- `docs/discovery/batch-2-catalog-domain-concepts.md`

**Pay special attention to:**
- Image upload path handling in `MyUtilities.UploadFile()`
- Extension validation rules in file upload
- Category and brand filtering SQL WHERE clauses
- Product listing vs product detail flows
- Cross-domain: product tables are read by cart and orders domains

#### Batch 3 — Shopping Cart

**Files to analyse:**
- `com/servlet/addtocart.java`
- `com/servlet/addtocartnull.java`, `addtocartnulla.java`
- `com/servlet/removecart.java`, `removecarta.java`
- `com/servlet/removecartnull.java`, `removecartnulla.java`
- `com/servlet/removetable_cart.java`
- `com/entity/cart.java`
- `src/main/webapp/cart.jsp`, `carta.jsp`, `cartnull.jsp`, `cartnulla.jsp`, `cartnullqty.jsp`
- Relevant DAO methods (`DAO4.java`)

**Outputs:**
- `docs/discovery/batch-3-cart-flows.md`
- `docs/discovery/batch-3-cart-components.md`
- `docs/discovery/batch-3-cart-domain-concepts.md`

**Pay special attention to:**
- `addtocart` vs `addtocartnull` distinction (logged-in customer vs guest/null flow)
- Cart quantity update logic and validation
- `removetable_cart` — bulk-clear cart after order placement (cross-domain trigger)
- Cookie check for customer identity before adding to cart
- SQL WHERE clauses filtering cart by customer email

#### Batch 4 — Orders & Checkout

**Files to analyse:**
- `com/servlet/payprocess.java`
- `com/servlet/ShippingAddress2.java`
- `com/servlet/removeorders.java`, `remove_orders.java`
- `com/servlet/removetable_order_details.java`
- `com/entity/orders.java`, `order_details.java`, `viewlist.java`
- `src/main/webapp/ShippingAddress.jsp`, `confirmonline.jsp`, `confirmpayment.jsp`, `paymentfail.jsp`, `orders.jsp`, `orderdetails.jsp`
- Relevant DAO methods (`DAO5.java`)

**Outputs:**
- `docs/discovery/batch-4-orders-flows.md`
- `docs/discovery/batch-4-orders-components.md`
- `docs/discovery/batch-4-orders-domain-concepts.md`

**Pay special attention to:**
- Payment processing flow: shipping → confirm → pay → order creation
- Order status transitions (if any status field exists)
- `removetable_order_details` — admin table management flow
- Cross-domain: order creation reads cart items (cart domain) and product data (catalog domain)
- SQL for inserting order_details from cart items

#### Batch 5 — Admin Management

**Files to analyse:**
- `src/main/webapp/adminhome.jsp`
- `src/main/webapp/managecustomers.jsp`
- `src/main/webapp/managetables.jsp`
- `com/servlet/deletecustomer.java`
- `com/servlet/remove_contactus.java`
- `src/main/webapp/table_cart.jsp`, `table_orders.jsp`, `table_order_details.jsp`, `table_contactus.jsp`
- `com/entity/usermaster.java`

**Outputs:**
- `docs/discovery/batch-5-admin-flows.md`
- `docs/discovery/batch-5-admin-components.md`
- `docs/discovery/batch-5-admin-domain-concepts.md`

**Pay special attention to:**
- Admin-only auth checks (tname cookie) on all admin JSPs
- Admin dashboard navigation and data display flows
- Raw table viewer JSPs (`managetables.jsp`, `table_*.jsp`)
- Customer deletion cascade effects
- Cross-domain: admin reads all tables across all domains

#### Batch 6 — Contact

**Files to analyse:**
- `com/servlet/addContactus.java`
- `com/servlet/addContactusc.java`
- `com/servlet/remove_contactus.java`
- `com/entity/contactus.java`
- `src/main/webapp/contactus.jsp`, `contactusc.jsp`
- `src/main/webapp/table_contactus.jsp`

**Outputs:**
- `docs/discovery/batch-6-contact-flows.md`
- `docs/discovery/batch-6-contact-components.md`
- `docs/discovery/batch-6-contact-domain-concepts.md`

**Pay special attention to:**
- Difference between `addContactus` (guest) and `addContactusc` (customer) flows
- Form validation conditions for contact submission
- Admin read/delete of contact messages (cross-domain: admin batch)

#### Consolidation Step

After all 6 batches:

1. **Cross-domain table matrix** (`docs/discovery/cross-domain-table-matrix.md`)  
   Map every SQLite table to the flows that read and write it across all batches.

2. **Consolidation gaps** (`docs/discovery/consolidation-gaps.md`)  
   Flag undocumented business rules, missing WHERE clause documentation, and incomplete validation chains discovered during cross-domain analysis.

3. **Merge and link** — update batch documents to reference related flows in other batches.

---

### Phase 2: Business Documentation

**Agent:** `business-documenter`  
**Skill:** `java-analysis`  
**Output directory:** `docs/business/`

#### Tasks

1. **Define actors** — Guest, Registered Customer, Admin
2. **Create use cases** — one `UC_*.md` per major user goal
3. **Write business requirements** — consolidated `BUREQ.md`
4. **Create business process diagrams** — one `BP_*.md` per workflow (BPMN-style in Mermaid)
5. **Business overview** — `docs/business/overview/business-overview.md`

#### Expected Use Cases

| ID | Title | Actor |
|---|---|---|
| UC_AUTH_001 | Register as Customer | Guest |
| UC_AUTH_002 | Log In as Customer | Guest |
| UC_AUTH_003 | Log In as Admin | Admin |
| UC_CAT_001 | Browse Product Catalogue | Guest / Customer |
| UC_CAT_002 | Add Product (Admin) | Admin |
| UC_CART_001 | Add Item to Cart | Customer |
| UC_CART_002 | Remove Item from Cart | Customer |
| UC_ORD_001 | Place an Order / Checkout | Customer |
| UC_ORD_002 | View Order History | Customer / Admin |
| UC_ADM_001 | Manage Customers | Admin |
| UC_ADM_002 | Manage Tables / Data | Admin |
| UC_CONT_001 | Submit Contact Message | Guest / Customer |

#### Expected Business Processes

| ID | Title |
|---|---|
| BP_REGISTRATION | Customer Self-Registration |
| BP_SHOPPING | Browse → Select → Add to Cart |
| BP_CHECKOUT | Cart → Shipping → Payment → Order |
| BP_ADMIN_PRODUCT | Admin Adds/Views Products |
| BP_CONTACT | Customer/Guest Sends Contact Message |

---

### Phase 3: Technical / Functional Documentation

**Agent:** `technical-documenter`  
**Skill:** `java-analysis`  
**Output directory:** `docs/functional/`

#### Tasks

1. **Derive functional requirements** — one `FUREQ_*.md` per business requirement, covering servlet behaviour, validation rules, and data access
2. **Document non-functional requirements** — `NFUREQ_001.md` (security baseline), `NFUREQ_002.md` (scalability/concurrency notes)
3. **Create technical flow diagrams** — one `FF_*.md` per flow (sequence diagrams in Mermaid)
4. **Document servlet API map** — URL → Servlet → DAO mapping table
5. **Document DB schema** — all SQLite tables, columns, constraints

#### Expected Functional Flows

| ID | Title |
|---|---|
| FF_LOGIN | Admin / Customer Login Flow |
| FF_REGISTER | Customer Registration Flow |
| FF_CATALOG | Product Browse and Filter Flow |
| FF_CART | Add / Remove Cart Items Flow |
| FF_CHECKOUT | Checkout / Payment Processing Flow |

---

### Phase 4: Documentation Coordination

**Agent:** `doc-coordinator`  
**Skill:** `java-analysis`  
**Output directory:** `docs/`, `docs/domain/`, `docs/traceability/`

#### Tasks

1. **Validate directory structure** — verify all expected artefacts exist
2. **Create master index** — `docs/index.md` with links to all documents
3. **Create system overview** — `docs/system-overview.md` with architecture diagram
4. **Build traceability matrix** — `docs/traceability/requirement-matrix.md` linking BUREQs → FUREQs → flows → servlets
5. **Create domain concepts catalogue** — `docs/domain/domain-concepts-catalog.md`
6. **Create ubiquitous language glossary** — `docs/domain/ubiquitous-language.md`
7. **Create bounded contexts map** — `docs/domain/bounded-contexts.md`

---

### Phase 5: Verification

**Agent:** `verification`  
**Skill:** `java-analysis`  
**Output directory:** `docs/verification/`

#### Tasks

| ID | Task | Output |
|---|---|---|
| V-1 | Build table usage matrix — map every DB table to flows that read/write it | `table-usage-matrix.md` |
| V-2 | Repository query deep analysis — read actual DAO/JSP query code; compare WHERE conditions against documented business rules | Part of `gap-report.md` |
| V-3 | Validation completeness check — enumerate all validations in servlets/JSPs; verify each has condition + true/false outcomes documented | Part of `gap-report.md` |
| V-4 | Cross-domain dependency verification — verify all cross-batch table dependencies have bidirectional documentation links | `cross-domain-dependencies.md` |
| V-5 | Entity state machine verification — map all status fields and transitions in entity classes and SQL | Part of `gap-report.md` |
| V-6 | Generate consolidated gap report with severity classification and remediation prompts | `gap-report.md` |

---

## Completeness Criteria

### Per-Flow

- [ ] All WHERE clauses in DAO methods called by the flow are documented as business rules
- [ ] All validation conditions have both true AND false outcomes documented
- [ ] Cross-domain dependencies are linked (upstream producers, downstream consumers)
- [ ] Entity status transitions are enumerated with from → to states
- [ ] Error handling paths are documented (redirect targets: `fail.jsp`, `failc.jsp`, `paymentfail.jsp`, etc.)
- [ ] Cookie auth checks are documented for every protected endpoint
- [ ] File upload validation rules are documented (extension whitelist in `MyUtilities`)

### Per-Domain

- [ ] All flows meet per-flow criteria
- [ ] Cross-domain dependencies are bidirectionally linked
- [ ] Business use cases reference all relevant flows (including cross-domain inputs)
- [ ] Functional requirements cover all business rules extracted from DAO queries

---

## Output Directory Structure

```
docs/
├── EcommerceApp-state.json
├── documentation-plan.md
├── index.md                           (Phase 4)
├── system-overview.md                 (Phase 4)
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
│   │   ├── UC_AUTH_001.md
│   │   ├── UC_AUTH_002.md
│   │   ├── UC_AUTH_003.md
│   │   ├── UC_CAT_001.md
│   │   ├── UC_CAT_002.md
│   │   ├── UC_CART_001.md
│   │   ├── UC_CART_002.md
│   │   ├── UC_ORD_001.md
│   │   ├── UC_ORD_002.md
│   │   ├── UC_ADM_001.md
│   │   ├── UC_ADM_002.md
│   │   └── UC_CONT_001.md
│   ├── processes/
│   │   ├── BP_REGISTRATION.md
│   │   ├── BP_SHOPPING.md
│   │   ├── BP_CHECKOUT.md
│   │   ├── BP_ADMIN_PRODUCT.md
│   │   └── BP_CONTACT.md
│   └── BUREQ.md
├── functional/
│   ├── index.md
│   ├── requirements/
│   │   ├── FUREQ_AUTH_001.md
│   │   ├── FUREQ_AUTH_002.md
│   │   ├── FUREQ_CAT_001.md
│   │   ├── FUREQ_CART_001.md
│   │   ├── FUREQ_ORD_001.md
│   │   ├── FUREQ_ADM_001.md
│   │   ├── NFUREQ_001.md
│   │   └── NFUREQ_002.md
│   ├── flows/
│   │   ├── FF_LOGIN.md
│   │   ├── FF_REGISTER.md
│   │   ├── FF_CATALOG.md
│   │   ├── FF_CART.md
│   │   └── FF_CHECKOUT.md
│   └── integration/
│       ├── db-schema.md
│       └── servlet-api-map.md
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

## Effort Estimate

| Phase | Agent | Est. Tasks | Complexity |
|---|---|---|---|
| Planning | Planning Agent | 1 | Low |
| Discovery — 6 batches × 7 tasks | Discovery Agent | 42 | High |
| Discovery — Consolidation | Discovery Agent | 3 | Medium |
| Business | Business Documenter | 5 | Medium |
| Technical | Technical Documenter | 5 | High |
| Coordination | Doc Coordinator | 5 | Medium |
| Verification | Verification Agent | 6 | High |
| **Total** | | **67** | |

---

## Key Architectural Notes for Downstream Agents

1. **No Spring / no DI** — dependency injection is manual (`new DAOx(DBConnect.getConn())`).
2. **Static singleton Connection** — `DBConnect.getConn()` is not thread-safe; document as NFR.
3. **Cookie-only auth** — no `HttpSession`; auth state lives entirely in browser cookies.
4. **JSP tripling** — three JSP variants per page (guest / customer `c` / admin `a`); document each role's access scope.
5. **Flash cookies** — `maxAge=10` short-lived cookies carry success/fail messages across redirects.
6. **Legacy MySQL driver loaded but SQLite used** — `Class.forName("com.mysql.cj.jdbc.Driver")` in `DBConnect.java` is intentional; do not remove.
7. **Image uploads** — original filenames used as-is (path traversal risk); extension validation exists in `MyUtilities`; document the validation rule.

# Material Service Architecture Audit

**GitHub Issue:** #1
**Service:** `material-service`
**Epic:** Material Knowledge Base & Iraqi Translation
**Milestone:** M1 — Material Foundation

## 1. Purpose

This audit documents the current architecture of `material-service` before extending it into the Offeria Material Knowledge Base.

The goal is to understand the existing implementation, identify reusable components, document technical debt, and define the minimum changes required for future material intelligence features.

No production behavior is changed as part of this audit.

---

## 2. Current Architecture

The service currently follows a conventional layered Spring Boot architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL

DTO ↔ Mapper ↔ Entity
```

Main components:

```text
controller/
    MaterialController

domain/entity/
    Material

dto/request/
    MaterialRequestDTO

dto/response/
    MaterialResponseDTO

mapper/
    MaterialMapper

repository/
    MaterialRepository

service/
    MaterialService

service/impl/
    MaterialServiceImpl

exception/
    GlobalExceptionHandler
    MaterialServiceException
    ResourceNotFoundException
    ErrorResponse
```

The current separation of responsibilities is suitable as a foundation and does not require a full architectural rewrite.

---

## 3. Current Material Domain Model

The current `Material` entity contains:

```text
id
nameEn
nameAr
unit
createdAt
updatedAt
```

The entity uses UUID identifiers and Hibernate-managed creation/update timestamps.

Database indexes currently exist for:

```text
name_en
name_ar
```

### Current limitation

The existing model assumes that a material has exactly one English name and one Arabic name.

This is insufficient for the planned Material Knowledge Base because real RFQ materials may have:

* multiple English names
* Iraqi market terminology
* standard Arabic terminology
* supplier terminology
* legacy Excel terminology
* technical aliases
* spelling variations
* abbreviations
* part numbers
* vehicle/application context

The domain model should therefore evolve without discarding the existing `Material` concept.

---

## 4. Existing API

Base endpoint:

```text
/api/v1/materials
```

Current operations:

```text
POST   /api/v1/materials
GET    /api/v1/materials/{id}
GET    /api/v1/materials
PUT    /api/v1/materials/{id}
DELETE /api/v1/materials/{id}
```

The list endpoint supports pagination and an optional `query` parameter.

Example:

```text
GET /api/v1/materials?query=filter
```

The existing API provides a useful CRUD foundation.

---

## 5. Current Search Strategy

Current search checks:

```text
nameEn
OR
nameAr
```

using case-insensitive partial matching.

Conceptually:

```text
LIKE %query%
```

### Limitations

The current search does not support:

* normalized names
* exact normalized lookup
* aliases
* Iraqi spelling variations
* duplicate detection
* fuzzy matching
* semantic matching
* approval status
* source prioritization

For example:

```text
Oil Filter
Engine Oil Filter
FILTER ASSY, OIL
فلتر زيت
فلتر دهن
فلتر الدهن
```

may refer to the same logical material but currently cannot be modeled as one material with multiple aliases.

---

## 6. DTO and Validation

`MaterialRequestDTO` currently requires:

```text
nameEn
nameAr
unit
```

All three fields are mandatory.

### Compatibility concern

Making `nameAr` mandatory conflicts with the planned workflow.

A new material may enter the system before its Iraqi-market translation has been reviewed.

Future workflow:

```text
Unknown material
    ↓
Candidate created
    ↓
AI or human translation suggestion
    ↓
PENDING_REVIEW
    ↓
Human approval
    ↓
APPROVED
```

Therefore, an authoritative Iraqi translation should not necessarily be required during initial material creation.

This behavior must be redesigned carefully to avoid breaking existing API clients.

---

## 7. Persistence Strategy

The service uses:

```text
Spring Data JPA
PostgreSQL
Hibernate
```

Development configuration currently uses:

```text
ddl-auto: update
```

Production uses:

```text
ddl-auto: validate
```

No explicit schema migration tool such as Flyway or Liquibase is currently configured.

### Risk

Upcoming Material Knowledge Base work will require schema changes such as:

```text
material aliases
status
normalized names
additional indexes
possibly source metadata
```

Relying on Hibernate automatic schema updates would make database evolution difficult to track and unsafe across environments.

A database migration strategy should be introduced before significant schema changes.

---

## 8. Existing Tests

Current test coverage includes:

### Application context

```text
MaterialServiceApplicationTests
```

Verifies that the Spring context loads.

### Service tests

`MaterialServiceImplTest` currently covers examples of:

```text
create material
get material by ID
material not found
```

### Controller tests

`MaterialControllerTest` currently verifies creation through:

```text
POST /api/v1/materials
```

### Missing or limited coverage

Additional testing will eventually be required for:

```text
search
pagination
update
delete
validation errors
duplicate prevention
normalized lookup
alias lookup
approval workflow
repository integration
database migrations
```

These tests should be added alongside the features that require them rather than added all at once during this audit.

---

## 9. Build and Runtime

The project currently uses:

```text
Java 21
Spring Boot 3.5.x
Spring Data JPA
Spring Validation
Spring Web
Spring Cloud Eureka Client
PostgreSQL
MapStruct
Lombok
JUnit / Mockito
H2 for tests
```

The service includes a multi-stage Docker build.

The current Docker build packages the application using:

```text
mvn package -DskipTests
```

This means successful Docker image creation does not itself guarantee that the test suite has passed.

Testing should remain a separate CI responsibility.

---

## 10. Technical Debt Relevant to Material Knowledge Base

The following technical debt has been identified.

### Domain

* Material supports only one English and one Arabic name.
* No alias domain exists.
* No material lifecycle/status exists.
* No canonical/normalized material name exists.
* No distinction exists between Iraqi terminology and standard Arabic.
* No source metadata exists for translations.

### Search

* Search is limited to basic partial matching.
* No normalization strategy exists.
* No duplicate detection exists.
* No alias lookup exists.
* No fuzzy or semantic matching exists.

### Persistence

* No database migration framework is configured.
* Development relies on Hibernate `ddl-auto=update`.

### API

* Arabic name is mandatory during creation.
* Current contracts may require compatibility planning before domain changes.

### Tests

* Core CRUD has partial coverage.
* Search and future knowledge-base behavior require additional tests.

---

## 11. Backward Compatibility Risks

Future domain changes must preserve existing users of:

```text
POST /api/v1/materials
GET /api/v1/materials
GET /api/v1/materials/{id}
PUT /api/v1/materials/{id}
DELETE /api/v1/materials/{id}
```

Primary risks include:

### Renaming fields

Changing:

```text
nameEn
nameAr
```

directly to new names could break API consumers.

A safer migration may retain existing API fields temporarily while introducing richer internal concepts.

### Making translation optional

Changing validation behavior around `nameAr` affects existing assumptions and must be explicitly tested.

### Database changes

New fields and relationships must be introduced through controlled migrations rather than Hibernate-generated schema changes.

### Search behavior

Replacing the current search implementation must preserve expected basic search behavior while adding aliases and normalization.

---

## 12. Recommended Target Direction

The existing service should be evolved rather than rewritten.

A possible future model is:

```text
Material
    id
    canonicalEnglishName
    preferredIraqiName
    standardArabicName
    unit
    category
    status
    createdAt
    updatedAt

MaterialAlias
    id
    materialId
    alias
    language
    aliasType
    source
    approved
```

Exact field names and relationships should be decided in a separate domain-design issue.

This audit intentionally does not implement that model.

---

## 13. Recommended Follow-up Work

The following work should be handled as separate Issues.

### Material Foundation

1. Design Material Knowledge Base domain model
2. Introduce database migration strategy
3. Extend Material persistence model
4. Implement MaterialAlias domain
5. Implement material-name normalization
6. Implement approved Iraqi translation lookup
7. Implement exact and alias search

### Later Work

Legacy Excel import, AI matching, RFQ integration, and MCP integration should remain outside the initial foundation work.

---

## 14. Audit Conclusion

The existing `material-service` provides a good CRUD foundation and should not be rewritten.

The main architectural gap is that the current model represents a translated material record, while Offeria now requires a reusable Material Knowledge Base.

The recommended strategy is incremental evolution:

```text
Existing Material CRUD
        ↓
Database migrations
        ↓
Richer Material domain
        ↓
Material aliases
        ↓
Normalization
        ↓
Approved translation lookup
        ↓
Legacy knowledge import
        ↓
RFQ integration
        ↓
AI assistance
        ↓
MCP integration
```

This approach preserves useful existing code while introducing the new capabilities through small, reviewable GitHub Issues.

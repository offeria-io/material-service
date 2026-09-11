# Material Knowledge Base Domain Design

## 1. Purpose

Define the domain model required to evolve `material-service` from a simple CRUD service into the Offeria Material Knowledge Base.

This design focuses on:
- canonical material identification
- Iraqi-market terminology
- aliases and alternative names
- review and approval lifecycle
- normalization for search
- legacy Excel compatibility

No persistence changes are implemented in this issue.

---

## 2. Current Model

The current `Material` entity contains:

- `id`
- `nameEn`
- `nameAr`
- `unit`
- `createdAt`
- `updatedAt`

Limitations:

- Arabic name is mandatory
- no canonical material concept
- no alias support
- no approval lifecycle
- no normalized search values
- no legacy-data provenance
- no classification or specifications

---

## 3. Proposed Aggregate

### Material

Represents one canonical material in the knowledge base.

Proposed fields:

| Field | Required | Description |
|---|---|---|
| id | Yes | UUID primary identifier |
| canonicalEnglishName | Yes | Primary canonical English name |
| preferredIraqiName | No | Approved Iraqi-market preferred name |
| standardArabicName | No | Optional standard Arabic terminology |
| normalizedEnglishName | Yes | Search-normalized English value |
| normalizedIraqiName | No | Search-normalized Iraqi value |
| unit | No | Default unit of measure |
| category | No | High-level classification |
| subCategory | No | More specific classification |
| manufacturer | No | Manufacturer when relevant |
| brand | No | Brand when relevant |
| partNumber | No | Manufacturer/supplier part number |
| specification | No | Technical material specification |
| source | Yes | Origin of the material record |
| status | Yes | Review and approval state |
| createdAt | Yes | Creation timestamp |
| updatedAt | Yes | Last update timestamp |

---

## 4. MaterialAlias

Represents an alternative name for the same material.

Proposed fields:

| Field | Required | Description |
|---|---|---|
| id | Yes | UUID identifier |
| materialId | Yes | Parent Material |
| alias | Yes | Original alias text |
| normalizedAlias | Yes | Normalized value used for search |
| language | No | Language of the alias |
| aliasType | Yes | Meaning/source category of the alias |
| source | Yes | Origin of the alias |
| preferred | Yes | Whether this is a preferred alias |
| approved | Yes | Whether this alias may be used automatically |
| createdAt | Yes | Creation timestamp |

Example:

Material:

`Oil Filter`

Aliases:

- `Engine Oil Filter`
- `FILTER ASSY, OIL`
- `فلتر زيت`
- `فلتر الدهن`

Preferred Iraqi name:

`فلتر دهن`

---

## 5. Material Status Lifecycle

### PENDING_REVIEW

Material exists but is not yet approved for automatic reuse.

Typical sources:
- new RFQ material
- AI suggestion
- legacy import candidate

### APPROVED

Material has been reviewed and may be reused automatically.

### REJECTED

Candidate was rejected because it is:
- invalid
- duplicate
- incorrectly translated
- incorrectly classified

Lifecycle:

`PENDING_REVIEW -> APPROVED`

or

`PENDING_REVIEW -> REJECTED`

---

## 6. Alias Types

Proposed values:

- `ENGLISH_STANDARD`
- `IRAQI_MARKET`
- `STANDARD_ARABIC`
- `TECHNICAL`
- `LEGACY`

Examples:

`Oil Filter` → ENGLISH_STANDARD

`FILTER ASSY, OIL` → TECHNICAL

`فلتر دهن` → IRAQI_MARKET

`فلتر زيت` → STANDARD_ARABIC

---

## 7. Material Sources

Proposed values:

- `MANUAL`
- `LEGACY_EXCEL`
- `RFQ`
- `AI_SUGGESTED`
- `IMPORT`

The source records how the material entered the knowledge base.

---

## 8. Normalization

Normalization supports deterministic material lookup before AI is used.

Examples:

`FILTER ASSY, OIL`
→ `filter assy oil`

`  فلتر   الدهن `
→ `فلتر الدهن`

Normalization may include:

- trimming whitespace
- collapsing repeated spaces
- lowercasing English text
- removing unnecessary punctuation
- normalizing common Arabic character variants where appropriate

The original value must always be preserved.

---

## 9. Search Strategy

Recommended lookup order:

1. canonical exact match
2. normalized canonical match
3. exact alias match
4. normalized alias match
5. fuzzy/similar candidate search
6. AI-assisted suggestion

AI must not override an already approved database result.

---

## 10. Approval Workflow

New unknown RFQ material:

1. Material is not found
2. candidate is created
3. status = `PENDING_REVIEW`
4. AI may suggest:
    - canonical English name
    - Iraqi-market name
    - category
    - possible existing matches
5. human reviews the candidate
6. approved material becomes `APPROVED`
7. future RFQs reuse the approved translation

---

## 11. Legacy Excel Strategy

Legacy Excel data must not be imported directly into production Material records.

Required flow:

`Raw Legacy Data`
→ `Normalization`
→ `Material Extraction`
→ `Duplicate Detection`
→ `Human Review`
→ `Approved Material`

Example raw value:

`8-راس روط سعر 200$`

must not become a Material name as-is.

The system must extract the actual material terminology before approval.

---

## 12. Backward Compatibility

Existing fields:

- `nameEn`
- `nameAr`

will eventually require migration.

Possible mapping:

`nameEn`
→ `canonicalEnglishName`

`nameAr`
→ candidate value for `preferredIraqiName`

However, existing Arabic values must not automatically be assumed to be approved Iraqi-market terminology.

Database migration is handled by a separate issue.

---

## 13. Out of Scope

This design does not implement:

- JPA entity changes
- Flyway migrations
- Excel import
- AI integration
- semantic search
- MCP
- RFQ integration

---

## 14. Future Extensions

Potential future concepts:

### MaterialApplication

May represent vehicle/application compatibility.

Possible fields:

- materialId
- applicationType
- make
- model
- variant

This is intentionally excluded from the initial Material model.

---

## 15. Design Decisions

- `Material` represents the canonical business concept.
- Alternative terminology belongs in `MaterialAlias`.
- Iraqi-market terminology is treated as first-class business knowledge.
- `preferredIraqiName` is nullable until approved.
- approved database knowledge has priority over AI.
- AI suggestions require human approval before becoming authoritative.
- original legacy values must be preserved during import.

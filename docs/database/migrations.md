# Database Migration Strategy

## Tool

`material-service` uses Flyway for database schema migrations.

Flyway is the authoritative mechanism for creating and evolving the database schema.

Hibernate is configured with:

```yaml
ddl-auto: validate
```

Hibernate validates the schema but does not modify it.

## Migration Location

Migration files are stored in:

```text
src/main/resources/db/migration
```

## Naming Convention

Flyway versioned migrations use:

```text
V<version>__<description>.sql
```

Examples:

```text
V1__create_materials_table.sql
V2__add_material_status.sql
V3__create_material_alias_table.sql
```

## Initial Migration

`V1__create_materials_table.sql` represents the current baseline schema of the existing Material domain model.

The development database was created from an empty PostgreSQL database, so no `baseline-on-migrate` configuration is required.

## Development Workflow

When a schema change is required:

1. Create a new Flyway migration file.
2. Never modify an already applied migration.
3. Run the application against PostgreSQL.
4. Verify the migration in `flyway_schema_history`.
5. Run the automated test suite.
6. Commit the migration together with the related application change when appropriate.

## Production Behavior

Production also uses Flyway migrations.

Hibernate remains configured with:

```yaml
ddl-auto: validate
```

This ensures that database schema changes are explicit, version-controlled, and reviewable.

## Important Rule

Once a migration has been applied to a shared environment, do not edit or rename it.

Create a new migration instead.

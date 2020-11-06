# DB library spike

## Runbook

First run:
1. Start database container.
2. Run schema migration.
3. DB code generation.

### Start database container

```bash
docker-compose up
```

### Schema migration

```bash
sbt db/flywayMigrate
```

### DB code generation

```bash
sbt jooqCodegen
```

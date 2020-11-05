# DB library spike

## Runbook

First run:
1. Start database container.
2. Run schema migration.

### Start database container

```bash
docker-compose up
```

### Schema migration

```bash
sbt db/flywayMigrate
```

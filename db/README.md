This folder contains the exported database schema and sample data for the Valorant E-Sports project.

How to import:

```bash
# from project root
mysql -u root -p < db/schema_and_data.sql
```

Notes:
- Do NOT commit production credentials into the repository.
- If you expose any DB passwords accidentally, rotate them immediately.

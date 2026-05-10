# Valorant E-Sports Tournament Tracker

JavaFX + MySQL starter project for tracking Valorant esports data.

## What is included

- JavaFX app entry point in `src/main/java/app/Main.java`
- MySQL connection helper in `src/main/java/app/DBConnection.java`
- Domain models for the 9 core tables
- SQL schema and sample data in `db/schema_and_sample_data.sql`
- JDBC DAO layer for the core entities

## Setup on Linux Mint

Install tools:

```bash
sudo apt update
sudo apt install -y git maven mysql-server curl
```

Install Java 21 with SDKMAN:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk list java
# install one Java 21 distribution from the list
```

Start MySQL and create the database:

```bash
sudo systemctl start mysql
mysql -u root -p < db/schema_and_sample_data.sql
```

Set database credentials with environment variables:

```bash
export VALORANT_DB_URL="jdbc:mysql://localhost:3306/valorant_esports"
export VALORANT_DB_USER="root"
export VALORANT_DB_PASSWORD="your-password"
```

Run the app:

```bash
mvn javafx:run
```

Compile only:

```bash
mvn -DskipTests compile
```

## Notes

- The `app.gui.PlayerSearchController` is still empty, so the GUI is only partially wired.
- The DAO layer is now in place, so the next step is hooking controllers to those DAOs.

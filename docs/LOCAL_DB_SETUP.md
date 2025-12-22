# 🔧 Local Database Configuration for Dev Testing

## 📍 Where to Set MySQL Credentials

### ✅ **RECOMMENDED: Use `application-local.yml`**

**Location:** `src/main/resources/application-local.yml`

**This file is already created for you!** Just edit lines 14-15:

```yaml
spring:
  datasource:
    username: root                    # ← Your MySQL username
    password: your_password_here      # ← Your MySQL password
```

---

## 🚀 Quick Start Guide

### Step 1: Create MySQL Database

Open MySQL command line or MySQL Workbench and run:

```sql
CREATE DATABASE rntn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Edit `application-local.yml`

Open: `src/main/resources/application-local.yml`

Change these lines:
```yaml
username: root              # Your actual MySQL username
password: your_password     # Your actual MySQL password
```

### Step 3: Run Application with Local Profile

**Option A - Maven Command:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Option B - IntelliJ IDEA:**
1. Edit Run Configuration
2. Add VM options: `-Dspring.profiles.active=local`
3. Click Run

**Option C - Environment Variable:**
```bash
# Windows Command Prompt
set SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run

# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="local"
mvn spring-boot:run
```

---

## 🎯 Configuration Options

### Option 1: application-local.yml (BEST for dev) ✅

**Pros:**
- ✅ Easy to edit
- ✅ Git-ignored (secure)
- ✅ Profile-specific
- ✅ Includes dev-friendly settings (SQL logging, etc.)

**Location:** `src/main/resources/application-local.yml`

**Activate:** `-Dspring.profiles.active=local`

---

### Option 2: Environment Variables

Set these before running:

**Windows CMD:**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=rntn_db
set DB_USER=root
set DB_PASSWORD=your_password
```

**Windows PowerShell:**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="rntn_db"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=rntn_db
export DB_USER=root
export DB_PASSWORD=your_password
```

---

### Option 3: Use Default Values

If you don't set anything, the application uses these defaults from `application.yml`:

```yaml
username: ${DB_USER:rntn_user}      # Default: rntn_user
password: ${DB_PASSWORD:rntn_password}  # Default: rntn_password
```

**Create a MySQL user with these credentials:**
```sql
CREATE USER 'rntn_user'@'localhost' IDENTIFIED BY 'rntn_password';
GRANT ALL PRIVILEGES ON rntn_db.* TO 'rntn_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📊 Connection Properties

| Property | Default | Your Value | Description |
|----------|---------|------------|-------------|
| **Host** | localhost | ___________ | MySQL server |
| **Port** | 3306 | ___________ | MySQL port |
| **Database** | rntn_db | ___________ | Database name |
| **Username** | rntn_user | ___________ | MySQL user |
| **Password** | rntn_password | ___________ | MySQL password |

---

## 🧪 Testing the Connection

### 1. Check MySQL is Running
```bash
mysql --version
# Should show: mysql Ver 8.0.x...
```

### 2. Test MySQL Connection
```bash
mysql -u root -p
# Enter password when prompted
# Should connect to MySQL

# Inside MySQL:
SHOW DATABASES;
# Should see rntn_db if created
```

### 3. Run the Application
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Look for these logs:**
```
✅ HikariPool-1 - Starting...
✅ HikariPool-1 - Start completed.
✅ Flyway Community Edition X.X.X
✅ Successfully applied X migrations
✅ Started RntnSentimentApiApplication in X.XXX seconds
```

### 4. Check Swagger UI
Open: http://localhost:8080/swagger-ui.html

Should see all API endpoints!

---

## 🔧 Troubleshooting

### ❌ Error: "Access denied for user"

**Problem:** Wrong username or password

**Solution:**
1. Check your MySQL username: `SELECT USER();` in MySQL
2. Update `application-local.yml` with correct credentials
3. Or reset MySQL password:
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
   ```

---

### ❌ Error: "Communications link failure"

**Problem:** MySQL not running or wrong host/port

**Solution:**
1. Check MySQL is running:
   ```bash
   # Windows
   net start MySQL80
   
   # Linux/Mac
   sudo systemctl start mysql
   ```
2. Verify port in `application-local.yml` matches MySQL port
3. Check firewall isn't blocking port 3306

---

### ❌ Error: "Unknown database 'rntn_db'"

**Problem:** Database not created

**Solution:**
```sql
CREATE DATABASE rntn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### ❌ Error: "Port 8080 already in use"

**Problem:** Another app is using port 8080

**Solution:** Change port in `application-local.yml`:
```yaml
server:
  port: 8081  # or any available port
```

---

### ❌ Error: Flyway migration failed

**Problem:** Database schema conflicts

**Solution:**
```bash
# Option 1: Clean and recreate (⚠️ DELETES ALL DATA)
mvn flyway:clean
mvn flyway:migrate

# Option 2: Check migration status
mvn flyway:info

# Option 3: Use fresh database
DROP DATABASE rntn_db;
CREATE DATABASE rntn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📝 File Locations Summary

```
rntn08122025/
├── src/main/resources/
│   ├── application.yml              # Main config (uses env vars)
│   └── application-local.yml        # ← EDIT THIS for local dev! ✅
│
└── LOCAL_DB_SETUP.md               # ← You're reading this
```

---

## 🔒 Security Notes

⚠️ **IMPORTANT:**
- ✅ `application-local.yml` is in `.gitignore` (won't be committed)
- ✅ Never commit passwords to Git
- ✅ Use environment variables for production
- ✅ Change default passwords in production

---

## ✅ Quick Checklist

For dev testing, make sure you have:

- [ ] MySQL installed and running
- [ ] Database `rntn_db` created
- [ ] Edited `application-local.yml` with your MySQL credentials
- [ ] Started app with local profile: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] Checked Swagger UI: http://localhost:8080/swagger-ui.html
- [ ] Tested an API endpoint

---

## 💡 Pro Tips

1. **Use application-local.yml** - It's already configured with dev-friendly settings (SQL logging, detailed errors, etc.)

2. **Check logs** - With local profile, you'll see all SQL queries in the console

3. **Use Swagger** - http://localhost:8080/swagger-ui.html to test all endpoints

4. **Database Tools** - Use MySQL Workbench or DBeaver to inspect the database

5. **Hot Reload** - Changes to Java code will auto-reload (with Spring DevTools)

---

## 🆘 Still Having Issues?

1. Check MySQL is running: `mysql --version`
2. Check `application-local.yml` credentials are correct
3. Check database exists: `SHOW DATABASES;` in MySQL
4. Check application logs for specific error messages
5. Try with default credentials (create rntn_user/rntn_password)

---

**Need more help?** Check the main `README.md` or project documentation in `docs/` folder.

**Ready to go?** Just edit `application-local.yml` and run with `-Dspring.profiles.active=local`! 🚀


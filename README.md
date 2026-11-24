# PDSR LATEST — Online public business directory
This is an installable source package, not an APK or a live deployment. It replaces the offline arbitrary-record search with a restricted, exact-number public business directory. No private SIM owner lookup, CNIC, residential address, bulk phonebook collection or arbitrary spreadsheet import. The admin must confirm each listing is authorized and public. No search query log is stored on the backend by this code; local history can be cleared. Hosting provider access logs may exist.

## Hostinger setup
1. Create a dedicated HTTPS subdomain and MySQL database/user in hPanel. Enable SSL. Use PHP 8.1+ with PDO MySQL.
2. Import hosting/schema.sql via phpMyAdmin into the new DB. Upload the contents of hosting (including .htaccess) to the subdomain document root. Do not overwrite other websites.
3. Copy config.example.php to config.php. Set database values and a random secret (64 lowercase hex characters). Generate a password hash with PHP password_hash using a unique strong password, and a secret with bin2hex(random_bytes(32)). Do not use sample values. Prefer storing config outside public root and updating common.php path. Verify config and schema URLs are denied before continuing.
4. Open https://YOUR-SUBDOMAIN/admin.php and sign in. Add only a verified public business listing with a real public source and authorization. Do not upload leaked or private records.
5. Test search.php using an HTTPS POST with form field number. Check success, no match, invalid input, rate limit, and unauthenticated admin access. Remove your test records.

## Android
Open android/PDSR_LATEST in Android Studio, set endpoint in MainActivity.kt to the actual HTTPS search.php URL. Sync Gradle, install required SDK, build and run on an emulator and real phone. The wrapper JAR is not included; use Android Studio's Gradle setup or a compatible installed Gradle. Verify target API requirements in Play Console, update application ID if needed, and create a signed AAB. Never share signing keys. Use a directory description, not a claim of official SIM registration data. Provide privacy policy and accurate Data Safety declarations, establish a listing correction/removal contact, review hosting logs and retention before launch.

## QA limits
PHP syntax checked and pure normalization cases tested locally. MySQL integration, hosting, Android compilation, emulator/device testing, load/security audits and Play review have not been performed. No bulk Excel import, cloud user accounts, cloud search history, or automatic backups included. Configure hosting backups independently. Public endpoint has per-IP rate limiting, not authenticated user access; production abuse review is required. No guarantee of Play acceptance.

# Local Backend Switch (Android)

This project now reads the backend base URL from `BuildConfig.BACKEND_BASE_URL`.

## Where It Comes From
The value is injected in `app/build.gradle`:
- `debug` uses `backend.baseUrl.debug`
- `release` uses `backend.baseUrl.release`

Both can be overridden in `local.properties` (recommended, not committed).

## Recommended Local Debug Setup
In `local.properties`, add:

```properties
backend.baseUrl.debug=http://10.0.2.2:5000/
```

Notes:
- `10.0.2.2` is the Android emulator alias for the host machine.
- Ensure the backend is running on port `5000`.

## Debug Keystore (If Packaging Fails)
Debug signing is configured to use:
- `.data/android/debug.keystore`

If it does not exist yet, generate it locally:

```bash
mkdir -p .data/android
keytool -genkeypair -v \
  -keystore .data/android/debug.keystore \
  -storepass android \
  -keypass android \
  -alias androiddebugkey \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US" \
  -noprompt
```

## Build Commands

```bash
./gradlew assembleDebug
./gradlew installDebug
```

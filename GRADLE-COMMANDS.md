# Helpful Gradle Commands

Run these from the repository root:

```bash
cd /Users/efthemios/Documents/projects/pasabayan/pasabayan-android
```

## Fast Daily Checks

```bash
# Compile app Kotlin only (quick signal for code issues)
./gradlew :app:compileDebugKotlin

# Merge resources only (useful for string/XML errors)
./gradlew :app:mergeDebugResources

# Build debug APK
./gradlew :app:assembleDebug
```

## Payments / Stripe Focus

```bash
# Payments ViewModel tests only
./gradlew :app:testDebugUnitTest --tests "*payments*viewmodel*"

# All payments-related app tests
./gradlew :app:testDebugUnitTest --tests "*payments*"

# Core network payments tests
./gradlew :core:network:testDebugUnitTest --tests "*payments*"
```

## Module-Level Test Commands

```bash
# All app unit tests
./gradlew :app:testDebugUnitTest

# Core network unit tests
./gradlew :core:network:testDebugUnitTest

# Core session unit tests
./gradlew :core:session:testDebugUnitTest

# Core design system JVM tests
./gradlew :core:designsystem:testDebugUnitTest
```

## Clean Rebuild (When Things Get Weird)

```bash
# Stop daemon first (helps with stale Java/JDK state)
./gradlew --stop

# Clean + rebuild app
./gradlew clean :app:assembleDebug
```

## Useful Debug Flags

```bash
# Show stacktrace for failures
./gradlew :app:compileDebugKotlin --stacktrace

# More verbose logs
./gradlew :app:mergeDebugResources --info
```

## Current Recommended Validation Sequence

```bash
./gradlew :app:mergeDebugResources
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --tests "*payments*"
```

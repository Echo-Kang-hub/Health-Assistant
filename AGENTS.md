# Repository Guidelines

This repository is a single-module Android app built with the Gradle wrapper. Use Android Studio for day-to-day work and the `./gradlew` commands below for CI-friendly builds.

## Project Structure & Module Organization
- `app/` contains the Android application module.
- `app/src/main/java/com/example/healthassistant/` holds Java source (e.g., `MainActivity.java`).
- `app/src/main/res/` stores layouts, drawables, and values (`strings.xml`, `themes.xml`, `colors.xml`).
- `app/src/test/java/` contains local JVM unit tests; `app/src/androidTest/java/` contains instrumented tests.
- `gradle/libs.versions.toml` centralizes dependency and plugin versions.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a debug APK.
- `./gradlew installDebug` installs the debug build to a connected device/emulator.
- `./gradlew test` runs local unit tests in `app/src/test`.
- `./gradlew connectedAndroidTest` runs instrumented tests on a device/emulator.
- `./gradlew lint` runs Android Lint checks.

## Coding Style & Naming Conventions
- Java 17 is the target (`compileOptions` in `app/build.gradle`). Use 4-space indentation and Android Studio defaults for formatting.
- Classes use PascalCase (`MainActivity`), methods/fields use camelCase, and packages stay under `com.example.healthassistant`.
- Resource names are `snake_case` (e.g., `activity_main.xml`, `ic_launcher_foreground`). Keep user-facing text in `app/src/main/res/values/strings.xml`.

## Testing Guidelines
- Unit tests use JUnit 4 (`testImplementation libs.junit`). Instrumented tests use AndroidX test runner and Espresso.
- Place tests in the matching package path. Name test classes `*Test.java` (see `ExampleUnitTest.java`).

## Commit & Pull Request Guidelines
- Commits are short, imperative summaries (e.g., "Rebuild project with Java and Groovy"). Keep scope focused.
- After making changes, create a commit before finishing the task. Do not leave the worktree dirty.
- Prefer Conventional Commits with scope when possible: `type(scope): summary` (e.g., `fix(network): switch debug base url`).
- PRs should describe the change, include testing notes (commands run), and attach screenshots for UI changes.

## Configuration & Release Notes
- App ID, SDK levels, and build types live in `app/build.gradle`. Update `gradle/libs.versions.toml` for dependency version bumps.
- Do not store secrets in the repo; use local environment or IDE-managed configs if needed.

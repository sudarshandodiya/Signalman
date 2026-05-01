# Project Signalman: Advanced URL Router

## Overview
Signalman is an Android application designed to act as the default handler for all HTTP and HTTPS URLs. It provides users with powerful customization options to route specific URLs to specific applications or browsers based on user-defined rules (Regex or simple matching).

## Agents & Roles

### 1. Architect Agent
- **Responsibility**: Designing the overall system architecture, including rule storage and the routing engine.
- **Tasks**:
    - Define the data model for routing rules.
    - Design the Intent filter to capture all URLs.
    - Plan the integration of a rule engine.

### 2. Developer Agent
- **Responsibility**: Implementing the core functionality and UI.
- **Tasks**:
    - Implement the `Intent` handling logic in `MainActivity` (or a dedicated `RoutingActivity`).
    - Build the UI for adding, editing, and deleting rules.
    - Implement the rule matching logic.
    - Handle package-specific launching (e.g., launching YouTube in Firefox).

### 3. Researcher/Documentation Agent
- **Responsibility**: Researching Android Intent system limitations and documenting the project.
- **Tasks**:
    - Research how to register as a default browser/URL handler.
    - Document the rule syntax and usage.

## CI / Pre-Push Checklist

Before pushing changes, run the following commands locally to ensure CI (`.github/workflows/pr-checks.yml`) passes:

```bash
./gradlew spotlessCheck --no-daemon   # Code formatting
./gradlew detekt --no-daemon          # Static analysis
./gradlew assembleDebug --no-daemon   # Build debug APK
./gradlew testDebugUnitTest --no-daemon  # Unit tests
```

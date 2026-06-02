# Upgrade Plan: intelligent-bus-guidance-system (20260602163618)

- **Generated**: 2026-06-03 16:37:00
- **HEAD Branch**: main
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 17.0.15: /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin (current project JDK, used by baseline)
- JDK 21.0.4: /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin (target JDK for upgrade validation)

**Build Tools**
- Maven: **<TO_BE_INSTALLED>** Maven 3.9.9+ (required for Java 21, no local Maven installation detected)
- Maven Wrapper: none present

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260602163618
- Run tests before and after the upgrade: true

## Upgrade Goals

- Java 21

## Technology Stack

| Technology/Dependency    | Current | Min Compatible | Why Incompatible |
| ------------------------ | ------- | -------------- | ---------------------------------------------- |
| Java                     | 17      | 21             | User requested latest LTS                      |
| Maven                    | none detected | 3.9.9       | Required for stable Java 21 builds; project has no wrapper |
| maven-compiler-plugin    | unspecified | 3.11.0      | Explicit version needed for Java 21 source/target |
| maven-surefire-plugin    | 3.2.5   | 3.0.0         | Compatible with Java 21; no change required     |
| JUnit Jupiter            | 5.10.2  | 5.10.2        | Compatible with Java 21                        |
| Gson                     | 2.10.1  | 2.10.1        | Compatible with Java 21                        |

## Derived Upgrades

- Java 21 → Install Maven 3.9.9+ because no Maven installation or wrapper is available.
- Java 21 → Add explicit `maven-compiler-plugin` version 3.11.0 to guarantee Java 21 compilation support.
- Java 21 → Update GitHub Actions `java-version` from 17 to 21 for CI consistency.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| pom.xml | `<maven.compiler.source>` | 17 | upgrade | 21 | Upgrade runtime target to Java 21 |
| pom.xml | `<maven.compiler.target>` | 17 | upgrade | 21 | Upgrade runtime target to Java 21 |
| pom.xml | `maven-compiler-plugin` | none specified | add | 3.11.0 | Required explicit compiler plugin version for Java 21 support |

### Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| None | N/A | N/A | N/A | No source-level Java 21 migration changes expected |

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|----------------|--------|
| .github/workflows/maven.yml | actions/setup-java `java-version` | 17 | 21 | Update CI to run with Java 21 |

### CI/CD Changes

| File | Location | Current | Required Change |
|------|----------|---------|----------------|
| .github/workflows/maven.yml | `java-version` | 17 | 21 |

### Risks & Warnings

- **No local Maven installation detected**: The upgrade requires a Maven runtime to execute. **Mitigation**: Install Maven 3.9.9+ during Step 1 and use it for all build verification.
- **No Maven wrapper present**: The repo does not provide a consistent Maven wrapper. **Mitigation**: Use the installed Maven runtime and keep build changes minimal.
- **Default compiler plugin version is unspecified**: Relying on Maven's default compiler plugin may fail for Java 21. **Mitigation**: Add an explicit `maven-compiler-plugin` version 3.11.0.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure the required Java 21 runtime and Maven build tool are available before modifying project files.
  - **Changes to Make**: Install Maven 3.9.9+ and verify JDK 21 presence.
  - **Verification**: `mvn -version` with JDK 21, expected `Apache Maven 3.9.9+` and Java 21.

- Step 2: Setup Baseline
  - **Rationale**: Capture current project build/test behavior on Java 17 before making upgrade changes.
  - **Changes to Make**: Run baseline Maven compile/test with JDK 17.
  - **Verification**: `mvn -q clean test-compile && mvn -q clean test` on JDK 17.

- Step 3: Upgrade Java target configuration
  - **Rationale**: Apply the Java 21 runtime upgrade and ensure build tooling is explicitly compatible.
  - **Changes to Make**: Update `pom.xml` compiler source/target to 21, add `maven-compiler-plugin` 3.11.0, update GitHub Actions Java version to 21.
  - **Verification**: `mvn -q clean test-compile` with JDK 21.

- Step 4: CVE Validation & Fix
  - **Rationale**: Validate direct dependencies for known vulnerabilities and fix any reported issues before final validation.
  - **Changes to Make**: Scan direct dependencies, upgrade any vulnerable versions, verify compilation.
  - **Verification**: `mvn -q clean test-compile` with JDK 21 and re-run CVE scan after fixes.

- Step 5: Final Validation
  - **Rationale**: Confirm the upgrade is complete, stable, and behaviorally intact.
  - **Changes to Make**: Fix any test failures discovered after Java 21 upgrade.
  - **Verification**: `mvn -q clean test` with JDK 21 and achieve 100% passing tests.

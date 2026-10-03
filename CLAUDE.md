# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`checkstyle4bluej` is a BlueJ IDE extension (JavaFX + BlueJ Extensions2 API) that runs
[Checkstyle](https://checkstyle.org/) against student code inside BlueJ, surfaces violations in an
audit window, and lets users pick/manage Checkstyle config files via BlueJ's Preferences UI.

Most of the actual UI/violation-tracking machinery (audit window, violation manager, package/file
event handlers, rule definitions) lives in an external dependency, **BlueJ-Linting-Core**
(`no.ntnu.iir.bluej.extensions.linting.core.*`, pulled from JitPack —
https://github.com/NTNU-IE-IIR/BlueJ-Linting-Core). This repo only implements the Checkstyle-specific
plumbing on top of that core. See `docs/ARCHITECTURE.md` for class and sequence diagrams covering both
this repo and the core classes it relies on. When something isn't in this repo's `src/`, it's almost certainly in
that core library — don't assume it's missing.

## Build & release commands

```bash
mvn clean verify          # compile + run tests
mvn -B package             # build the shaded plugin jar (used by CI)
mvn clean verify           # also what tools/buildAndInstallLocally.ps1 runs before copying the jar
```

- Build output: `target/checkstyle4bluej-<version>.jar` — this is the shaded/fat jar (built via
  `maven-shade-plugin`) that gets installed into BlueJ. A `-original.jar` (unshaded) is also produced.
- There is currently no real test suite (`src/test/java/.../checkstyle/` only has a `.gitkeep`) — do
  not assume `mvn test` exercises meaningful coverage.
- Local install/dev loop (Windows only, see `tools/buildAndInstallLocally.ps1`): builds then copies the
  shaded jar into `C:\Program Files\BlueJ\lib\extensions2\`. On macOS/Linux, do the equivalent manually:
  build with `mvn clean verify`, then copy the non-`-original` jar from `target/` into one of BlueJ's
  `extensions2` directories (see README's install table) and restart BlueJ to test.
- `tools/updateBlueJdeps.ps1` (Windows) and `tools/updateBlueJdeps.sh` (macOS, zsh) install BlueJ's own
  `bluej.jar` from a local BlueJ installation into the repo's local Maven repo at `lib/` as
  `bluej:bluej:<version>`, via `mvn install:install-file`. Both take the version as an argument (e.g.
  `./tools/updateBlueJdeps.sh 6.0.0 [installDir]`). The Windows script reads from
  `C:\Program Files\BlueJ\lib\`; the macOS script searches `/Applications/BlueJ.app/Contents/Java` and
  `~/Applications/BlueJ.app/Contents/Java` unless an `installDir` is given. After installing a new
  version, bump the `bluej:bluej` dependency version in `pom.xml` to match.

### Releases (CI-driven, do not do manually)

Releases are cut via GitHub Actions, not by running `mvn release:*` locally:
1. `.github/workflows/stage.yml` (manual dispatch, from `develop`) runs
   `mvn release:clean release:prepare release:perform` with explicit release/next-dev versions, then
   triggers `publish.yml`.
2. `.github/workflows/publish.yml` merges the release tag into `main`, builds, and uploads the jar to
   GitHub Releases.

## Architecture

Entry point: `CheckstyleExtension` (`bluej.extensions2.Extension` subclass). `startup(BlueJ)` wires
everything together — this is the best starting point for understanding how the pieces connect:

- **`checker/CheckerService`** — owns a Checkstyle `Checker` instance. `setConfiguration(path)`
  re-initializes the `Checker` from scratch on every config change (`initChecker()` is called again to
  avoid mixing two configs) and loads config via `ConfigurationLoader`. `checkFile`/`checkFiles` are
  no-ops unless `enable()` has been called. Implements the core library's `ICheckerService`.
- **`checker/CheckerListener`** — a Checkstyle `AuditListener` that translates Checkstyle
  `AuditEvent`s into the core library's `Violation`/`RuleDefinition` objects and feeds them into a
  `ViolationManager` (core library). Violations are keyed by file name; `fileStarted` clears old
  violations for a file before it's rechecked so results don't accumulate stale entries.
- **`CheckstylePreferences`** (implements BlueJ's `PreferenceGenerator`) — builds the JavaFX
  Preferences pane (Tools → Preferences → Extensions), manages the map of named Checkstyle configs
  (`configMap`: name → path/URL), and persists it as JSON (via Jackson) into BlueJ's extension
  properties (`Checkstyle.ConfigMap`, `Checkstyle.DefaultConfig`). Two built-in configs (`Google`,
  `Sun`, from `src/main/resources/config/*.xml`) are always injected and cannot be edited/deleted from
  the UI. `saveValues()`/`configureCheckerService()` push the selected config into `CheckerService` and
  re-check all open packages on success, or disable checking and show an `ErrorDialog` if the config is
  invalid.
- **`CheckstyleConfigFormDialog`** — modal dialog used by the Preferences pane's Add/Edit buttons to
  create/edit a single (name, path) config entry.
- **`CheckstyleStatusBar`** (extends `HBox`, implements `CheckstylePreferencesListener`) — small status
  widget (on/off indicator + config picker) meant to be shown in the core library's `AuditWindow`;
  listens for config changes via `CheckstylePreferencesListener.onConfigChanged`.
- **`CheckstyleMenuBuilder`** (extends BlueJ's `MenuGenerator`) — adds "Show Checkstyle overview" to the
  Tools menu, delegating to the core library's `PackageEventHandler.showProjectWindow`.
- **`CheckstyleIconMapper`** — maps Checkstyle severity names (`warning`, `error`) to icon URLs from
  `src/main/resources/images/`, via the core library's `IconMapper` interface.

Checking is triggered by the core library's `FilesChangeHandler`/`PackageEventHandler` (registered as
BlueJ class/package listeners in `startup()`), not by anything in this repo directly — this repo mainly
supplies the `CheckerService`/`CheckerListener` those handlers call into.

## Key constraints

- **Java 21** (`maven.compiler.release`), targets **BlueJ 6.0.0** (which bundles Java 21.0.6 and JavaFX
  23.0.2; `javafx.version` in the pom is pinned to match) and the **Extensions2 API major version 3**
  (`isCompatible()` checks `getExtensionsAPIVersionMajor() == 3`). JavaFX and the `bluej:bluej` jar are
  `provided`-scope — they come from the BlueJ runtime and are excluded from the shaded jar.
- **Maven itself must run on a JDK 21+ runtime** — `javac --release 21` fails with `release version 21
  not supported` on anything older, regardless of what `JAVA_HOME`/IDE project SDK you *think* is
  active. If you hit that error, point `JAVA_HOME` (terminal) or the Project SDK (IntelliJ) at a 21+
  JDK. `.idea/` and `*.iml` are gitignored, so IntelliJ settings are per-machine.
- Checkstyle config files loaded by users **must be compatible with the Checkstyle version pinned in
  `pom.xml`** (`checkstyle.version`, currently 14.1.0 — keep the version stated in the README in sync when bumping it). The
  bundled `src/main/resources/config/google_checks.xml`/`sun_checks.xml` must also be valid for that
  version.
- The BlueJ API dependency (`bluej:bluej:6.0.0`, the full `bluej.jar` from BlueJ 6, which contains the
  `bluej.extensions2` API) isn't on Maven Central — it's resolved from the file-based Maven repo
  checked into `lib/` (see `pom.xml`'s `local_repository`). Earlier versions used the separate
  `bluejext2` API jar; that has been removed. Use the `tools/updateBlueJdeps.*` scripts to install a
  newer BlueJ version.
- `BlueJ-Linting-Core` is resolved via JitPack (`com.github.NTNU-IE-IIR:BlueJ-Linting-Core`, currently
  1.2.1). JitPack builds it from a Git tag in that repo, so a new core version must be tagged/released on
  GitHub before the pom can use it — a version that only exists in your local `~/.m2` will build locally
  but fail in CI. When debugging behavior that "isn't in this repo," check that project.

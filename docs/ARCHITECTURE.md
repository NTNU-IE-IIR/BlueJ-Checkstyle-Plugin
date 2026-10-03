# Architecture

This document describes the architecture of **checkstyle4bluej**, a [BlueJ](https://www.bluej.org/)
extension that runs [Checkstyle](https://checkstyle.org/) against the code in open BlueJ projects and
shows the violations in an audit window.

## Overview

The extension is a thin, Checkstyle-specific layer on top of two external pieces:

| Layer | Provided by | Responsibility |
|---|---|---|
| Host IDE | **BlueJ Extensions2 API** (`bluej.extensions2.*`, `provided` scope) | Extension lifecycle, class/package events, Preferences and Tools-menu hooks, extension property storage |
| Generic linting framework | **BlueJ-Linting-Core** (`no.ntnu.iir.bluej.extensions.linting.core.*`, via JitPack) | Event handlers, violation store, audit window UI, error dialog, `ICheckerService` / `IconMapper` contracts |
| Checkstyle plugin (this repo) | `no.ntnu.iir.bluej.extensions.linting.checkstyle.*` | Running Checkstyle, translating its audit events into core `Violation`s, managing Checkstyle config files and preferences UI |
| Linter engine | **Checkstyle** (`com.puppycrawl.tools.checkstyle.*`, version pinned in `pom.xml`) | Parsing configuration and checking Java source files |

```mermaid
flowchart LR
    subgraph BlueJ["BlueJ IDE (host)"]
        API["Extensions2 API"]
    end

    subgraph Plugin["checkstyle4bluej (this repo)"]
        Ext["CheckstyleExtension"]
        Prefs["CheckstylePreferences"]
        Svc["checker.CheckerService"]
        Lst["checker.CheckerListener"]
        UI["StatusBar / MenuBuilder / ConfigFormDialog / IconMapper"]
    end

    subgraph Core["BlueJ-Linting-Core"]
        Handlers["FilesChangeHandler / PackageEventHandler"]
        VM["ViolationManager"]
        AW["AuditWindow"]
    end

    CS["Checkstyle engine"]

    API -- "startup()" --> Ext
    API -- "class / package events" --> Handlers
    Handlers -- "checkFile(s)" --> Svc
    Svc --> CS
    CS -- "AuditEvents" --> Lst
    Lst -- "violations" --> VM
    VM -- "onViolationsChanged" --> AW
    Prefs -- "setConfiguration / enable / disable" --> Svc
    UI --- AW
    Ext -. wires .-> Prefs & Svc & Lst & UI & Handlers
```

The key design idea is **inversion through the core library**: the core library owns *when* files are
checked (reacting to BlueJ events) and *how* results are displayed, while this repo only supplies
*what* checks a file (`CheckerService`, an `ICheckerService`) and *how* results are translated
(`CheckerListener`).

## Package structure

```
no.ntnu.iir.bluej.extensions.linting.checkstyle
├── CheckstyleExtension            BlueJ entry point; wires everything together
├── CheckstylePreferences          Preferences pane + config persistence + config switching
├── CheckstylePreferencesListener  Observer interface for config changes
├── CheckstyleConfigFormDialog     Modal add/edit dialog for one (name, path) config entry
├── CheckstyleStatusBar            ON/OFF indicator + config picker shown in the AuditWindow
├── CheckstyleMenuBuilder          Tools-menu item "Show Checkstyle overview"
├── CheckstyleIconMapper           Severity name -> icon URL
├── ProvidedConfigs                Finds configs shipped in extensions2/checkstyle4bluej/
├── SystemInfo                     Build info (version), generated from src/main/java-templates/
└── checker
    ├── CheckerService             Owns and configures the Checkstyle Checker
    └── CheckerListener            Checkstyle AuditListener -> core Violations
```

Resources (`src/main/resources`):

- `config/google_checks.xml`, `config/sun_checks.xml` – the two built-in configurations ("Google" and
  "Sun"), always present and not editable/deletable from the UI.
- `config/pom.properties` – filtered at build time; used to report the Checkstyle version in error
  messages.
- `images/warning.png`, `images/error.png` – severity icons.
- `styles/text-input.css` – error styling for the config form dialog.

## Class diagrams

### Plugin classes and their collaborators

```mermaid
classDiagram
    direction LR

    class Extension {
        <<BlueJ API>>
    }
    class PreferenceGenerator {
        <<interface, BlueJ API>>
        +getWindow() Pane
        +loadValues()
        +saveValues()
    }
    class MenuGenerator {
        <<BlueJ API>>
        +getToolsMenuItem(BPackage) MenuItem
    }
    class ICheckerService {
        <<interface, Core>>
        +enable()
        +disable()
        +isEnabled() boolean
        +checkFile(File, String)
        +checkFiles(List~File~, String)
    }
    class IconMapper {
        <<interface, Core>>
        +getIcon(String) URL
    }
    class AuditListener {
        <<interface, Checkstyle>>
    }

    class CheckstyleExtension {
        +startup(BlueJ)
        +isCompatible() boolean
        +getVersion() String
        +getName() String
        +getDescription() String
        +getURL() URL
    }

    class CheckerService {
        -Checker checker
        -boolean enabled
        -List~AuditListener~ listeners
        -initChecker()
        +setConfiguration(String configPath)
        +addListener(AuditListener)
        +removeListener(AuditListener)
        +enable()
        +disable()
        +isEnabled() boolean
        +checkFile(File, String)
        +checkFiles(List~File~, String)
    }

    class CheckerListener {
        -ViolationManager violationManager
        +auditStarted(AuditEvent)
        +fileStarted(AuditEvent)
        +addError(AuditEvent)
        +addException(AuditEvent, Throwable)
        +fileFinished(AuditEvent)
        +auditFinished(AuditEvent)
    }

    class CheckstylePreferences {
        -BlueJ blueJ
        -String currentConfig
        -HashMap~String,String~ configMap
        -List~CheckstylePreferencesListener~ listeners
        -Properties pomProperties
        +initPane()
        +getWindow() Pane
        +loadValues()
        +saveValues()
        +getConfigKeys() Set~String~
        +setConfig(String configKey)
        +getCurrentConfig() String
        +getService() ICheckerService
        +addConfigChangeListener(CheckstylePreferencesListener)
        +removeConfigChangeListener(CheckstylePreferencesListener)
        -configureCheckerService()
        -reloadUiData()
        -notifyListeners()
    }

    class CheckstylePreferencesListener {
        <<interface>>
        +onConfigChanged(String currentConfig)
    }

    class CheckstyleStatusBar {
        -Label statusIndicator
        -ComboBox~String~ currentConfigComboBox
        +onConfigChanged(String)
        -updateIndicator()
    }

    class CheckstyleConfigFormDialog {
        -TextField configNameTextField
        -TextField configPathTextField
        +CheckstyleConfigFormDialog()
        +CheckstyleConfigFormDialog(String name, String path)
        -convertResult(ButtonType) SimpleEntry
        -evaluateValidity()
    }

    class CheckstyleMenuBuilder {
        +getToolsMenuItem(BPackage) MenuItem
    }

    class CheckstyleIconMapper {
        -HashMap~String,URL~ iconMap
        +getIcon(String) URL
    }

    Extension <|-- CheckstyleExtension
    PreferenceGenerator <|.. CheckstylePreferences
    MenuGenerator <|-- CheckstyleMenuBuilder
    ICheckerService <|.. CheckerService
    AuditListener <|.. CheckerListener
    IconMapper <|.. CheckstyleIconMapper
    CheckstylePreferencesListener <|.. CheckstyleStatusBar

    CheckerService o-- "*" AuditListener : forwards to Checker
    CheckstylePreferences --> CheckerService : configures
    CheckstylePreferences --> ViolationManager : clears
    CheckstylePreferences o-- "*" CheckstylePreferencesListener : notifies
    CheckstylePreferences ..> CheckstyleConfigFormDialog : opens
    CheckstyleStatusBar --> CheckstylePreferences : reads / setConfig
    CheckerListener --> ViolationManager : writes violations
    CheckstyleMenuBuilder --> PackageEventHandler : showProjectWindow

    CheckstyleExtension ..> CheckerService : creates
    CheckstyleExtension ..> CheckerListener : creates
    CheckstyleExtension ..> CheckstylePreferences : creates
    CheckstyleExtension ..> CheckstyleStatusBar : creates
    CheckstyleExtension ..> CheckstyleMenuBuilder : creates
    CheckstyleExtension ..> CheckstyleIconMapper : creates

    class ViolationManager {
        <<Core>>
    }
    class PackageEventHandler {
        <<Core>>
    }
```

### BlueJ-Linting-Core classes used by the plugin

These classes live in the external core library but are central to understanding the runtime
behaviour.

```mermaid
classDiagram
    direction TB

    class ClassListener {
        <<interface, BlueJ API>>
    }
    class PackageListener {
        <<interface, BlueJ API>>
    }

    class FilesChangeHandler {
        -ViolationManager violationManager
        -ICheckerService checkerService
        +classStateChanged(ClassEvent)
        +classNameChanged(ClassEvent)
        +classRemoved(ClassEvent)
        -processFile(String, BClass)
    }

    class PackageEventHandler {
        -HashMap~String,AuditWindow~ projectWindowMap
        -ViolationManager violationManager
        -ICheckerService checkerService
        +packageOpened(PackageEvent)
        +packageClosing(PackageEvent)
        +openProjectWindow(BPackage)
        +showProjectWindow(BPackage)
        +checkAllPackagesOpen(ViolationManager, ICheckerService)$
    }

    class ViolationManager {
        -HashMap~String,List~Violation~~ violations
        -List~ViolationListener~ listeners
        -List~BPackage~ bluePackages
        -HashMap~String,BClass~ blueClassMap
        +addViolations(String, List~Violation~)
        +getViolations(String) List~Violation~
        +setViolations(String, List~Violation~)
        +removeViolations(String)
        +clearViolations()
        +addListener(ViolationListener)
        +addBluePackage(BPackage)
        +removeBluePackage(BPackage)
        +getBluePackages() List~BPackage~
        +syncBlueClassMap()
        +getBlueClass(String) BClass
    }

    class ViolationListener {
        <<interface>>
        +onViolationsChanged(HashMap)
    }

    class AuditWindow {
        +onViolationsChanged(HashMap)
        +setTitlePrefix(String)$
        +setStatusBar(HBox)$
    }

    class Violation {
        +getSummary() String
        +getBClass() BClass
        +getLocation() TextLocation
        +getRuleDefinition() RuleDefinition
    }

    class RuleDefinition {
        +setIconMapper(IconMapper)$
        +getTitle() String
        +getRuleId() String
        +getSeverityIcon() URL
    }

    class ICheckerService {
        <<interface>>
    }

    ClassListener <|.. FilesChangeHandler
    PackageListener <|.. PackageEventHandler
    ViolationListener <|.. AuditWindow
    FilesChangeHandler --> ViolationManager
    FilesChangeHandler --> ICheckerService
    PackageEventHandler --> ViolationManager
    PackageEventHandler --> ICheckerService
    PackageEventHandler o-- "*" AuditWindow : one per project
    ViolationManager o-- "*" ViolationListener : notifies
    ViolationManager o-- "*" Violation
    Violation --> RuleDefinition
```

## Key design decisions

- **Single shared state.** `CheckstyleExtension.startup()` creates exactly one `CheckerService`, one
  `ViolationManager` and one `CheckstylePreferences`; every other component receives them through its
  constructor (manual dependency injection). There is no static service locator apart from the static
  setters the core library exposes (`RuleDefinition.setIconMapper`, `AuditWindow.setTitlePrefix`,
  `AuditWindow.setStatusBar`).
- **Observer pattern, twice.**
  - Checkstyle → plugin: `CheckerListener` is registered as a Checkstyle `AuditListener` on the
    `Checker` and receives audit events while files are processed.
  - Preferences → UI: `CheckstylePreferences` notifies `CheckstylePreferencesListener`s (currently
    only `CheckstyleStatusBar`) whenever the active configuration or saved preferences change.
  - (In the core library, `ViolationManager` likewise notifies `ViolationListener`s, i.e. the
    `AuditWindow`s.)
- **Fresh `Checker` per configuration.** `CheckerService.setConfiguration()` calls `initChecker()` to
  build a brand-new Checkstyle `Checker` before configuring it, so modules from a previous
  configuration can never linger. Because of that, `CheckerService` keeps its own list of
  `AuditListener`s and re-attaches them to every new `Checker`.
- **Enable/disable gate.** `checkFile`/`checkFiles` are no-ops while the service is disabled. The
  service is disabled at construction and whenever a configuration fails to load, so an invalid
  config never produces partial or misleading results.
- **Violations keyed by file name.** `CheckerListener.fileStarted()` removes all existing violations
  for a file before Checkstyle reports new ones, so re-checking a file always replaces (never
  appends to) its previous results.
- **Persistence through BlueJ.** User-defined configs are stored as a JSON map (Jackson) in BlueJ's
  extension properties under `Checkstyle.ConfigMap`; the default config name under
  `Checkstyle.DefaultConfig`. The built-in Google/Sun entries are stripped before saving and
  re-injected (as classpath resource URLs) on load.

## Sequence diagrams

### 1. Extension startup

BlueJ loads the extension jar and calls `startup()`, which builds and wires the object graph.

```mermaid
sequenceDiagram
    autonumber
    participant BJ as BlueJ
    participant Ext as CheckstyleExtension
    participant RD as RuleDefinition (core)
    participant Svc as CheckerService
    participant VM as ViolationManager (core)
    participant Lst as CheckerListener
    participant Pref as CheckstylePreferences
    participant SB as CheckstyleStatusBar
    participant AW as AuditWindow (core)
    participant PEH as PackageEventHandler (core)

    BJ->>Ext: isCompatible()
    Ext-->>BJ: API major version == 3
    BJ->>Ext: startup(blueJ)
    Ext->>RD: setIconMapper(new CheckstyleIconMapper())
    Ext->>Svc: new CheckerService()  (disabled, fresh Checker)
    Ext->>VM: new ViolationManager()
    Ext->>Lst: new CheckerListener(violationManager)
    Ext->>Svc: addListener(checkerListener)
    Ext->>Pref: new CheckstylePreferences(blueJ, checkerService, violationManager)
    activate Pref
    Pref->>Pref: initPane()
    Pref->>BJ: getExtensionPropertyString("Checkstyle.ConfigMap")
    Pref->>Pref: parse JSON, add built-in Google & Sun
    Pref->>BJ: getExtensionPropertyString("Checkstyle.DefaultConfig")
    Pref->>Pref: currentConfig = default config
    deactivate Pref
    Ext->>AW: setTitlePrefix(name)
    Ext->>SB: new CheckstyleStatusBar(preferences)
    activate SB
    SB->>Pref: setConfig(currentConfig)
    Note right of Pref: Loads config into CheckerService<br/>(see diagram 4)
    deactivate SB
    Ext->>Pref: addConfigChangeListener(statusBar)
    Ext->>AW: setStatusBar(statusBar)
    Ext->>PEH: new PackageEventHandler(violationManager, checkerService)
    Ext->>BJ: addClassListener(new FilesChangeHandler(...))
    Ext->>BJ: addPackageListener(packageEventHandler)
    Ext->>BJ: setPreferenceGenerator(preferences)
    Ext->>BJ: setMenuGenerator(new CheckstyleMenuBuilder(packageEventHandler))
```

### 2. Opening a project – initial check of all files

When a project is opened, the core library creates an `AuditWindow` for it and checks every compiled
class in all open packages.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant BJ as BlueJ
    participant PEH as PackageEventHandler (core)
    participant AW as AuditWindow (core)
    participant VM as ViolationManager (core)
    participant Svc as CheckerService
    participant CK as Checkstyle Checker
    participant Lst as CheckerListener

    User->>BJ: open project
    BJ->>PEH: packageOpened(event)
    PEH->>PEH: openProjectWindow(package)
    alt no AuditWindow for this project yet
        PEH->>AW: new AuditWindow(package, projectPath)
        PEH->>VM: addListener(auditWindow)
        PEH->>VM: addBluePackage(...) for each package in project
        PEH->>Svc: enable()
        PEH->>VM: syncBlueClassMap()
        PEH->>PEH: checkAllPackagesOpen(vm, svc)
        PEH->>VM: clearViolations()
        loop each open BPackage
            PEH->>Svc: checkFiles(compiled .java files, "utf-8")
            Svc->>CK: process(files)
            Note over CK,Lst: AuditEvent flow, see diagram 3
        end
    end
```

### 3. Re-checking a file after it is compiled

This is the most frequent action: the user edits and compiles a class, and its violations are
refreshed.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant BJ as BlueJ
    participant FCH as FilesChangeHandler (core)
    participant VM as ViolationManager (core)
    participant Svc as CheckerService
    participant CK as Checkstyle Checker
    participant Lst as CheckerListener
    participant AW as AuditWindow (core)

    User->>BJ: compile class
    BJ->>FCH: classStateChanged(event)
    FCH->>VM: removeViolations(className)
    alt class is compiled
        FCH->>Svc: checkFile(javaFile, "utf-8")
        alt service enabled
            Svc->>CK: setCharset("utf-8")
            Svc->>CK: process([javaFile])
            CK->>Lst: auditStarted(event)
            Lst->>VM: syncBlueClassMap()
            CK->>Lst: fileStarted(event)
            Lst->>VM: removeViolations(fileName)
            VM-->>AW: onViolationsChanged(map)
            loop each Checkstyle violation
                CK->>Lst: addError(auditEvent)
                Lst->>VM: getBlueClass(path)
                Lst->>Lst: build RuleDefinition (message, moduleId, severity)<br/>and Violation (BClass, TextLocation(line, col))
                Lst->>VM: addViolations / setViolations(fileName, list)
                VM-->>AW: onViolationsChanged(map)
            end
            CK->>Lst: fileFinished(event)
            CK->>Lst: auditFinished(event)
        else service disabled
            Svc-->>FCH: no-op
        end
    end
```

`classRemoved` only removes the class's violations; `classNameChanged` removes violations under the
old name and re-checks the class.

### 4. Switching the active Checkstyle configuration

The user picks a different configuration in the status bar of the `AuditWindow`. The same
`setConfig()` path is also used once during startup.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant SB as CheckstyleStatusBar
    participant Pref as CheckstylePreferences
    participant VM as ViolationManager (core)
    participant Svc as CheckerService
    participant CL as Checkstyle ConfigurationLoader
    participant PEH as PackageEventHandler (core)
    participant ED as ErrorDialog (core)

    User->>SB: select config in combo box
    SB->>Pref: setConfig(configKey)
    Pref->>Pref: currentConfig = configKey
    Pref->>Pref: configureCheckerService()
    Pref->>VM: clearViolations()
    Pref->>Svc: setConfiguration(configMap[configKey])
    Svc->>Svc: initChecker()  (new Checker, re-attach listeners)
    Svc->>CL: loadConfiguration(path/URL, PropertiesExpander)
    alt configuration valid
        CL-->>Svc: Configuration
        Svc->>Svc: checker.configure(config)
        Pref->>Svc: enable()
        Pref->>PEH: checkAllPackagesOpen(vm, svc)
        Note right of PEH: Re-checks all open packages<br/>(see diagrams 2 and 3)
    else CheckstyleException
        CL-->>Svc: throws CheckstyleException
        Svc-->>Pref: throws CheckstyleException
        Pref->>Svc: disable()
        Pref->>ED: new ErrorDialog(msg incl. Checkstyle version).show()
    end
    Pref->>SB: onConfigChanged(currentConfig)
    SB->>SB: refresh combo items and ON/OFF indicator
```

### 5. Managing configurations in Preferences

Adding, editing or deleting a configuration in **Tools → Preferences → Extensions**, followed by
pressing OK.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant BJ as BlueJ Preferences
    participant Pref as CheckstylePreferences
    participant Dlg as CheckstyleConfigFormDialog

    BJ->>Pref: getWindow()
    Pref-->>BJ: VBox (default-config combo, table, buttons)
    User->>Pref: click "Add config" / "Edit selected"
    Pref->>Dlg: new CheckstyleConfigFormDialog([name, path])
    Pref->>Dlg: showAndWait()
    User->>Dlg: enter name + path (or Browse...)
    Dlg->>Dlg: evaluateValidity()  (name not Google/Sun, both non-empty)
    User->>Dlg: Save
    Dlg-->>Pref: SimpleEntry(name, path)
    Pref->>Pref: configMap.put(name, path) (edit: remove old key first)
    Pref->>Pref: reloadUiData()
    User->>BJ: OK
    BJ->>Pref: saveValues()
    Pref->>Pref: serialize configMap without Google/Sun (Jackson)
    Pref->>BJ: setExtensionPropertyString("Checkstyle.ConfigMap", json)
    Pref->>BJ: setExtensionPropertyString("Checkstyle.DefaultConfig", default)
    Pref->>Pref: setConfig(default)
    Note right of Pref: Reloads CheckerService, re-checks open<br/>packages and notifies the status bar (see diagram 4)
```

After the settings are saved, `saveValues()` makes the selected default config the active one and
reconfigures the `CheckerService`. Changes such as a new default or an edited path for the active
config take effect immediately, without restarting BlueJ.

### 6. Showing the overview window from the Tools menu

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant BJ as BlueJ
    participant MB as CheckstyleMenuBuilder
    participant PEH as PackageEventHandler (core)
    participant AW as AuditWindow (core)

    BJ->>MB: getToolsMenuItem(package)
    MB-->>BJ: MenuItem "Show Checkstyle overview"
    User->>BJ: Tools → Show Checkstyle overview
    BJ->>MB: menu action
    MB->>PEH: showProjectWindow(package)
    PEH->>AW: show() (if a window exists for the project)
```

## Build and packaging

- Maven project, Java 21 (`maven.compiler.release`), targeting the BlueJ Extensions2 API major
  version 3 (BlueJ 6.0.0).
- `maven-shade-plugin` produces the installable fat jar `target/checkstyle4bluej-<version>.jar`
  containing Checkstyle, Jackson and BlueJ-Linting-Core. JavaFX and BlueJ's own `bluej.jar` are
  `provided` by BlueJ at runtime and are not bundled.
- `bluej:bluej` (BlueJ 6's `bluej.jar`, which contains the Extensions2 API) is resolved from the
  file-based Maven repository in `lib/`; BlueJ-Linting-Core from JitPack
  (`com.github.NTNU-IE-IIR:BlueJ-Linting-Core`).
- Releases are produced by one GitHub Actions workflow (`.github/workflows/release.yml`), run from
  `develop`; it also fast-forwards `main` to the release tag.

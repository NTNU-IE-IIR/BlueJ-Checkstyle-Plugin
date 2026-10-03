# Debugging

checkstyle4bluej is a BlueJ extension: it is loaded by BlueJ and runs **inside BlueJ's own JVM**. It cannot
be launched on its own from the IDE. To debug it, you:

1. build the plugin and install the jar into a BlueJ `extensions2` directory,
2. start BlueJ with a JDWP debug agent, and
3. attach a remote debugger (e.g. IntelliJ IDEA) to that JVM.

The steps below are written for **macOS with BlueJ 6.0.0** and have been verified there. The same
principle applies on Windows and Linux, but the paths differ (see the install table in the
[README](../README.md#installing-the-extension)) and the helper script is macOS-only.

## Prerequisites

- BlueJ 6.0.0 installed (default location: `/Applications/BlueJ.app`)
- A JDK 21 installed (`/usr/libexec/java_home -v 21` should print its path)
- Maven, running on JDK 21+

## Quick start (macOS)

Use the helper script [`tools/debugInBlueJ.sh`](../tools/debugInBlueJ.sh):

```bash
./tools/debugInBlueJ.sh                 # build, install and start BlueJ in debug mode
./tools/debugInBlueJ.sh --suspend       # BlueJ waits for the debugger before starting
./tools/debugInBlueJ.sh --no-build      # reinstall the existing jar in target/ without rebuilding
./tools/debugInBlueJ.sh --port 5006     # use another debug port (default: 5005)
```

The script:

1. runs `mvn clean package`,
2. removes any older `checkstyle4bluej-*.jar` from `BlueJ.app/Contents/Java/extensions2/` (so BlueJ
   doesn't load two versions) and copies in the newly built shaded jar,
3. starts BlueJ under JDK 21 with a JDWP agent listening on `localhost:5005`.

Then [attach IntelliJ](#attaching-intellij-idea).

If BlueJ or the JDK live elsewhere, override the defaults with environment variables:

| Variable | Default |
|---|---|
| `BLUEJ_APP` | `/Applications/BlueJ.app` |
| `BLUEJ_EXT_DIR` | `$BLUEJ_APP/Contents/Java/extensions2` |
| `BLUEJ_JAVA_HOME` | `$(/usr/libexec/java_home -v 21)` |

## Manual steps (macOS)

This is what the script does, in case you want to run the steps yourself.

### 1. Build and install the plugin

```bash
mvn clean package
cp target/checkstyle4bluej-1.1.0-SNAPSHOT.jar /Applications/BlueJ.app/Contents/Java/extensions2/
```

Use the shaded jar, not the `-original` one. Re-copy it after every code change.

### 2. Start BlueJ with a debug agent

The `BlueJ.app` launcher can't be given extra JVM options without editing its signed `Info.plist`, so
start BlueJ's main class directly with a JDK 21:

```bash
B=/Applications/BlueJ.app/Contents/Java
"$(/usr/libexec/java_home -v 21)/bin/java" \
  '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:5005' \
  -Dapple.laf.useScreenMenuBar=true \
  -cp "$B/boot.jar:$(ls $B/javafx-*.jar | paste -sd: -)" \
  bluej.Boot
```

Notes:

- Keep the quotes around the `-agentlib` option: zsh would otherwise try to expand parts of it as a
  filename pattern.
- BlueJ's JavaFX jars must be on the **classpath**. Putting them on the module path makes BlueJ fail at
  startup with an `IllegalAccessError` (`com.sun.glass.ui` is not exported).
- `WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module'` is expected and
  harmless.
- To debug `CheckstyleExtension.startup()`, use `suspend=y`: the JVM then waits for the debugger to
  attach before BlueJ starts and loads extensions.

When BlueJ is up, the terminal shows `Listening for transport dt_socket at address: 5005` and
`INFO: Starting checkstyle4bluej`.

## Attaching IntelliJ IDEA

1. **Run → Edit Configurations… → + → Remote JVM Debug**
2. Host `localhost`, port `5005` (or the port you passed with `--port`)
3. *Use module classpath*: `checkstyle4bluej`
4. Set breakpoints and start the configuration with **Debug**

### Useful breakpoints

| Where | When it's hit |
|---|---|
| `CheckstyleExtension.startup` | Extension is loaded (needs `--suspend` / `suspend=y`) |
| `CheckerService.setConfiguration` | A Checkstyle config file is loaded |
| `CheckstylePreferences.configureCheckerService` | Config switched in the status bar, or OK pressed in Preferences |
| `CheckerListener.fileStarted` / `addError` | A file is checked / each violation reported |

To step into the BlueJ-Linting-Core classes (e.g. `FilesChangeHandler.classStateChanged`,
`PackageEventHandler.openProjectWindow`), use IntelliJ's *Download Sources*; the core library publishes
a sources jar. See [ARCHITECTURE.md](ARCHITECTURE.md) for how these classes interact.

## Making changes while debugging

- **HotSwap**: while attached, **Build → Recompile** of a changed class reloads it in the running BlueJ
  if only method bodies changed.
- **Structural changes** (new/removed methods or fields, changed signatures) can't be hot-swapped:
  rebuild, reinstall the jar and restart BlueJ (`./tools/debugInBlueJ.sh` does all three).

## Logs

| Output | Where it goes |
|---|---|
| `java.util.logging` (`LOGGER` in `CheckstyleExtension`) and stdout | The terminal BlueJ was started from |
| `e.printStackTrace()` and BlueJ's own log | `~/Library/Preferences/org.bluej/bluej-debuglog.txt` |

The debug log accumulates across runs; each run starts with a `BlueJ run started: <date>` line.

Known harmless entries:

- `MismatchedInputException: No content to map due to end-of-input` from
  `CheckstylePreferences.loadValues` — on first run no user configs have been saved yet, so the stored
  JSON is empty. The exception is caught and the built-in configs are used.
- `ConnectTimeoutException: Connect to blackbox.bluej.org:443` — BlueJ's own data collection, unrelated
  to the plugin.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `no matches found: -agentlib:jdwp=...` | zsh glob expansion — quote the `-agentlib` argument. |
| `NoClassDefFoundError: javafx/application/Application` | JavaFX jars are missing from the classpath. |
| `IllegalAccessError ... com.sun.glass.ui` | JavaFX jars were put on the module path; use the classpath instead. |
| `release version 21 not supported` during the build | Maven is running on an older JDK; point `JAVA_HOME` at a JDK 21+. |
| Breakpoints are never hit | Check that the jar in `extensions2` is the one you just built, and that no other `checkstyle4bluej` jar is installed in another `extensions2` directory (user, system or project). |
| `Address already in use` on startup | Another BlueJ debug session is still running; stop it or use `--port`. |

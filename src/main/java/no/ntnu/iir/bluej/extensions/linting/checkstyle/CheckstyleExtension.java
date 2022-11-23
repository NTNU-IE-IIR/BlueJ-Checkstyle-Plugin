package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import java.net.URL;
import java.util.logging.Logger;
import no.ntnu.iir.bluej.extensions.linting.checkstyle.checker.CheckerListener;
import no.ntnu.iir.bluej.extensions.linting.checkstyle.checker.CheckerService;
import no.ntnu.iir.bluej.extensions.linting.core.handlers.FilesChangeHandler;
import no.ntnu.iir.bluej.extensions.linting.core.handlers.PackageEventHandler;
import no.ntnu.iir.bluej.extensions.linting.core.ui.AuditWindow;
import no.ntnu.iir.bluej.extensions.linting.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.extensions.linting.core.violations.ViolationManager;

/**
 * The main class representing this BlueJ extension.
 */
public class CheckstyleExtension extends Extension {
  private static final Logger LOGGER = Logger.getLogger(CheckstyleExtension.class.getName());

  /**
   * Initialization of the extension.
   * This method is called by BlueJ when the extension can start its activity.
   *
   * @param blueJ A proxy object representing the BlueJ editor
   */
  @Override
  public void startup(BlueJ blueJ) {
    LOGGER.info("Starting " + this.getName());

    RuleDefinition.setIconMapper(new CheckstyleIconMapper());
    CheckerService checkerService = new CheckerService();
    ViolationManager violationManager = new ViolationManager();

    CheckerListener checkerListener = new CheckerListener(violationManager);
    checkerService.addListener(checkerListener);

    CheckstylePreferences preferences = new CheckstylePreferences(
        blueJ,
        checkerService,
        violationManager
    );

    AuditWindow.setTitlePrefix(this.getName());

    CheckstyleStatusBar checkstyleStatusBar = new CheckstyleStatusBar(preferences);
    preferences.addConfigChangeListener(checkstyleStatusBar);
    AuditWindow.setStatusBar(checkstyleStatusBar);

    PackageEventHandler packageEventHandler = new PackageEventHandler(
        violationManager,
        checkerService
    );

    blueJ.addClassListener(new FilesChangeHandler(
        violationManager,
        checkerService
    ));
    blueJ.addPackageListener(packageEventHandler);
    blueJ.setPreferenceGenerator(preferences);
    blueJ.setMenuGenerator(new CheckstyleMenuBuilder(packageEventHandler));
  }

  @Override
  public boolean isCompatible() {
    int versionMajor = Extension.getExtensionsAPIVersionMajor();

    return (versionMajor == 3);
  }

  /**
   * Version of the extension.
   *
   * @return Version number, as major.minor.patch
   */
  @Override
  public String getVersion() {
    return this.getClass().getPackage().getImplementationVersion();
  }

  /**
   * URL where more information about the extension is available.
   *
   * @return a URL instance to the Git repository of this extension.
   */
  @Override
  public URL getURL() {
    try {
      return new URL("https://github.com/NTNU-IE-IIR/BlueJ-Checkstyle-Plugin/");
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * A short name of the extension, will be displayed in BlueJ menu/dialog.
   *
   * @return The name of the extension.
   */
  @Override
  public String getName() {
    return this.getClass().getPackage().getImplementationTitle();
  }

  /**
   * A human-readable description of the extension.
   *
   * @return Brief description of the extension.
   */
  @Override
  public String getDescription() {
    return "Checkstyle for BlueJ.";
  }
}
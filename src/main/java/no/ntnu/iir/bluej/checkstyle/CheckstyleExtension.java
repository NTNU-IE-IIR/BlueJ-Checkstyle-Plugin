package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerListener;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerService;
import no.ntnu.iir.bluej.extensions.linting.core.handlers.FilesChangeHandler;
import no.ntnu.iir.bluej.extensions.linting.core.handlers.PackageEventHandler;
import no.ntnu.iir.bluej.extensions.linting.core.ui.AuditWindow;
import no.ntnu.iir.bluej.extensions.linting.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.extensions.linting.core.violations.ViolationManager;

public class CheckstyleExtension extends Extension {
  private static final Logger LOGGER = Logger.getLogger(CheckstyleExtension.class.getName());
  
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
    blueJ.setMenuGenerator(
        new CheckstyleMenuBuilder(packageEventHandler)
    );
  }

  @Override
  public boolean isCompatible() {
    int versionMajor = Extension.getExtensionsAPIVersionMajor();
    int versionMinor = Extension.getExtensionsAPIVersionMinor();
    return (versionMajor == 3 && versionMinor == 2);
  }

  @Override
  public String getVersion() {
    return this.getClass().getPackage().getImplementationVersion();
  }

  @Override
  public URL getURL() {
    try {
      return new URL("https://github.com/NTNU-IE-IIR/BlueJ-Checkstyle-Plugin/");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String getName() {
    return this.getClass().getPackage().getImplementationTitle();
  }

  @Override
  public String getDescription() {
    return String.join(
      "", // delimiter
      "Checkstyle for BlueJ."
    );
  }
}
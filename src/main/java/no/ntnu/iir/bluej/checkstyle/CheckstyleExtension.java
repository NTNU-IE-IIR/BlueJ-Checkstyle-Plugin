package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import java.util.logging.Logger;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerListener;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerService;
import no.ntnu.iir.bluej.checkstyle.core.handlers.FilesChangeHandler;
import no.ntnu.iir.bluej.checkstyle.core.handlers.PackageEventHandler;
import no.ntnu.iir.bluej.checkstyle.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

public class CheckstyleExtension extends Extension {
  public static final Logger LOGGER = Logger.getLogger(CheckstyleExtension.class.getName());
  
  @Override
  public void startup(BlueJ blueJ) {
    LOGGER.info("Starting " + this.getName());
    
    RuleDefinition.setIconMapper(new CheckstyleIconMapper());
    CheckerService checkerService = new CheckerService();
    ViolationManager violationManager = new ViolationManager();

    CheckerListener checkerListener = new CheckerListener(violationManager);

    checkerService.addListener(checkerListener);
    
    FilesChangeHandler filesChangeHandler = new FilesChangeHandler(
        violationManager, 
        checkerService
    );

    CheckstylePreferences preferences = new CheckstylePreferences(
        blueJ, 
        checkerService, 
        violationManager
    );

    CheckstyleStatusBar checkstyleStatusBar = new CheckstyleStatusBar(preferences);
    preferences.addConfigChangeListener(checkstyleStatusBar);

    PackageEventHandler packageEventHandler = new PackageEventHandler(
        this.getName(),
        violationManager,
        checkerService,
        checkstyleStatusBar
    );

    blueJ.addClassListener(filesChangeHandler);
    blueJ.addPackageListener(packageEventHandler);
    blueJ.setPreferenceGenerator(
        new CheckstylePreferences(blueJ, checkerService, violationManager)
    );
    blueJ.setMenuGenerator(
        new CheckstyleMenuBuilder(packageEventHandler)
    );
  }

  @Override
  public boolean isCompatible() {
    return true;
  }

  @Override
  public String getVersion() {
    return this.getClass().getPackage().getImplementationVersion();
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
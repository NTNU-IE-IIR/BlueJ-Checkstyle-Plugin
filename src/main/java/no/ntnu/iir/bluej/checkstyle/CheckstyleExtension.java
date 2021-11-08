package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

import java.util.logging.Logger;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerService;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

public class CheckstyleExtension extends Extension {
  public static final Logger LOGGER = Logger.getLogger(CheckstyleExtension.class.getName());
  
  @Override
  public void startup(BlueJ blueJ) {
    LOGGER.info("Starting checkstyle4bluej");
    ViolationManager violationManager = new ViolationManager();
    CheckerService checkerService = new CheckerService(violationManager);
    checkerService.init();
    blueJ.setMenuGenerator(new CheckstyleMenuBuilder(checkerService));
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
package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import java.util.logging.Logger;
import no.ntnu.iir.bluej.checkstyle.core.handlers.AppEventHandler;
import no.ntnu.iir.bluej.checkstyle.core.ui.AuditWindow;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

public class CheckstyleExtension extends Extension {
  public static final Logger LOGGER = Logger.getLogger(CheckstyleExtension.class.getName());

  @Override
  public void startup(BlueJ blueJ) {
    LOGGER.info("Starting checkstyle4bluej");

    AuditWindow auditWindow = new AuditWindow("checkstyle4bluej");
    ViolationManager violationManager = new ViolationManager();
    violationManager.addListener(auditWindow);

    blueJ.addApplicationListener(new AppEventHandler(auditWindow));
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
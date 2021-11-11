package no.ntnu.iir.bluej.checkstyle.core.handlers;

import bluej.extensions2.event.ApplicationEvent;
import bluej.extensions2.event.ApplicationListener;
import no.ntnu.iir.bluej.checkstyle.core.ui.AuditWindow;

/**
 * Represents an AppEvent handler.
 * Responsible for handling BlueJ applications events.
 */
public class AppEventHandler implements ApplicationListener {
  private AuditWindow auditWindow;

  /**
   * Instantiates a new AppEventHandler.
   * 
   * @param auditWindow the AuditWindow instance to display.
   */
  public AppEventHandler(AuditWindow auditWindow) {
    this.auditWindow = auditWindow;
  }

  /**
   * Shows the auditWindow when blueJ is ready.
   */
  @Override
  public void blueJReady(ApplicationEvent applicationEvent) {
    this.auditWindow.show();
  }
}

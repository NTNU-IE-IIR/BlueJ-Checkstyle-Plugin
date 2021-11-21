package no.ntnu.iir.bluej.checkstyle.checker;

import bluej.extensions2.editor.TextLocation;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

/**
 * Represents a CheckerListener.
 * Responsible for adding errors to the ViolationManager, 
 * and purging old errors from the ViolationManager when a file is reprocessed.
 */
public class CheckerListener implements AuditListener {
  private ViolationManager violationManager;

  public CheckerListener(ViolationManager violationManager) {
    this.violationManager = violationManager;
  }

  /**
   * Fired when Checkstyle finds a violation in the file being processed.
   * Adds violations to the ViolationManager instance. 
   */
  @Override
  public void addError(AuditEvent auditEvent) {
    String fileName = auditEvent.getFileName();
    File file = new File(fileName);
    Violation violation = new Violation(
        auditEvent.getMessage(), 
        file, 
        new TextLocation(auditEvent.getLine(), auditEvent.getColumn())
    );

    List<Violation> violations = violationManager.getViolations(file.getName());
    if (violations != null) {
      violations.add(violation);
    } else {
      ArrayList<Violation> violationList = new ArrayList<>();
      violationList.add(violation);
      violationManager.addViolations(fileName, violationList);
    }
  }

  @Override
  public void addException(AuditEvent auditEvent, Throwable throwable) {
    // do nothing
  }

  @Override
  public void auditFinished(AuditEvent auditEvent) {
    // do nothing
  }

  @Override
  public void auditStarted(AuditEvent auditEvent) {
    // do nothing
  }

  @Override
  public void fileFinished(AuditEvent auditEvent) {
    // do nothing
  }

  /**
   * Fired when a file is starting to get processed.
   * Should delete the old entry, in order to get a new List which 
   * reflects the current state of the file.
   */
  @Override
  public void fileStarted(AuditEvent auditEvent) {
    this.violationManager.removeViolations(auditEvent.getFileName());
  }
}

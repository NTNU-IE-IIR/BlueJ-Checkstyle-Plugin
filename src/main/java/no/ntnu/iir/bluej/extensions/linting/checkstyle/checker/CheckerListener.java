package no.ntnu.iir.bluej.extensions.linting.checkstyle.checker;

import bluej.extensions2.BClass;
import bluej.extensions2.editor.TextLocation;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.iir.bluej.extensions.linting.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.extensions.linting.core.violations.Violation;
import no.ntnu.iir.bluej.extensions.linting.core.violations.ViolationManager;

/**
 * Represents a CheckerListener.
 * Responsible for adding errors to the ViolationManager,
 * and purging old errors from the ViolationManager when a file is reprocessed.
 */
public class CheckerListener implements AuditListener {
  private final ViolationManager violationManager;

  public CheckerListener(ViolationManager violationManager) {
    this.violationManager = violationManager;
  }

  /**
   * Fired when Checkstyle finds a violation in the file being processed.
   * Adds violations to the ViolationManager instance.
   */
  @Override
  public void addError(AuditEvent auditEvent) {
    Violation violation = createViolationFrom(auditEvent);
    addViolationToManager(violation, auditEvent.getFileName());
  }

  private Violation createViolationFrom(AuditEvent auditEvent) {
    RuleDefinition ruleDefinition = new RuleDefinition(
        auditEvent.getMessage(),
        auditEvent.getModuleId(),
        null,
        auditEvent.getSeverityLevel().getName(),
        null
    );
    BClass sourceClass = getSourceClass(auditEvent);
    return new Violation(
        auditEvent.getMessage(),
        sourceClass,
        new TextLocation(auditEvent.getLine(), auditEvent.getColumn()),
        ruleDefinition
    );
  }

  private BClass getSourceClass(AuditEvent auditEvent) {
    String fileName = auditEvent.getFileName();
    File file = new File(fileName);
    return this.violationManager.getBlueClass(file.getPath());
  }

  private void addViolationToManager(Violation violation, String fileName) {
    List<Violation> violations = violationManager.getViolations(fileName);
    if (violations != null) {
      violations.add(violation);
      violationManager.setViolations(fileName, violations);
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

  /**
   * This method is called when the CheckStyle audit is started.
   * Make sure that necessary data is initialized for the audit.
   *
   * @param auditEvent the event details
   */
  @Override
  public void auditStarted(AuditEvent auditEvent) {
    try {
      this.violationManager.syncBlueClassMap();
    } catch (Exception e) {
      e.printStackTrace();
    }
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

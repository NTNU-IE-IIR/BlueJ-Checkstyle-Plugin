package no.ntnu.iir.violations;

import bluej.extensions2.editor.TextLocation;
import java.io.File;

/**
 * Represents a Checkstyle Rule violation.
 * Holds details of where a violation occured, a summary of the rule violated 
 * and an optional RuleDefinition that can be shown to the end-user.
 * Should be instantiated when a violation occurs, and passed to appropriate objects.
 */
public class Violation {
  private String summary;
  private File file;
  private TextLocation location;
  private String ruleDefinition;

  /**
   * Constructs a new Violation without a RuleDefinition.
   * 
   * @param summary String containing a brief explanation of the violation
   * @param file File where the violation was found
   * @param location TextLocation of where in the file the violation was found
   */
  public Violation(String summary, File file, TextLocation location) {
    this.summary = summary;
    this.file = file;
    this.location = location;
  }

  /**
   * Constructs a new Violation with a RuleDefinition.
   * 
   * @param summary String containing a brief explanation of the violation
   * @param file File where the violation was found
   * @param location TextLocation of where in the file the violation was found
   * @param ruleDefinition RuleDefinition containing a description of the violated rule
   */
  public Violation(String summary, File file, TextLocation location, String ruleDefinition) {
    this.summary = summary;
    this.file = file;
    this.location = location;
    this.ruleDefinition = ruleDefinition;
  }

  /**
   * Returns a brief explanation of the violation.
   * 
   * @return A brief explanation of the violation.
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Returns the file where the violation was found.
   * 
   * @return A File where the violation was found.
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns the TextLocation of where the violation was found in the file.
   * 
   * @return A TextLocation of where the violation was found in the file.
   */
  public TextLocation getLocation() {
    return location;
  }


  /**
   * Returns the RuleDefinition of the violated rule.
   * 
   * @return A RuleDefinition of the violated rule.
   */
  public String getRuleDefinition() {
    return ruleDefinition;
  }
}

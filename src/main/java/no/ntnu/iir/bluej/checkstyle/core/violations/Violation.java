package no.ntnu.iir.bluej.checkstyle.core.violations;

import bluej.extensions2.BClass;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
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
  private BClass blueClass;
  private TextLocation location;
  private RuleDefinition ruleDefinition;

  /**
   * Constructs a new Violation without a RuleDefinition.
   * 
   * @param summary a String containing a brief explanation of the violation
   * @param blueClass the BlueJ Class file where the violation was found
   * @param location the TextLocation where the violation was found in the file
   */
  public Violation(String summary, BClass blueClass, TextLocation location) {
    this.summary = summary;
    this.blueClass = blueClass;
    this.location = location;
  }

  /**
   * Constructs a new Violation with a RuleDefinition.
   * 
   * @param summary a String containing a brief explanation of the violation
   * @param blueClass the BlueJ Class file where the violation was found
   * @param location the TextLocation where the violation was found in the file
   * @param ruleDefinition a RuleDefinition containing a description of the violated rule
   */
  public Violation(
      String summary, 
      BClass blueClass, 
      TextLocation location, 
      RuleDefinition ruleDefinition
  ) {
    this.summary = summary;
    this.blueClass = blueClass;
    this.location = location;
    this.ruleDefinition = ruleDefinition;
  }

  /**
   * Returns a brief explanation of the violation.
   * 
   * @return a brief explanation of the violation.
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Returns the BlueJ Class instance where the violation was found. 
   * 
   * @return the BlueJ Class instance where the violation was found
   */
  public BClass getBClass() {
    return this.blueClass;
  }

  /**
   * Returns the file where the violation was found.
   * 
   * @return the File where the violation was found
   */
  public File getFile() throws ProjectNotOpenException, PackageNotFoundException {
    return this.blueClass.getJavaFile();
  }

  /**
   * Returns the TextLocation of where the violation was found in the file.
   * 
   * @return the TextLocation where the violation was found in the file
   */
  public TextLocation getLocation() {
    return location;
  }


  /**
   * Returns the RuleDefinition of the violated rule.
   * 
   * @return the RuleDefinition of the violated rule
   */
  public RuleDefinition getRuleDefinition() {
    return ruleDefinition;
  }
}

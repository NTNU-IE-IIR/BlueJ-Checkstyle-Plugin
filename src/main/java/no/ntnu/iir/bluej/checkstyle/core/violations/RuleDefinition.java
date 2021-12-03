package no.ntnu.iir.bluej.checkstyle.core.violations;

import java.net.URL;
import no.ntnu.iir.bluej.checkstyle.core.util.IconMapper;

/**
 * Represents a linter rule definition.
 * Responsible for holding information that should be displayed for a user
 * when they want to see a more detailed description of a violated rule.
 */
public class RuleDefinition {
  private String title;
  private String ruleId;
  private String description;
  private String severity;
  private String type;

  private static IconMapper iconMapper;

  /**
   * Constructs a new RuleDefinition for linter rule.
   * 
   * @param title a string representing the title of the rule definition
   * @param ruleId a string representing the rules unique identifier
   * @param description a string describing the rule
   * @param severity a string describing the severity of the rule
   * @param type a string describing the type of rule
   */
  public RuleDefinition(
      String title, 
      String ruleId, 
      String description, 
      String severity, 
      String type
  ) {
    this.title = title;
    this.ruleId = ruleId;
    this.description = description;
    this.severity = severity;
    this.type = type;
  }

  public static void setIconMapper(IconMapper iconMapper) {
    RuleDefinition.iconMapper = iconMapper;
  }

  /**
   * Returns the title of the rule definition.
   * 
   * @return the title of the rule definition
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the unique identifier of the rule.
   * 
   * @return the unique identifier of the rule
   */
  public String getRuleId() {
    return ruleId;
  }

  /**
   * Returns the description of the rule.
   * 
   * @return the description of the rule
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the URL to the icon representing the severity, or null.
   * 
   * @return the URL to the icon representing the severity, or null
   */
  public URL getSeverityIcon() {
    return RuleDefinition.iconMapper.getIcon(this.severity);
  }

  /**
   * Returns the URL to the icon representing the type, or null.
   * 
   * @return the URL to the icon representing the type, or null
   */
  public URL getTypeIcon() {
    return RuleDefinition.iconMapper.getIcon(this.type);
  }
}

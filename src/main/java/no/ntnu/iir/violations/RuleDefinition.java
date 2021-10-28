package no.ntnu.iir.violations;

/**
 * Represents a linter rule definition.
 * Responsible for holding information that should be displayed for a user
 * when they want to see a more detailed description of a violated rule.
 */
public class RuleDefinition {
  private String title;
  private String ruleId;
  private String description;

  /**
   * Constructs a new RuleDefinition for linter rule.
   * 
   * @param title String representing the title of the rule definition.
   * @param ruleId String representing the rules unique identifier.
   * @param description String describing the rule.
   */
  public RuleDefinition(String title, String ruleId, String description) {
    this.title = title;
    this.ruleId = ruleId;
    this.description = description;
  }

  /**
   * Returns the title of the rule definition.
   * 
   * @return The title of the rule definition.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the unique identifier of the rule.
   * 
   * @return The unique identifier of the rule.
   */
  public String getRuleId() {
    return ruleId;
  }

  /**
   * Returns the description of the rule.
   * 
   * @return The description of the rule.
   */
  public String getDescription() {
    return description;
  }
}

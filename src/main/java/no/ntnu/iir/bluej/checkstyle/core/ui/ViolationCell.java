package no.ntnu.iir.bluej.checkstyle.core.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;

/**
 * Custom ListCell Factory for Violations.
 */
public class ViolationCell extends ListCell<Violation> {
  private HBox hbox = new HBox();
  private Label summaryLabel;
  private Pane pane = new Pane();
  private Button ruleButton = new Button("Open rule description");
  private Button viewButton = new Button("View in editor");
  private Violation violation;

  /**
   * Instantiates a new Violation Cell.
   */
  public ViolationCell() {
    super();

    this.summaryLabel = new Label();
    HBox.setHgrow(pane, Priority.ALWAYS);

    ruleButton.setVisible(false);
    ruleButton.setOnAction(clickEvent -> {
      // should open rule definition in a WebView
    });

    viewButton.setOnAction(clickEvent -> {
      // should open text location in editor
    });

    hbox.getChildren().addAll(this.summaryLabel, this.pane, this.ruleButton, this.viewButton);
    hbox.setSpacing(2);
  }

  /**
   * Updates the cell. 
   * Method is called by JavaFX when factory is utilized.
   */
  @Override
  protected void updateItem(Violation violation, boolean empty) {
    super.updateItem(violation, empty);
    setText(null);
    setGraphic(null);

    if (violation != null && !empty) {
      this.violation = violation;
      this.summaryLabel.setText(violation.getSummary());
      this.setGraphic(hbox);

      if (violation.getRuleDefinition() != null) {
        this.ruleButton.setVisible(true);
      }
    }
  }
}

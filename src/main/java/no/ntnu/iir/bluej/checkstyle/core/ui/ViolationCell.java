package no.ntnu.iir.bluej.checkstyle.core.ui;

import bluej.extensions2.editor.TextLocation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import no.ntnu.iir.bluej.checkstyle.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;

/**
 * Represents a ListCell Factory for Violations.
 * Responsible for creating new ListCells and layouting specifically for Violations.
 * Also handles click events for buttons within the Cell.
 */
public class ViolationCell extends ListCell<Violation> {
  private HBox hbox = new HBox();
  private Label summaryLabel;
  private Label hintLabel;
  private Violation violation;
  private ScrollPane rulePane;

  /**
   * Instantiates a new Violation Cell.
   */
  public ViolationCell(ScrollPane rulePane) {
    super();

    this.summaryLabel = new Label();
    this.hintLabel = new Label();
    this.rulePane = rulePane;
    
    hbox.getChildren().addAll(this.summaryLabel, hintLabel);
    hbox.setSpacing(2);
    this.setOnMouseClicked(this::handleMouseClick);
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
      TextLocation location = violation.getLocation();
      this.hintLabel.setText(
          String.format("[%s, %s]", location.getLine(), location.getColumn())
      );
      this.setGraphic(hbox);
      this.setTooltip(new Tooltip("Double click to view in editor"));
    }
  }

  /**
   * Responsible for handling mouseClick events on the ListCell.
   * Should show rule description (if any) in the overview window.
   * Should also highlight the issue on double-click.
   * 
   * @param mouseEvent the MouseEvent emitted when a user clicks the cell.
   */
  private void handleMouseClick(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 2) {
      // show the violation in the editor
    } else {
      // render rule description if any
      RuleDefinition definition = violation.getRuleDefinition();

      if (definition == null) {
        this.rulePane.setContent(
            new Label("The selected violation does not have a rule description...")
        );
      } else {
        // Render the description as HTML
        WebView webView = new WebView();
        webView.getEngine().loadContent(
            definition.getDescription(),
            "text/html"
        );
        // Bind width and height to parent ScrollPane
        webView.prefWidthProperty().bind(this.rulePane.widthProperty());
        webView.prefHeightProperty().bind(this.rulePane.heightProperty());
        this.rulePane.setContent(webView);
      }
    }
  }
}

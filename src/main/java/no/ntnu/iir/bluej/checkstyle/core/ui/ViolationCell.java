package no.ntnu.iir.bluej.checkstyle.core.ui;

import bluej.extensions2.editor.TextLocation;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.net.URL;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import no.ntnu.iir.bluej.checkstyle.core.editor.EditorNotifier;
import no.ntnu.iir.bluej.checkstyle.core.violations.RuleDefinition;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;

/**
 * Represents a ListCell Factory for Violations.
 * Responsible for creating new ListCells and layouting specifically for Violations.
 * Also handles click events for buttons within the Cell.
 */
public class ViolationCell extends ListCell<Violation> {
  private HBox hbox;
  private HBox iconBox;
  private Label summaryLabel;
  private Label hintLabel;
  private Violation violation;
  private WebView ruleWebView;

  /**
   * Instantiates a new Violation Cell.
   * 
   * @param ruleWebView the WebView to display a violations rule description to
   */
  public ViolationCell(WebView ruleWebView) {
    super();

    this.iconBox = new HBox();
    this.iconBox.setMinWidth(36);
    this.iconBox.setMaxWidth(36);
    this.iconBox.setAlignment(Pos.CENTER_RIGHT);
    
    this.summaryLabel = new Label();
    this.hintLabel = new Label();
    this.ruleWebView = ruleWebView;
    
    
    this.hbox = new HBox();
    this.hbox.getChildren().addAll(this.iconBox, this.summaryLabel, this.hintLabel);
    this.hbox.setSpacing(2);
    this.setStyle("-fx-padding: 3px 0px 3px 0px;");
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

      URL typeIconUrl = violation.getRuleDefinition().getTypeIcon();
      ImageView typeIconView = null;

      if (typeIconUrl != null) {
        Image typeIcon = new Image(typeIconUrl.toString());
        typeIconView = new ImageView(typeIcon);
        typeIconView.setFitHeight(16);
        typeIconView.setFitWidth(16);
      }
      
      URL severityIconUrl = violation.getRuleDefinition().getSeverityIcon();
      ImageView severityIconView = null;
      if (severityIconUrl != null) {
        Image severityIcon = new Image(severityIconUrl.toString());
        severityIconView = new ImageView(severityIcon);
        severityIconView.setFitHeight(16);
        severityIconView.setFitWidth(16);
      }

      List<ImageView> icons = Lists.newArrayList(typeIconView, severityIconView);
      Iterables.removeIf(icons, Predicates.isNull());
      this.iconBox.getChildren().setAll(icons);

      TextLocation location = violation.getLocation();
      this.hintLabel.setOpacity(0.75);
      this.hintLabel.setText(
          String.format("[%s, %s]", location.getLine(), location.getColumn())
      );
      this.setGraphic(this.hbox);
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
    // only handle clicks where a cell has content.
    if (violation != null) {      
      if (mouseEvent.getClickCount() == 2) {
        try {
          EditorNotifier.highlightLine(violation);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        // render rule description if any
        RuleDefinition definition = violation.getRuleDefinition();
  
        if (definition.getDescription() == null) {
          this.ruleWebView.getEngine().loadContent(
              "The selected violation does not have a rule description..."
          );
        } else {
          ruleWebView.getEngine().loadContent(
              definition.getDescription(),
              "text/html"
          );
        }
      }
    }
  }
}

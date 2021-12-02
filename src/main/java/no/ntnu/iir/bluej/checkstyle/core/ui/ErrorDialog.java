package no.ntnu.iir.bluej.checkstyle.core.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Wrapper class for JavaFX Alerts.
 * Responsible for simplifying creating ErrorDialogs.
 */
public class ErrorDialog extends Alert {
  /**
   * Constructs a new Alert of type Error.
   * 
   * @param content the content text to display in the dialog
   */
  public ErrorDialog(String header, String description, String content) {
    super(AlertType.ERROR);

    this.setTitle("An error occured!");
    this.setHeaderText(header);
    VBox contentVBox = new VBox();
    contentVBox.setSpacing(10);

    Label descriptionLabel = new Label(description);
    descriptionLabel.setStyle("-fx-font-size: 14px");

    TextFlow causeText = new TextFlow();
    
    Text causeLabel = new Text("Caused by:\n");
    causeLabel.setStyle("-fx-font-weight: bold");

    Text causeContent = new Text(content);

    causeText.getChildren().addAll(causeLabel, causeContent);

    contentVBox.getChildren().addAll(descriptionLabel, causeText);
    this.getDialogPane().setContent(contentVBox);
  }
}

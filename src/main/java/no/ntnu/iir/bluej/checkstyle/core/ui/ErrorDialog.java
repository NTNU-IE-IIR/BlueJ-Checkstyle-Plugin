package no.ntnu.iir.bluej.checkstyle.core.ui;

import javafx.scene.control.Alert;

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
  public ErrorDialog(String header, String content) {
    super(AlertType.ERROR);

    this.setTitle("An error occured!");
    this.setHeaderText(header);
    this.setContentText("Caused by:\n" + content);
  }
}

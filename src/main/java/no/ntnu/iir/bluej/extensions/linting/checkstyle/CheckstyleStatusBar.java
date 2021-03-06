package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Represents a Checkstyle Status Bar.
 * Should be displayed at the top of the AuditWindow.
 */
public class CheckstyleStatusBar extends HBox implements CheckstylePreferencesListener {
  private CheckstylePreferences preferences;
  private Label statusIndicator;
  private ComboBox<String> currentConfigComboBox;

  /**
   * Instantiates the status bar.
   * 
   * @param preferences preferences instance
   */
  public CheckstyleStatusBar(CheckstylePreferences preferences) {
    super();
    this.setSpacing(8);
    this.setPadding(new Insets(6));
    this.setAlignment(Pos.CENTER_LEFT);

    this.preferences = preferences;
    this.currentConfigComboBox = new ComboBox<>();
    this.currentConfigComboBox.getItems().setAll(preferences.getConfigKeys());
    this.currentConfigComboBox.getSelectionModel().select(this.preferences.getCurrentConfig());
    this.currentConfigComboBox.valueProperty().addListener(
        (obs, previous, current) -> {
          if (previous != null && current != null && !previous.equals(current)) {
            this.preferences.setConfig(current);
          }
        }
    );

    this.statusIndicator = new Label("Status: unknown");

    this.getChildren().addAll(
        this.statusIndicator,
        new Label("Current config:"),
        this.currentConfigComboBox
    );

    this.preferences.setConfig(this.preferences.getCurrentConfig());
    this.updateIndicator();
  }

  /**
   * Handles when configuration has changed.
   */
  @Override
  public void onConfigChanged(String currentConfig) {
    this.currentConfigComboBox.getItems().setAll(this.preferences.getConfigKeys());
    this.currentConfigComboBox.getSelectionModel().select(currentConfig);
    this.updateIndicator();
  }

  /**
   * Updates the status indicator depending on the Services status.
   */
  private void updateIndicator() {
    if (this.preferences.getService().isEnabled()) {
      this.statusIndicator.setText("Status: ON");
    } else {
      this.statusIndicator.setText("Status: OFF");
    }
  }
}

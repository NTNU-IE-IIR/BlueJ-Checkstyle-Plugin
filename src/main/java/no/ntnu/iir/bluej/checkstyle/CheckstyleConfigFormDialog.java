package no.ntnu.iir.bluej.checkstyle;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Represents a form dialog for defining Checkstyle config files.
 */
public class CheckstyleConfigFormDialog extends Dialog<SimpleEntry<String, String>> {
  private TextField configNameTextField;
  private TextField configPathTextField;
  private ButtonType saveButtonType;

  /**
   * Instantiates a new Dialog without predefined fields.
   * Used for adding new entries.
   */
  public CheckstyleConfigFormDialog() {
    super();
    this.setTitle("Adding a config file");
    this.setHeaderText("Add a Checkstyle Configuration file");
    this.initPane();
    this.setResultConverter(this::convertResult);
  }

  /**
   * Instantiates a new Dialog with predefined fields.
   * Used for editing existing entries.
   * 
   * @param configName the predefined configuration file name
   * @param configPath the predefined configuration file path
   */
  public CheckstyleConfigFormDialog(String configName, String configPath) {
    this();
    this.setTitle("Editing config file");
    this.setHeaderText("Editing a Checkstyle configuration file");
    this.configNameTextField.setText(configName);
    this.configPathTextField.setText(configPath);
  }

  private void initPane() {
    this.configNameTextField = new TextField();
    this.configPathTextField = new TextField();
    this.saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
    
    Button configPathBrowseButton = new Button("Browse");
    configPathBrowseButton.setOnAction(this::onBrowseConfigPath);
    
    GridPane formGridPane = new GridPane();
    formGridPane.setHgap(10);
    formGridPane.setVgap(10);
    formGridPane.setPadding(new Insets(10));
    
    formGridPane.add(new Label("Config name"), 0, 0);
    formGridPane.add(configNameTextField, 1, 0);

    formGridPane.add(new Label("Config path"), 0, 1);
    formGridPane.add(configPathTextField, 1, 1);
    formGridPane.add(configPathBrowseButton, 2, 1);

    this.getDialogPane().setContent(formGridPane);
    
    this.getDialogPane().getButtonTypes().addAll(this.saveButtonType, ButtonType.CANCEL);
  }

  private SimpleEntry<String, String> convertResult(ButtonType buttonType) {
    SimpleEntry<String, String> result = null;
    
    if (buttonType == this.saveButtonType) {
      result = new SimpleEntry<>(
        this.configNameTextField.getText(), 
        this.configPathTextField.getText()
      );
    }

    return result;
  }

  /**
   * Handles click events for the browseConfigPathButton.
   * Shows a FileChooser, and sets the textInput to the files path.
   * If a file was not chosen, it ignores setting the value.
   * 
   * @param event the event that caused this method to be called 
   */
  private void onBrowseConfigPath(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
    fileChooser.getExtensionFilters().add(extensionFilter);
    File fileChosen = fileChooser.showOpenDialog(this.getDialogPane().getScene().getWindow());

    if (fileChosen != null) {
      this.configPathTextField.setText(fileChosen.getPath());
    }
  }
}

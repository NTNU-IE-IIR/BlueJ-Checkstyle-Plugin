package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerService;

public class CheckstylePreferences implements PreferenceGenerator {
  private BlueJ blueJ;
  private GridPane pane;
  private CheckerService checkerService;
  private CheckBox useProvidedCheckBox;
  private ComboBox<String> providedConfigList;
  private HashMap<String, String> providedConfigs;
  private TextField customConfigPath;
  private Button browseConfigPathButton;

  public static final String CHECKSTYLE_USE_PROVIDED = "Checkstyle.UseProvided";
  public static final String CHECKSTYLE_CONFIG_SELECTED = "Checkstyle.SelectedConfig";
  public static final String CHECKSTYLE_CONFIG_PATH = "Checkstyle.CustomConfigPath";

  /**
   * Constructs a new PreferencesGenerator implemenetation.
   * 
   * @param blueJ the BlueJ instance to load and save preferences to
   * @param checkerService the CheckerService instance to configure on save
   */
  public CheckstylePreferences(BlueJ blueJ, CheckerService checkerService) {
    this.blueJ = blueJ;
    this.checkerService = checkerService;
    this.providedConfigs = new HashMap<>();
    this.providedConfigs.put("Google", "config/google_checks.xml");
    this.providedConfigs.put("Sun", "config/sun_checks.xml");
    this.initPane();
    this.loadValues();
  }

  /**
   * Instantiates necessary UI elements and places them in the grid.
   */
  public void initPane() {
    this.pane = new GridPane();
    this.pane.setVgap(10);
    this.pane.setHgap(5);

    ColumnConstraints labelColumn = new ColumnConstraints();
    ColumnConstraints fieldColumn = new ColumnConstraints(100, 100, Double.MAX_VALUE);
    ColumnConstraints buttonColumn = new ColumnConstraints();

    fieldColumn.setHgrow(Priority.ALWAYS);

    this.pane.getColumnConstraints().addAll(labelColumn, fieldColumn, buttonColumn);

    this.useProvidedCheckBox = new CheckBox();
    this.useProvidedCheckBox.setOnAction(this::onUseProvidedToggle);

    this.providedConfigList = new ComboBox<>();
    this.providedConfigList.getItems().addAll(this.providedConfigs.keySet());

    pane.add(new Label("Use provided configs"), 0, 0);
    pane.add(useProvidedCheckBox, 1, 0);

    pane.add(new Label("Select a provided config"), 0, 1);
    pane.add(providedConfigList, 1, 1);

    this.customConfigPath = new TextField();

    this.browseConfigPathButton = new Button("Browse");
    this.browseConfigPathButton.setOnAction(this::onBrowseConfigPath);

    pane.add(new Label("Custom config location"), 0, 2);
    pane.add(this.customConfigPath, 1, 2);
    pane.add(this.browseConfigPathButton, 2, 2);

  }

  @Override
  public Pane getWindow() {
    return this.pane;
  }

  /**
   * Loads Extension properties from BlueJs internal state.
   */
  @Override
  public void loadValues() {
    this.useProvidedCheckBox.setSelected(
        Boolean.parseBoolean(this.blueJ.getExtensionPropertyString(CHECKSTYLE_USE_PROVIDED, "true"))
    );

    this.providedConfigList.setValue(
        this.blueJ.getExtensionPropertyString(CHECKSTYLE_CONFIG_SELECTED, "Google")
    );

    this.customConfigPath.setText(
        this.blueJ.getExtensionPropertyString(CHECKSTYLE_CONFIG_PATH, "")
    );

    // make UI evaluate what fields should be active on load
    this.onUseProvidedToggle(null);
    this.configureCheckerService();
  }

  /**
   * Saves Extension properties to BlueJs internal state and configures the CheckerService.
   */
  @Override
  public void saveValues() {
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_USE_PROVIDED, String.valueOf(this.useProvidedCheckBox.isSelected())
    );
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_CONFIG_SELECTED, this.providedConfigList.getValue()
    );
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_CONFIG_PATH, this.customConfigPath.getText()
    );
  }

  private void configureCheckerService() {
    String configUri = "";

    if (this.useProvidedCheckBox.isSelected()) {
      String configName = this.providedConfigList.getValue();
      String configPath = this.providedConfigs.get(configName);
      configUri = this.getClass().getClassLoader().getResource(configPath).toString();
    } else {
      configUri = this.customConfigPath.getText();  
    }

    try {
      this.checkerService.setConfiguration(configUri);
    } catch (CheckstyleException e) {
      // TODO: Show a dialog telling the user that the config file does not exist.
      System.out.println("unable to open file");
    }
  }

  private void onUseProvidedToggle(ActionEvent event) {
    if (useProvidedCheckBox.isSelected()) {
      this.providedConfigList.setDisable(false);
      this.customConfigPath.setDisable(true);
      this.browseConfigPathButton.setDisable(true);
    } else {
      this.providedConfigList.setDisable(true);
      this.customConfigPath.setDisable(false);
      this.browseConfigPathButton.setDisable(false);
    }
  }

  private void onBrowseConfigPath(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
    fileChooser.getExtensionFilters().add(extensionFilter);
    File fileChosen = fileChooser.showOpenDialog(this.pane.getScene().getWindow());

    if (fileChosen != null) {
      this.customConfigPath.setText(fileChosen.getPath());
    }
  }

}

package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BPackage;
import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import no.ntnu.iir.bluej.checkstyle.core.handlers.PackageEventHandler;
import no.ntnu.iir.bluej.checkstyle.core.ui.ErrorDialog;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

/**
 * Represents a Preferences class.
 * Responsible for loading and saving preferences utilizing BlueJ internals.
 * Also responsible for generating the configuration options in the Preferences tab in BlueJ.
 */
public class CheckstylePreferences implements PreferenceGenerator {
  private BlueJ blueJ;
  private CheckerService checkerService;
  private ViolationManager violationManager;
  private GridPane pane;
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
  public CheckstylePreferences(
      BlueJ blueJ, 
      CheckerService checkerService, 
      ViolationManager violationManager
  ) {
    this.blueJ = blueJ;
    this.checkerService = checkerService;
    this.violationManager = violationManager;
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

  /**
   * Returns the Window that should be rendered to the BlueJ preferences.
   * 
   * @return the Window that should be rendered to the BlueJ preferences
   */
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

  /**
   * Configures the CheckerService to use the user defined preferences.
   */
  private void configureCheckerService() {
    String configUri = "";

    if (this.useProvidedCheckBox.isSelected()) {
      String configName = this.providedConfigList.getValue();
      String configPath = this.providedConfigs.get(configName);
      configUri = this.getClass().getClassLoader().getResource(configPath).toString();
    } else {
      configUri = this.customConfigPath.getText();  
    }

    this.violationManager.clearViolations();

    try {
      this.checkerService.setConfiguration(configUri);
      this.checkerService.enable();
      PackageEventHandler.checkAllPackagesOpen(this.violationManager, this.checkerService);
    } catch (CheckstyleException e) {
      this.checkerService.disable();
      ErrorDialog errorDialog = new ErrorDialog(
          "The set Checkstyle configuration was invalid, checking is disabled.",
          "Disabled checking to prevent errors.\n" + e.getMessage()
      );
      errorDialog.show();
    }
  }

  /**
   * Handles toggle events for the useProvidedCheckBox.
   * Disables inputs based on the selection state.
   * 
   * @param event the event that caused this method to be called
   */
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
    File fileChosen = fileChooser.showOpenDialog(this.pane.getScene().getWindow());

    if (fileChosen != null) {
      this.customConfigPath.setText(fileChosen.getPath());
    }
  }

}

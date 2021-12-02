package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
  private String currentConfig;
  private CheckerService checkerService;
  private ViolationManager violationManager;
  private VBox pane;
  private ComboBox<String> defaultConfigComboBox;
  private HashMap<String, String> configMap; // (config name, config path)
  private TextField addConfigPathInput;
  private TableView<Entry<String, String>> tableView;
  private ObjectMapper objectMapper;
  
  public static final String CHECKSTYLE_DEFAULT_CONFIG = "Checkstyle.DefaultConfig";
  public static final String CHECKSTYLE_CONFIG_MAP = "Checkstyle.ConfigMap";
  private static final String CHECKSTYLE_BUILTIN_GOOGLE = "Google";
  private static final String CHECKSTYLE_BUILTIN_SUN = "Sun";

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
    this.configMap = new HashMap<>();
    this.objectMapper = new ObjectMapper();
    this.initPane();
    this.loadValues();
  }

  /**
   * Instantiates necessary UI elements and places them in the wrapping pane.
   * The pane holds all the UI elements that will be shown in the Preferences tab.
   */
  public void initPane() {
    this.pane = new VBox();
    this.pane.setSpacing(10);

    this.defaultConfigComboBox = new ComboBox<>();
    HBox defaultConfigHBox = new HBox();
    defaultConfigHBox.setAlignment(Pos.CENTER_LEFT);
    defaultConfigHBox.setSpacing(5);
    defaultConfigHBox.getChildren().addAll(
        new Label("Select a default config"),
        this.defaultConfigComboBox
    );

    this.addConfigPathInput = new TextField();
    this.addConfigPathInput.promptTextProperty().set("Config file path");

    Button browseConfigPathButton = new Button("Browse");
    browseConfigPathButton.setOnAction(this::onBrowseConfigPath);

    this.tableView = new TableView<>();

    TableColumn<Entry<String, String>, String> configNameColumn = new TableColumn<>("Config name");
    configNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    configNameColumn.setCellValueFactory(param ->
        new SimpleStringProperty(param.getValue().getKey())
    );

    // fix the width of the column
    configNameColumn.setMinWidth(115);
    configNameColumn.setMaxWidth(115);

    TableColumn<Entry<String, String>, String> configPathColumn = new TableColumn<>("Config path");
    configPathColumn.setCellFactory(TextFieldTableCell.forTableColumn());

    // custom cell factory for hiding paths for built-in config files
    configPathColumn.setCellValueFactory(param -> {
      SimpleStringProperty value = null;
      if (param.getValue().getKey().equals(CHECKSTYLE_BUILTIN_GOOGLE)
          || param.getValue().getKey().equals(CHECKSTYLE_BUILTIN_SUN)) {
        value = new SimpleStringProperty("(built-in)");
      } else {
        value = new SimpleStringProperty(param.getValue().getValue());
      }
      return value;
    });

    // fix the width of the column
    configPathColumn.setMinWidth(400);
    configPathColumn.setMaxWidth(400);

    this.tableView.getColumns().add(configNameColumn);
    this.tableView.getColumns().add(configPathColumn);
    this.tableView.setMaxHeight(200);

    this.configMap.entrySet().forEach(this.tableView.getItems()::add);
    Button addButton = new Button("Add config");
    addButton.setOnAction(event -> {
      CheckstyleConfigFormDialog dialog = new CheckstyleConfigFormDialog();
      dialog.showAndWait();

      SimpleEntry<String, String> result = dialog.getResult();

      if (result != null) {
        this.configMap.put(result.getKey(), result.getValue());
        this.reloadUiData();
      }
    });

    Button editButton = new Button("Edit selected");
    editButton.setOnAction(event -> {
      Entry<String, String> selected = this.tableView.getSelectionModel().getSelectedItem();
      CheckstyleConfigFormDialog dialog = new CheckstyleConfigFormDialog(
          selected.getKey(),
          selected.getValue()
      );

      dialog.showAndWait();

      SimpleEntry<String, String> result = dialog.getResult();

      if (result != null) {
        this.configMap.remove(selected.getKey());
        this.configMap.put(result.getKey(), result.getValue());
        this.reloadUiData();
      }
    });

    Button deleteButton = new Button("Delete selected");
    deleteButton.setOnAction(event -> {
      Entry<String, String> selected = this.tableView.getSelectionModel().getSelectedItem();
      this.configMap.remove(selected.getKey());
      this.reloadUiData();
    });

    // default buttons to be disabled
    deleteButton.setDisable(true);
    editButton.setDisable(true);

    // handle disable edit/delete buttons for builtin config files
    this.tableView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldSelection, newSelection) -> {
          deleteButton.setDisable(true);
          editButton.setDisable(true);
          if (newSelection != null) {
            String configKey = newSelection.getKey();
            if (!configKey.equals(CHECKSTYLE_BUILTIN_GOOGLE) 
                && !configKey.equals(CHECKSTYLE_BUILTIN_SUN)) {
              deleteButton.setDisable(false);
              editButton.setDisable(false);
            }
          }
        }
    );

    HBox actionHBox = new HBox();
    actionHBox.getChildren().addAll(addButton, editButton, deleteButton);
    actionHBox.setSpacing(5);

    pane.getChildren().add(defaultConfigHBox);
    pane.getChildren().add(this.tableView);
    pane.getChildren().add(actionHBox);
  }

  /**
   * Reloads UI elements that depend on data from the HashMap.
   * Should be called when a change is made to update to the latest source of truth.
   */
  private void reloadUiData() {
    String selectedPre = this.defaultConfigComboBox.getSelectionModel().getSelectedItem();
    this.defaultConfigComboBox.getItems().setAll(this.configMap.keySet());
    if (this.configMap.containsKey(selectedPre)) {
      this.defaultConfigComboBox.getSelectionModel().select(selectedPre);
    } else {
      // set the first key in the set to be default (in case removed was selected)
      String firstKey = this.configMap.keySet().iterator().next();
      this.defaultConfigComboBox.getSelectionModel().select(firstKey);
    }
    this.tableView.getItems().setAll(this.configMap.entrySet());
  }

  /**
   * Returns a set of the keys to all configuration file references.
   * 
   * @return a set of the keys to all configuration file references
   */
  public Set<String> getConfigKeys() {
    return this.configMap.keySet();
  }

  /**
   * Sets the current configuration of Checkstyle and configures the service.
   * 
   * @param configKey the configuration key to fetch path from
   */
  public void setConfig(String configKey) {
    if (this.configMap.containsKey(configKey)) {
      this.currentConfig = configKey;
      this.configureCheckerService();
    } else {
      throw new IllegalArgumentException(
        "Could not find a config with key: " + configKey
      );
    }
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
    String configJsonString = this.blueJ.getExtensionPropertyString(
        CHECKSTYLE_CONFIG_MAP, // key
        ""                     // default
    );

    try {
      TypeReference<HashMap<String, String>> typeReference = 
          new TypeReference<HashMap<String, String>>() {};

      this.configMap = this.objectMapper.readValue(configJsonString, typeReference);
    } catch (Exception e) {
      this.configMap.put(
          CHECKSTYLE_BUILTIN_GOOGLE, 
          this.getClass().getClassLoader().getResource("config/google_checks.xml").toString()
      );

      this.configMap.put(
          CHECKSTYLE_BUILTIN_SUN, 
          this.getClass().getClassLoader().getResource("config/sun_checks.xml").toString()
      );
    }

    this.defaultConfigComboBox.setValue(
        this.blueJ.getExtensionPropertyString(CHECKSTYLE_DEFAULT_CONFIG, CHECKSTYLE_BUILTIN_GOOGLE)
    );
    
    this.currentConfig = this.defaultConfigComboBox.getValue();
    this.reloadUiData();
    this.configureCheckerService();
  }

  /**
   * Saves Extension properties to BlueJs internal state and configures the CheckerService.
   */
  @Override
  public void saveValues() {
    String configMapAsString = "";
    try {
      configMapAsString = this.objectMapper.writeValueAsString(this.configMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_CONFIG_MAP, 
        configMapAsString
    ); 
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_DEFAULT_CONFIG, this.defaultConfigComboBox.getValue()
    );
  }

  /**
   * Configures the CheckerService to use the user defined preferences.
   */
  private void configureCheckerService() {
    String configUri = this.configMap.get(this.currentConfig);

    this.violationManager.clearViolations();

    try {
      this.checkerService.setConfiguration(configUri);
      this.checkerService.enable();
      PackageEventHandler.checkAllPackagesOpen(this.violationManager, this.checkerService);
    } catch (CheckstyleException e) {
      this.checkerService.disable();
      ErrorDialog errorDialog = new ErrorDialog(
          "The set Checkstyle configuration was invalid, checking is disabled.",
          "Disabled checking to prevent errors.",
          e.getMessage()
      );
      errorDialog.show();
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
      this.addConfigPathInput.setText(fileChosen.getPath());
    }
  }

}

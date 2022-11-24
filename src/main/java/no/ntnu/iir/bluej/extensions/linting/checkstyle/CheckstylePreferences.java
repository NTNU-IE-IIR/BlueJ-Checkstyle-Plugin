package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import no.ntnu.iir.bluej.extensions.linting.checkstyle.checker.CheckerService;
import no.ntnu.iir.bluej.extensions.linting.core.checker.ICheckerService;
import no.ntnu.iir.bluej.extensions.linting.core.handlers.PackageEventHandler;
import no.ntnu.iir.bluej.extensions.linting.core.ui.ErrorDialog;
import no.ntnu.iir.bluej.extensions.linting.core.violations.ViolationManager;

/**
 * Represents a Preferences class.
 * Responsible for loading and saving preferences utilizing BlueJ internals.
 * Also responsible for generating the configuration options in the Preferences tab in BlueJ.
 * TODO - the word "also" here is a hint that the class has too many responsibilities
 */
public class CheckstylePreferences implements PreferenceGenerator {
  private final BlueJ blueJ;
  private String currentConfig;
  private final CheckerService checkerService;
  private final ViolationManager violationManager;
  private VBox pane;
  private ComboBox<String> defaultConfigComboBox;
  private HashMap<String, String> configMap; // (config name, config path)
  // TODO - is this field used in any way? The value is set, but is it added to the UI somewhere?
  private TextField addConfigPathInput;
  private TableView<Entry<String, String>> tableView;
  private final ObjectMapper objectMapper;
  private final List<CheckstylePreferencesListener> listeners;
  private Properties pomProperties;
  
  private static final String CHECKSTYLE_DEFAULT_CONFIG = "Checkstyle.DefaultConfig";
  private static final String CHECKSTYLE_CONFIG_MAP = "Checkstyle.ConfigMap";
  private static final String CHECKSTYLE_BUILTIN_GOOGLE = "Google";
  private static final String CHECKSTYLE_BUILTIN_SUN = "Sun";

  private static final Logger LOGGER = Logger.getLogger(
      CheckstylePreferences.class.getName()
  );

  /**
   * Constructs a new PreferencesGenerator implementation.
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
    this.listeners = new ArrayList<>();
    this.initPane();
    this.loadValues();
  }

  /**
   * Instantiates necessary UI elements and places them in the wrapping pane.
   * The pane holds all the UI elements that will be shown in the Preferences tab.
   */
  public void initPane() {
    createConfigPathInput();
    createBrowseButton();
    createMainPane();
    ObservableList<Node> paneChildren = this.pane.getChildren();
    paneChildren.addAll(
        createDefaultConfigComboBox(),
        createTableView(),
        createActionBox()
    );
  }

  private void createConfigPathInput() {
    this.addConfigPathInput = new TextField();
    this.addConfigPathInput.promptTextProperty().set("Config file path");
  }

  private void createBrowseButton() {
    // TODO - is this button used anywhere?
    Button browseConfigPathButton = new Button("Browse");
    browseConfigPathButton.setOnAction(this::onBrowseConfigPath);
  }

  private void createMainPane() {
    this.pane = new VBox();
    this.pane.setSpacing(10);
  }

  private TableView<Entry<String, String>> createTableView() {
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

    this.tableView = new TableView<>();
    this.tableView.getColumns().add(configNameColumn);
    this.tableView.getColumns().add(configPathColumn);
    this.tableView.setMaxHeight(200);
    this.configMap.entrySet().forEach(this.tableView.getItems()::add);
    return this.tableView;
  }

  /**
   * Create rules for disabling the editing and deletion buttons when a built-in item is selected.
   * @param editButton The "Edit" button
   * @param deleteButton The "Delete" button
   */
  private void disableButtonsForBuiltInConfigs(Button editButton, Button deleteButton) {
    this.tableView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldSelection, newSelection) -> {
          boolean editingDisabled = isBuiltInItem(newSelection);
          deleteButton.setDisable(editingDisabled);
          editButton.setDisable(editingDisabled);
        }
    );
  }

  /**
   * Check whether the provided selection contains a built-in configuration item.
   * @param selection A selection item. Null when no item selected.
   * @return True when the selected item is a built-in configuration item, false otherwise.
   *     Also returns false when no item is selected.
   */
  private boolean isBuiltInItem(Entry<String, String> selection) {
    boolean isBuiltIn = false;
    if (selection != null) {
      String configKey = selection.getKey();
      isBuiltIn = configKey.equals(CHECKSTYLE_BUILTIN_GOOGLE)
          || configKey.equals(CHECKSTYLE_BUILTIN_SUN);
    }
    return isBuiltIn;
  }

  private Button createAddButton() {
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
    return addButton;
  }

  private Button createEditButton() {
    Button editButton = new Button("Edit selected");
    editButton.setDisable(true);

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

    return editButton;
  }

  private Button createDeleteButton() {
    Button deleteButton = new Button("Delete selected");
    deleteButton.setOnAction(event -> {
      Entry<String, String> selected = this.tableView.getSelectionModel().getSelectedItem();
      this.configMap.remove(selected.getKey());
      this.reloadUiData();
    });

    // default buttons to be disabled
    deleteButton.setDisable(true);
    return deleteButton;
  }

  private HBox createActionBox() {
    Button addButton = createAddButton();
    Button editButton = createEditButton();
    Button deleteButton = createDeleteButton();
    disableButtonsForBuiltInConfigs(editButton, deleteButton);
    HBox actionBox = new HBox();
    actionBox.getChildren().addAll(addButton, editButton, deleteButton);
    actionBox.setSpacing(5);
    return actionBox;
  }


  /**
   * Create a ComboBox for the default configuration, wrap it in an HBox.
   * @return HBox pane wrapping the ComboBox
   */
  private HBox createDefaultConfigComboBox() {
    this.defaultConfigComboBox = new ComboBox<>();
    HBox defaultConfigBox = new HBox();
    defaultConfigBox.setAlignment(Pos.CENTER_LEFT);
    defaultConfigBox.setSpacing(5);
    defaultConfigBox.getChildren().addAll(
        new Label("Select a default config"),
        this.defaultConfigComboBox
    );
    return defaultConfigBox;
  }

  /**
   * Reloads UI elements that depend on data from the HashMap.
   * Should be called when a change is made to update to the latest source of truth.
   */
  private void reloadUiData() {
    String selectedPre = this.defaultConfigComboBox.getSelectionModel().getSelectedItem();
    this.defaultConfigComboBox.getItems().setAll(this.configMap.keySet());
    if (this.configMap.get(selectedPre) != null) {
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
    this.currentConfig = configKey;
    this.configureCheckerService();
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
      e.printStackTrace();
    }

    // load provided configs
    this.configMap.put(
        CHECKSTYLE_BUILTIN_GOOGLE, 
        this.getClass().getClassLoader().getResource("config/google_checks.xml").toString()
    );

    this.configMap.put(
        CHECKSTYLE_BUILTIN_SUN, 
        this.getClass().getClassLoader().getResource("config/sun_checks.xml").toString()
    );
    
    this.defaultConfigComboBox.setValue(
        this.blueJ.getExtensionPropertyString(CHECKSTYLE_DEFAULT_CONFIG, CHECKSTYLE_BUILTIN_GOOGLE)
    );
    
    if (this.currentConfig == null) {
      this.currentConfig = this.defaultConfigComboBox.getValue();
    }

    this.reloadUiData();

    try {
      InputStream inputStream = this.getClass()
          .getClassLoader().getResourceAsStream("config/pom.properties");
      Properties checkstyleProps = new Properties();
      checkstyleProps.load(inputStream);
      this.pomProperties = checkstyleProps;
    } catch (Exception e) {
      // should not happen, only in case file/resource does not exist.
      e.printStackTrace(); 
    }
  }

  /**
   * Saves Extension properties to BlueJs internal state and configures the CheckerService.
   */
  @Override
  public void saveValues() {
    String configMapAsString = copyConfigWithoutBuiltIn();
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_CONFIG_MAP,
        configMapAsString
    );
    this.blueJ.setExtensionPropertyString(
        CHECKSTYLE_DEFAULT_CONFIG,
        this.defaultConfigComboBox.getValue()
    );

    this.notifyListeners();
  }

  /**
   * Create a copy of the configuration, skip the built-in values.
   * @return Copy of the configuration as a JSON string
   */
  private String copyConfigWithoutBuiltIn() {
    String configMapAsString = "";
    try {
      // Copy everything but the provided configs
      HashMap<String, String> copy = new HashMap<>(this.configMap);
      copy.remove(CHECKSTYLE_BUILTIN_GOOGLE);
      copy.remove(CHECKSTYLE_BUILTIN_SUN);
      configMapAsString = this.objectMapper.writeValueAsString(copy);
    } catch (Exception e) {
      LOGGER.severe("Could not copy configuration: " + e.getMessage());
    }
    return configMapAsString;
  }

  /**
   * Configures the CheckerService to use the user defined preferences.
   */
  private void configureCheckerService() {
    String configUri = this.configMap.get(this.currentConfig);

    this.violationManager.clearViolations();

    try {
      this.checkerService.configure(configUri);
      this.checkerService.enable();
      PackageEventHandler.checkAllPackagesOpen(this.violationManager, this.checkerService);
    } catch (CheckstyleException e) {
      this.checkerService.disable();
      ErrorDialog errorDialog = new ErrorDialog(
          "The set Checkstyle configuration was invalid, checking is disabled.",
          "Please make sure the selected configuration file exists and is compatible\n"
          + "with version " + pomProperties.getProperty("checkstyle.version") + " of Checkstyle.",
          e.getMessage()
      );
      errorDialog.show();
    }

    this.notifyListeners();
  }

  /**
   * Handles click events for the browseConfigPathButton.
   * Shows a FileChooser, and sets the textInput to the file path.
   * If a file was not chosen, it ignores setting the value.
   * 
   * @param event the event that caused this method to be called \n
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

  /**
   * Returns the CheckerService configured by this class.
   * 
   * @return the CheckerService configured by this class
   */
  public ICheckerService getService() {
    return this.checkerService;
  }

  /**
   * Returns the current configuration.
   * 
   * @return the current configuration
   */
  public String getCurrentConfig() {
    return this.currentConfig;
  }

  /**
   * Adds a config change listener to the list of listeners.
   * 
   * @param listener the listener to add to the list of listeners
   */
  public void addConfigChangeListener(CheckstylePreferencesListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes a config change listener to the list of listeners.
   *
   * @param listener the listener to remove from the list of listeners
   */
  public void removeConfigChangeListener(CheckstylePreferencesListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Helper method for notifying all the config change listeners.
   */
  private void notifyListeners() {
    this.listeners.forEach(listener -> 
        listener.onConfigChanged(this.currentConfig)
    );
  }
 
}

package no.ntnu.iir.bluej.checkstyle.core.ui;

import bluej.extensions2.BPackage;
import bluej.extensions2.ProjectNotOpenException;
import java.util.HashMap;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationListener;

/**
 * Represents a GUI Window responsible for displaying linter violations.
 * This is the main window for the Linter extensions.
 * Listens to the ViolationListener, and updates the GUI when violations change.
 */
public class AuditWindow extends Stage implements ViolationListener {
  private VBox vbox;
  private BPackage bluePackage;
  private String projectDirectory;
  private ScrollPane rulePane;

  /**
   * Constructs a new AuditWindow. 
   * 
   * @param windowTitle the Title of the Window.
   */
  public AuditWindow(
      String windowTitle, 
      BPackage bluePackage, 
      String projectDirectory
  ) throws ProjectNotOpenException {
    super();

    this.bluePackage = bluePackage;
    this.projectDirectory = projectDirectory;

    String formattedTitle = String.format(
        "%s - %s", 
        windowTitle, 
        bluePackage.getProject().getName()
    );

    this.setTitle(formattedTitle);
    this.initScene();
  }
  
  private void initScene() {
    this.vbox = new VBox();
    this.vbox.getChildren().add(
        new Label("No violations found in this project")
    );

    ScrollPane violationsPane = new ScrollPane();
    violationsPane.setContent(this.vbox);
    violationsPane.setFitToWidth(true);

    this.rulePane = new ScrollPane();
    this.rulePane.setContent(new Label("Select a rule to see it's explanation here..."));

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.VERTICAL);
    splitPane.getItems().addAll(violationsPane, this.rulePane);

    Scene scene = new Scene(splitPane, 750, 500);
    this.setScene(scene);
  }

  @Override
  public void onViolationsChanged(HashMap<String, List<Violation>> violationsMap) {
    this.vbox.getChildren().clear();
    violationsMap.forEach((fileName, violations) -> {
      // only add to window if it's source is from the correct project
      if (fileName.startsWith(this.projectDirectory)) {
        ListView<Violation> violationList = new ListView<>();
        violationList.setCellFactory(violation -> new ViolationCell(this.bluePackage, this.rulePane));
  
        violations.forEach(violationList.getItems()::add);
  
        // set list height to its estimated height to hide empty rows
        violationList
          .prefHeightProperty()
          .bind(Bindings.size(violationList.getItems()).multiply(24));

        String paneTitle = String.format("%s (%s violations)", fileName, violations.size());
        TitledPane pane = new TitledPane(paneTitle, violationList);
        this.vbox.getChildren().add(pane);
      }
    });
  }
}

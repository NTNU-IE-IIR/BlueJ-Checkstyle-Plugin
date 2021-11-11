package no.ntnu.iir.bluej.checkstyle.core.ui;

import java.util.HashMap;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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

  /**
   * Constructs a new AuditWindow. 
   * 
   * @param windowTitle the Title of the Window.
   */
  public AuditWindow(String windowTitle) {
    super();

    this.setTitle(windowTitle);
    this.initScene();
  }
  
  private void initScene() {
    ScrollPane scrollPane = new ScrollPane();
    this.vbox = new VBox();
    scrollPane.setContent(this.vbox);
    scrollPane.setFitToWidth(true);
    Scene scene = new Scene(scrollPane, 600, 300);
    this.setScene(scene);
  }

  @Override
  public void onViolationsChanged(HashMap<String, List<Violation>> violationsMap) {
    this.vbox.getChildren().clear();
    violationsMap.forEach((fileName, violations) -> {
      ListView<Violation> violationList = new ListView<>();
      violationList.setCellFactory(violation -> new ViolationCell());

      violations.forEach(violationList.getItems()::add);

      String paneTitle = String.format("%s (%s violations)", fileName, violations.size());
      TitledPane pane = new TitledPane(paneTitle, violationList);
      this.vbox.getChildren().add(pane);
    });
  }
}

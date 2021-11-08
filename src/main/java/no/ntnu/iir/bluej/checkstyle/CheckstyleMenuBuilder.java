package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BClass;
import bluej.extensions2.MenuGenerator;
import java.util.Arrays;
import javafx.scene.control.MenuItem;
import no.ntnu.iir.bluej.checkstyle.checker.CheckerService;

/**
 * Represents a MenuBuilder.
 * Responsible for building Menus for different contexts.
 */
public class CheckstyleMenuBuilder extends MenuGenerator {
  private CheckerService checkerService;

  public CheckstyleMenuBuilder(CheckerService checkerService) {
    this.checkerService = checkerService;
  }
  
  /**
   * Adds a MenuItem to the Class right click context, allowing you to check a single class file.
   */
  @Override
  public MenuItem getClassMenuItem(BClass blueJClass) {
    MenuItem menuItem = new MenuItem("Check code with Checkstyle");

    menuItem.setOnAction(event -> {
      try {
        this.checkerService.checkCode(
            Arrays.asList(blueJClass.getJavaFile()), 
            "utf-8"
        );
      } catch (Exception e) {
        // TODO: Show an error message/dialog to the user to let them know something is wrong.
        e.printStackTrace();
      }
    });

    return menuItem;
  }
}
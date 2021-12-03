package no.ntnu.iir.bluej.checkstyle;

import bluej.extensions2.BPackage;
import bluej.extensions2.MenuGenerator;
import javafx.scene.control.MenuItem;
import no.ntnu.iir.bluej.checkstyle.core.handlers.PackageEventHandler;

public class CheckstyleMenuBuilder extends MenuGenerator {
  private PackageEventHandler packageEventHandler;

  public CheckstyleMenuBuilder(PackageEventHandler packageEventHandler) {
    this.packageEventHandler = packageEventHandler;
  }

  @Override
  public MenuItem getToolsMenuItem(BPackage bluePackage) {
    MenuItem menuItem = new MenuItem("Show Checkstyle overview");
    menuItem.setOnAction(event -> packageEventHandler.showProjectWindow(bluePackage));
    return menuItem;
  }
}

package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import java.net.URL;
import java.util.HashMap;
import no.ntnu.iir.bluej.extensions.linting.core.util.IconMapper;

/**
 * Represents a IconMapper implementation for Checkstyle.
 * Responsible for mapping String values to an icon URL.
 */
public class CheckstyleIconMapper implements IconMapper {
  private HashMap<String, URL> iconMap;

  /**
   * Instantiates the IconMapper. 
   * Responsible for loading/mapping key-value pairs to the HashMap.
   */
  public CheckstyleIconMapper() {
    this.iconMap = new HashMap<>();
    this.iconMap.put(
        "warning", 
        this.getClass().getClassLoader().getResource("images/warning.png")
    );
    this.iconMap.put(
        "error",
        this.getClass().getClassLoader().getResource("images/error.png")
    );
  }

  /**
   * Returns the URL of the icon mapped to the input name key.
   * 
   * @return the URL of the icon mapped to the input name key
   */
  @Override
  public URL getIcon(String name) {
    return this.iconMap.get(name);
  }
}

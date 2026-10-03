package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds Checkstyle configuration files provided by a BlueJ installation or user.
 * Any *.xml file placed in a "checkstyle4bluej" folder inside one of BlueJ's
 * extensions2 directories is loaded automatically, named after the file
 * (e.g. "school_checks.xml" becomes "school_checks").
 * This lets an installation package ship a Checkstyle configuration without
 * any setup by the user.
 */
public final class ProvidedConfigs {
  /** Name of the folder (inside an extensions2 directory) to load configs from. */
  public static final String FOLDER_NAME = "checkstyle4bluej";

  /** Name of the provided config that is used as default if the user has not chosen one. */
  public static final String DEFAULT_CONFIG_NAME = "default_checks";

  private static final String XML_EXTENSION = ".xml";

  private ProvidedConfigs() {
  }

  /**
   * Finds the provided configuration files in the given extensions2 directories.
   * Directories that do not exist are ignored. If more than one directory contains
   * a file with the same name, the one in the later directory is used.
   *
   * @param extensionDirs the extensions2 directories to search, in increasing priority
   *
   * @return a map of config names to absolute file paths, sorted by name within each directory
   */
  public static Map<String, String> find(List<File> extensionDirs) {
    Map<String, String> configs = new LinkedHashMap<>();

    for (File extensionDir : extensionDirs) {
      File[] files = new File(extensionDir, FOLDER_NAME).listFiles(
          file -> file.isFile() && file.getName().toLowerCase().endsWith(XML_EXTENSION)
      );

      if (files != null) {
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
          configs.put(configName(file), file.getAbsolutePath());
        }
      }
    }

    return configs;
  }

  /**
   * Chooses which config to use as the default.
   * Uses the saved default if it still exists, otherwise the provided
   * "default_checks" config if there is one, otherwise the fallback.
   *
   * @param savedDefault the default config saved in the preferences, or null if none
   * @param availableConfigs the names of all configs that can be used
   * @param fallback the config to use if no other default applies
   *
   * @return the name of the config to use as default
   */
  public static String chooseDefault(
      String savedDefault,
      Set<String> availableConfigs,
      String fallback
  ) {
    String chosen = fallback;

    if (savedDefault != null && availableConfigs.contains(savedDefault)) {
      chosen = savedDefault;
    } else if (availableConfigs.contains(DEFAULT_CONFIG_NAME)) {
      chosen = DEFAULT_CONFIG_NAME;
    }

    return chosen;
  }

  /**
   * Returns the config name for a file: its name without the .xml extension.
   *
   * @param file the config file
   *
   * @return the config name
   */
  private static String configName(File file) {
    String fileName = file.getName();
    return fileName.substring(0, fileName.length() - XML_EXTENSION.length());
  }
}

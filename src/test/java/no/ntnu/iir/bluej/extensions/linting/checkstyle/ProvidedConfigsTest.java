package no.ntnu.iir.bluej.extensions.linting.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProvidedConfigsTest {
  @TempDir
  Path tempDir;

  /**
   * Creates an extensions2 directory with a checkstyle4bluej folder holding the given files.
   */
  private File extensionsDir(String name, String... fileNames) throws IOException {
    Path extensionsDir = this.tempDir.resolve(name);
    Path configDir = Files.createDirectories(extensionsDir.resolve(ProvidedConfigs.FOLDER_NAME));
    for (String fileName : fileNames) {
      Files.writeString(configDir.resolve(fileName), "<module name=\"Checker\"/>");
    }
    return extensionsDir.toFile();
  }

  @Test
  void findsXmlFilesNamedAfterTheFile() throws IOException {
    File system = this.extensionsDir("system", "school_checks.xml", "b.XML");

    Map<String, String> configs = ProvidedConfigs.find(List.of(system));

    assertEquals(List.of("b", "school_checks"), List.copyOf(configs.keySet()));
    String expectedPath = new File(system, ProvidedConfigs.FOLDER_NAME + "/school_checks.xml")
        .getAbsolutePath();
    assertEquals(expectedPath, configs.get("school_checks"));
  }

  @Test
  void ignoresOtherFilesAndSubfolders() throws IOException {
    File system = this.extensionsDir("system", "notes.txt", "school_checks.xml");
    Files.createDirectories(system.toPath().resolve(ProvidedConfigs.FOLDER_NAME + "/sub.xml"));

    Map<String, String> configs = ProvidedConfigs.find(List.of(system));

    assertEquals(Set.of("school_checks"), configs.keySet());
  }

  @Test
  void ignoresMissingDirectories() {
    Map<String, String> configs = ProvidedConfigs.find(List.of(
        this.tempDir.resolve("does-not-exist").toFile()
    ));

    assertTrue(configs.isEmpty());
  }

  @Test
  void laterDirectoryWinsOnNameClash() throws IOException {
    File system = this.extensionsDir("system", "school_checks.xml");
    File user = this.extensionsDir("user", "school_checks.xml");

    Map<String, String> configs = ProvidedConfigs.find(List.of(system, user));

    assertEquals(1, configs.size());
    assertTrue(configs.get("school_checks").startsWith(user.getAbsolutePath()));
  }

  @Test
  void chooseDefaultKeepsSavedDefaultIfItExists() {
    String chosen = ProvidedConfigs.chooseDefault(
        "Sun", Set.of("Google", "Sun", ProvidedConfigs.DEFAULT_CONFIG_NAME), "Google"
    );

    assertEquals("Sun", chosen);
  }

  @Test
  void chooseDefaultUsesDefaultChecksWhenNothingIsSaved() {
    String chosen = ProvidedConfigs.chooseDefault(
        null, Set.of("Google", "Sun", ProvidedConfigs.DEFAULT_CONFIG_NAME), "Google"
    );

    assertEquals(ProvidedConfigs.DEFAULT_CONFIG_NAME, chosen);
  }

  @Test
  void chooseDefaultIgnoresSavedDefaultThatNoLongerExists() {
    String chosen = ProvidedConfigs.chooseDefault("Removed", Set.of("Google", "Sun"), "Google");

    assertEquals("Google", chosen);
  }
}

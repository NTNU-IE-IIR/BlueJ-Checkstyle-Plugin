package no.ntnu.iir.bluej.extensions.linting.checkstyle;

/**
 * Holds build information taken from pom.xml.
 * This file is a template: the templating-maven-plugin fills in the placeholders
 * at build time and writes the result to target/generated-sources/java-templates/.
 * Edit this template, not the generated copy.
 */
public final class SystemInfo {
  /** The version of the extension, as set in pom.xml. */
  public static final String VERSION = "${project.version}";

  private SystemInfo() {
  }
}

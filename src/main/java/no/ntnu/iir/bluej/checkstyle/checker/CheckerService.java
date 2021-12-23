package no.ntnu.iir.bluej.checkstyle.checker;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import no.ntnu.iir.bluej.extensions.linting.core.checker.ICheckerService;

/**
 * Represents a Checker service.
 * Responsible for handling requests to check files using a Checkstyle checker.
 */
public class CheckerService implements ICheckerService {
  private Checker checker;
  private boolean enabled;
  private List<AuditListener> listeners;
  

  /**
   * Constructs a new CheckerService.
   */
  public CheckerService() {
    this.enabled = false;
    this.listeners = new ArrayList<>();
    this.initChecker();
  }

  private void initChecker() {
    this.checker = new Checker();
    this.checker.setBasedir(null);
    this.checker.setModuleClassLoader(Checker.class.getClassLoader());

    for (AuditListener listener : this.listeners) {
      this.checker.addListener(listener);
    }
  }

  @Override
  public void enable() {
    this.enabled = true;
  }

  @Override
  public void disable() {
    this.enabled = false;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Configures Checkstyle to use given configuration.
   * 
   * @param configPath the path to the configuration file to use
   * 
   * @throws CheckstyleException if an error condition within Checkstyle occurs.
   */
  public void setConfiguration(String configPath) throws CheckstyleException {
    Properties checkstyleProperties = new Properties();
    this.initChecker(); // re init checker to prevent using two configs
    this.checker.configure(ConfigurationLoader.loadConfiguration(
        configPath,
        new PropertiesExpander(checkstyleProperties)
    ));
  }

  /**
   * Adds a AuditListener to the Checker.
   * 
   * @param listener the AuditListener to add to the Checker.
   */
  public void addListener(AuditListener listener) {
    this.listeners.add(listener);
    this.checker.addListener(listener);
  }

  /**
   * Removes a AuditListener from the Checker.
   * 
   * @param listener the AuditListener to remove from the Checker.
   */
  public void removeListener(AuditListener listener) {
    this.listeners.remove(listener);
    this.checker.removeListener(listener);
  }

  /**
   * Checks a single file using Checkstyle.
   * 
   * @param fileToCheck a File to check with Checkstyle.
   * @param charset the Files charset encoding.
   * 
   * @throws UnsupportedEncodingException if an unsupported encoding is used.
   * @throws CheckstyleException if an error condition within Checkstyle occurs.
   */
  public void checkFile(
      File fileToCheck,
      String charset
  ) throws UnsupportedEncodingException, CheckstyleException {
    if (this.enabled) {
      this.checker.setCharset(charset);
      this.checker.process(List.of(fileToCheck));
    }
  }

  /**
   * Checks a list of files using Checkstyle.
   * 
   * @param filesToCheck a List of Files to check with Checkstyle.
   * @param charset the Files charset encoding.
   * 
   * @throws UnsupportedEncodingException if an unsupported encoding is used.
   * @throws CheckstyleException if an error condition within Checkstyle occurs.
   */
  public void checkFiles(
      List<File> filesToCheck, 
      String charset
  ) throws UnsupportedEncodingException, CheckstyleException {
    if (this.enabled) {
      this.checker.setCharset(charset);
      this.checker.process(filesToCheck);
    }
  }
}

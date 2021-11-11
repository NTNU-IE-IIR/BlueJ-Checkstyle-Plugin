package no.ntnu.iir.bluej.checkstyle.checker;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import no.ntnu.iir.bluej.checkstyle.core.violations.ViolationManager;

/**
 * Represents a Checker service.
 * Responsible for handling requests to check files using a Checkstyle checker.
 */
public class CheckerService {
  private ViolationManager violationManager;
  private Checker checker;

  public CheckerService(ViolationManager violationManager) {
    this.violationManager = violationManager;
  }
  
  /**
   * Initializes the CheckerService.
   */
  public void init() {
    this.checker = new Checker();
    
    this.checker.setBasedir(null);
    this.checker.setModuleClassLoader(Checker.class.getClassLoader());
    try {
      this.setConfiguration();
    } catch (Exception e) {
      // TODO: Show error message/dialog to the user to let them know something is wrong
      // Likely caused by a faulty config file used
      e.printStackTrace();
    }

    this.checker.addListener(
        new CheckerListener(this.violationManager)
    );
  }

  /**
   * Configures Checkstyle to use given configuration.
   * TODO: Read Configuration from BlueJ preferences.
   * 
   * @throws IOException if an error reading the configuration file occurs.
   * @throws CheckstyleException if an error condition within Checkstyle occurs.
   */
  public void setConfiguration() throws IOException, CheckstyleException {
    Properties checkstyleProperties = new Properties();
    this.checker.configure(ConfigurationLoader.loadConfiguration(
        this.getClass().getClassLoader().getResource("config/google_checks.xml").toString(),
        new PropertiesExpander(checkstyleProperties)
    ));
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
    this.checker.setCharset(charset);
    this.checker.process(List.of(fileToCheck));
  }

  /**
   * Checks a list of files.
   * 
   * @param filesToCheck a List of Files to check.
   * @param charset the files charset encoding.
   * 
   * @throws UnsupportedEncodingException if an unsupported encoding is used.
   * @throws CheckstyleException if an error condition within Checkstyle occurs.
   */
  public void checkFiles(
      List<File> filesToCheck, 
      String charset
  ) throws UnsupportedEncodingException, CheckstyleException {
    this.checker.setCharset(charset);
    this.checker.process(filesToCheck);
  }
}

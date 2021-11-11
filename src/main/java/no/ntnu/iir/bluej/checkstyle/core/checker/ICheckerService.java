package no.ntnu.iir.bluej.checkstyle.core.checker;

import java.io.File;
import java.util.List;

/**
 * Represents an interface for CheckerServices.
 * Classes responsible for triggering file checks should implement this.
 */
public interface ICheckerService {
  /**
   * Checks a single file.
   * 
   * @param fileToCheck the File to check.
   * @param charset the Charset of the File to check.
   */
  void checkFile(File fileToCheck, String charset);

  /**
   * Checks a List of files.
   * 
   * @param filesToCheck a List of Files to check.
   * @param charset the Charset of the Files to check
   */
  void checkFiles(List<File> filesToCheck, String charset);
}

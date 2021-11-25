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
   * 
   * @throws Exception if an error occurs while processing the file
   */
  void checkFile(File fileToCheck, String charset) throws Exception;

  /**-
   * Checks a List of files.
   * 
   * @param filesToCheck a List of Files to check.
   * @param charset the Charset of the Files to check
   * 
   * @throws Exception if an error occurs while processing files
   */
  void checkFiles(List<File> filesToCheck, String charset) throws Exception;
}

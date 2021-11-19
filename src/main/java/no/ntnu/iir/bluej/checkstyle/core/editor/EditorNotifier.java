package no.ntnu.iir.bluej.checkstyle.core.editor;

import bluej.extensions2.BClass;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.editor.TextLocation;

/**
 * Represents a utility class for notifying the BlueJ editor.
 * Responsible for communicating with the BlueJ editor and displaying
 * errors/red lines under a TextLocation and highlighting text on request.
 */
public class EditorNotifier {
  private EditorNotifier() {}

  /**
   * Highlights a line in a file in the BlueJ editor.
   * Fetches the JavaEditor proxy or opens a new one.
   * 
   * @param blueClass the BClass to fetch JavaEditor from
   * @param textLocation the TextLocation of where to highlight
   * 
   * @throws ProjectNotOpenException project was not open
   * @throws PackageNotFoundException package could not be found
   */
  public static void highlightLine(
      BClass blueClass, 
      TextLocation textLocation
  ) throws ProjectNotOpenException, PackageNotFoundException {
    JavaEditor fileEditor = blueClass.getJavaEditor();
    int lineLength = fileEditor.getLineLength(textLocation.getLine());
    fileEditor.setVisible(true);
    fileEditor.setSelection(
        textLocation,
        new TextLocation(textLocation.getLine(), lineLength)
    );
  }
}

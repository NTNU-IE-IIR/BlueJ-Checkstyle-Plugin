package no.ntnu.iir.bluej.checkstyle.core.editor;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.editor.TextLocation;
import java.io.File;

/**
 * Represents a utility class for notifying the BlueJ editor.
 * Responsible for communicating with the BlueJ editor and displaying
 * errors/red lines under a TextLocation and highlighting text on request.
 */
public class EditorNotifier {
  private BPackage bluePackage;

  public EditorNotifier(BPackage bluePackage) {
    this.bluePackage = bluePackage;
  }

  /**
   * Highlights a TextLocation in a File in the BlueJ editor.
   * Attempts to find the BClass in the BPackage, opens the editor 
   * and sets the selection to the TextLocation.
   * 
   * @param file the File to highlight text in
   * @param textLocation the TextLocation of where to highlight
   * 
   * @throws ProjectNotOpenException project was not open
   * @throws PackageNotFoundException package could not be found
   * @throws ClassNotFoundException if the Class file could not be found
   */
  public void highlightLine(
      File file, 
      TextLocation textLocation
  ) throws ProjectNotOpenException, PackageNotFoundException, ClassNotFoundException {

    BClass[] blueClasses = this.bluePackage.getClasses();
    BClass foundClass = null;
    String targetAbsolutePath = file.getAbsolutePath();
    
    // search for the BClass matching the input File
    for (int i = 0; (i < blueClasses.length && foundClass == null); i++) {
      BClass currentClass = blueClasses[i];
      if (targetAbsolutePath.equals(currentClass.getJavaFile().getAbsolutePath())) {
        foundClass = currentClass;
      }
    }

    if (foundClass != null) {
      JavaEditor fileEditor = foundClass.getJavaEditor();
      int lineLength = fileEditor.getLineLength(textLocation.getLine());
      fileEditor.setSelection(
          textLocation,
          new TextLocation(textLocation.getLine(), lineLength)
      );
      fileEditor.setVisible(true);
    } else {
      // TODO: not sure if this is the right exception...
      throw new ClassNotFoundException("Could not find a class source from File");
    }
    
  }
}

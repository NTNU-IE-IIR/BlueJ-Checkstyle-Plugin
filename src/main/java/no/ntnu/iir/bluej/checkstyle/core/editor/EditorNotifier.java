package no.ntnu.iir.bluej.checkstyle.core.editor;

import bluej.compiler.CompileType;
import bluej.compiler.Diagnostic;
import bluej.compiler.Diagnostic.DiagnosticOrigin;
import bluej.editor.Editor;
import bluej.extensions2.BClass;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.EditorBridge;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.editor.TextLocation;
import no.ntnu.iir.bluej.checkstyle.core.violations.Violation;

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
   * Sets the JavaEditor to be visible, and then sets the selection.
   * 
   * @param blueClass the BClass to fetch the JavaEditor proxy object from
   * @param textLocation the TextLocation of where to highlight
   * 
   * @throws ProjectNotOpenException if the BCLass' project was not open
   * @throws PackageNotFoundException if the BClass' package was not found
   */
  public static void highlightLine(
      BClass blueClass, 
      TextLocation textLocation
  ) throws ProjectNotOpenException, PackageNotFoundException {
    JavaEditor fileEditor = blueClass.getJavaEditor();
    int lineLength = fileEditor.getLineLength(textLocation.getLine() - 1);
    fileEditor.setVisible(true);
    fileEditor.setSelection(
        textLocation,
        new TextLocation(textLocation.getLine(), lineLength)
    );
  }

  /**
   * Shows a diagnostic message underneath a specified line.
   * Uses the EditorBridge to get Editor from JavaEditor proxy object.
   * Utilizes the Editor object to show a Diagnostic message to the user.
   * 
   * @param blueClass the BClass to fetch the JavaEditor proxy object from
   * @param violation the Violation to show diagnostic message about
   */
  public static void showDiagnostic(
      BClass blueClass, 
      Violation violation
  ) throws ProjectNotOpenException, PackageNotFoundException {
    JavaEditor javaEditor = blueClass.getJavaEditor();
    javaEditor.setVisible(true);
    Editor editor = EditorBridge.getJavaEditor(javaEditor);
    TextLocation textLocation = violation.getLocation();
    int lineLength = javaEditor.getLineLength(textLocation.getLine() - 1);

    // TODO: Check effect of identifier
    Diagnostic diagnosticMessage = new Diagnostic(
        1,                                      // type
        violation.getSummary(),                 // message
        violation.getFile().getName(),          // fileName
        Long.valueOf(textLocation.getLine()),   // startLine
        Long.valueOf(textLocation.getColumn()), // startColumn
        Long.valueOf(textLocation.getLine()),   // endLine
        Long.valueOf(lineLength),               // endColumn
        DiagnosticOrigin.UNKNOWN,               // origin
        1                                       // identifier
    );

    // TODO: Check effect of errorIndex
    editor.displayDiagnostic(
        diagnosticMessage,    // diagnostic
        0,                    // errorIndex
        CompileType.EXTENSION // compileType
    );
  }
}

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
   * @param violation the source violation to highlight in the editor
   * 
   * @throws ProjectNotOpenException if the BCLass' project was not open
   * @throws PackageNotFoundException if the BClass' package was not found
   */
  public static void highlightLine(
      Violation violation
  ) throws ProjectNotOpenException, PackageNotFoundException {
    BClass blueClass = violation.getBClass();
    TextLocation textLocation = violation.getLocation();
    JavaEditor fileEditor = blueClass.getJavaEditor();

    int lineNumber = textLocation.getLine() - 1;
    int lineLength = fileEditor.getLineLength(textLocation.getLine() - 1);
    int startColumn = (textLocation.getColumn() > 0) ? textLocation.getColumn() - 1 : 1;

    fileEditor.setVisible(true);
    fileEditor.setSelection(
        new TextLocation(lineNumber, startColumn),
        new TextLocation(lineNumber, lineLength)
    );
  }

  /**
   * Shows a diagnostic message underneath a specified line.
   * Uses the EditorBridge to get Editor from JavaEditor proxy object.
   * Utilizes the Editor object to show a Diagnostic message to the user.
   * 
   * @param violation the Violation to show diagnostic message about
   */
  @Deprecated
  public static void showDiagnostic(
      Violation violation
  ) throws ProjectNotOpenException, PackageNotFoundException {
    BClass blueClass = violation.getBClass();
    JavaEditor javaEditor = blueClass.getJavaEditor();
    Editor editor = EditorBridge.getJavaEditor(javaEditor);
    TextLocation textLocation = violation.getLocation();
    int startLine = (textLocation.getLine() > 0) ? textLocation.getLine() : 1;      
    int startColumn = (textLocation.getColumn() > 0) ? textLocation.getColumn() : 1;
    int lineLength = javaEditor.getLineLength(textLocation.getLine() - 1);

    // TODO: Check effect of identifier
    Diagnostic diagnosticMessage = new Diagnostic(
        1,                                      // type
        violation.getSummary(),                 // message
        violation.getFile().getName(),          // fileName
        Long.valueOf(startLine),                // startLine
        Long.valueOf(startColumn),              // startColumn
        Long.valueOf(startLine),                // endLine
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

package no.ntnu.iir.bluej.checkstyle.core.violations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a Violation manager.
 * Responsible for managing violations and notifying listeners
 * when violations change.
 */
public class ViolationManager {
  private List<ViolationListener> listeners;
  private HashMap<String, List<Violation>> violations;

  /**
   * Constructs a new Violation manager.
   */
  public ViolationManager() {
    this.listeners = new ArrayList<>();
    this.violations = new HashMap<>();
  }

  /**
   * Adds an entry containing a files violations.
   * Notifies listeners that violations have changed.
   * 
   * @param fileName a String representing the name of the file.
   * @param violations a List of violations found in the file.
   */
  public void addViolations(String fileName, List<Violation> violations) {
    this.violations.put(fileName, violations);
    this.notifyListeners();
  }
  
  /**
   * Returns a list of violations for a specific file.
   * 
   * @param fileName a String representing the name of the file.
   * @return a List of violations for a specific file.
   */
  public List<Violation> getViolations(String fileName) {
    return this.violations.get(fileName);
  }

  /**
   * Updates an entry containing a files violations.
   * Notifies listeners that violations have changed.
   * 
   * @param fileName a String representing the name of the file.
   * @param violations a List of the violations found in the file.
   */
  public void setViolations(String fileName, List<Violation> violations) {
    this.violations.replace(fileName, violations);
    this.notifyListeners();
  }

  /**
   * Removes violations for a file and notifies the listeners of the change.
   * 
   * @param fileName a String representing the name of the file.
   */
  public void removeViolations(String fileName) {
    this.violations.remove(fileName);
    this.notifyListeners();
  }

  /**
   * Adds a new listener to the list of listeners.
   * 
   * @param listener a ViolationListener to add to the list of listeners.
   */
  public void addListener(ViolationListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes a listener from the list of listeners.
   * 
   * @param listener a ViolationListener to remove from the list of listeners.
   */
  public void removeListener(ViolationListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Notifies listening classes that a change has been made in the violations.
   */
  private void notifyListeners() {
    this.listeners.forEach(listener -> listener.onViolationsChanged(this.violations));
  }
}

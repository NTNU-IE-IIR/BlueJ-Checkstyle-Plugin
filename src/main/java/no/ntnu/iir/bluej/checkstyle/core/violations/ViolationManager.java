package no.ntnu.iir.bluej.checkstyle.core.violations;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a Violation manager.
 * Responsible for managing violations and notifying listeners when violations change. 
 * Also handles mapping between file paths and BlueJ Class instances.
 */
public class ViolationManager {
  private List<ViolationListener> listeners;
  private HashMap<String, List<Violation>> violations;
  private List<BPackage> bluePackages;
  private HashMap<String, BClass> blueClassMap;

  /**
   * Constructs a new Violation manager.
   */
  public ViolationManager() {
    this.listeners = new ArrayList<>();
    this.violations = new HashMap<>();
    this.bluePackages = new ArrayList<>();
    this.blueClassMap = new HashMap<>();
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
   * Clears violations for all files and notifies the listeners of the change.
   */
  public void clearViolations() {
    this.violations.clear();
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

  /**
   * Adds a BlueJ package to the list of packages to sync file names and BlueJ classes from. 
   * 
   * @param bluePackage the BlueJ Package to add to the list of packages
   */
  public void addBluePackage(BPackage bluePackage) {
    this.bluePackages.add(bluePackage);
  }

  /**
   * Removes a BlueJ Package from the list of packages to sync files names and BlueJ classes from.
   * 
   * @param bluePackage the BlueJ Package to remove from the list of packages
   */
  public void removeBluePackage(BPackage bluePackage) {
    this.bluePackages.remove(bluePackage);
  }

  /**
   * Synchronizes the blueClassMap with the BClasses in the current package.
   * 
   * @throws ProjectNotOpenException if the BlueJ project was not opened
   * @throws PackageNotFoundException if the Package was not found
   */
  public void syncBlueClassMap() throws ProjectNotOpenException, PackageNotFoundException {
    for (BPackage bluePackage : this.bluePackages) {
      BClass[] blueClasses = bluePackage.getClasses();
      for (BClass blueClass : blueClasses) {
        String filePath = blueClass.getJavaFile().getPath();
        this.blueClassMap.put(filePath, blueClass);
      }
    }
  }

  /**
   * Gets a BlueJ Class instance from a files path.
   * 
   * @param filePath the path of the file to find a BlueJ Class from
   * 
   * @return the found BlueJ Class instance or null
   */
  public BClass getBlueClass(String filePath) {
    return this.blueClassMap.get(filePath);
  }
}

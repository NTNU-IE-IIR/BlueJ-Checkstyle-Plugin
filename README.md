# BlueJ-Checkstyle-Plugin
A Checkstyle plugin/extension for BlueJ


## Installing the extension

1. Download the latest version of the extension found [here][1]
2. Move the downloaded JAR to a BlueJ extensions2 directory
3. Start BlueJ

  **BlueJ Extensions can be installed in three different directories:**
  - `User directory` installs for this user
  - `System directory` installs for all users of this system
  - `Project directory` installs for this project only
  
To install for a project, make a directory called `extensions2` in the projects root directory and move the JAR to that directory.


**In order to install for a user/system place the JAR in one of these directories:**

| Operating System | Install-type | Directories                                                  |
|------------------|--------------|--------------------------------------------------------------|
| **Mac**          | *User*       | `$HOME/Library/Preferences/org.bluej/extensions2`            |
|                  | *System*     | `<BLUEJ_HOME>/BlueJ.app/Contents/Resources/Java/extensions2` |
| **Unix**         | *User*       | `$HOME/.bluej/extensions2`                                   | 
|                  | *System*     | `<BLUEJ_HOME>/lib/extensions2`                               |
| **Windows**      | *User*       | `%USERNAME%\bluej\extensions2`                               | 
|                  | *System*     | `%PROGRAMFILES%\BlueJ\lib\extensions2`                       |

**Tip:** For Mac users, Control-click BlueJ.app and choose Show Package Contents to find the system directory.

[1]: https://github.com/NTNU-IE-IIR/BlueJ-Checkstyle-Plugin/releases/latest
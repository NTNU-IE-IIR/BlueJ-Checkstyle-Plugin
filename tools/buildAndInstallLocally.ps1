mvn clean verify;

$InstallDir = "C:\Program Files\BlueJ";

Copy-Item -Path "$PSScriptRoot\..\target\checkstyle4bluej-1.0-SNAPSHOT.jar" -Destination "$InstallDir\lib\extensions2\";
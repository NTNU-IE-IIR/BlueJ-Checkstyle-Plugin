mvn clean verify;

$InstallDir = "C:\Program Files\BlueJ";

Copy-Item -Path "$PSScriptRoot\..\target\checkstyle4bluej-1.0-SNAPSHOT-shaded.jar" -Destination "$InstallDir\lib\extensions2\";
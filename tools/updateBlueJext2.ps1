# Simple toolscript for updating bluejext2.jar

[CmdletBinding()]
param (
  [Parameter(
    Mandatory=$true, 
    HelpMessage="The version the BlueJ extensions2 library. Should follow the format: n.n.n (e.g: 5.0.2)"
  )]
  [String] $Version
);

$InstallDir = "C:\Program Files\BlueJ";
$ExtensionsJar = "$InstallDir\lib\bluejext2.jar";

$fileExists = Test-Path -Path $ExtensionsJar;

if ($fileExists) {
  mvn install:install-file `
    -Dfile="$InstallDir\lib\bluejext2.jar" `
    -DgroupId="bluej" `
    -DartifactId="bluejext2" `
    -Dversion="$Version" `
    -Dpackaging="jar" `
    -DgeneratePom="true" `
    -DlocalRepositoryPath="$PSScriptRoot\..\lib\"
} else {
  return "$ExtensionJar does not exist.";
}
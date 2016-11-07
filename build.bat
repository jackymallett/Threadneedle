::
:: Build Threadneedle and run without configuration files. Directory for Java
:: classes created if not present.
::

@echo off

:: Create classes directory if it doesn't exist
if not exist classes mkdir classes

:: You may need to change this if Java is installed somewhere else on your computer
set myJavaDir=C:\Program Files\Java
set myJreDir=%myJavaDir%\jre1.8.0_91
set myJdkDir=%myJavaDir%\jdk1.8.0_91
if not exist "%myJreDir%" (goto :myJreDirError)
if not exist "%myJdkDir%" (goto :myJdkDirError)

set myClassPath="classes;%myJreDir%\lib\ext\jfxrt.jar"
set myCompileCMD="%myJdkDir%\bin\javac" -d classes -extdirs lib -cp %myClassPath%

@echo on
:: Compile everything in the right order
%myCompileCMD% src/base/*.java
%myCompileCMD% src/statistics/*.java
%myCompileCMD% src/core/*.java
%myCompileCMD% src/agents/*.java
%myCompileCMD% src/charts/*.java
%myCompileCMD% src/gui/*.java

:: Run Threadneedle
java -cp ".;classes;src/resources;lib/*" gui.Threadneedle %*

$echo off

goto :eof
:myJreDirError
:myJdkDirError
echo Please edit build.bat to set the correct Java directories.

@echo off
setlocal
set "NOS_JAVA=java"
if exist "%JAVA_HOME%\bin\java.exe" set "NOS_JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%USERPROFILE%\Documents\Codex\jdks\jdk-17.0.21+9\bin\java.exe" set "NOS_JAVA=%USERPROFILE%\Documents\Codex\jdks\jdk-17.0.21+9\bin\java.exe"
if not exist "%~dp0outputs\NOS.jar" (
  echo Arquivo outputs\NOS.jar ausente. Consulte o README para preparar o jogo.
  pause
  exit /b 1
)
"%NOS_JAVA%" -Xmx768m -jar "%~dp0outputs\NOS.jar"
if errorlevel 1 pause

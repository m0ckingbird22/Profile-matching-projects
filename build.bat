@echo off
REM Build ulang dist\SPKProfileMatching.jar tanpa NetBeans.
REM Butuh JDK 25+ terinstall dan `javac` / `jar` tersedia di PATH.
setlocal
cd /d "%~dp0"

echo [1/4] Bersihkan build lama...
if exist build\classes rmdir /s /q build\classes
mkdir build\classes

echo [2/4] Compile sources...
dir /s /b /a-d src\*.java > build\sources.txt
javac -encoding UTF-8 -d build\classes ^
  -cp "lib\flatlaf-3.7.2.jar;lib\mysql-connector-j-9.7.0.jar;lib\pdfbox-2.0.30\*" ^
  @build\sources.txt
if errorlevel 1 (
  echo Compile GAGAL.
  exit /b 1
)

echo [3/4] Copy resources...
xcopy /y /i src\com\bkk\spk\resources\* build\classes\com\bkk\spk\resources\ >nul

echo [4/4] Package JAR...
(
  echo Manifest-Version: 1.0
  echo Created-By: Build script
  echo Class-Path: lib/mysql-connector-j-9.7.0.jar lib/flatlaf-3.7.2.jar lib/pdfbox-2.0.30.jar lib/fontbox-2.0.30.jar lib/commons-logging-1.2.jar
  echo Main-Class: com.bkk.spk.view.MainApp
  echo.
) > build\manifest.txt

if not exist dist mkdir dist
jar cfm dist\SPKProfileMatching.jar build\manifest.txt -C build\classes .

if errorlevel 1 (
  echo Package GAGAL.
  exit /b 1
)

echo.
echo BUILD SUKSES -^> dist\SPKProfileMatching.jar
endlocal

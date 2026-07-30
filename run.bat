@echo off
REM Jalankan aplikasi SPK Profile Matching tanpa NetBeans.
REM Klik 2x file ini, atau jalan dari Command Prompt.
cd /d "%~dp0"
java -jar dist\SPKProfileMatching.jar

echo.
echo ====================================================
echo Aplikasi sudah ditutup. Pesan error (kalau ada) ada di atas.
echo Tekan tombol apa saja untuk tutup jendela ini.
echo ====================================================
pause >nul

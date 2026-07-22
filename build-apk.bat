@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Building APK...
echo ========================================

:: تحديد المسار الكامل للمجلد الحالي
set "CURRENT_DIR=%CD%"

:: التحقق مما إذا كنا داخل مجلد tablet-app أم لا
if exist "%CURRENT_DIR%\gradlew.bat" (
    set "GRADLE_DIR=%CURRENT_DIR%"
    echo ✅ Found gradlew.bat in current folder
) else if exist "%CURRENT_DIR%\tablet-app\gradlew.bat" (
    set "GRADLE_DIR=%CURRENT_DIR%\tablet-app"
    echo ✅ Found gradlew.bat in tablet-app\
) else (
    echo ❌ ERROR: gradlew.bat not found!
    echo Make sure you have a folder named 'tablet-app' with gradlew.bat inside.
    echo Or run this script from the folder that contains tablet-app\
    pause
    exit /b 1
)

:: الانتقال إلى مجلد Gradle
cd /d "%GRADLE_DIR%"

:: تنفيذ البناء
echo 🛠️ Running Gradle build...
call gradlew.bat assembleDebug

:: التحقق من نجاح البناء
if exist "%GRADLE_DIR%\app\build\outputs\apk\debug\app-debug.apk" (
    echo ========================================
    echo ✅ APK built successfully!
    echo 📁 Location: %GRADLE_DIR%\app\build\outputs\apk\debug\app-debug.apk
    echo ========================================
    start "" "%GRADLE_DIR%\app\build\outputs\apk\debug"
) else (
    echo ❌ Build failed. Check the error messages above.
)

pause
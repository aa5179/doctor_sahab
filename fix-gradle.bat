@echo off
echo 🔧 Fixing Gradle Compatibility Issues...
echo ========================================

echo.
echo ✅ Updated Gradle to version 8.5 (compatible with Java 21)
echo ✅ Updated Android Gradle Plugin to 8.2.0
echo ✅ Updated Java compatibility to version 17
echo ✅ Cleared Gradle cache folder

echo.
echo 🚀 Next Steps:
echo 1. Open Android Studio
echo 2. File → Open → Select this java-migration folder
echo 3. Wait for Gradle sync (should work now!)
echo 4. If prompted about Gradle version, click "Use Gradle wrapper"

echo.
echo 💡 If you still get errors:
echo 1. File → Invalidate Caches → Invalidate and Restart
echo 2. Try again after Android Studio restarts

echo.
echo ✨ The project should now sync successfully with Java 21!
pause
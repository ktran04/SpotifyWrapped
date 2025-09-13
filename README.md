# CS2340-Proj2

This project appears to be an Android application built using Gradle. Below is a summary of the folder structure and its main components:

## Structure
- `app/` - Contains the main Android app source code, build configuration, and resources.
  - `build.gradle` - App-level Gradle build configuration.
  - `google-services.json` - Firebase/Google services configuration.
  - `proguard-rules.pro` - ProGuard rules for code obfuscation.
  - `src/` - Source code and resources for the app.
- `build.gradle` - Project-level Gradle build configuration.
- `gradle.properties` - Gradle properties for the project.
- `settings.gradle` - Gradle settings file.
- `local.properties` - Local configuration (SDK paths, etc.).
- `gradle/` - Gradle wrapper and version management.
  - `libs.versions.toml` - Version catalog for dependencies.
  - `wrapper/` - Gradle wrapper files.
- `.idea/` - IDE configuration files (Android Studio/IntelliJ).
- `.gitignore` - Git ignore rules for the project.

## How to Build and Run
1. **Clone the repository:**
   ```sh
   git clone <repo-url>
   ```
2. **Open in Android Studio:**
   - Open the project folder (`CS2340-Proj2`) in Android Studio.
3. **Build the project:**
   - Use the Gradle tasks or the "Run" button in Android Studio.
4. **Run the app:**
   - Connect an Android device or use an emulator.
   - Click "Run" in Android Studio.

## Notes
- Make sure you have the correct Android SDK installed.
- The `google-services.json` file is required for Firebase integration.
- The project uses Gradle for build automation and dependency management.

## License
Add your license information here.


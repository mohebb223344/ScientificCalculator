# Scientific Calculator - GitHub Ready

A Kotlin + Jetpack Compose scientific calculator with:
- Casio-style responsive UI
- Portrait and landscape layouts
- DEG/RAD mode
- Normal/inverse trig functions
- Scientific expression parser
- Factorial and percentage
- M+, M-, MR, MC memory
- Calculation history
- Dark/light theme toggle

## Build APK online with GitHub Actions

1. Create a GitHub repository.
2. Upload the contents of this folder to the repository root.
3. Go to **Actions**.
4. Select **Build Android APK**.
5. Click **Run workflow** (or push to `main`).
6. Open the completed run.
7. Download the **ScientificCalculator-debug** artifact.
8. Extract the artifact ZIP to get `app-debug.apk`.

This project intentionally uses GitHub Actions' Gradle setup to install Gradle on the runner, so the Gradle wrapper files are not required in the repository.

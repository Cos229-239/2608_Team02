# Class Project Template

Start here. Keep this README updated with your team name, members, and a short plan.

Prototype pages:

- `index.html` - roster editor prototype landing page
- `roster-v2-sample.json` - sample editable roster shape for future class setup work

# Out in the Open (OTO)

Out in the Open (OTO) is an Android-only outdoor preparedness application designed to support outdoor exploration, emergency preparation, and future Crisis Mode capabilities.

## Current Project Status

The repository currently contains the initial working Android project scaffold. Explorer Mode, Crisis Mode, maps, offline content, APIs, notifications, and community reporting features have not yet been implemented.

## Approved Android Baseline

- Platform: Android only
- Language: Kotlin
- User interface: Jetpack Compose and Material 3
- Package and namespace: `com.cos229239.team02.oto`
- Minimum SDK: 26
- Compile SDK: 37
- Target SDK: 36
- Build configuration: Gradle Kotlin DSL
- Project structure: Single Android `app` module
- Version control application: GitHub Desktop

The project compiles with SDK 37 while targeting Android 16/API 36. Do not change SDK levels, package identity, Gradle versions, or shared dependencies without team approval.

## Required Software

Each developer needs:

- GitHub Desktop
- Android Studio Quail 3
- Android SDK Platform 37
- An Android 16/API 36 or newer emulator, or a compatible physical Android device

Use the Java runtime included with Android Studio. Do not install a separate version of Gradle; use the Gradle wrapper committed with the project.

## Opening the Project

Open the repository root in Android Studio.

The project root is the folder containing:

- `app`
- `gradle`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `build.gradle.kts`

Do not open only the `Documents` folder or only the `app` folder.

## Build and Test Commands

### macOS or Linux

```bash
./gradlew build
./gradlew test
```

### Windows PowerShell

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

Run these commands from the repository root.

## Course Git Workflow

1. The shared Android baseline is maintained in `dev`.
2. Each student creates one persistent personal branch from the latest `dev`.
3. Students work, commit, and push only on their personal branches.
4. Before later integration, merge the latest `dev` down into the personal branch and verify the application.
5. Approved personal work is merged up into `dev` with a Build Buddy.
6. The Build Master promotes stable, approved work from `dev` to `main` only when authorized.

### Branch Rules

- Do not work directly in `main`.
- Do not use or modify `PatrickPersonal`; it belongs to the instructor.
- Do not create a fork for routine team development.
- Do not create weekly or feature branches.
- Do not force-push or rewrite another contributor's history.
- Use GitHub Desktop for course Git operations.
- Do not authenticate GitHub through Android Studio.

## Team Members

Each team member adds only their own name here from their personal branch.

Jeremy Dusablon

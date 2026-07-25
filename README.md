# 🎵 Sepotify - Music Player & Social Media App

![Sepotify Logo](https://raw.githubusercontent.com/manelyhamedani/Sepotify/main/app/src/main/res/sepotify.png)

> A music player and social media Android application developed as a Mobile Programming course project.

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📖 About The Project

**Sepotify** is a full-featured Android application that combines music playback with social networking. It was built as the final project for the **Mobile Programming** university course. The app allows users to listen to their favorite tracks, create playlists, discover trending music, and share their music taste with friends.

## ✨ Key Features

- **Music Player**: Play, pause, skip, and seek through audio tracks using ExoPlayer.
- **Trending & New Releases**: Discover what's hot on the home screen, including trending songs and new releases.
- **Public Playlists**: Browse global playlists created by other users, alongside your own local playlists.
- **Chat**: Real-time messaging with other users, including typing indicators.
- **Follow System**: Follow other users to see their activity.
- **Offline Downloads**: Download songs and cover art for offline playback using WorkManager.
- **User Profiles**: Customize your profile with a picture and bio.
- **Authentication**: Sign in with email via Supabase Auth.
- **Search**: Find songs, artists, and other users.

## 🛠️ Built With

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM with Clean Architecture (data, domain, ui, di layers)
- **Backend & Database**: [Supabase](https://supabase.com/) (Auth, PostgREST, Realtime, Storage) — used for remote data, authentication, and real-time features
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room) — for offline caching and downloaded songs
- **Download Management**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) + [Ktor Client](https://ktor.io/) — background downloads of audio and cover art
- **Media Playback**: [ExoPlayer](https://exoplayer.dev/) (Media3) — audio playback and media session
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Paging**: [AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) — for paginated lists
- **DataStore**: [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore) — for local user preferences
- **Serialization**: [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html)
- **HTTP Client**: [Ktor](https://ktor.io/) with OkHttp engine
- **Auth**: [Supabase Auth](https://supabase.com/docs/guides/auth)

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Latest version recommended)
- **JDK 11** or higher
- **Android SDK** (API Level 26+)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/manelyhamedani/Sepotify.git
2. **Open the project in Android Studio**:
    - Launch Android Studio.
    - Select File > Open and navigate to the cloned Sepotify folder.
    - Allow Gradle to sync and download dependencies.
4. **Build and Run**:

    - Connect an Android device or start an emulator.
    - Click the Run button in the toolbar.

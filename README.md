# StackItXML — Collection Tracker App

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-XML_UI-green?logo=android)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase)
![Firestore](https://img.shields.io/badge/Database-Firestore-orange?logo=firebase)
![Status](https://img.shields.io/badge/Project-University_Assignment-blue)

A simple Android app built with Kotlin and XML layouts, designed to help users track item counts within collections. Includes user authentication, collection sharing, individual item counting, and basic statistics.

Developed as a **university project** focused on core Android development skills and Firebase integration.

---

## 🚀 Features

- **🔐 User Authentication**
  - Register and log in using email and password.

- **📂 Collection Management**
  - Create, view, and manage collections.
  - Share collections with other users via email.

- **📊 Item Tracking**
  - View items in a collection.
  - Increase or decrease your personal count of each item.

- **📈 Collection Statistics**
  - View the most counted item.
  - See which user contributed most to a collection.

---

## 🛠 Technologies Used

- **Kotlin** — Core development language.
- **Android XML Layouts** — For UI design.
- **Firebase Authentication** — User login & registration.
- **Cloud Firestore** — NoSQL database for data storage.
- **Kotlin Coroutines** — For asynchronous Firebase calls.

---

## 📦 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Java 11+ (bundled with Android Studio)
- Android device or emulator

### Installation & Setup

1. **Clone the Repository**

```bash
git clone https://github.com/danielhoyosceldran/stackitxml.git
cd stackitxml
```

2. **Open in Android Studio**

- `File > Open` and select the project directory.
- Once project opened:
  1. Check if you have the `gradle.properties' file in the project root.
  2. If not, create the file.
  3. Even if you created the file or is was already created, it should contain this lines:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

3. **Firebase Setup**

- Go to [Firebase Console](https://console.firebase.google.com/).
- Create or select a Firebase project.
- Register your Android app:
  - Package Name: `com.example.stackitxml`
  - (Optional) Nickname: `StackItXML`
- Download `google-services.json` and place it in the `app/` directory.
- Enable **Email/Password Authentication**:
  - `Build > Authentication > Sign-in method`
- Setup **Firestore Database**:
  - Start in **Test Mode** for development (you will have 30 days of unlimited access).
  - Choose a location (e.g., `europe-west1`).

4. **Sync Gradle**

- `File > Sync Project with Gradle Files`

5. **Run the App**

- Use a connected Android device or emulator.

---

## 📁 Project Structure

```plaintext
.
├── .gradle/                               # Local Gradle caches (ignored by Git)
├── .idea/                                 # Android Studio project settings (mostly ignored by Git)
├── .gitignore                             # Specifies files and folders to be ignored by Git
├── build.gradle.kts                       # Project-level Gradle configuration
├── gradle.properties                      # Project-wide Gradle settings (e.g., AndroidX flag, JVM args - NOT committed to Git)
├── gradlew                                # Gradle wrapper script (Unix)
├── gradlew.bat                            # Gradle wrapper script (Windows)
├── local.properties                       # Local SDK location (NOT committed to Git)
├── README.md                              # Project documentation
├── settings.gradle.kts                    # Gradle settings for multi-module projects (defines app module)
│
└── app/                                   # Main application module
    ├── build.gradle.kts                   # Module-level Gradle configuration (dependencies, Android settings)
    ├── google-services.json               # Firebase configuration (MUST be added manually to app/ after cloning - NOT committed to Git)
    ├── proguard-rules.pro                 # ProGuard/R8 rules for code shrinking and obfuscation
    │
    └── src/main/
        ├── AndroidManifest.xml            # Application manifest (declares components, permissions)
        │
        ├── java/com/example/stackitxml/   # Kotlin source code
        │   ├── MainActivity.kt            # App entry point (redirects to Login/Home)
        │   │
        │   ├── data/                      # Data Layer
        │   │   ├── model/                 # Data Models
        │   │   │   ├── Collection.kt      # Data class for a collection
        │   │   │   ├── Item.kt            # Data class for an item within a collection
        │   │   │   └── User.kt            # Data class for user profiles
        │   │   │
        │   │   └── repository/            # Data Repositories
        │   │       └── FirestoreRepository.kt # Handles all Firebase (Auth, Firestore) interactions
        │   │
        │   ├── ui/                        # User Interface Layer (Activities & Adapters)
        │   │   ├── auth/                  # Authentication Screens
        │   │   │   ├── LoginActivity.kt   # User login screen
        │   │   │   └── RegisterActivity.kt# User registration screen
        │   │   │
        │   │   ├── collectiondetail/      # Collection Detail & Item Management
        │   │   │   ├── CollectionDetailActivity.kt # Displays items, handles counting & sharing
        │   │   │   └── ItemAdapter.kt     # Adapter for items in RecyclerView
        │   │   │
        │   │   ├── home/                  # Home Screen & Collection Listing
        │   │   │   ├── CollectionAdapter.kt # Adapter for collections in RecyclerView
        │   │   │   └── HomeActivity.kt    # Main screen showing user's collections
        │   │   │
        │   │   └── statistics/            # Statistics Screen
        │   │       └── StatisticsActivity.kt # Displays collection statistics
        │   │
        │   └── util/                      # Utility Classes
        │       ├── Constants.kt           # Application-wide constants (e.g., Firestore collection names)
        │       └── DialogUtils.kt         # Helper for showing consistent UI messages/dialogs
        │
        └── res/                           # Application resources
            ├── drawable/                  # Drawable resources (images, custom shapes)
            │   ├── ic_launcher_background.xml
            │   ├── ic_launcher_foreground.xml
            │   ├── logo.png
            │   └── table_border.xml       # Drawable for table borders
            │
            ├── layout/                    # XML UI Layouts
            │   ├── activity_collection_detail.xml
            │   ├── activity_home.xml
            │   ├── activity_login.xml
            │   ├── activity_main.xml
            │   ├── activity_register.xml
            │   ├── activity_statistics.xml
            │   ├── dialog_add_collection.xml
            │   ├── dialog_add_item.xml
            │   ├── dialog_delete_confirmation.xml # Generic confirmation dialog
            │   ├── dialog_share_collection.xml
            │   ├── item_collection.xml      # Layout for a single collection item in Home screen
            │   ├── item_collection_item.xml # Layout for a single item in Collection Detail screen
            │   └── item_statistics_row.xml  # Layout for a single row in Statistics table
            │
            ├── mipmap-anydpi-v26/         # Launcher icons (adaptive)
            │   ├── ic_launcher.xml
            │   └── ic_launcher_round.xml
            │
            ├── mipmap-hdpi/               # Launcher icons (various densities)
            │   ├── ic_launcher.webp
            │   └── ic_launcher_round.webp
            │   └── ... (other mipmap folders: mdpi, xhdpi, xxlhdpi, xxxhdpi)
            │
            ├── values/                    # Default resource values
            │   ├── colors.xml             # Custom color definitions
            │   ├── dimens.xml             # Dimension definitions (e.g., font sizes, margins)
            │   ├── strings.xml            # String resources
            │   ├── styles.xml             # Custom UI component styles (e.g., text appearances)
            │   └── themes.xml             # Application themes (light mode)
            │
            ├── values-night/              # Resources for dark mode
            │   └── themes.xml             # Application themes (dark mode overrides)
            │
            └── xml/                       # XML configuration files
                ├── backup_rules.xml
                └── data_extraction_rules.xml
```

---

## 🛢 Firestore Database Structure

- **`users` Collection**
  - **Document ID:** `userId` (Firebase UID)
  - **Fields:**
    - `email`: String
    - `username`: String
    - `accessibleCollectionIds`: Array of collection IDs

- **`collections` Collection**
  - **Document ID:** Auto-generated
  - 
  - **Fields:**
    - `name`: String
    - `description`: String
    - `ownerId`: UID of creator
    - `memberIds`: Array of UIDs
    - `createdAt`: Timestamp

- **`items` Subcollection** (`collections/{collectionId}/items`)
  - **Document ID:** Auto-generated
  - **Fields:**
    - `name`: String
    - `creatorId`: UID of creator
    - `personalCount`: Map (`userId`: count)
    - `totalCount`: Sum of all counts
    - `createdAt`: Timestamp

---

## 📖 Notes

- This project is intended for educational purposes.
- Firestore **Test Mode**.

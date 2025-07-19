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
- **AndroidX Libraries** — Including `RecyclerView`, `CardView`, and others.

---

## 📦 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Java 11+ (bundled with Android Studio)
- Android device or emulator

### Installation & Setup

1. **Clone the Repository**

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

2. **Open in Android Studio**

- `File > Open` and select the project directory.

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
  - Start in **Test Mode** for development.
  - Choose a location (e.g., `europe-west1`).

4. **Sync Gradle**

- `File > Sync Project with Gradle Files`

5. **Run the App**

- Use a connected Android device or emulator.

---

## 📁 Project Structure

```plaintext
app/src/main/java/com/yourcompany/yourappname/
├── ui/
│   ├── auth/                  # Login & Registration
│   ├── home/                  # Home screen & collections
│   ├── collectiondetail/      # Items & counts
│   └── statistics/            # Collection statistics
├── data/
│   ├── model/                 # Data models (User, Collection, Item)
│   └── repository/            # Firestore repository
└── util/                      # Utilities (constants, dialogs)

app/src/main/res/
├── layout/                    # XML UI layouts
├── values/                    # Colors, strings, themes
└── drawable/, mipmap/         # Icons and images

app/src/main/AndroidManifest.xml
app/build.gradle
google-services.json
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

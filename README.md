# Dishcover 🍽️

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub issues](https://img.shields.io/github/issues/LyondVa/Dishcover)](https://github.com/LyondVa/Dishcover/issues)
[![GitHub forks](https://img.shields.io/github/forks/LyondVa/Dishcover)](https://github.com/LyondVa/Dishcover/network)
[![GitHub stars](https://img.shields.io/github/stars/LyondVa/Dishcover)](https://github.com/LyondVa/Dishcover/stargazers)

**Dishcover is a modern, offline-first Android application designed to help users discover, create, manage, and share recipes. It combines robust personal recipe management with social features, allowing users to build a vibrant community around their shared passion for cooking.**

This project is built with a focus on a clean, scalable, and maintainable architecture, ensuring a high-quality user experience, excellent performance, and data integrity.

***

### ✨ Key Features

*   **Offline-First Functionality:** Core features are available without an internet connection. View, create, and edit recipes seamlessly, and all changes will sync automatically when connectivity is restored.
*   **Full Recipe Management:** Create, view, update, and delete your personal recipes with comprehensive details like ingredients, instructions, cooking time, and images.
*   **Cookbook Organization:** Group your recipes into themed collections (cookbooks) and share them publicly or with specific users.
*   **Social Feed & Interaction:** Discover new recipes and posts from users you follow in a personalized social feed. Engage with the community through likes and comments.
*   **Powerful Search & Discovery:** Find recipes with advanced search filters, including ingredients, cooking time, and tags. Get personalized recommendations based on your preferences.
*   **Secure Authentication:** Sign up and log in securely using email/password or social media accounts.
*   **Content Moderation:** Tools for users to report inappropriate content and for administrators to review and manage it, ensuring a safe community.
*   **Multi-Language Support:** Designed to support multiple languages, including English and Vietnamese, with proper localization for measurements (metric/imperial).

***

### 🏛️ Architecture

Dishcover is built using modern architectural principles to ensure it is robust, testable, and scalable.

*   **Clean Architecture:** The project follows a strict separation of concerns into **Presentation**, **Domain**, and **Data** layers. This isolates business logic from UI and implementation details, making the codebase flexible and easy to maintain.
*   **MVVM (Model-View-ViewModel):** The presentation layer uses the MVVM pattern to separate UI state and logic from the UI rendering, integrating perfectly with Jetpack Compose.
*   **Reactive & Asynchronous:** The app is fully reactive, using **Kotlin Flow** and **Coroutines** to manage asynchronous operations, data streams, and UI updates, ensuring a smooth and responsive user experience.
*   **Dependency Injection:** We use **Hilt** to manage dependencies, which simplifies development, promotes loose coupling, and makes testing easier.

***

### 🛠️ Tech Stack & Dependencies

*   **100% Kotlin:** The entire application is written in Kotlin.
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
*   **Backend & Cloud Services:** [Firebase](https://firebase.google.com/)
    *   **Authentication:** Firebase Authentication for user management.
    *   **Database:** Firestore for real-time, cloud-based NoSQL data storage.
    *   **Storage:** Cloud Storage for Firebase for images and media.
    *   **Notifications:** Firebase Cloud Messaging for push notifications.
*   **Local Persistence:** [Room](https://developer.android.com/training/data-storage/room) for robust offline database storage.
*   **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for DI in Android.
*   **Networking:** (Implicitly through Firebase SDK)
*   **Code Quality:** Detekt and Android Lint.

***

### 🚀 Getting Started

To get a local copy up and running, please follow these steps.

#### Prerequisites

*   Android Studio (latest version recommended)
*   A Google account to set up Firebase services.

#### Installation

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/LyondVa/Dishcover.git
    cd Dishcover
    ```

2.  **Set up Firebase:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    *   Add a new Android app to your Firebase project with the package name from this project (check the `build.gradle` file).
    *   Follow the setup instructions to download the `google-services.json` file.
    *   Place the `google-services.json` file in the `app/` directory of this project.
    *   In the Firebase Console, enable **Authentication** (with Email/Password and Google providers), **Firestore**, and **Cloud Storage**.

3.  **Build and Run:**
    *   Open the project in Android Studio.
    *   Let Gradle sync and download all the required dependencies.
    *   Build and run the app on an Android emulator or a physical device.

***

### 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

***

### 📜 License

Distributed under the MIT License. See `LICENSE.txt` for more information.

***

### 📧 Contact

LinkedIn: https://www.linkedin.com/in/nhat-pham-401991318/

Project Link: [https://github.com/LyondVa/Dishcover](https://github.com/LyondVa/Dishcover)

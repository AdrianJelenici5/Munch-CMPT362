![munch_logo](https://github.com/user-attachments/assets/03c613e9-8fb9-46f0-9a08-c558dbe73f87)

Munch App 🍽️
=============

<img width="360" alt="image" src="https://github.com/user-attachments/assets/77cb9d02-92f3-4498-a1db-ea73d4e38d6d">
<img width="361" alt="image" src="https://github.com/user-attachments/assets/1a80a6c0-adb0-41b3-a883-6ceb1c67bf0c">


Munch is a restaurant discovery and group decision-making app that helps friends decide where to eat through a Tinder-style interface and group voting system.

Features ✨
----------

-   Tinder-style restaurant discovery
-   Smart group voting system
-   Personal preference learning
-   Location-based recommendations
-   Review integration with Yelp and Google Places

Architecture 🏗️
----------------

Munch follows MVVM **Clean Architecture** pattern with the following tech stack:

-   **Language**: Kotlin
-   **Backend**: Firebase (Auth, Firestore, Storage)
-   **Local Storage**: Room Database
-   **APIs**: Yelp Fusion API, Google Maps API

### Architecture Diagram

![Munch-Final-MVVM drawio](https://github.com/user-attachments/assets/78093f7e-7d59-4ee8-820a-5714226f796b)


### Threading Model

![Munch-Final-Threads drawio](https://github.com/user-attachments/assets/c4f3aa96-8218-4579-a219-0922fc0d9a0e)


Getting Started 🚀
------------------

### Prerequisites

-   Android Studio
-   Firebase Project
-   SHA-1 and SHA-256 certificates for Google Sign-In

### Firebase Setup

1.  Add your Firebase configuration:
    -   Add SHA-1 and SHA-256 certificates to Firebase project for Google Sign-In:

        `# Debug SHA-1 and SHA-256
        ```bash
        cd android
        ./gradlew signingReport
        ```

    -   Add the certificates to Firebase Console under Project Settings > Your Apps > SHA certificate fingerprints

### Project Setup

1.  Clone the repository:

```bash
git clone https://github.com/your-username/munch-app.git](https://github.com/AdrianJelenici5/Munch-CMPT362.git
```

1.  API Keys:
    -   Yelp API key is already configured in the project
    -   Update `local.properties` with Google Maps API key (if needed)

2.  Build and run the project in Android Studio

### Troubleshooting

Common issues and solutions:

-   If Google Sign-In isn't working:
    -   Verify SHA-1 and SHA-256 are correctly added to Firebase Console
    -   Ensure `google-services.json` is up to date
    -   Check if the package name matches Firebase configuration

Current Limitations ⚠️
----------------------

### API Restrictions

-   **Yelp API**: Currently on Free Tier
    -   Limited to 300 requests per day
    -   Maximum of 3 API calls per second
    -   Business photos limited to 3 per listing
    -   Search radius maximum of 40000 meters
-   **Firebase**:
    -   Free Spark Plan limits:
        -   1GB storage
        -   10GB/month data transfer
        -   50K/day read operations
        -   20K/day write operations

### Known Issues

-   Location updates may drain battery faster in background
-   Restaurant images may load slowly on slower connections
-   Limited offline functionality for now
-   Restaurant might not show due to the daily request limits

Future Improvements 🚀
----------------------

We will add more potential improvements.

Documentation 📚
----------------

For detailed documentation, visit our [website](https://munch-app.onrender.com/) which includes:

-   Complete API reference
-   Architecture guide
-   UI/UX guidelines
-   Implementation details
-   Testing strategies

Contact 📬
----------

### App Support

Email: <munchapp.cmpt362@gmail.com>

### Development Team

-   Jun Pin Foo - <junpinf@sfu.ca>
-   Adrian Jelenici - <aja60@sfu.ca>
-   Darian Wong - <dtw11@sfu.ca>
-   Gabriel Cheng - <gyc5@sfu.ca>

APK Link: <https://drive.google.com/file/d/1ySZhiUKVul3I13NMod8wSwL5Um1RXVpV/view>

Website: <https://munch-app.onrender.com/>

* * * * *

Made with ❤️ by Group 12

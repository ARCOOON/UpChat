# UpChat

[![Latest Build](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml/badge.svg)](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml)

UpChat is an Android chat client built with Kotlin, Firebase Realtime Database, and a locally managed cryptography stack. This document focuses on how the project is organized and how to get productive quickly as a contributor.

---

## What’s in the box?

- **Realtime messaging** – Conversations sync through Firebase Database and Firebase Cloud Messaging.
- **Encrypted content** – AES-GCM keys are negotiated per conversation, with canonical MACs for message integrity.
- **Media sharing** – Inline photo capture/upload with storage backed by Firebase Storage.
- **Secure storage** – Jetpack DataStore wrapped in Android Keystore protects local session data.
- **Tooling** – GitHub Actions workflow builds debug/release artifacts and surfaces lint output.

---

## Project layout

```
app/
├─ src/main/java/com/devusercode/upchat/
│  ├─ security/         # AES, MAC, HKDF, RSA helpers
│  ├─ utils/            # Conversation helpers, storage access, activity utilities
│  ├─ adapter/          # RecyclerView adapters and view holders
│  ├─ models/           # Firebase-backed data models
│  └─ *Activity.kt      # UI entry points for each screen
├─ src/main/res/        # XML layouts, drawables, strings
└─ build.gradle         # Android module configuration
```

Gradle build logic lives at the root alongside the `settings.gradle` file. Unit and instrumentation tests mirror the source tree under `src/test` and `src/androidTest` respectively.

---

## Architecture overview

| Layer | Notes |
| --- | --- |
| **UI** | View-based activities and RecyclerView adapters render conversations, profiles, and onboarding flows. Compose interop utilities are present while a full migration is planned. |
| **Domain** | `ConversationUtil` coordinates message send/receive flows, atomic conversation creation, and MAC payload assembly. |
| **Security** | `security/` contains the symmetric/asymmetric primitives; `MessageIntegrity` unifies canonical payload generation. |
| **Data** | Firebase Database/Storage handle persistence, with `StorageController` wrapping encrypted DataStore for local preferences and session state. |
| **Background** | `MessagingService` subscribes to FCM notifications for new messages. |

---

## Getting started

1. **Clone the repo**
   ```bash
   git clone https://github.com/ARCOOON/UpChat.git
   cd UpChat
   ```
2. **Supply Firebase config**
   - Place `google-services.json` inside `app/`.
   - Ensure Database and Storage rules match your test environment.
3. **Install dependencies**
   ```bash
   ./gradlew dependencies
   ```
4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
5. **Execute checks**
   ```bash
   ./gradlew lint test
   ```

---

## Development tips

- **Cryptography** – `ConversationKeyManager` publishes RSA public keys and distributes shared secrets. When extending message types, use `MessageIntegrity.canonicalPayload` to keep MAC coverage consistent.
- **Messaging pipeline** – `ConversationActivity` requests a shared key, feeds it to `ConversationUtil`, and observes Firebase for message streams. `MessageAdapter` binds decrypted content and verification state in view holders.
- **Secure persistence** – `StorageController` exposes suspending getters/setters backed by an encrypted DataStore; prefer these over direct SharedPreferences access.
- **UI consistency** – Activity transition helpers live in `ActivityTransitions.kt`; update both enter and exit animations when adding new flows.
- **Testing** – Run `./gradlew lint` for static checks and `./gradlew test` for unit coverage before submitting patches.

---

## Contributing

1. Fork the repository and branch from `main`.
2. Follow Kotlin coding standards and keep changes modular.
3. Document security-sensitive modifications clearly in commit messages and pull requests.
4. Provide screenshots or screen recordings when altering UI behavior.
5. Open a pull request once lint and tests pass locally.

---

## Security

- Per-conversation secrets are established via RSA-wrapped keys and expanded with HKDF.
- Message data is encrypted with AES-GCM; MACs cover ciphertext plus sender metadata.
- Local secrets are stored with encrypted Jetpack DataStore using Android Keystore-backed keys.
- Report vulnerabilities privately to **security@upchat.app** (PGP preferred). Avoid public issues for sensitive disclosures.

---

## License

UpChat is distributed under the [Apache 2.0 License](LICENSE).

# UpChat

[![Latest Build](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml/badge.svg)](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml)

UpChat is a privacy-focused Android messenger that pairs Firebase-backed real-time messaging with local cryptography to keep conversations private while remaining fast and easy to use.

---

## Project Snapshot

| Area | Status | Highlights |
| --- | --- | --- |
| **Messaging** | ✅ Stable | Text chat, per-conversation encryption, read-friendly UI |
| **Media** | ⚠️ Partial | Photo capture/sharing supported; file, audio, and rich previews pending |
| **Security** | 🚧 Improving | HKDF-derived AES keys, MAC verification, secure storage via encrypted DataStore |
| **Platform** | ✅ Ready | CI builds (APK + bundle), in-app updater, Google Play signing config |

Legend: ✅ complete · ⚠️ limited/partial · 🚧 in progress · ⏳ planned

---

## Architecture Overview

- **Client**: Android app written in Kotlin, currently using View-based activities with incremental Jetpack Compose support.
- **Backend**: Firebase Realtime Database for conversations, Firebase Storage for media, Firebase Cloud Messaging for push notifications.
- **Security**: End-to-end symmetric encryption with per-conversation secrets (HKDF + AES-GCM), message authentication via HMAC, secure local persistence using Jetpack DataStore + Android Keystore.
- **CI/CD**: GitHub Actions pipeline building debug/release artifacts and publishing lint results.

---

## Roadmap

### Q1 2024 – Reliability & Security Hardening *(in progress)*

| Goal | Target | Status | Notes |
| --- | --- | --- | --- |
| Canonical MAC payloads for every message type | Feb | ✅ | Unified text and media verification states. |
| Per-conversation key negotiation | Feb | ✅ | RSA-wrapped secrets distributed through Firebase. |
| Jetpack DataStore-backed secure preferences | Mar | ✅ | Replaced legacy shared preferences with encrypted store. |
| Atomic conversation bootstrap | Mar | ✅ | Uses `updateChildren` for crash-safe initialization. |
| Message send failure surfacing | Mar | ✅ | UI now reports Firebase errors to users. |
| Attachment integrity verification | Mar | 🚧 | Hashing file bytes and aligning MAC payload still outstanding. |
| Media download UX refresh | Mar | 🚧 | Material progress dialog introduced; needs Compose migration. |

### Q2 2024 – Feature Expansion *(planned)*

| Goal | Target | Status | Dependencies |
| --- | --- | --- | --- |
| Jetpack Compose UI migration | Apr–Jun | ⏳ | Requires Compose navigation, theming parity. |
| Rich media support (files, audio, link previews) | May | ⏳ | Depends on secure media hashing and storage rules. |
| Conversation actions (delete, reply, edit) | May | ⏳ | Needs server-side fan-out, optimistic UI states. |
| Preference center (theme, privacy toggles) | Jun | ⏳ | Requires Compose settings scaffolding & datastore schema. |
| Block/report flows | Jun | ⏳ | Requires backend policy decisions. |

### Q3 2024 – Scalability & Ops *(planned)*

| Goal | Target | Status | Notes |
| --- | --- | --- | --- |
| Modularize codebase by feature | Jul | ⏳ | Split into `core`, `feature-*`, `data` Gradle modules. |
| Automated regression suite | Aug | ⏳ | Compose UI tests + integration mocks. |
| Telemetry & analytics opt-in | Sep | ⏳ | Privacy-preserving metrics, user consent flow. |

---

## Current Feature Matrix

| Feature | Status | Details |
| --- | --- | --- |
| Conversations | ✅ | Real-time send/receive, delivery timestamps, MAC verification indicator. |
| Media sharing | ⚠️ | Inline photo sharing, gallery uploads; file/audio queued for implementation. |
| User directory | ✅ | Discover users, view public profile data, initiate conversations. |
| Home screen | ✅ | Displays latest chats, allows single-message removal (bulk delete pending). |
| Notifications | ✅ | Firebase Cloud Messaging integration for new messages. |
| Updates | ✅ | In-app update checker + downloader backed by Firebase Storage. |
| Preferences | ⏳ | Planned privacy and appearance controls. |

---

## Getting Started

1. **Clone and configure**
   ```bash
   git clone https://github.com/ARCOOON/UpChat.git
   cd UpChat
   ```
2. **Provide Firebase credentials**
   - Add `google-services.json` to `app/`.
   - Configure Realtime Database & Storage security rules.
3. **Build**
   ```bash
   ./gradlew assembleDebug
   ```
4. **Run tests**
   ```bash
   ./gradlew test
   ```

---

## Contributing

1. Fork the repository and create a branch per feature/fix.
2. Follow Kotlin coding conventions and keep security-sensitive changes well documented.
3. Run lint and unit tests before opening a pull request: `./gradlew lint test`.
4. Document UI/UX changes with screenshots or screen recordings where possible.

Bug reports and feature proposals are welcome through GitHub issues. Please include logs, reproduction steps, and screenshots to speed up triage.

---

## Security Practices

- Secrets are derived per conversation using HKDF and stored encrypted with each participant's RSA key.
- All messages include a canonical MAC payload covering ciphertext and metadata; verification status is surfaced in the UI.
- Local sensitive data is persisted via encrypted Jetpack DataStore backed by the Android Keystore.
- Pending improvements include secure attachment hashing, Compose-based security prompts, and audit logging.

To report vulnerabilities, please email **security@upchat.app** (PGP preferred) rather than opening a public issue.

---

## License

UpChat is distributed under the [Apache 2.0 License](LICENSE).

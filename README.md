# UpChat | Development Channel

UpChat is an open-source, privacy-first messaging application for Android.  
Modern UI, real-time communication stack, zero-nonsense approach to secure messaging.

[![Building Release (v2)](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml/badge.svg?branch=v2)](https://github.com/ARCOOON/UpChat/actions/workflows/build.yml)

---

## Key Value

UpChat is built for developers, power users, and security-conscious individuals seeking a transparent and customizable alternative to mainstream messengers.  
The project focuses on performance, privacy, and modular architecture, enabling long-term maintainability and rapid feature rollout.

---

## Feature Overview

### Core Messaging
| Feature | Status |
|--------|--------|
| Real-time chat | ✅ |
| Send text messages | 🚧 (in development) |
| File sharing | ❌ |
| Photo sharing | ✅ Basic |
| Video sharing | ❌ |
| Audio messages | ❌ |
| Link preview & highlighting | ❌ |
| Reactions, reply, edit, delete | ❌ Planned |
| End-to-end encryption | ✅ AES-based prototype, evolving |

### App Capabilities
| Feature | Status |
|--------|--------|
| Auto-update (in-app) | ✅ |
| Release builds: APK + AAB | ✅ |
| Customizable UI (colors, bubble shape, themes) | 🚧 |
| Privacy controls (block, allow list) | 🚧 |
| View all users | 🚧 |
| User profile views | 🚧 |
| Conversation deletion | 🚧 |
| Account management | 📅 |

---

## Architecture & Tech Stack

| Layer | Technology |
|-------|-----------|
| Platform | Android (Min SDK 28, Target 36) |
| UI | Jetpack Compose |
| DI | Hilt |
| Navigation | Navigation Compose |
| Backend | Firebase Realtime Database & Firebase Auth |
| Security | AES encryption + MAC integrity layer |
| Build | Gradle w/ CI automation |
| Language | Kotlin |

---

## Project Goals

- Independent open-source communication stack
- Modern Android design language
- Strong cryptographic integrity (moving toward fully audited E2E)
- Fast CI/CD release pipeline
- Full modularization for maintainability
- Developer-friendly codebase and documentation

---

## Roadmap (High-Level)

- ✅ Bootstrapped UI and chat core
- ✅ CI w/ GitHub Actions (APK + AAB)
- 🚧 Message sending pipeline rewrite
- 🚧 Fully encrypted messaging lifecycle
- 🚧 Media sharing pipeline (images, videos, audio)
- 🚧 Advanced message actions (edit, delete, reply threads)
- 🚧 Push notifications
- 🚧 Theming engine + UX polish
- 📅 Cloud function support for scalable events
- 📅 Desktop & Web clients (future vision)

---

### _Legend_

| Feature | Status |
|---------|--------|
| ✅ | Finished |
| ❌ | Not finished |
| 🚧 | In Progress / Working on |
| 📅 | Planned |

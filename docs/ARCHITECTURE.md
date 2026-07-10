# Architecture notes

This document matches what is in the **`Raman`** branch codebase and how to talk about it in interviews.

## What this repo contains

| Area | Implementation |
|------|----------------|
| **Client** | Android app (Java/Kotlin), MVVM-style activities, Firebase SDK |
| **Auth & data** | Firebase Auth, Realtime Database, Cloud Messaging |
| **Chat** | Real-time messaging between employers and helpers |
| **Translation** | MyMemory API for multilingual chat |
| **Video interviews** | Jitsi Meet SDK embedded in-app |
| **Employer workflow** | Job posts, helper listings, booking, admin-style search/filter |
| **CV / PDF** | Helper PDF upload flow; receipt export uses Android `PdfDocument` |
| **Matching signals** | Employer criteria and ranking fields (e.g. language skills, `rankingByAge`) stored and surfaced in job/helper flows |

Open the app from the **`FypProject`** folder in Android Studio (not the repo root).

## Matching & ML (interview talking points)

The **FYP report and demo** described a hybrid pipeline:

1. PDF CV upload  
2. Text/skill extraction  
3. Rule-based filters + **scikit-learn** ranking  

That pipeline was part of the **academic deliverable** (including Python/scikit-learn and pdfplumber in the project write-up). The Android repo on `Raman` focuses on the **production demo path**: Firebase-backed profiles, employer criteria, ranking fields, and search/filter — integrated with chat, translation, and video interviews.

If asked in an interview:

- Explain the **designed** ML pipeline from your FYP report.  
- Explain what this **GitHub repo** proves: full Android product integration, team lead, Firebase, Jitsi, multilingual UX, and measurable **35% efficiency gain** in user testing.

## CI

GitHub Actions runs **repository checks** on push (project layout, docs, Gradle wrapper). Full `assembleDebug` builds are not run in CI because Jitsi/Firebase require local `google-services.json` — build in Android Studio instead.

## Security

- Do not commit `google-services.json` or API keys.  
- Use `app/google-services.json.example` as a template.

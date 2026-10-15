# Sophames

*Every day, ours.*

A private couple's app for two: days together, anniversary countdowns and a photo memories timeline.
Native Android (Kotlin, Jetpack Compose, Room), offline-first, with Supabase sync planned.

**Author:** direk james

## What works now
- **Welcome reveal** with a "Licensed to Sophia & James" membership card and a day counter that ticks up
- **Home:** days together, years/months/days breakdown, progress to the next day milestone, next two countdowns, latest memory
- **Countdowns:** next anniversary, monthsaries (every 18th), day milestones (100, 200 ... 1000, 1500 ...), your own dates (yearly repeat optional)
- **Memories timeline:** photos (compressed on-device), title, note, date, grouped by month, view / edit / delete
- **Light-first design** with a dark mode toggle (sun/moon button on Home, plus Light / Dark / System in Settings)
- **Date reminders** (about 9 AM, the day before and on the day)
- Supabase-ready data model (UUID ids, `updatedAt`, soft deletes, `dirty` flag). Sync itself is the next step.

## Open and run
1. Install **Android Studio** (it ships the Android SDK; IntelliJ Community alone is not enough for Android).
2. **File → Open** → this `Sophames` folder, let Gradle sync (first sync downloads dependencies).
3. Plug in a phone (or start an emulator) and press **Run**.

Build an installable APK:
- Android Studio: **Build → Build APK(s)** (debug), or `./gradlew assembleRelease` for the smaller release build.
  Release is signed with the debug key on purpose, so it installs without extra keystore setup.
- Send it to her phone and allow "install unknown apps" once.

## Fonts
The app currently uses Android's built-in serif and sans. For the full premium look, add Fraunces and
Manrope (free, SIL OFL, from Google Fonts) as `app/src/main/res/font/fraunces.ttf` and `manrope.ttf`
and swap the two lines at the top of `ui/theme/Type.kt` (instructions are in that file).

## Supabase
See [SUPABASE_SETUP.md](SUPABASE_SETUP.md). SQL lives in `supabase/schema.sql` and `supabase/link_couple.sql`.

## Project layout
```
app/src/main/java/com/direkjames/sophames/
  data/       Room entities + DAOs, DataStore settings, photo storage
  domain/     Countdown and milestone math (pure Kotlin, easy to test)
  reminders/  Daily WorkManager reminder + notification channel
  ui/theme/   Colors, type, theme (light + dark)
  ui/components/  Cards, buttons, progress, date picker, floating nav bar
  ui/screens/ Welcome, Home, Countdowns, Memories, Editor, Detail, Settings
supabase/     Database schema, security rules, couple linking
```

## Test checklist
1. **First launch:** welcome reveal plays, card shows "Licensed to Sophia & James", Day counter ticks up to today's number, button enables last.
2. **Notification permission** prompt appears on Android 13+ when tapping "Open Sophames".
3. **Home numbers:** on Oct 16, 2026 it should read 636 days and "1 year · 8 months · 28 days". Next countdown: 21st monthsary on Oct 18.
4. **Dark mode:** sun/moon button on Home flips the theme instantly; Settings → Appearance (Light / Dark / System) works; the choice survives closing the app.
5. **Add a memory** with a photo from the gallery: it appears on the timeline and on Home as "Latest memory", photo not sideways.
6. **Edit** the memory (change title, replace the photo, remove the photo) and **delete** it (photo disappears from the timeline).
7. **Countdowns:** add a custom date (yearly on / off), check its "in N days" label, delete it.
8. **Change the start date** in Settings and confirm every counter updates.
9. **Rotate the phone** on the editor screen mid-edit: text and chosen photo are kept.
10. **Airplane mode:** everything above still works.
11. **Reminders:** add a custom date for tomorrow, keep reminders on, and check for a notification around 9 AM (Android can delay it a little to save battery).

# Grokipedia integration notes

## Changes made

The Grokipedia client was added as an isolated Android library module rather than mixed into WikiReader's existing Wikipedia API and data packages:

- Added `:grokipedia` to `settings.gradle.kts`.
- Added the `android-library` plugin alias to `gradle/libs.versions.toml`.
- Created `grokipedia/` with its own Android library build configuration and package `org.nsh07.grokipedia`.
- Added `GrokipediaApi`, Retrofit setup (`createGrokipediaApi`), and serializable response models in `grokipedia/src/main/java/org/nsh07/grokipedia/GrokipediaApi.kt`.
- The client currently exposes read-only search, typeahead, page preview, page listing, stats, and TTS-section endpoints. Its JSON decoder ignores unknown fields, and a custom serializer accepts selected count/position values represented as either JSON strings or numbers.
- Added `implementation(project(":grokipedia"))` to `app/build.gradle.kts`, making the library available to the app at compile time.

## Not implemented

No WikiReader screen, navigation, ViewModel, repository, or Wikipedia model was changed to call the Grokipedia client. There is no user-facing Grokipedia feature yet. The existing Wikipedia data/network layer remains separate. Write operations (suggestions and edit submissions) and CLI-only behavior were not ported.

## Verification

The app and Grokipedia module compile successfully with `:app:assembleDebug`. `:app:installDebug` has succeeded on the TT290 device for earlier revisions. The most recent source compiled, but installation failed when ADB reported no connected device; the final animated-corner crash guard still needs on-device verification.

## Curious Reader package and build integration

- Set the Android `applicationId` to `quest.devinthebm.curiousreader` while retaining the Kotlin namespace `org.nsh07.wikireader`.
- Renamed the launcher label to “Curious Reader” in the default and localized string resources; updated the app's external metadata/links in `README.md` and `discoverium.yml`.
- Added the root Android-library plugin alias and corrected the Retrofit kotlinx-serialization converter coordinate/API so the existing modules build with Retrofit 3.0.0.
- The app uses Java/Kotlin 17. Build commands in this environment use `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.
- Set the Git `origin` remote to `https://github.com/Dev-in-the-BM/Curious-Reader` and updated project-facing repository links in the README, About screen, metadata, and template issue form.

## Yapchik and D-pad UI

- Included the local Yapchik module from `/home/naftali/Tinkering/Libraries/yapchik/yapchik`, installed it from `WikiReaderApplication`, and enabled its softkeys for this keypad-first app.
- Set Yapchik to `HIDE_WITH_BAR`, retained its automatic content inset, and set `navGuardDp = 0`: the activity's Android `Theme.Material` parent is classified as `FRAMEWORK`, which enables Yapchik's automatic guard height. That guard can lift the softkey row; zero disables the extra offset so the row sits flush with the bottom navigation area.
- Yapchik manages the bottom navigation bar, not the top status bar. The status indicators remain visible by design; edge-to-edge does not itself hide either system bar.
- The home search/top bar is hidden at rest. The left Yapchik softkey opens a bottom-left options popup containing Search, Navigation, Share, Save/Remove, and Random; the Wikipedia-language item was intentionally removed because language is available in app settings. The right softkey smoothly scrolls an article to its top.
- Removed the now-unused app-bar and floating-toolbar nested-scroll connections from the home screen so hidden chrome does not participate in feed/article scrolling. Feed and article lists are focusable and handle D-pad up/down with animated list scrolling; navigation rail requests focus when expanded.
- Ordinary Compose buttons/cards remain focusable using their standard Compose interaction semantics. Runtime touch-scroll verification remains pending because the latest ADB install attempt had no connected device; the device had also shown the app's loading/logo state while network was unavailable.
- The Top softkey scroll action uses `scrollToItem` from `viewModelScope`: `animateScrollToItem` crashed because that scope has no Compose `MonotonicFrameClock`. D-pad scrolling animation remains in the Compose UI scope.
- Diagnosed an intermittent article-opening crash from ADB: `RoundedCornerShape` received a spring-animated corner of `-0.071 px`. Animated row/image corner values are now clamped to nonnegative Dp in history, language, and search result rows so spring overshoot cannot create invalid outlines.
- Added `**/build/` to `.gitignore` so module-level Gradle outputs (including `grokipedia/build/`) are not committed.

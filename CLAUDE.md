# Project changes and implementation notes

This file records the changes made to the repository, how they were implemented, and the reasons behind the decisions. It also calls out behavior that is intentionally not implemented.

## App identity and repository

- Changed the Android install ID to `quest.devinthebm.curiousreader` in `app/build.gradle.kts`. The Kotlin/Android `namespace` remains `org.nsh07.wikireader` so existing source imports, generated `R` references, and manifest component names do not need a risky mass rename.
- Changed the launcher label to **Curious Reader** in the default and translated `app_name` resources. Updated repository-facing links in the README, About screen, Discoverium metadata, and template issue form to the new GitHub project.
- Changed the local Git `origin` fetch/push URL to `https://github.com/Dev-in-the-BM/Curious-Reader`. This is repository configuration, not a push; no remote push was performed.

## Android build and Retrofit setup

- Added the root version-catalog alias for `com.android.library` because the new Android library module needs that plugin declared through the root plugin catalog.
- Replaced the obsolete Jake Wharton Retrofit serialization converter coordinate with Square's `com.squareup.retrofit2:converter-kotlinx-serialization`, aligned to Retrofit 3.0.0. Updated `AppContainer.kt` to use `retrofit2.converter.kotlinx.serialization.asConverterFactory`, matching that artifact's API.
- The project targets Java/Kotlin 17. On this workstation, invoke Gradle with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` because the system default JDK 25 is incompatible with the configured Kotlin JVM target.
- Added `**/build/` to `.gitignore` so module-level Gradle intermediates are not accidentally checked in.

## Grokipedia client module

- Added `:grokipedia` as a separate Android library module with package `org.nsh07.grokipedia`, its own `build.gradle.kts`, Retrofit client construction, and serializable response models in `grokipedia/src/main/java/org/nsh07/grokipedia/GrokipediaApi.kt`.
- Kept it separate from the existing Wikipedia network/data packages so the new API models and endpoints do not alter existing Wikipedia behavior. The module currently provides read-only search, typeahead, preview, page-listing, stats, and TTS-section endpoints. Unknown JSON fields are ignored; selected count/position fields accept either JSON strings or numbers.
- Added `implementation(project(":grokipedia"))` to the app so the module is available at compile time.
- **Not implemented:** no screen, navigation destination, repository, ViewModel, or user flow calls Grokipedia yet. Write endpoints (including suggestions and edit submission) and CLI-only behavior were not ported.

## Yapchik integration and window layout

- Included the user-requested local module at `/home/naftali/Tinkering/Libraries/yapchik/yapchik`, added it as `:yapchik`, and called `Yapchik.install(this)` in `WikiReaderApplication.onCreate()`.
- The Gradle project path is intentionally absolute to that local checkout, matching the request to use this library copy. Builds on another machine must provide that path or replace it with a published/copied module dependency.
- Set Yapchik mode to `ON` so softkeys are available even if its automatic keypad-device detector does not recognize the TT290. Set navigation policy to `HIDE_WITH_BAR`; retained Yapchik's automatic content inset so Compose content is not obscured by the injected bar.
- The activity uses an Android `Theme.Material` parent. Yapchik classifies this as `FRAMEWORK` and ordinarily reserves an OEM navigation-strip guard. This app sets `Yapchik.navGuardDp = 0` because the intended layout is for the softkey bar to sit flush at the bottom rather than be lifted by that extra guard.
- Yapchik's navigation-bar policy controls the **bottom navigation bar**, not the **top status bar**. The status icons are therefore intentionally not hidden. `enableEdgeToEdge()` controls how app content lays out around system bars; it does not by itself hide the status bar.
- Softkey actions are registered on the home screen with `Softkeys.of(activity)`: LEFT is Options; RIGHT is Top on article pages. The popup is positioned at the lower-left softkey edge and is opened by the LEFT softkey. No hamburger button is rendered at the bottom of the page.

## D-pad and home-screen UX

- Hid the home search/top bar while idle. Search now appears when chosen from the Options popup, which also contains Navigation, Share, Save/Remove, and Random. The Wikipedia-language option was removed from this popup because language remains available in app settings.
- Removed the old horizontal floating toolbar because its actions now live in the softkey popup. Also removed the now-unused search-app-bar and floating-toolbar nested-scroll connections from the home Scaffold; hidden chrome should not participate in scroll dispatch to article/feed content.
- Made feed and article LazyColumns focusable and gave them D-pad up/down handlers that animate list scrolling. The handlers use a Compose coroutine scope so list animations have the required frame clock. Existing Compose buttons/cards retain their normal focus behavior.
- When the navigation rail expands, a `FocusRequester` sends focus to its first navigation item, so the D-pad can immediately operate the opened menu.
- The Top softkey uses `scrollToItem(0)` in `HomeScreenViewModel`. An earlier `animateScrollToItem(0)` call crashed because it ran in `viewModelScope`, which does not provide Compose's `MonotonicFrameClock`; immediate scrolling avoids that invalid coroutine context.

## Intermittent article-opening crash

- ADB captured an intermittent Compose crash: `RoundedCornerShape` received a bottom corner of `-0.071 px` while recomposition updated a graphics-layer outline. This very small negative value is consistent with a spring-animated Dp corner radius overshooting below zero during a pressed-row release; it explains why the crash was intermittent rather than tied to one article's data.
- Clamped animated corner radii to at least `0.dp` in history rows (including their thumbnails), language rows, and search-history/prefix/result rows before constructing `RoundedCornerShape`. Saved/settings row implementations already clamp their animated shape values.
- The crash was not consistently reproducible, so the defensive clamp is based on the captured failure signature. Build/install was verified after the fix; repeated reproduction testing across problematic articles was not completed.

## Verification

- `./gradlew :app:assembleDebug` succeeds with Java 17.
- `./gradlew :app:installDebug` succeeded on the connected TT290 device with the current source. ADB has intermittently dropped the device between commands, so an individual install attempt may need to be retried after reconnecting it.
- The crash signature was captured from device logcat. D-pad/list scrolling and edge-to-edge/nav-bar behavior were not exhaustively tested across all screens or devices.

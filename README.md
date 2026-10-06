# Step Counter

A mobile app for iPhone and Android that shows your daily step count in Home Screen widgets.
- **iPhone:** reads your steps from Apple Health, with Lock Screen widgets too. Built with SwiftUI, WidgetKit and HealthKit.
- **Android:** reads your steps from Health Connect. Built with Jetpack Compose, Glance and Health Connect.

Neither version uses third-party code.

**[Install on iPhone](#install-it-on-your-iphone)** · **[Download for Android](https://github.com/AhmadBukhari8966/step-counter-widget/releases/latest/download/StepCounter.apk)** · [All releases](https://github.com/AhmadBukhari8966/step-counter-widget/releases)

![iOS 17+](https://img.shields.io/badge/iOS-17%2B-blue) ![Android 9+](https://img.shields.io/badge/Android-9%2B-3DDC84) ![Swift 6](https://img.shields.io/badge/Swift-6-orange) ![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF) ![License: MIT](https://img.shields.io/badge/License-MIT-green)

<p>
  <img src="docs/screenshots/widget-medium.png" width="420" alt="iPhone medium widget: a progress ring with 7,532 steps, 2,468 to go, and a 7-day bar chart">
  <img src="docs/screenshots/android/widget-medium.png" width="380" alt="Android medium widget: a ring with 8,532 steps, 1,468 to go, and a 7-day chart">
</p>

### iPhone

<table>
  <tr>
    <td><img src="docs/screenshots/app-dashboard.png" width="230" alt="The iPhone app: today's steps in a large ring, three stat tiles, and a 7-day bar chart"></td>
    <td><img src="docs/screenshots/widget-extra-large.png" width="230" alt="Extra Large Portrait widget with a ring, bar chart, weekly stats and a day-by-day list"></td>
    <td><img src="docs/screenshots/lock-screen.png" width="230" alt="Lock Screen with a rectangular steps widget and a circular gauge"></td>
  </tr>
  <tr>
    <td align="center">The app</td>
    <td align="center">Extra Large Portrait widget (iOS 27)</td>
    <td align="center">Lock Screen widgets</td>
  </tr>
</table>

### Android

<table>
  <tr>
    <td><img src="docs/screenshots/android/dashboard.png" width="230" alt="The Android app: today's steps in a large ring, three stat tiles, and a 7-day bar chart"></td>
    <td valign="top">
      <img src="docs/screenshots/android/widget-large.png" width="260" alt="Large Android widget with a ring, step count, 7-day chart and weekly totals"><br><br>
      <img src="docs/screenshots/android/widget-small.png" width="125" alt="Small Android widget with a ring and 7,532 steps">
      <img src="docs/screenshots/android/widget-dark.png" width="125" alt="Small Android widget in dark mode">
    </td>
  </tr>
  <tr>
    <td align="center">The app</td>
    <td align="center">Large, small and dark widgets</td>
  </tr>
</table>

## Features

**On both:**
- **App:** today's steps in a progress ring, the last 7 days as a chart, and a daily goal you can change.
- **Five color themes:** Classic, Sunset, Ocean, Forest and Midnight. Each widget can have its own.
- **Refresh button** on the medium and larger widgets.
- **Dark mode** in the app and the widgets.
- **No double counting:** steps from your phone, a watch and fitness apps are combined the way the health app on your phone combines them.
- **Private:** no account, no analytics, no network access. Your data stays on your device.

**On iPhone:**
- **Home Screen widget** in small, medium and large sizes, plus Extra Large Portrait on iOS 27.
- **Lock Screen widgets:** a circular gauge, a rectangular card, and a line of text above the clock.
- **Works everywhere widgets do:** StandBy, CarPlay, and the tinted and clear Home Screen styles.

**On Android:**
- **One widget that adapts to its size:** small (2×2), medium (4×2) and large (4×4). Drag its edges to switch.
- **Classic theme follows your wallpaper's Material You colors**, on Android 12 and later.
- **Works with any step source** that writes to Health Connect, such as your phone's own step tracking, Google Fit, Fitbit or Samsung Health.
- **No 7-day limit:** install the APK once and it keeps working.

<table>
  <tr>
    <td valign="top"><img src="docs/screenshots/widget-small.png" width="150" alt="iPhone small widget: a ring with 7,532 steps of a 10K goal"></td>
    <td valign="top"><img src="docs/screenshots/widget-large.png" width="300" alt="iPhone large widget: ring, step count, 7-day bar chart and weekly totals"></td>
    <td valign="top"><img src="docs/screenshots/android/widget-sunset.png" width="300" alt="Android large widget in the Sunset theme"></td>
  </tr>
  <tr>
    <td align="center">iPhone small</td>
    <td align="center">iPhone large</td>
    <td align="center">Android, Sunset theme</td>
  </tr>
</table>

## Requirements

| | iPhone | Android |
| --- | --- | --- |
| Phone | iOS 17 or later (iPhone or iPad) | Android 9 or later |
| Step data | Apple Health | Health Connect: built into Android 14 and later; on Android 9 to 13, the app sends you to Google Play to install it |
| To install | A Mac with **Xcode 27 or later** and an **Apple ID**. A free one works; see [Free or paid Apple ID](#free-or-paid-apple-id). | Nothing. Download the APK on your phone. |
| To build it yourself | Xcode 27 or later | Android Studio 2026.1 or later |

## Install it on your Android phone

Android lets you install apps from outside the Play Store, and there's no 7-day limit like on iPhone.

1. On your phone, open this link to download the app: **[StepCounter.apk](https://github.com/AhmadBukhari8966/step-counter-widget/releases/latest/download/StepCounter.apk)**. It's also on the [Releases](https://github.com/AhmadBukhari8966/step-counter-widget/releases) page.
2. Open the downloaded file. If Android asks, allow your browser or Files app to install unknown apps.
3. Tap **Install**, then **Open**. If Google Play Protect warns that it doesn't recognize the developer, tap **More details › Install anyway**.
4. In the app, tap **Connect Health Connect**, then **Allow all** and **Allow**, so the app can read your steps.
5. When Health Connect asks whether Step Counter can **access data in the background**, tap **Allow** so the widget can refresh while the app is closed.

To update later, download the newest APK and install it over the old one. Your data and widgets stay.

To build the app yourself with Android Studio, or sign your own builds, see [android/README.md](android/README.md).

<img src="docs/screenshots/android/theme-picker.png" width="200" alt="Android theme picker with Classic, Sunset, Ocean, Forest and Midnight">

## Install it on your iPhone

### 1. Get the code

```sh
git clone https://github.com/AhmadBukhari8966/step-counter-widget.git
cd step-counter-widget
```

Or click **Code › Download ZIP** on GitHub and unzip it.

### 2. Use your own app identifiers

Bundle identifiers and App Groups must be unique across every Apple developer, so replace the ones in this project with yours. In Terminal, from the project folder:

```sh
./scripts/configure.sh com.yourname
```

Use any reverse-DNS prefix, such as `com.janedoe`. The script updates the app, the widget, and the App Group they share.

If you know your 10-character Team ID, add it as a second argument (`./scripts/configure.sh com.janedoe ABCDE12345`) and you can skip choosing a team in step 3. Paid members can find it at [developer.apple.com/account](https://developer.apple.com/account) under Membership details.

<details>
<summary>Prefer to change the identifiers by hand?</summary>

Replace the prefix in these places:

1. **Bundle Identifier** of both targets in Xcode (Signing & Capabilities). The widget's must start with the app's, for example `com.janedoe.StepCounter` and `com.janedoe.StepCounter.Widget`.
2. **App Group** in both targets' Signing & Capabilities, for example `group.com.janedoe.StepCounter`. This edits `StepCounter/StepCounter.entitlements` and `StepCounterWidget/StepCounterWidget.entitlements`.
3. **`AppGroup.identifier`** in `Shared/AppGroup.swift`, set to the same App Group.

</details>

### 3. Sign in and choose your team in Xcode

1. Open `StepCounter.xcodeproj`.
2. Go to **Xcode › Settings › Accounts**, click **+**, choose **Apple ID**, and sign in.
3. In the left sidebar, click the blue **StepCounter** project at the top. If the sidebar shows something else, press ⌘1.
4. Under **Targets**, select **StepCounter**, open **Signing & Capabilities**, and set **Team** to your name or organization. If you don't see the Targets list, click the sidebar button at the top-left of the editor.
5. Do the same for **StepCounterWidgetExtension**.

Xcode saves your team in the project file. To keep it out of your commits, put it in `Config/Signing.local.xcconfig` instead, which Git ignores. `configure.sh` writes that file when you give it a Team ID.

### 4. Run it on your iPhone

1. Connect your iPhone with a cable, unlock it, and tap **Trust**.
2. At the top of the Xcode window, click the device name and choose your iPhone.
3. Press **▶ Run** (⌘R). The first run can take a few minutes while Xcode prepares the phone.
4. If your iPhone asks for **Developer Mode**, turn it on in Settings › Privacy & Security › Developer Mode. The phone restarts; confirm, then press Run again.
5. **Free Apple ID only:** if the app won't open, go to Settings › General › VPN & Device Management, tap your Apple ID, and tap **Trust**. Then open Step Counter from the Home Screen.

Use **Run**, not **Build**. Build only compiles; Run installs the app and opens it. After the first cable connection, Xcode can usually install over Wi-Fi when your Mac and iPhone are on the same network.

### 5. Connect Apple Health

Open Step Counter, tap **Connect Apple Health**, turn on **Steps**, and tap **Allow**. On iOS 27, Health also asks how much history to share. Either choice works, because the app reads only the last 7 days.

<img src="docs/screenshots/app-onboarding.png" width="230" alt="First-run screen: a walking figure in an orange circle, 'Your Steps at a Glance', and a Connect Apple Health button">

## Add the widgets

### On Android

- **From the app:** scroll down and tap **Add to Home Screen**.
- **From the Home Screen:** touch and hold an empty spot, tap **Widgets**, and find **Step Counter**.
- **Resize it:** touch and hold the widget and drag its edges. It switches between the small, medium and large layouts as it grows.
- **Change its colors:** touch and hold the widget, tap **Settings**, and pick a theme.
- **Refresh:** tap **↻** on the medium or large widget, or open the app.

### On iPhone

- **Home Screen:** touch and hold an empty spot, tap **Edit › Add Widget**, search for "Step Counter", pick a size, and tap **Add Widget**. On iOS 18 and later you can also touch and hold the Step Counter icon and pick a widget size from the top of its menu.
- **Lock Screen:** touch and hold the Lock Screen, tap **Customize › Lock Screen**, tap the widget area, and choose Step Counter.
- **Change a widget's colors:** touch and hold it, choose **Edit Widget**, and pick a **Theme**.
- **Refresh:** tap **↻** on a medium or larger widget, or open the app.

<table>
  <tr>
    <td valign="top" align="center"><img src="docs/screenshots/theme-picker.png" width="230" alt="Edit Widget sheet with the theme menu open: Classic, Sunset, Ocean, Forest, Midnight"><br>Theme picker</td>
    <td valign="top" align="center">
      <img src="docs/screenshots/widget-dark.png" width="380" alt="Medium widget in dark mode"><br>Dark mode<br><br>
      <img src="docs/screenshots/widget-clear.png" width="380" alt="Medium widget in the clear Home Screen style, drawn in white on glass"><br>Clear Home Screen style (iOS 26+)
    </td>
  </tr>
</table>

## Free or paid Apple ID

This only applies to iPhone. The Android APK has no time limit.

| | Free Apple ID | Apple Developer Program ($99/year) |
| --- | --- | --- |
| Install on your own iPhone | Yes | Yes |
| How long an install lasts | 7 days | 1 year |
| TestFlight and the App Store | No | Yes |

With a free Apple ID the app stops opening after 7 days. To renew it, connect your iPhone and press **Run** in Xcode again. That reinstalls over the old copy, so your Health permission, goal and widgets stay.

## How it works

### iPhone

```mermaid
flowchart LR
    Health[(Apple Health)] -->|statistics query| App[Step Counter app]
    Health -->|statistics query, while unlocked| Widget[Widgets]
    App -->|latest totals and goal| Group[(Shared App Group)]
    Group -->|last saved totals, while locked| Widget
    Health -.->|background delivery, up to hourly| App
```

- **Reading steps:** a HealthKit statistics collection query sums the last 7 days. HealthKit merges iPhone and Apple Watch samples the same way the Health app does, so the totals match.
- **Sharing with the widget:** the app and widget extension share an App Group, which holds the latest daily totals and the goal.
- **While the iPhone is locked:** iOS encrypts Health data, so the widget can't read it then. It shows the last totals the app or widget saved instead.
- **Refreshing:** the widget asks for a new timeline about every 15 minutes, as often as iOS allows. It also schedules a reset to 0 at midnight.
- **In the background:** the app registers a HealthKit observer with background delivery. iOS wakes it when new steps are recorded (at most hourly for steps), and it reloads the widgets only if the numbers changed, to save the widgets' refresh budget.
- **Interactive pieces:** the refresh button and the theme option are App Intents.

### Android

```mermaid
flowchart LR
    HC[(Health Connect)] -->|aggregate query| App[Step Counter app]
    HC -->|aggregate query, with background access| Job[Background job]
    App -->|latest totals and goal| Store[(Saved on the phone)]
    Job --> Store
    Store --> Widget[Widget]
    Job -.->|about every 15 minutes| Widget
```

- **Reading steps:** Health Connect's aggregation adds up the last 7 days per day. It follows the priority order in Health Connect's settings, so steps from a phone and a watch aren't counted twice.
- **Sharing with the widget:** the app and widget save the latest totals and your goal on the phone, so the widget always has something to show.
- **Refreshing:** a background job asks to run every 15 minutes. Android decides when it actually runs and can hold it back for an hour or more to save battery. Each run reads Health Connect (if you've allowed background access) and redraws the widget, which also resets it to 0 after midnight. Opening the app or tapping ↻ always refreshes right away.
- **Drawing:** Android widgets can't draw shapes, so the ring and the bar chart are drawn into images at the widget's exact size.

## Project structure

| Path | What's in it |
| --- | --- |
| `StepCounter/` | The iPhone app: SwiftUI screens, the view model, and HealthKit background updates |
| `StepCounterWidget/` | The iPhone widget extension: timeline provider, widget layouts, and App Intents |
| `Shared/` | Built into both iPhone targets: HealthKit queries, the shared cache and goal, themes, and the ring and bar chart views |
| `Config/` | iPhone signing settings. Your Team ID goes in `Signing.local.xcconfig`. |
| `scripts/configure.sh` | Switches the iPhone bundle IDs, App Group and team to yours |
| `android/` | The Android app, with its own [README](android/README.md) |
| `android/app/src/main/java/.../data/` | Health Connect queries, the step snapshot, and saved totals and goal |
| `android/app/src/main/java/.../ui/` | The Android app's screens, ring and chart |
| `android/app/src/main/java/.../widget/` | The Android widget's layouts, themes, theme picker, refresh button and background job |

The iPhone folders are synchronized with Xcode, so new files you add there are picked up automatically.

## Development

**iPhone:**
- **Previews:** open `StepCounterWidget/StepsWidget.swift` and show the canvas to see every widget size with sample data.
- **Simulator:** the iOS Simulator has Apple Health, but it starts with no steps. Open the Health app in the Simulator, find **Steps**, and add some data.
- **Debugging the widget:** run the **StepCounterWidgetExtension** scheme to launch the widget directly on the Simulator's Home Screen.
- **Code style:** Swift 6 language mode with strict concurrency. Please keep builds free of warnings.
- **Widget fonts:** in widgets, set fonts as `.system(.title2, design: .rounded).weight(.bold)`. On iOS 27, widgets drop the weight and design you pass to `.system(_:design:weight:)`. `Font.rounded(_:)` in `Shared/StepTheme.swift` does this for you.

**Android:**
- **Open the project:** in Android Studio, choose **File › Open** and select the `android` folder.
- **Emulator:** an emulator running Android 14 or later has Health Connect but no steps. Debug builds include a helper that loads a sample week:

  ```sh
  adb shell pm grant com.ahmadbukhari.stepcounter android.permission.health.WRITE_STEPS
  adb shell am broadcast -n com.ahmadbukhari.stepcounter/.debug.SeedStepsReceiver -f 32
  ```

- **Release builds:** `./gradlew assembleRelease` in the `android` folder. See [android/README.md](android/README.md#signing-your-own-builds) for signing with your own key.

## Troubleshooting

### iPhone

| Problem | Fix |
| --- | --- |
| "Signing for StepCounter requires a development team" | Choose your team for both targets ([step 3](#3-sign-in-and-choose-your-team-in-xcode)). |
| "Failed to register bundle identifier" or the App Group isn't available | Someone else already uses that identifier. Run `./scripts/configure.sh` with a different prefix. |
| The app won't open after installing | Trust your developer profile ([step 4](#4-run-it-on-your-iphone)). With a free Apple ID, 7 days may have passed; press Run again. |
| The app shows 0 steps | Make sure Steps is allowed in Settings › Privacy & Security › Health › Step Counter, and that Fitness Tracking is on in Settings › Privacy & Security › Motion & Fitness. |
| The widget isn't up to date | iOS limits how often widgets refresh. Open the app or tap ↻. While the phone is locked, the widget shows the last saved count. |

### Android

| Problem | Fix |
| --- | --- |
| The app shows 0 steps | Open Health Connect › Data and access › Steps and check that steps are recorded there. Under **Data sources and priority**, make sure the app that records your steps is listed. |
| The widget only updates when you open the app | Allow background access: Health Connect › App permissions › Step Counter › **Access data in the background**. |
| "Get Health Connect" appears | You're on Android 13 or earlier. Install Health Connect from Google Play, then open Step Counter again. |
| The widget picker doesn't show Step Counter | Open the app once after installing it. |
| "App not installed" when updating | The new APK was signed with a different key. Uninstall the old version first. |

## Privacy

Step Counter only reads your step count. It doesn't write to Apple Health or Health Connect, doesn't use the network, and doesn't collect analytics.
- **iPhone:** totals are stored in the App Group on your device so the widget can show them. Both targets include a privacy manifest (`PrivacyInfo.xcprivacy`).
- **Android:** daily totals are stored on your phone and deleted when you uninstall the app. Health Connect shows the app's privacy explanation from its permission screen.

## Contributing

Issues and pull requests are welcome. For a pull request, please describe what you changed and how you tested it, and make sure the project builds without warnings.

## License

Step Counter is released under the [MIT License](LICENSE).

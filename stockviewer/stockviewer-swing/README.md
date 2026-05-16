# 📈 StockViewer — Kotlin/Swing Stock Chart App

A dark-themed desktop stock market viewer built with Kotlin and Java Swing.
Data is pulled live from the **Alpha Vantage** free API.

---

## Features

| Feature     | Details                                   |
|-------------|-------------------------------------------|
| Chart types | Candlestick · Line · Area                 |
| Periods     | 1W · 1M · 3M · 6M · 1Y · 2Y · 5Y · ALL    |
| Stats bar   | Open, High, Low, Close, Change %, # Bars  |
| Any symbol  | Type any ticker (IBM, AAPL, TSLA, MSFT …) |
| Live data   | Alpha Vantage daily-adjusted endpoint     |

---

## Prerequisites

| Tool   | Version                                  |
|--------|------------------------------------------|
| JDK    | 17+ (21 recommended)                     |
| Gradle | Bundled via wrapper — nothing to install |

---

## Quick start

```bash
# 1 — unzip / enter the folder
cd stockviewer

# 2 — run directly (Gradle downloads Kotlin automatically)
./gradlew run          # Linux / macOS

# 3 — OR build a fat-jar and double-click / run anywhere
./gradlew jar
java -jar build/libs/stockviewer.jar
```

---

## API Key

The app ships with `demo` as the API key, which only works for the
**IBM** ticker and is rate-limited to a few requests per minute.

To use any ticker get a **free** key at <https://www.alphavantage.co/support/#api-key>
(free tier = 25 requests / day, 500 / month) and set it in the source:

```kotlin
// StockViewer.kt  line ~35
private const val API_KEY = "YOUR_KEY_HERE"
```

Then rebuild:

```bash
./gradlew jar
```

---

## Project layout

```
stockviewer/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/kotlin/com/stockviewer/
    └── StockViewer.kt          ← entire app (~350 lines)
```

---

## Keyboard shortcut

Press **Enter** inside the symbol field to load data without clicking the button.

---

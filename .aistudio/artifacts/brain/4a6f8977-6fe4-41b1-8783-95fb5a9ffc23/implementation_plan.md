# Q3E System Companion: Wi-Fi, FM Radio, Bluetooth & Power Manager (Ktheme Edition)

A high-efficiency system utility and hardware manager designed specifically for compact portable media players (like the Timkoo Q3E) and Android devices, featuring smart battery-saving Wi-Fi auto-switching, direct hardware FM chip tuner access with fallback audio, Bluetooth connection & audio device management, and Kaleaon's Ktheme design system.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following revisions have been incorporated based on your feedback:
> - **Direct Hardware FM Chip Access**: In addition to launching any existing OEM radio app, the app now includes a **Direct FM Chip Driver** using reflection on `IFMRadioService`, MediaTek FM hardware bindings, `/dev/radio0` device node detection, and `AudioSystem` FM audio routing. This allows tuning frequencies directly on the physical FM chip even if no OEM FM radio app is installed.
> - **Bluetooth Device & Audio Management**: Added a dedicated **Bluetooth Manager** to scan, pair, connect, and disconnect Bluetooth headphones, speakers, and car kits. Includes connected device battery indicators and auto-disables discovery after connection to conserve battery.
> - **Wi-Fi Auto-Switching & Power Management**: Smart battery-saver policy. Conserves battery by remaining idle when connected to a healthy access point, only initiating low-power scans when signal drops below threshold (-75 dBm) or disconnects.
> - **Design System (`ktheme`)**: Implements Kaleaon's modular Ktheme architecture with three authentic palette packs: **Terminal & Neon** (OLED true dark with glowing cyan/green accents, ideal for compact media player screens and maximum battery savings), **Lumiya Classic Dark** (deep slate graphite with amber highlights), and **Frutiger Aero / Daylight** (clean translucent surfaces with vivid sky blue).

---

## 1. Overview & Core Concept

### What It Does
The app gives Timkoo Q3E and portable Android media player users complete command over connectivity, radio entertainment, and battery consumption:
1. **Wi-Fi Hub**: Scans available access points, displays real-time signal strength (RSSI), detects captive portals / login requirements, facilitates quick connection with credential saving, and automates seamless switching to known, higher-strength networks.
2. **FM Radio Manager & Direct Chip Driver**:
   - **Direct Chip Tuning**: Directly controls the device's FM radio tuner chip via MediaTek/AOSP `IFMRadioService` reflection and hardware intents when no OEM FM app is installed.
   - **OEM App Launcher**: One-tap trigger if the Timkoo Q3E has a pre-installed vendor radio app.
   - **Antenna Detection**: Detects 3.5mm headphone plug state (required as the FM antenna on handheld players).
   - **Live Audio Fallback**: Embedded digital audio streams and presets when the physical chip is unavailable or headphones are unplugged.
3. **Bluetooth Manager**:
   - Toggle Bluetooth power state.
   - Discover and list nearby audio peripherals (headphones, earbuds, speakers).
   - Manage paired devices with one-tap Connect/Disconnect audio profile (A2DP/Headset).
   - Battery-saving discovery: Automatically terminates scanning after 12 seconds or upon successful connection.
4. **Power & Device Telemetry**: Monitors battery level, charging current, temperature, and Wi-Fi/Bluetooth power states, with a one-tap "Ultra Battery Saver" toggle.
5. **Ktheme Palette Engine**: Dynamic theme switching between Kaleaon's signature palettes (Terminal & Neon, Lumiya Dark, Frutiger Aero) tailored for small touch displays (e.g., 4.0-inch 480×854).

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Wi-Fi Optimization & Auto-Switching Flow**:
   - Displays current connection status: SSID, BSSID, IP address, Link Speed (Mbps), Signal Quality (dBm meter), and Internet reachability.
   - Available networks are listed with signal badges, security indicators (Open, WEP, WPA2, WPA3), and captive portal badges.
   - Smart Auto-Switch monitors signal degradation and switches smoothly to higher-strength saved networks without dropping packets.

2. **FM Radio & Direct Chip Control Flow**:
   - The user opens the **FM Radio** tab.
   - The app checks the physical hardware:
     - Probes for MediaTek / AOSP `IFMRadioService` or `/dev/radio0` chip interface.
     - Checks 3.5mm headphone jack state (alerts user: *"Plug in wired headphones to act as FM antenna"*).
   - The user drags the tactile frequency slider (87.5 MHz – 108.0 MHz) or taps frequency step buttons (+/- 0.1 MHz).
   - When the user presses **Power / Tune**, the app calls the Direct Chip Driver to power up the FM receiver and routes audio to the headset/speaker via `AudioSystem`.
   - If the device lacks a compatible physical chip, it automatically provides the high-fidelity web stream backup with station metadata and presets.

3. **Bluetooth Audio & Device Flow**:
   - The user opens the **Bluetooth** tab.
   - View Bluetooth toggle, local adapter name, and currently connected audio device (with battery percentage badge if reported).
   - Tap **"Scan for Devices"**: Animates scanning radar; lists discovered Bluetooth devices categorized by audio type (Headphones, Speaker, Car, Other).
   - Tap any paired or discovered device to initiate pairing or connect to the A2DP audio profile.
   - Battery-friendly auto-stop halts radio discovery once connected or after 12 seconds.

4. **Battery & Power Manager Flow**:
   - The user views battery percentage, estimated runtime remaining, battery temperature, and radio power states.
   - Profiles: *Performance Mode*, *Balanced Mode*, and *Ultra Saver Mode* (halts background scanning, enforces short screen timeout).

5. **Ktheme Appearance Flow**:
   - Seamlessly switch between:
     - **Terminal & Neon** (Default): Pure `#05080F` OLED background, `#00F0FF` electric cyan, `#00FF9D` neon indicators.
     - **Lumiya Dark**: `#0D1117` slate background, `#F59E0B` warm amber accents.
     - **Frutiger Aero**: Glossy `#E2E8F0` surface, `#0EA5E9` sky blue accents.

---

## 3. Technical Architecture & Component Structure

### Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                               UI Layer                                 │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                          Q3eMainScaffold                         │  │
│  │   TopBar (Ktheme Selector, Battery Status Badge, Power Quick-Bar) │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│        │                 │                    │                  │     │
│  ┌─────▼───────┐   ┌─────▼──────────┐   ┌─────▼──────────┐ ┌─────▼───┐ │
│  │ WifiScreen  │   │ FmRadioScreen  │   │BluetoothScreen │ │PowerScrn│ │
│  │ ─────────── │   │ ────────────── │   │ ────────────── │ │─────────│ │
│  │ • Current AP│   │ • Direct Chip  │   │ • Scan & Pair  │ │• Battery│ │
│  │ • RSSI Bar  │   │ • Tuner Dial   │   │ • Audio Sink   │ │• Profile│ │
│  │ • AutoSwitch│   │ • Headphone Det│   │ • Disconnect   │ │• Saver  │ │
│  └─────────────┘   └────────────────┘   └────────────────┘ └─────────┘ │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────────────┐
│                            ViewModel Layer                             │
│  ┌──────────────────────┐  ┌─────────────────┐  ┌──────────────────┐   │
│  │    WifiViewModel     │  │  RadioViewModel │  │BluetoothViewModel│   │
│  └──────────────────────┘  └─────────────────┘  └──────────────────┘   │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────────────┐
│                       Hardware & Services Layer                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ FmChipManager                                                    │  │
│  │  • MediaTek IFMRadioService & AOSP hardware reflection probe     │  │
│  │  • Broadcast intents: com.mediatek.FMRadio.FMRadioService        │  │
│  │  • Headset plug BroadcastReceiver (WiredHeadsetReceiver)         │  │
│  │  • AudioSystem FM device audio routing + Web stream fallback     │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ BluetoothManagerHelper                                           │  │
│  │  • BluetoothAdapter, BluetoothA2dp, BluetoothHeadset profiles    │  │
│  │  • Timed discovery (12s auto-stop) & Battery Level listener      │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ WifiNetworkManager                                               │  │
│  │  • WifiManager, ConnectivityManager, Captive Portal checker      │  │
│  │  • Smart hysteresis auto-switcher (-75 dBm threshold)            │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ KthemeManager (Terminal/Neon, Lumiya Dark, Frutiger Aero)         │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Direct FM Chip Implementation Details
1. **Reflection Probe**:
   - Queries `android.os.ServiceManager.getService("fm_radio")` or `ServiceManager.getService("FmRadio")`.
   - Probes `Class.forName("com.mediatek.fmradio.IFMRadioService$Stub")` and `Class.forName("android.hardware.fmradio.IFmRadioService$Stub")`.
   - Probes vendor service intents (`com.mediatek.FMRadio.FMRadioService`, `com.android.fmradio.FmRadioService`).
2. **Audio Routing**:
   - Audio routing via `android.media.AudioSystem` (`DEVICE_OUT_FM`, `DEVICE_IN_FM_TUNER`) or `AudioTrack`/`AudioRecord` loopback.
3. **Headset Antenna Guard**:
   - Registers `Intent.ACTION_HEADSET_PLUG` to notify user if antenna is missing and offer web fallback.

### Bluetooth Manager Implementation Details
1. **Adapter & Permissions**:
   - Manages `BLUETOOTH`, `BLUETOOTH_ADMIN`, and modern runtime permissions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` for Android 12+, with backward-compatible handling for older Android).
2. **Profile Proxy**:
   - Connects to `BluetoothProfile.A2DP` and `BluetoothProfile.HEADSET` to query connected device states and signal routing.
3. **Power-Efficient Discovery**:
   - Uses `bluetoothAdapter.startDiscovery()` with a strict 12-second coroutine cancellation timer to prevent battery drain.

---

## 4. Implementation Steps (Upon Approval)

1. **Theme & Palette Tokens (`Ktheme.kt`)**: Define Kaleaon's Ktheme color tokens, typography, and dynamic palette provider.
2. **Direct FM Chip & Audio Engine (`FmChipManager.kt`)**: Implement hardware reflection, broadcast driver, antenna receiver, and fallback audio player.
3. **Bluetooth Controller (`BluetoothManagerHelper.kt`)**: Implement device scanning, bonding, A2DP profile connection, and battery level reading.
4. **Wi-Fi Controller & Auto-Switch Engine (`WifiManagerHelper.kt`)**: Implement reactive Wi-Fi scanning, RSSI calculation, captive portal detection, and battery-friendly auto-switching logic.
5. **Battery & Power Monitor (`BatteryHelper.kt`)**: Implement battery telemetry and low-power interval controls.
6. **Composables & Screens**:
   - `WifiScreen`: Signal meters, network cards, connect dialogs, auto-switch configuration.
   - `FmRadioScreen`: Direct chip tuner dial, antenna status badge, frequency presets, volume, stream fallback.
   - `BluetoothScreen`: Scan radar, paired devices, discovered audio devices, connection buttons.
   - `PowerScreen`: Battery gauge, runtime estimator, power-saving profiles.
   - `Q3eMainScaffold`: Compact bottom navigation and quick Ktheme switcher.
7. **Permissions & Manifest**: Configure permissions for Wi-Fi, Bluetooth (including legacy and modern Android), Location, Audio, and WakeLock.
8. **Verification**: Run `compile_applet` and verify zero errors.

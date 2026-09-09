# Xune-HD

<div align="center">

# 📱 Xune-HD
**The Zune HD, reborn as an Android music player — sister app to Not-Zune**

[![Android](https://img.shields.io/badge/Platform-Android%209%2B-3DDC84)]()
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)]()
[![Audio](https://img.shields.io/badge/Audio-Media3%20%2F%20ExoPlayer-FA2A55)]()
[![License](https://img.shields.io/badge/License-MIT-111111)]()

</div>

---

Xune-HD is a faithful re-creation of the **Microsoft Zune HD on-device
interface** (2009) for modern Android phones: white-on-black typography-first
Metro design, a text menu cropped at the screen edge, Quickplay parked "left
and rear", crossbar navigation, swipe-to-skip Now Playing floating over artist
photography, and the tri-state heart rating.

It is the **device-side sibling** of [Not-Zune](https://github.com/Heretek-AI/not-zune)
(the Zune *desktop* re-implementation). Just as the Zune HD was the device and
Zune 4.8 was the desktop, Xune-HD is the phone and Not-Zune is the desktop.

## ✨ What's implemented

- **Home menu** — giant lowercase items cropped at the right screen edge;
  slide right to reveal **Quickplay**.
- **Quickplay** — Now Playing card, **Pins** (long-press anything to pin),
  **History**, **New**.
- **Music crossbar** — `albums · artists · playlists · songs · genres`, flick
  horizontally to switch pivots.
- **Alphabet rail** — tap/drag the faint letters to jump through lists.
- **Now Playing** — floats over artist photography (MusicBrainz-MBID keyed,
  user-configurable catalog server), swipe ← → to skip, tap for the transport
  overlay (play/pause center, volume top/bottom, prev/next sides, swipe ↑ ↓
  for volume), tri-state heart rating, and the idle **screensaver** with
  slow-drifting metadata.
- **Tap-the-cut-off-header to go back** — the Zune HD's signature navigation,
  on every screen.
- **Device mode** — the entire UI re-rendered inside a letterboxed 480×272
  canvas, the authentic Zune HD screen (hold the phone in landscape).
- **Accent themes** — zune pink, orange, electric cyan, vivid lime, deep
  purple. Palette-driven color wash on Now Playing.

## 📄 Design canon

`docs/zune-hd-ui-canon.md` is the authoritative interaction spec, compiled
from 2009 reviews and community resources. `docs/design-tokens.md` holds the
portable token set. The unit-test suite audits screens against the invariant
list (zero corner radius, no Material leakage, heart-not-stars).

## 🔒 Licensing posture

- **Not-Zune** (MIT) — design tokens, concepts. Thank you.
- **MedTune** (MIT) — starting skeleton; see NOTICE.md.
- **MusicIn2001** — Research-Only license: used strictly as a behavioral
  specification. **No code was copied.**
- **Selawik** (SIL OFL 1.1) — Segoe-metric stand-in for Zegoe. Import your
  own Zegoe if you have it.
- Zune, Zegoe and the Zune HD are Microsoft trademarks. Xune-HD is an
  independent homage; nothing Microsoft is bundled.

## 🛠️ Building

```bash
# Android SDK + JDK 17+ required; local.properties points at your SDK
./gradlew assembleDebug          # debug APK
./gradlew test                   # unit tests + design-invariant audit
./gradlew lint                   # android lint
```

The release build minifies with R8; keep rules in `app/proguard-rules.pro`.

## 🗺️ Roadmap

- Wi-Fi sync with Not-Zune: the phone enrolls as a Zune-HD-like device in
  Not-Zune's sync engine (JSON manifest, ZMDB-style database).
- Podcasts & video pivots, home-screen widget, Zune-style lock shade.
- Real MTP/MTPZ sync to a physical Zune HD over USB host (stretch).

---

*"The Zune HD's UI is everything but an example of Apple minimalism."* —
Gizmodo, 2009

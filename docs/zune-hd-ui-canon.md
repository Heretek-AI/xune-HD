# Zune HD UI Canon

The authoritative description of the Microsoft Zune HD on-device interface that
Xune-HD replicates. Compiled from contemporaneous reviews (CNET, Gizmodo,
SlashGear, TechCrunch, Windows Central, ZDNet, PCMag, ITPro Today, Sept 2009),
the ZuneDev/Not-Zune decompilation corpus, and ZuneRedux community resources.
Every screen in Xune-HD must be checkable against this document.

## 1. Hardware & canvas

- Display: 3.3" capacitive multi-touch OLED, **480x272** (wide), Tegra APX.
- Buttons: power/hold (top), home (below screen), media/quickplay (left edge).
- On Android we re-render this canvas at `480x272` design units (see
  `DeviceCanvas`) for **device mode**, and reflow the same components for
  **adaptive mode** on tall phones.

## 2. Core design language (Metro / Zegoe)

- White text on matte black. Typography **is** the UI; chrome is banned.
- Font: Zegoe UI (Microsoft-modified Segoe). We ship **Selawik** (OFL,
  Segoe-metric) by default; users may import Zegoe themselves.
- Menu labels are lowercase, oversized, and **deliberately cropped at the
  right screen edge** (Gizmodo: "the word marketplace is cut off at the
  penultimate letter").
- Opacity communicates state: active 100%, neighbors ~40%, metadata ~60%.
- Zero corner radius, zero drop shadows, no skeuomorphism. Ever.
- Accent color themes: Zune pink `#FA2A55` (default), orange `#F09609`,
  cyan `#1BA1E2`, lime `#339933`, purple `#A200FF`.
- Kinetic scrolling with strong deceleration (cubic/exponential ease-out).
- Lists stagger their entrance (~15-25 ms per item cascading slide).

## 3. Navigation model

### 3.1 Home menu (default view)
A vertical text list: `music · videos · pictures · radio · marketplace ·
social · podcasts · internet · settings` (Zune HD firmware 4.x). In Xune-HD
the functional entries are `music` and `settings`; future pivots may join.
- Flick vertically to scroll (kinetic).
- Tap an entry to enter. The whole entry is the button — text only.
- Items are cropped at the right edge as a signature.

### 3.2 Quickplay (the "left screen")
- **Slide the home menu right** to reveal Quickplay parked "left and rear"
  with a parallax 3D slide (Windows Central: "left and to the rear, in a bit
  of visual 3D trickery").
- Quickplay contains: **Now Playing** (current track card: art, title,
  artist), **Pins** (user-pinned items — long-press anything to pin),
  **History** (recently played), **New** (recently added).
- Purpose: bypass collection drilling; reach relevant content instantly.
- On device the left-edge hardware button summons it; in Xune-HD it is the
  home screen's left page.

### 3.3 Crossbar ("sub-menus arrayed left to right across the top")
- Inside `music`: top crossbar row is `albums · artists · playlists · songs ·
  genres` (real Zune order).
- Flick horizontally on the crossbar (or content) to switch pivots; content
  slides horizontally beneath the fixed crossbar.
- Lists scroll vertically: textual (artists, genres) or grid (albums).

### 3.4 Back gesture = tap the cut-off header
- There is no hardware back. The **partially visible top-of-screen heading**
  (e.g. the bottom of "SETT" on the Settings screen) is the back button
  (ZDNet Quick Start Guide discovery; Gizmodo confirms).
- Now Playing is the exception: it shows an explicit left-arrow back button
  (Gizmodo).

### 3.5 Alphabet jump
- In long lists, faint letters run alongside the list (SlashGear).
- Tap any letter → a full A–Z index pops up → pick a letter to jump.

## 4. Now Playing (the signature screen)

Layout (from ITPro Today's walkthrough):
- Explicit **back arrow** top-left.
- **Artist** name and **album** name (tappable: artist → their crossbar page
  with albums/songs/bio/photos/related; song title → the album's track list).
- **Album art** prominent.
- Bottom row: **shuffle**, **repeat**, **rating** (heart / broken heart).
- The card **floats over artist photography** fetched from zune.net keyed by
  MusicBrainz ID (see `net/` in Xune-HD; recreated by ZuneArtistImages).

Interactions:
- **Idle for a few seconds → screensaver**: metadata (artist, track, album,
  length, art) slowly scrolls over the artist photo, "super-smooth".
- **Swipe left/right → skip** to next/previous track.
- **Tap empty space → transport overlay**: play/pause center, volume up /
  volume down at top/bottom, previous/next at left/right (ITPro Today).
  Also summoned by the device's media button.
- **Swipe up/down (overlay visible) → volume** up/down.

Ratings (tri-state heart, from the Zune desktop/HD family):
- Heart = favorite (prioritized by Smart DJ/Quickplay).
- Broken heart = dislike (skipped in shuffle).
- Unrated = neutral.

## 5. Lock/wake behavior
- Wake shows the user wallpaper behind a "software shade"; **slide the shade
  up** to reveal the home screen (ITPro Today). Xune-HD v1 treats the Android
  lock screen as the wake surface; the in-app shade is a stretch goal.

## 6. Motion rules
- Navigation: content slides horizontally with deceleration; deeper screens
  slide in from the right while the parent dims slightly leftward.
- Crossbar pivot switch: horizontal slide, no bounce.
- Kinetic lists: fling, then long deceleration to rest.
- Everything fast: Metro is "designed to feel fast and responsive".

## 7. Banned in Xune-HD (invariant list, mirrors Not-Zune)
- `RoundedCornerShape` / any nonzero corner radius on UI chrome.
- Drop shadows on text/buttons; gradient chrome; skeuomorphic textures.
- Icon-only navigation in the main hierarchy (text is navigation).
- Material default colors/typography leaking into screens.
- Star ratings (Zune is heart-based, tri-state).

## 8. Reference library
- not-zune (desktop) design-system skill + extracted Zune assets (MIT).
- ZuneRedux/zune-hd-apps — original HD app archive (design reference only).
- spidersandmoths/ZuneArtistImages — recreated catalog.zune.net semantics.
- zuneupdate.com — community resource server (resources.zune.net).
- BillyOutlast/MusicIn2001 — behavioral spec only (Research-Only license).
- Period reviews, Sept 2009 (listed above) for interaction ground truth.

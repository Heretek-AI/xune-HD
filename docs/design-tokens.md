# Xune-HD Design Tokens

Ported from the Not-Zune `zune-design-system` skill (MIT) and adapted for the
Zune HD device UI. All Compose code must consume these tokens; raw hex literals
in screens are an invariant violation.

## Surfaces
| Token | Hex | Usage |
|---|---|---|
| `SurfaceBackground` | `#111111` | App canvas |
| `SurfaceElevated` | `#181818` | Panels, crossbar strip |
| `SurfaceTile` | `#202020` | Album art placeholder, cards |
| `SurfaceTileHover` | `#2A2A2A` | Pressed/hovered tile |
| `SurfaceBorderSubtle` | `#2C2C2C` | Hairline dividers (0.5-1 dp) |

## Accents (user-selectable)
| Name | Primary | Bright |
|---|---|---|
| Zune Pink (default) | `#FA2A55` | `#FF4D79` |
| Zune Orange | `#F09609` | `#FFA726` |
| Zune Cyan | `#1BA1E2` | `#33B5E5` |
| Zune Lime | `#339933` | `#4CAF50` |
| Zune Purple | `#A200FF` | `#B388FF` |

## Text opacities (white only)
| State | Opacity |
|---|---|
| Active / primary | 1.0 |
| Hover / pressed | 0.85 |
| Secondary metadata | 0.60 |
| Inactive crossbar/menu | 0.40 |
| Watermark type | 0.08 |

## Type scale (device-mode design units, 480x272 canvas)
| Role | Size | Weight | Case |
|---|---|---|---|
| Home menu item | 34 | Light | lowercase |
| Cropped screen header (back-tap) | 40 | Light | lowercase |
| Crossbar pivot | 18 | Light | lowercase |
| Now Playing title | 26 | Light | lowercase |
| Now Playing artist/album | 15 | Light | lowercase |
| List primary | 14 | Normal | — |
| List secondary | 11 | Normal | — |
| Caption / time | 9 | Normal | — |
| Alphabet index | 10 | Normal | — |

## Motion
- Deceleration: cubic ease-out; pivot slides 320-420 ms.
- List stagger entrance: 15-25 ms/item.
- Now Playing screensaver text drift: continuous, ~40 px/s, opacity 0.9.
- Quickplay reveal: 420 ms with parallax at 0.6x.

## Geometry
- Device canvas: 480 x 272 design units.
- Screen margins: 16 (edges), 24 (crossbar leading).
- Crossbar strip height: 34.
- List row height: 40; album grid tile: 92 with 8 gutter.
- Corner radius: **0 everywhere.**

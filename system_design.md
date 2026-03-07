# System Design - GUI PySide6

## Direction
- Style: PIXEL ART + Glassmorphism + Liquid Glass + macOS minimal modern.
- Output must feel premium and intentional, not "AI generic".
- Focus on professional UI/UX, clear hierarchy, and clean organization.

## Visual Principles
- Dark theme as base.
- Secondary color: red (used for highlights, active states, alerts).
- Frosted glass surfaces with subtle blur and transparency.
- Minimal gradients only when needed for depth.
- No glow effect at all.
- Avoid color noise and visual clutter.

## Color System
- `bg_primary`: `#0E0F12`
- `bg_secondary`: `#14161B`
- `surface_glass`: `rgba(255,255,255,0.08)`
- `surface_glass_strong`: `rgba(255,255,255,0.14)`
- `border_soft`: `rgba(255,255,255,0.18)`
- `text_primary`: `#F2F3F5`
- `text_secondary`: `#A7ACB5`
- `red_secondary`: `#D44242`
- `red_secondary_hover`: `#E05252`
- `red_secondary_pressed`: `#B63838`

## Typography
- Use a modern sans-serif with macOS feel (clean, geometric, high readability).
- Strong scale:
  - Title: 24-30px, semibold
  - Section: 16-18px, medium
  - Body: 13-14px, regular
  - Caption: 11-12px, regular
- Keep line length short for dense panels.

## Layout and Hierarchy
- 8px spacing grid (8/16/24/32 rhythm).
- Strong information hierarchy:
  - Top app bar for context + quick actions
  - Left navigation rail/panel
  - Main content in cards/panels
  - Right optional detail panel (when needed)
- Use alignment and spacing to separate groups, not extra colors.
- Keep generous negative space between modules.

## Component Language
- Window/card corners: 10-14px radius.
- Inputs/buttons/cards use frosted glass layer + subtle border.
- Buttons:
  - Primary: neutral glass + red accent on focus/active
  - Secondary: transparent glass with thin border
  - Danger/action emphasis: red secondary
- States:
  - Hover: slight surface increase (opacity +4-6%)
  - Pressed: darker fill, no glow
  - Focus: clean red outline/border, 1-2px

## Pixel Art Integration
- Use pixel-art only as identity accents (icons, separators, mini motifs).
- Keep core UI modern and crisp; pixel art should not reduce readability.
- Prefer controlled pixel details in empty states, badges, and decorative corners.

## Motion and Interaction
- Short, calm transitions (120-200ms, ease-out).
- Use fade/slide for panel changes.
- Avoid flashy animations and springy effects.
- No neon, no bloom, no glow trails.

## Do / Dont
- Do: minimal, structured, premium, consistent.
- Do: red as secondary accent with restrained usage.
- Do: layered glass depth with subtle contrast.
- Dont: exaggerated gradients.
- Dont: too many accent colors.
- Dont: glow effects.
- Dont: generic template-like layout.

## PySide6 Implementation Notes
- Build with `QFrame` layered panels for glass surfaces.
- Use Qt style sheets with rgba backgrounds and soft borders.
- If available, apply blur on background containers to reinforce frosted effect.
- Centralize tokens (colors, radius, spacing, typography) in one theme module.

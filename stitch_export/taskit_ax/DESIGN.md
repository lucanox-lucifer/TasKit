---
name: TaskIt AX
colors:
  surface: '#0d1321'
  surface-dim: '#0d1321'
  surface-bright: '#333948'
  surface-container-lowest: '#080e1c'
  surface-container-low: '#151b29'
  surface-container: '#191f2e'
  surface-container-high: '#242a39'
  surface-container-highest: '#2f3544'
  on-surface: '#dde2f6'
  on-surface-variant: '#c1c7d4'
  inverse-surface: '#dde2f6'
  inverse-on-surface: '#2a303f'
  outline: '#8b919d'
  outline-variant: '#414752'
  surface-tint: '#a4c9ff'
  primary: '#a4c9ff'
  on-primary: '#00315d'
  primary-container: '#58a4ff'
  on-primary-container: '#00396a'
  inverse-primary: '#005fac'
  secondary: '#ddb7ff'
  on-secondary: '#490080'
  secondary-container: '#6f00be'
  on-secondary-container: '#d6a9ff'
  tertiary: '#ffba47'
  on-tertiary: '#442c00'
  tertiary-container: '#d99400'
  on-tertiary-container: '#4e3300'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d4e3ff'
  primary-fixed-dim: '#a4c9ff'
  on-primary-fixed: '#001c39'
  on-primary-fixed-variant: '#004883'
  secondary-fixed: '#f0dbff'
  secondary-fixed-dim: '#ddb7ff'
  on-secondary-fixed: '#2c0051'
  on-secondary-fixed-variant: '#6900b3'
  tertiary-fixed: '#ffddb0'
  tertiary-fixed-dim: '#ffba47'
  on-tertiary-fixed: '#281800'
  on-tertiary-fixed-variant: '#614000'
  background: '#0d1321'
  on-background: '#dde2f6'
  surface-variant: '#2f3544'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
  margin-mobile: 20px
  gutter-mobile: 12px
---

## Brand & Style
The design system is engineered to evoke a sense of focused productivity and futuristic efficiency. It targets high-performance individuals who value aesthetic clarity and fluid interaction. 

The visual style is **Glassmorphism**, characterized by deep, layered depths, vibrant accent gradients, and frosted translucent surfaces. This approach transforms a functional utility into a premium digital experience, using light and transparency to guide the user's focus through complex task lists.

## Colors
The palette is rooted in a "Midnight Obsidian" dark mode to reduce eye strain and maximize the vibrancy of accent elements. 

- **Primary & Secondary:** A high-energy gradient spanning from Electric Blue to Royal Purple. This is used exclusively for primary calls to action, active states, and completion markers.
- **Neutral:** Deep navy and charcoal tones form the background architecture, ensuring high contrast with text.
- **System Colors:** Success is represented by the primary blue; warnings by a soft amber; and destructive actions by a desaturated crimson to maintain the sophisticated mood.

## Typography
This design system utilizes **Inter** for its exceptional legibility and neutral, modern character. 

Hierarchy is established through significant weight shifts and generous line heights. **Display** and **Headline** styles use tighter letter spacing and heavier weights to create a "bold" editorial feel. **Label** styles often utilize medium or semi-bold weights at smaller sizes to ensure they remain legible against translucent backgrounds.

## Layout & Spacing
The system follows an **8px base grid** for most components, with a **4px sub-grid** for fine-tuning icons and text-to-label relationships. 

- **Mobile Philosophy:** A fluid vertical scroll with fixed horizontal margins of 20px. 
- **Touch Targets:** All interactive elements maintain a minimum hit area of 44x44px. 
- **Section Spacing:** Major content blocks are separated by 32px (xl) to create a clear visual distinction between different categories or dates.

## Elevation & Depth
Depth is created through a combination of backdrop filters and thin inner borders rather than heavy traditional shadows.

1.  **Level 0 (Base):** The darkest neutral navy (#0F141F).
2.  **Level 1 (Cards):** Translucent surface (8% white) with a `20px` backdrop blur and a `1px` white border at 10% opacity.
3.  **Level 2 (Modals/Popovers):** Higher opacity (12% white) with a `40px` backdrop blur and a subtle outer glow that matches the primary gradient color at 15% opacity.
4.  **Interactive State:** When pressed, elements should subtly scale down (98%) and increase blur density to simulate physical compression.

## Shapes
The shape language is ultra-modern and soft. 
- **Standard Cards:** Use a `1rem` (16px) radius to feel substantial yet approachable.
- **Containers & Modals:** Use a `1.5rem` (24px) radius to create a "nested" look within the screen's hardware corners.
- **Interactive Elements:** Buttons and tags utilize a full pill-shape (circular ends) to contrast against the rectangular task cards.

## Components
- **Task Cards:** Large, high-touch areas. Content is padded by `16px`. Status is indicated by a circular checkbox on the left that fills with the primary gradient upon completion.
- **Buttons:** Primary buttons use the `accent_gradient` with white text. Secondary buttons use the `surface_glass` style with a semi-transparent border.
- **Bottom Navigation:** A floating glass bar with a `24px` radius. Icons use an "active fill" state where the active tab glows with the primary blue.
- **Chips/Tags:** Small pill-shaped containers for priority levels (High, Medium, Low). High priority uses a desaturated red text with a subtle red-tinted glass background.
- **Input Fields:** Minimalist design; only a bottom border that transitions into the primary gradient when focused.
- **Progress Indicator:** A thin, horizontal gradient bar at the top of the "My Day" view to visualize completion percentage.
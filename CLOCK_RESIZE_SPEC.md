# Clock Widget Resize — Implementation Spec

> **Historical spec — the code has moved on.** Shipped differently: a single `clockScale` (not `clockScaleX/Y`), `ClockAdjustSheet` is now a `ThemedModalBottomSheet`, and adjust mode also has an "Alignment" pill below the height handle. See `docs/architecture/12-flow-drawer-search-and-app-actions.md` §4 and `CAPABILITIES.md` for current behavior.

Freeform (independent X/Y) scaling of the Home clock+date widget, corner-drag interaction, global + per-profile override. Long-press on the clock now opens a bottom sheet ("Change widget position" / "Resize clock widget") instead of jumping straight into the existing move-handle drag mode.

This spec assumes familiarity with the existing "move the clock block" feature (`clockZoneHeightDp`), which this builds directly on top of. It does not move — it's a vertical *offset*. This feature adds a second, independent axis: *scale*.

---

## 1. Scope

- Scales only the rendered clock (time) + date text — i.e. `ClockDisplay` in `ClockTemplates.kt`.
- Does **not** affect `CalendarEventsBlock` (the actual calendar events list) — these are already separate sibling composables inside `ClockBlock`'s `Column` ([ClockBlock.kt:74-101](app/src/main/kotlin/com/facetlauncher/app/ui/home/ClockBlock.kt#L74-L101)), so this boundary is free.
- Global default + per-profile override, following the exact `overrideClock` bundle pattern that `clockZoneHeightDp`/`clockAlignment`/`calendarAlignment` already use.
- Corner-drag ("canvas style") gesture, independent X/Y — glyphs will visibly stretch/squish under non-uniform scale; that's inherent to the request, not a bug.
- Long-press on the clock opens a bottom sheet with two options instead of directly revealing the move handle:
  1. **Change widget position** — reveals the existing move handle.
  2. **Resize clock widget** — reveals new corner-resize handle(s).

---

## 2. Data model

**`LauncherSettings.kt`** (~line 221, next to `clockZoneHeightDp`):
```kotlin
val clockScaleX: Float = 1f,
val clockScaleY: Float = 1f,
```

**`ProfileEntity.kt`** (~line 46, next to `clockZoneHeightDp`):
```kotlin
val clockScaleX: Float = 1f,
val clockScaleY: Float = 1f,
```

No new override boolean — reuse the existing `overrideClock` flag, exactly like `clockZoneHeightDp` does. Unlike `clockZoneHeightDp` (nullable — `null` means "never dragged, use the default formula"), scale always has a sane default (`1f`), so no nullability needed.

---

## 3. Persistence

**`SettingsRepository.kt`** — mirror `CLOCK_ZONE_HEIGHT_DP` (line 80) and `setClockZoneHeight`/`resetClockZoneHeight` (lines 341-347):

```kotlin
val CLOCK_SCALE_X = floatPreferencesKey("clock_scale_x")
val CLOCK_SCALE_Y = floatPreferencesKey("clock_scale_y")
```
Read with default `1f`. Add:
```kotlin
suspend fun setClockScale(scaleX: Float, scaleY: Float) {
    dataStore.edit { it[Keys.CLOCK_SCALE_X] = scaleX; it[Keys.CLOCK_SCALE_Y] = scaleY }
}
suspend fun resetClockScale() {
    dataStore.edit { it.remove(Keys.CLOCK_SCALE_X); it.remove(Keys.CLOCK_SCALE_Y) }
}
```

**`ProfileRepository.kt`** — mirror `setClockZoneHeight` (lines 111-118):
```kotlin
suspend fun setClockScale(profile: ProfileEntity, scaleX: Float, scaleY: Float) {
    profileDao.upsert(profile.copy(clockScaleX = scaleX, clockScaleY = scaleY))
}
```

Fold scale reset into the **existing** `resetClockPosition` — conceptually "reset clock widget position" should mean "back to how it shipped," size included:

```kotlin
// ProfileRepository.resetClockPosition (lines 125-127)
suspend fun resetClockPosition(profile: ProfileEntity) {
    profileDao.upsert(profile.copy(
        clockZoneHeightDp = null, clockAlignment = ClockAlignment.LEFT,
        calendarAlignment = ClockAlignment.LEFT, clockScaleX = 1f, clockScaleY = 1f,
    ))
}
```

Also add `clockScaleX`/`clockScaleY` params (defaulting to `profile.clockScaleX`/`Y`) to `updateOverridingClock` (lines 138-173), same as every other field in that clock-design bundle.

**`ClockStyleGalleryViewModel.kt`** — `resetClockPosition()` (lines 202-212), global branch:
```kotlin
settingsRepository.resetClockZoneHeight()
settingsRepository.resetClockScale()
settingsRepository.setClockAlignment(ClockAlignment.LEFT)
settingsRepository.setCalendarAlignment(ClockAlignment.LEFT)
```
No new setter method needed on this ViewModel — scale is never set from the gallery screen, only from the drag gesture (same as `clockZoneHeightDp`, which also has no gallery control besides the reset row).

---

## 4. Resolution (global vs. profile)

**`HomeUiState.kt`** (~line 94-95), mirroring `clockZoneHeightDp`:
```kotlin
val clockScaleX: Float
    get() = activeProfile.resolveOverride({ it.overrideClock }, { it.clockScaleX }, settings.clockScaleX)
val clockScaleY: Float
    get() = activeProfile.resolveOverride({ it.overrideClock }, { it.clockScaleY }, settings.clockScaleY)
```

**`ProfileCarouselViewModel.kt`** (~line 77-78), same pattern for preview cards:
```kotlin
fun clockScaleX(profileId: Long): Float =
    profile(profileId).resolveOverride({ it.overrideClock }, { it.clockScaleX }, globalSettings.clockScaleX)
// + clockScaleY equivalent
```

**`ProfileCarouselScreen.kt`** (~lines 254, 508-582) — already reproduces `clockZoneHeightDp`'s formula at scaled-down card size; add `clockScaleX`/`clockScaleY` params there too, applying the same scale modifier (section 5) so profile previews accurately reflect a profile's own resize.

---

## 5. Rendering mechanism

**This is the trickiest part — read carefully before implementing.**

A plain `Modifier.graphicsLayer(scaleX=, scaleY=)` only transforms *paint*, not *layout*: the parent still thinks the content occupies its original, unscaled size. That leaves stale gaps/overlaps against `CalendarEventsBlock` below it, and breaks the existing zone-height math, which measures `clockNaturalHeightPx` via `onGloballyPositioned` ([HomeScreen.kt:199](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L199)) to compute the move-handle's drag floor/ceiling.

You need scale that **participates in layout**: measure content at its natural size, then report the *scaled* size upward via `Modifier.layout {}`, painting via `Placeable.placeWithLayer {}`:

```kotlin
fun Modifier.freeformScale(scaleX: Float, scaleY: Float, anchor: TransformOrigin) = this.layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())          // natural, unconstrained size
    val w = (placeable.width * scaleX).roundToInt()
    val h = (placeable.height * scaleY).roundToInt()
    layout(w, h) {                                              // report SCALED size to the parent
        placeable.placeWithLayer(0, 0) {
            this.scaleX = scaleX; this.scaleY = scaleY; this.transformOrigin = anchor
        }
    }
}
```

Apply this in **`ClockBlock.kt`**, wrapping just the `modifier` passed to `ClockDisplay` (line 85: `Modifier.align(clockAlignment.resolve()).then(clockContentModifier)`) — **not** anywhere near `CalendarEventsBlock`. Single insertion point: none of the 35 templates in `ClockTemplates.kt` need to change, since they all render through the one `ClockDisplay` entry point ([ClockTemplates.kt:73-132](app/src/main/kotlin/com/facetlauncher/app/ui/home/clock/ClockTemplates.kt#L73-L132)) and none hardcode their own scale — only per-template `fontSize`/`letterSpacing` values in `sp`, which the layer transform scales uniformly without touching any of the ~35 `when` branches.

Payoff of doing it this way: since `clockNaturalHeightPx` in `HomeScreen.kt` is measured from the already-composed block, the existing zone-height floor/ceiling math automatically sees the post-scale height with no further changes — position and size compose correctly for free.

### 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table)

An earlier draft of this spec tried to give each corner handle its own fixed anchor (opposite corner stays put, like a typical image-editor resize handle). **That's wrong for this app** — `clockAlignment` can be Left, Center, or Right, and a hardcoded anchor doesn't know about that. Concretely, a fixed top-left anchor would try to grow a *right-aligned* clock further right, off the edge of a screen it's already flush against.

The correct design leans on machinery that already exists instead of reinventing anchoring:

- `freeformScale`'s own internal `transformOrigin` is always **`TransformOrigin.Center`** — full stop, regardless of which handle is dragged or what alignment is set. To do this correctly, place the natural-size placeable *centered* within the newly-reported `(w, h)` box before applying the graphicsLayer scale:
  ```kotlin
  layout(w, h) {
      placeable.placeWithLayer((w - placeable.width) / 2, (h - placeable.height) / 2) {
          scaleX = this@freeformScale.scaleX; scaleY = this@freeformScale.scaleY
          transformOrigin = TransformOrigin.Center
      }
  }
  ```
- **Horizontal anchoring is Compose's `align()`, already in `ClockBlock.kt` line 85** (`Modifier.align(clockAlignment.resolve())`). `align()` re-pins the correct edge to the parent on every recomposition based on whatever size the child currently reports — Start keeps the left edge fixed as width grows, End keeps the right edge fixed, CenterHorizontally grows symmetrically from the midpoint. Since `freeformScale` reports the *scaled* width upward through `layout()`, this behaves correctly automatically, for every alignment, with zero alignment-aware code inside the scale modifier itself.
- **Vertical anchoring is the *existing* zone-height offset formula** ([HomeScreen.kt:198](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L198): `handlePx - minGapPx - clockNaturalHeightPx`) — already pins the block's bottom edge near the position handle and grows height upward as `clockNaturalHeightPx` increases. Nothing new needed here either; it already behaves this way for the pre-existing zone-height feature and just keeps working once `clockNaturalHeightPx` reflects the post-scale height.

Net effect: **both resize handles feed the same underlying `scaleX`/`scaleY` state** — neither one owns its own separate anchor. Which edge visually stays fixed is entirely a function of the current `clockAlignment` (handled by `align()`) and the existing position system (handled by the zone-height formula), not of which handle you happened to grab. This is deliberately different from a typical image editor's per-corner-anchor resize handle — it's what makes the feature work correctly across Left/Center/Right alignment without off-screen growth or extra clamping logic tied to which handle was touched.

---

## 6. Gesture — new `ClockCornerHandle.kt`

Mirror `ClockZoneHandle.kt`'s structure and doc-comment conventions, but:

- Use `detectDragGestures` (not `detectVerticalDragGestures`) — need both X and Y deltas.
- Same `onDrag`/`onDragEnd` shape as `ClockZoneHandle` (live delta accumulated during drag, committed once on release) — mirror `HomeScreen.kt`'s existing `liveDragDeltaPx`/`onClockZoneHeightCommit` pattern (lines 143, 340-344) with a parallel `liveScaleDeltaX/Y` and `onClockScaleCommit(scaleX, scaleY)`.
- Position each handle at a corner of the *scaled* clock+date bounds — you'll need that rect via `onGloballyPositioned` on the `ClockDisplay`-wrapping Box specifically (separate from the whole-block `clockNaturalHeightPx` measurement, since date+clock bounds ≠ the whole `ClockBlock` including calendar events).
- **Two handles**: top-left and top-right corners of the current rendered bounds. Both handles drive the *same* `scaleX`/`scaleY` state (see 5.1) — there's no per-handle anchor to configure. Each just needs the correct sign convention for its side: on the left-side handle, dragging left grows, dragging right shrinks; on the right-side handle, dragging right grows, dragging left shrinks. (Four-corner vs. two-handle vs. one-handle is now purely a hit-target/UX choice, not an anchor-math one, since section 5.1 made anchoring alignment-driven rather than handle-driven.)

### 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong)

With Left or Right alignment, one edge is pinned and the *other* edge absorbs 100% of a size change — drag the free edge by `X` px, it moves `X` px. With Center alignment, there's no pinned edge — the box grows outward from the midpoint, so **both** edges move, each by only half of the total width change. Using the naive `scaleXDelta = dragAmountPx.x / naturalWidthPx` formula unmodified would make the edge under your finger track at only half speed for Center-aligned content — the handle visibly lags behind the touch.

Fix: compute a conversion factor from the current `clockAlignment` at the start of each drag —

```kotlin
val factor = if (clockAlignment == ClockAlignment.CENTER) 2f else 1f
val scaleXDelta = factor * dragAmountPx.x / naturalWidthPx
```

General rule: factor = `1 / (fraction of total growth visible on the edge being dragged)` — 1.0 for Left/Right, 2.0 for Center (each edge only shows half the growth). This is purely a gesture-math correction — the rendered *result* (5.1) is already correct without it; only the live drag tracking needs the factor.

**Knock-on effect**: because Center-aligned growth moves *both* edges, both handles must be repositioned every frame during a Center-aligned drag (not just the one being touched) — otherwise the handle you're not touching visually drifts out of sync with the actual rendered edge.

### 6.2 Re-validating scale after an alignment change (no active drag)

Clamping (section 7) only runs live, during an active drag, checked against whatever alignment is current at that moment. But scale and alignment are independently stored — nothing ties them together outside a drag. That means changing `clockAlignment` later (e.g. in the Clock Style Gallery, no gesture involved) can silently invalidate a previously-fine scale: resize to `scaleX = 2.2` while Left-aligned (plenty of room to grow right), then switch to Right-aligned — the same `2.2` now grows leftward from the right margin and can push the left edge off-screen, with nothing catching it.

**Fix**: `ClockStyleGalleryViewModel.setClockAlignment()` (and `ProfileRepository.setClockAlignment`) should re-clamp the currently-stored `clockScaleX` against the *new* alignment's available space as part of the same write, using the same space-based clamp logic as the live drag path (section 7) — not just validate at drag time. This does mean the alignment setter now needs to know about scale bounds, not just write the alignment enum.

---

## 7. Bounds / clamping

Two constraints, evaluated live during drag (same `coerceIn` pattern as `handlePx` at [HomeScreen.kt:161](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L161)):

- **Absolute range**: hard limits per axis, e.g. `0.5f..2.5f` — prevents unreadably tiny or grotesquely oversized text.
- **Available space**: horizontal scale shouldn't let rendered width exceed the content area's width (24dp-margined screen width). Vertical scale interacts with the *existing* zone-height ceiling (`HOME_CLOCK_ZONE_MAX_FRACTION = 0.5f`, [HomeScreen.kt:386](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L386)) — a tall scaled clock plus a dragged-down position could together overflow past the app list/dock.

**Recommendation**: ship fixed absolute min/max bounds first (no dynamic space-aware clamping) and only add a dynamic ceiling if testing shows real overflow — the fully general "size × position both compete for the same vertical budget" case is the fiddliest part of this feature and not worth solving up front.

---

## 8. Long-press → bottom sheet (replaces direct drag-mode reveal)

There's an existing, real pattern for exactly this shape of UI: `ContactConnectionsSheet.kt` + its host in `AppDrawerScreen.kt`. Build against that instead of inventing something new.

### 8.1 State

`HomeScreen.kt`'s single `dragModeEnabled: Boolean` (line 146) becomes an enum tracking which UI is showing:

```kotlin
private enum class ClockAdjustMode { NONE, MENU, POSITION, RESIZE }
var clockAdjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
```

- Long-press on the clock ([HomeScreen.kt:225](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L225), currently `onLongPress = { dragModeEnabled = true }`) now sets `clockAdjustMode = ClockAdjustMode.MENU`.
- The existing move handle ([HomeScreen.kt:335-346](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L335-L346)) renders when `clockAdjustMode == POSITION` (was `if (dragModeEnabled)`).
- The new corner handle(s) (section 6) render when `clockAdjustMode == RESIZE`.
- Tap-away-dismisses logic ([HomeScreen.kt:177-183](app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt#L177-L183)) resets to `NONE` from `POSITION` or `RESIZE`.

### 8.2 New sheet content — `ClockAdjustSheet.kt`

Same chrome as `ContactConnectionsSheet.kt` ([ContactConnectionsSheet.kt:101-117](app/src/main/kotlin/com/facetlauncher/app/ui/drawer/ContactConnectionsSheet.kt#L101-L117)): `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)` M3 modal-sheet shape, 34×4dp drag-rail affordance. Two rows, built on the `ConnectionRow` icon-tile-plus-label pattern (no subtitle/chevron needed — these aren't drill-down rows):

```kotlin
@Composable
fun ClockAdjustSheet(
    onChangePositionClick: () -> Unit,
    onResizeClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- **Row 1 — "Change widget position"**: reuse the exact icon the move handle already uses — `R.drawable.expand_content_24` rotated 315° ([ClockZoneHandle.kt:94-99](app/src/main/kotlin/com/facetlauncher/app/ui/home/ClockZoneHandle.kt#L94-L99)). Visual continuity between the sheet option and the handle it summons, and it's already "an icon that signifies up/down movement with the bar."
- **Row 2 — "Resize clock widget"**: needs a **new** drawable — this project only has `expand_content_24.xml` and the launcher icon in `res/drawable/` today. Material Symbols' `open_in_full` (two diagonal corner arrows pointing outward) reads well for "stretch/resize" — source it the same way `expand_content_24.xml` was added (Android Studio's vector asset picker against the Material Symbols set).

### 8.3 Hosting

Mirror `AppDrawerScreen.kt`'s scrim + slide-up structure exactly ([AppDrawerScreen.kt:342-378](app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt#L342-L378)), inside `HomeScreen.kt`'s own root `Box`, alongside the existing conditional handle block:

```kotlin
AnimatedVisibility(
    visible = clockAdjustMode == ClockAdjustMode.MENU,
    enter = fadeIn(tween(240)), exit = fadeOut(tween(240)),
) {
    Box(Modifier.fillMaxSize().background(Scrim).testTag("clock_adjust_scrim")
        .clickable { clockAdjustMode = ClockAdjustMode.NONE })
}
AnimatedVisibility(
    visible = clockAdjustMode == ClockAdjustMode.MENU,
    enter = slideInVertically(initialOffsetY = { it }, tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))),
    exit = slideOutVertically(targetOffsetY = { it }, tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))),
    modifier = Modifier.align(Alignment.BottomCenter),
) {
    ClockAdjustSheet(
        onChangePositionClick = { clockAdjustMode = ClockAdjustMode.POSITION },
        onResizeClick = { clockAdjustMode = ClockAdjustMode.RESIZE },
    )
}
```

Same 240ms scrim fade / 340ms slide with the project's standard `cubic-bezier(.32,.72,0,1)` easing — identical timing/feel to the existing contacts sheet, no new motion language introduced.

---

## 9. Testing plan

- **`SettingsRepositoryTest.kt` / `ProfileRepositoryTest.kt`**: setter persists; reset returns to `1f, 1f`; independence from other clock-bundle fields — same shape as existing `clockZoneHeightDp` tests.
- **`HomeScreenTest.kt`**:
  - New tests mirroring `zoneHandleCannotBeDraggedAboveTheClocksMinimumGap` / `zoneHeightCannotExceedHalfOfContentHeight` ([HomeScreenTest.kt:530-560](app/src/androidTest/kotlin/com/facetlauncher/app/ui/home/HomeScreenTest.kt#L530-L560)) — corner-drag clamps at min/max scale; `CalendarEventsBlock`'s own bounds/testTag unaffected by clock scale; reset restores `1f,1f`.
  - **Existing test impact**: the `enableClockDragMode()` helper (backing the tests above, plus others) currently does long-press → handle appears directly. It must become long-press → tap "Change widget position" row → *then* the handle appears. Update that one helper and every existing test using it gets the new flow for free. Add a parallel `enableClockResizeMode()` helper (long-press → tap "Resize clock widget") for the new corner-handle tests.
- **`ClockStyleGalleryScreenTest.kt`**: global vs. profile-scoped scale resolution (same shape as existing `clockAlignment`/`calendarAlignment` tests); reset row clears scale too.

---

## 10. Open decisions (resolve before implementing)

1. **Handle count**: two (top-left + top-right, current working assumption per section 6) vs. four (also bottom corners) vs. one. Purely a hit-target/UX call now — section 5.1's anchor design makes any count behave correctly, since anchoring is alignment-driven, not handle-driven.
2. **Absolute scale bounds**: `0.5–2.5` proposed, arbitrary — pick real numbers based on how templates look at extremes.
3. **Non-uniform stretch is intentional**: independent X/Y scaling will visibly distort glyph proportions at non-1:1 ratios. That's what "canvas style freeform" means, not a defect — confirm this is really the desired feel vs. a uniform-lock default with an optional freeform toggle.
4. **Dynamic space-aware clamping during a drag**: recommended to defer (section 7) — confirm that's acceptable, or decide up front if the size/position interaction needs to be bulletproof from day one.
5. **Re-validating scale on alignment change** (section 6.2): recommended — re-clamp stored scale whenever `clockAlignment` changes outside a drag, so switching alignment can't silently push an oversized clock off-screen. Confirm this is worth the extra coupling (the alignment setter now needs scale-clamp logic too), or accept the rare-case visual bug instead.

---

## Summary of files touched

| File | Change |
|---|---|
| `data/model/LauncherSettings.kt` | + `clockScaleX`, `clockScaleY` |
| `data/local/ProfileEntity.kt` | + `clockScaleX`, `clockScaleY` |
| `data/SettingsRepository.kt` | + DataStore keys, `setClockScale`, `resetClockScale` |
| `data/ProfileRepository.kt` | + `setClockScale`, fold into `resetClockPosition`, extend `updateOverridingClock` |
| `ui/home/clock/ClockStyleGalleryViewModel.kt` | extend `resetClockPosition()`; `setClockAlignment()` re-clamps stored scale (6.2) |
| `data/ProfileRepository.kt` `setClockAlignment` | re-clamps stored scale on alignment change (6.2) |
| `ui/home/HomeUiState.kt` | + `clockScaleX`/`Y` computed properties |
| `ui/profiles/ProfileCarouselViewModel.kt` | + `clockScaleX(profileId)`/`Y` |
| `ui/profiles/ProfileCarouselScreen.kt` | apply scale to preview card |
| `ui/home/ClockBlock.kt` | apply `freeformScale` modifier around `ClockDisplay` only |
| `ui/home/ClockZoneHandle.kt` | unchanged (reused as-is) |
| `ui/home/ClockCornerHandle.kt` | **new** — corner drag-to-resize handle(s) |
| `ui/home/ClockAdjustSheet.kt` | **new** — long-press bottom sheet with the two options |
| `ui/home/HomeScreen.kt` | `dragModeEnabled: Boolean` → `ClockAdjustMode` enum; host the sheet; wire corner handle(s) |
| `res/drawable/` | **new** icon for "Resize clock widget" (Material Symbols `open_in_full` or similar) |
| `HomeScreenTest.kt`, `SettingsRepositoryTest.kt`, `ProfileRepositoryTest.kt`, `ClockStyleGalleryScreenTest.kt` | new + updated tests per section 9 |

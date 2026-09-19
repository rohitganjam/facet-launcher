# Facet Launcher 0.1.10

## New Features
- Added full app translations for Spanish, French, German, and Portuguese, including natural, locale-specific grammar rather than simple word-for-word translation.
- Added a new setting to scale text size across the entire app, making it easier to read.

## Fixes
- Fixed incorrect pluralization/grammar for counted items (like dock, folder, favorite, and recent counts) in languages whose plural rules differ from English.
- Fixed a wallpaper color that didn't match the intended design.

## Improvements
- Finished translating the remaining screens (Drawer, Facets, Home, and Hub areas), so more of the app now correctly appears in your selected language.

## Internal
- Updated implementation plan, test registry, and knowledge graph docs to reflect completed phases (Drawer/Facets/Home/Hub, i18n wrap-up, font-size control, Pass 2 translations); test registry now covers 128 classes / 1054 cases.
- Added a `:benchmark` module for Macrobenchmark cold-start timing against the release build type; fixed up namespace, mismatched SDK versions, and naming from the Android Studio wizard template; added a `profileable` manifest tag for benchmark tracing.
- Expanded the Detekt rule set (potential-bugs, complexity, style, naming, exceptions) alongside the custom ComposeHardcodedText rule; added a self-installing pre-commit hook to run Detekt on staged changes; trimmed CLAUDE.md to remove rules now enforced by lint.
- Added a new `:detekt-rules` module with a custom ComposeHardcodedText rule plus compose-rules to gate the i18n migration; migrated data/model enum labels to string resources; fixed a latent Robolectric test config gap (`isIncludeAndroidResources` was never enabled) and two test assumptions that depended on it.
- Release 0.1.9 version bump.

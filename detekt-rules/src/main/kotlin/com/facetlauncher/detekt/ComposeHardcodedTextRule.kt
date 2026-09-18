package com.facetlauncher.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Flags string-literal arguments passed to Compose UI calls — the Kotlin/Compose equivalent of
 * Android Lint's built-in `HardcodedText` (which only scans XML `android:text`, never Compose
 * `Text(...)` calls, so it's useless here). Part of the language-translation migration
 * (IMPLEMENTATION_PLAN.md).
 *
 * Purely syntactic (no type resolution against the full classpath), so it stays fast. Two
 * triggers, both scoped to calls whose name starts with an uppercase letter (this project's
 * Compose naming convention):
 *  1. A named argument whose parameter name is a known text-carrying one (`text`, `title`,
 *     `label`, `contentDescription`, `message`, `hint`, `placeholder`, `description`) is bound to
 *     a string-literal template.
 *  2. `Text(...)`/`BasicText(...)`'s first *positional* argument (no named-argument syntax) is a
 *     string-literal template — covers the common `Text("Some label")` call shape directly.
 *
 * Written against detekt-api 2.0.0-alpha.6 (see detekt-rules/build.gradle.kts for why) — its
 * `Rule` API dropped `Issue`/`CodeSmell`/`Debt` in favor of plain `Finding(entity, message)`, and
 * `RuleSetProvider.instance()` takes no `Config` (see `FacetRuleSetProvider`).
 */
class ComposeHardcodedTextRule(config: Config) : Rule(
    config,
    "Hardcoded string in a Compose UI call — move to res/values/strings.xml and use " +
        "stringResource(R.string.x) (see IMPLEMENTATION_PLAN.md's \"Language translation\" section)."
) {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val functionName = expression.calleeExpression?.text ?: return
        if (functionName.isEmpty() || !functionName.first().isUpperCase()) return
        if (functionName in NON_COMPOSABLE_CONSTRUCTORS) return

        val arguments = expression.valueArgumentList?.arguments ?: return

        for ((index, argument) in arguments.withIndex()) {
            val stringLiteral = argument.stringLiteralOrNull() ?: continue
            val argumentName = argument.getArgumentName()?.asName?.asString()

            val isAnimationDebugLabel = argumentName == "label" && functionName in ANIMATION_LABEL_CALLS
            val isFlaggedNamedArg =
                argumentName != null && argumentName in TEXT_PARAM_NAMES && !isAnimationDebugLabel
            val isFlaggedFirstPositionalArg =
                argumentName == null && index == 0 && functionName in POSITIONAL_TEXT_CALLS

            if (isFlaggedNamedArg || isFlaggedFirstPositionalArg) {
                report(Finding(Entity.from(stringLiteral), description))
            }
        }
    }

    private fun KtValueArgument.stringLiteralOrNull(): KtStringTemplateExpression? =
        this.getArgumentExpression() as? KtStringTemplateExpression

    companion object {
        private val TEXT_PARAM_NAMES = setOf(
            "text", "title", "label", "contentDescription", "message", "hint", "placeholder", "description"
        )
        private val POSITIONAL_TEXT_CALLS = setOf("Text", "BasicText")

        // Kotlin can't syntactically distinguish a @Composable call from a data class
        // constructor call — both are just `Capitalized(...)`. Without full-classpath type
        // resolution (deliberately avoided, see class doc), a domain model whose field happens to
        // share a name with TEXT_PARAM_NAMES (title/label/description/...) looks identical to a
        // real Compose call. Found via real false positives on `AppInfo(..., label = "App $it")`
        // and `CalendarEvent(..., title = "Team standup")` preview/test fixture data — add more
        // here if another domain model class trips this.
        private val NON_COMPOSABLE_CONSTRUCTORS = setOf("AppInfo", "CalendarEvent")

        // `label` is ambiguous in Compose: a real UI string on most components, but a non-visual
        // debug/tracing tag (shown only in layout inspector / animation tooling) on these — e.g.
        // AnimatedContent(label = "app_context_menu_page"). Found via a real false positive
        // during rollout; keep this list in sync if a new animation API with the same shape shows up.
        private val ANIMATION_LABEL_CALLS = setOf(
            "AnimatedContent", "Crossfade", "updateTransition", "rememberTransition",
            "rememberInfiniteTransition", "animateFloatAsState", "animateDpAsState",
            "animateColorAsState", "animateIntAsState", "animateOffsetAsState",
            "animateRectAsState", "animateSizeAsState", "animateValueAsState"
        )
    }
}

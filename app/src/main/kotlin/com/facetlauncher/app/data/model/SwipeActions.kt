package com.facetlauncher.app.data.model

/** The four Home swipes, named by the finger's direction. */
enum class SwipeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    ;

    val isVertical: Boolean get() = this == UP || this == DOWN

    val opposite: SwipeDirection
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

/** What a Home swipe does. */
enum class SwipeAction { APP_DRAWER, HUB, SWITCH_FACETS, NOTIFICATION_SHADE, NONE }

/**
 * Settings → Gestures: the action each Home swipe triggers. An action may sit on more than one swipe, but
 * [SwipeAction.APP_DRAWER] always keeps at least one, so apps can never become unreachable.
 */
data class SwipeActions(
    val up: SwipeAction = SwipeAction.APP_DRAWER,
    val down: SwipeAction = SwipeAction.NOTIFICATION_SHADE,
    val left: SwipeAction = SwipeAction.SWITCH_FACETS,
    val right: SwipeAction = SwipeAction.HUB,
) {
    fun actionFor(direction: SwipeDirection): SwipeAction = when (direction) {
        SwipeDirection.UP -> up
        SwipeDirection.DOWN -> down
        SwipeDirection.LEFT -> left
        SwipeDirection.RIGHT -> right
    }

    /** Every swipe that triggers [action]; empty for [SwipeAction.NONE]. */
    fun directionsOf(action: SwipeAction): List<SwipeDirection> =
        if (action == SwipeAction.NONE) emptyList() else SwipeDirection.entries.filter { actionFor(it) == action }

    /** Whether [direction] may be set to [action]: anything but taking the app drawer off its only swipe. */
    fun canAssign(direction: SwipeDirection, action: SwipeAction): Boolean =
        action == SwipeAction.APP_DRAWER || directionsOf(SwipeAction.APP_DRAWER) != listOf(direction)

    /** [direction] now triggers [action]; refused (returns this unchanged) when [canAssign] says no. */
    fun assign(direction: SwipeDirection, action: SwipeAction): SwipeActions {
        if (!canAssign(direction, action)) return this
        return when (direction) {
            SwipeDirection.UP -> copy(up = action)
            SwipeDirection.DOWN -> copy(down = action)
            SwipeDirection.LEFT -> copy(left = action)
            SwipeDirection.RIGHT -> copy(right = action)
        }
    }

    /** Repairs a stored or imported mapping that lost the app drawer: it goes back on swipe up. */
    fun normalized(): SwipeActions = if (directionsOf(SwipeAction.APP_DRAWER).isEmpty()) copy(up = SwipeAction.APP_DRAWER) else this

    companion object {
        /** The mapping the on/off switches and "Swap left and right" of earlier versions described. */
        fun fromLegacy(leftEnabled: Boolean, rightEnabled: Boolean, downEnabled: Boolean, swapSides: Boolean) = SwipeActions(
            up = SwipeAction.APP_DRAWER,
            down = if (downEnabled) SwipeAction.NOTIFICATION_SHADE else SwipeAction.NONE,
            left = if (!leftEnabled) SwipeAction.NONE else if (swapSides) SwipeAction.HUB else SwipeAction.SWITCH_FACETS,
            right = if (!rightEnabled) SwipeAction.NONE else if (swapSides) SwipeAction.SWITCH_FACETS else SwipeAction.HUB,
        )

        /** Parses a stored action name, `null` for anything unrecognized. */
        fun parse(name: String?): SwipeAction? = name?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() }
    }
}

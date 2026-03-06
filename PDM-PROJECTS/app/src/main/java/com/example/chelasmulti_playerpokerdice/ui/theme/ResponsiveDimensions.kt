package com.example.chelasmulti_playerpokerdice.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

val LocalResponsiveDimensions = compositionLocalOf<ResponsiveDimensions?> { null }

data class ResponsiveDimensions(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,

    // Font sizes
    val titleFontSize: TextUnit,          // 12% of screen width, max 50sp (for main title)
    val largeFontSize: TextUnit,          // 5.5% of screen width, max 20sp (for buttons, headers)
    val mediumFontSize: TextUnit,         // 4.5% of screen width, max 16sp (for card titles)
    val regularFontSize: TextUnit,        // 4% of screen width, max 14sp (for normal text)
    val smallFontSize: TextUnit,          // 2.8% of screen width, max 10sp (for chat)

    // Icon sizes
    val largeIconSize: Dp,                // 9% of screen width, max 32dp
    val mediumIconSize: Dp,               // 5.5% of screen width, max 20dp

    // Button dimensions
    val buttonWidth: Dp,                  // 18% of screen width, max 60dp

    // Card dimensions
    val cardWidth: Dp,                    // 26% of screen width
    val chatCardHeight: Dp,               // 80% of screen width, max 300dp
    val chatHeight: Dp                    // 65% of screen width, max 250dp
)

@Composable
fun rememberResponsiveDimensions(): ResponsiveDimensions {
    val configuration = LocalConfiguration.current

    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val screenWidthDp = configuration.screenWidthDp.dp
        val screenHeightDp = configuration.screenHeightDp.dp

        ResponsiveDimensions(
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,

            // Font sizes
            titleFontSize = min(screenWidthDp.value * 0.12f, 50f).sp,
            largeFontSize = min(screenWidthDp.value * 0.055f, 20f).sp,
            mediumFontSize = min(screenWidthDp.value * 0.045f, 16f).sp,
            regularFontSize = min(screenWidthDp.value * 0.04f, 14f).sp,
            smallFontSize = min(screenWidthDp.value * 0.028f, 10f).sp,

            // Icon sizes
            largeIconSize = min(screenWidthDp.value * 0.09f, 32f).dp,
            mediumIconSize = min(screenWidthDp.value * 0.055f, 20f).dp,

            // Button dimensions
            buttonWidth = min(screenWidthDp.value * 0.18f, 60f).dp,

            // Card dimensions
            cardWidth = (screenWidthDp.value * 0.26f).dp,
            chatCardHeight = min(screenWidthDp.value * 0.8f, 300f).dp,
            chatHeight = min(screenWidthDp.value * 0.65f, 250f).dp
        )
    }
}

@Composable
fun localResponsiveDimensions(): ResponsiveDimensions {
    return LocalResponsiveDimensions.current ?: rememberResponsiveDimensions()
}

@Composable
fun ProvideResponsiveDimensions(content: @Composable () -> Unit) {
    val dimensions = rememberResponsiveDimensions()
    CompositionLocalProvider(LocalResponsiveDimensions provides dimensions) {
        content()
    }
}

data class GameResponsiveDimensions(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
    val playerCardHeight: Dp,
    val playerCardTopPadding: Dp,
    val playerCardBottomPadding: Dp,
    val sideSpacing: Dp,
    val avatarSize: Dp,
    val nameFontSize: TextUnit,
    val diceSize: Dp,
    val handRankFontSize: TextUnit,
    val cardWidth: Dp,
    val infoWidth: Dp,
    val tableDiceSize: Dp,
    val tableDiceSpacing: Dp,
    val tableHandRankFontSize: TextUnit,
    val tableTextFontSize: TextUnit,
    val tableButtonScale: Float,
    val tableRowSpacing: Dp,
    val tableTopPadding: Dp,
)

fun calculateGameDimensions(maxWidth: Dp, maxHeight: Dp): GameResponsiveDimensions {
    val verticalSpacing = (maxHeight * 0.03f).coerceAtLeast(16.dp)
    val horizontalSpacing = (maxWidth * 0.02f).coerceAtLeast(24.dp)
    val playerCardHeight = (maxHeight * 0.1f).coerceAtLeast(60.dp).coerceAtMost(100.dp)
    val sideSpacing = (maxWidth * 0.085f).coerceAtLeast(8.dp)

    val avatarSize = (maxHeight * 0.08f).coerceIn(48.dp, 72.dp)
    val diceSize = (maxHeight * 0.02f).coerceIn(12.dp, 18.dp)
    val cardWidth = (maxWidth * 0.14f).coerceIn(120.dp, 200.dp)
    val infoWidth = (cardWidth.value * 0.6f).dp

    return GameResponsiveDimensions(
        screenWidth = maxWidth,
        screenHeight = maxHeight,
        verticalSpacing = verticalSpacing,
        horizontalSpacing = horizontalSpacing,
        playerCardHeight = playerCardHeight,
        playerCardTopPadding = (maxHeight * 0.015f).coerceAtLeast(8.dp),
        playerCardBottomPadding = (maxHeight * 0.015f).coerceAtLeast(8.dp),
        sideSpacing = sideSpacing,
        avatarSize = avatarSize,
        nameFontSize = (maxHeight.value * 0.018f).coerceIn(12f, 18f).sp,
        diceSize = diceSize,
        handRankFontSize = (maxHeight.value * 0.01f).coerceIn(8f, 12f).sp,
        cardWidth = cardWidth,
        infoWidth = infoWidth,
        tableDiceSize = (maxHeight * 0.08f).coerceIn(48.dp, 80.dp),
        tableDiceSpacing = (maxWidth * 0.015f).coerceIn(8.dp, 16.dp),
        tableHandRankFontSize = (maxHeight.value * 0.025f).coerceIn(16f, 22f).sp,
        tableTextFontSize = (maxHeight.value * 0.022f).coerceIn(14f, 20f).sp,
        tableButtonScale = (maxHeight.value / 1000f).coerceIn(0.65f, 1f),
        tableRowSpacing = (maxWidth * 0.08f).coerceIn(64.dp, 160.dp),
        tableTopPadding = (maxHeight * 0.01f).coerceAtLeast(4.dp),
    )
}

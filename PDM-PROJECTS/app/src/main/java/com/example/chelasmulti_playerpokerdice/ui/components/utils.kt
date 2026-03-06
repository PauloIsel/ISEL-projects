package com.example.chelasmulti_playerpokerdice.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.ui.theme.localResponsiveDimensions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@Composable
fun ExpandableSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.secondary_background),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(bottom = if (expanded) 16.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = localResponsiveDimensions().mediumFontSize,
                color = colorResource(R.color.primary_text)
            )

            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colorResource(R.color.primary_text)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                text = content,
                fontSize = localResponsiveDimensions().mediumFontSize,
                textAlign = TextAlign.Justify,
                color = colorResource(R.color.primary_text)
            )
        }
    }
}

@Composable
fun GameRulesSection() {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.secondary_background),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(bottom = if (expanded) 16.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Game Rules",
                fontSize = localResponsiveDimensions().largeFontSize,
                color = colorResource(R.color.primary_text)
            )

            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colorResource(R.color.primary_text)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ExpandableSection(
                    title = "About the Game",
                    content = "Chelas Multi-Player Poker Dice is a turn-based dice game for 2 to 6 players.\n\nThe game is played over several rounds. In each round, players roll dice to form the strongest possible hand. At the end of each round, hands are compared and the strongest one wins.",
                    initiallyExpanded = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableSection(
                    title = "Basic Rules",
                    content = "Each round, every player takes one turn.\n\nDuring their turn, a player may roll the dice up to three times. After each roll, the player may keep any number of dice and re-roll the remaining ones.\n\nOnce a player finishes their rolls, their dice form a final hand for that round."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableSection(
                    title = "Dice Hands",
                    content = "Hands are ranked from strongest to weakest:\n\n• Five of a Kind\n• Four of a Kind\n• Full House\n• Straight (1–2–3–4–5)\n• Three of a Kind\n• Two Pair\n• Pair\n• Bust\n\nWhen two players have the same hand, higher dice values break the tie."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableSection(
                    title = "Winning the Game",
                    content = "The player with the strongest hand wins each round.\n\nAfter all rounds are completed, the player with the most round wins is declared the overall winner.\n\nIf a player leaves the game, they automatically lose. If only one player remains, that player is immediately declared the winner."
                )
            }
        }
    }
}

@Composable
fun IconOnlyButton(icon: ImageVector, onClick: () -> Unit,
    modifier: Modifier = Modifier, containerColor: Color? = null,
    contentColor: Color? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        modifier = modifier.size(48.dp),
        contentPadding = PaddingValues(8.dp),
        colors = buttonColors(
            containerColor = containerColor ?: colorResource(R.color.accent_highlight),
            contentColor = contentColor ?: colorResource(R.color.secondary_background),
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MainButton(onClick: () -> Unit, modifier: Modifier = Modifier,
    text: String? = null, enabled: Boolean = true,
    colorContrast: Boolean = false, content: @Composable () -> Unit = {}
) {
    val dimensions = localResponsiveDimensions()

    val containerColor = if (colorContrast) {
        colorResource(R.color.secondary_background)
    } else { colorResource(R.color.accent_highlight) }

    val contentColor = if (colorContrast) {
        colorResource(R.color.primary_text)
    } else { colorResource(R.color.secondary_background) }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(32.dp),
        colors = buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        text?.let { Text(it, fontSize = dimensions.largeFontSize) }
        content()
    }
}

@Composable
fun ThemedIconButton(icon: ImageVector, onClick: () -> Unit,
    modifier: Modifier = Modifier, showBackground: Boolean = true,
    colorContrast: Boolean = false
) {
    val containerColor = when {
        !showBackground -> Color.Transparent
        colorContrast -> colorResource(R.color.secondary_background)
        else -> colorResource(R.color.accent_highlight)
    }

    val contentColor = if (colorContrast) {
        colorResource(R.color.accent_highlight)
    } else { colorResource(R.color.secondary_background) }

    IconButton(
        onClick = onClick,
        colors = iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        )
    }
}

@Composable
fun DisplayQuitDialog(showDialog: Boolean, onDismiss: () -> Unit, onConfirmQuit: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = onConfirmQuit,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Yes", lineHeight = 8.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Cancel", lineHeight = 8.sp)
                }
            },
            text = { Text("Are you sure you want to quit?", lineHeight = 14.sp) },
            modifier = Modifier.alpha(0.8f).scale(0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier, titleContent: @Composable () -> Unit = {},
    navigationIconContent: @Composable () -> Unit = {}, actionsContent: @Composable () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = { titleContent() },
        navigationIcon = { navigationIconContent() },
        actions = { actionsContent() },
        colors = topAppBarColors(
            containerColor = colorResource(R.color.primary_background),
            titleContentColor = colorResource(R.color.primary_text),
            navigationIconContentColor = colorResource(R.color.primary_text),
        )
    )
}

@Composable
fun DefaultBottomBar(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit = {}
) {
    BottomAppBar(
        containerColor = colorResource(R.color.secondary_background),
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            content()
        }
    }
}

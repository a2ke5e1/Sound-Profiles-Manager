package com.a3.soundprofiles.ui.components.core

import androidx.annotation.IntRange
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Slider(
    modifier: Modifier = Modifier,
    title: String,
    startIcon: Painter,
    description: String? = null,
    disabled: Boolean = false,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChange: (Float) -> Unit,
    labelFormatter: (Float) -> String = {
        val format = NumberFormat.getPercentInstance()
        format.format(it)
    }
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isDragging by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val showLabel = isDragging || isPressed
    val lastStep = remember { mutableStateOf<Int?>(null) }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .alpha(if (!disabled) 1f else 0.5f)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (description != null) {
            Text(
                description, style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(Modifier.height(2.dp))
        Slider(
            enabled = !disabled,
            value = value,
            onValueChange = { newValue ->
                if (steps > 0) {
                    val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)

                    val currentStep = ((newValue - valueRange.start) / stepSize).roundToInt()

                    if (currentStep != lastStep.value) {
                        lastStep.value = currentStep
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                onValueChange(newValue)
            },
            onValueChangeFinished = {
                if (steps == 0) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            },
            valueRange = valueRange,
            steps = steps,
            interactionSource = interactionSource,
            thumb = { sliderState ->
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        TooltipLabel(
                            text = labelFormatter(value),
                            visible = showLabel,
                            modifier = Modifier
                                .offset(y = (-38).dp)
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(0, 0) {
                                        placeable.placeRelative(
                                            x = -placeable.width / 2,
                                            y = -placeable.height
                                        )
                                    }
                                }
                        )
                        SliderDefaults.Thumb(
                            interactionSource = interactionSource,
                            enabled = !disabled,
                            modifier = Modifier
                                .height(68.dp)
                        )
                    }
//                Label(
//                    label = {
//                        TooltipLabel(
//                            text = labelFormatter(value),
//                            visible = true,
//                        )
//                    },
//                    interactionSource = interactionSource,
//                ) {
//                    SliderDefaults.Thumb(
//                        interactionSource = interactionSource, enabled = !disabled
//                    )
//                }
            },
            track = {
                val iconSize = DpSize(20.dp, 20.dp)
                val iconPadding = 10.dp
                val thumbTrackGapSize = 6.dp
                val activeIconColor = SliderDefaults.colors().activeTickColor
                val inactiveIconColor = SliderDefaults.colors().inactiveTickColor
                val trackIconStart: DrawScope.(Offset, Color) -> Unit = { offset, color ->
                    translate(offset.x + iconPadding.toPx(), offset.y) {
                        with(startIcon) {
                            draw(iconSize.toSize(), colorFilter = ColorFilter.tint(color))
                        }
                    }
                }
                SliderDefaults.Track(
                    sliderState = it,
                    modifier = Modifier
                        .height(56.dp)
                        .drawWithContent {
                            drawContent()

                            val yOffset = size.height / 2 - iconSize.toSize().height / 2
                            val activeTrackStart = 0f
                            val activeTrackEnd =
                                size.width * it.coercedValueAsFraction - thumbTrackGapSize.toPx()
                            val inactiveTrackStart = activeTrackEnd + thumbTrackGapSize.toPx() * 2
                            val inactiveTrackEnd = size.width

                            val activeTrackWidth = activeTrackEnd - activeTrackStart
                            val inactiveTrackWidth = inactiveTrackEnd - inactiveTrackStart
                            val showIconLeft =
                                iconSize.toSize().width < activeTrackWidth - iconPadding.toPx() * 2

                            if (showIconLeft) {
                                trackIconStart(
                                    Offset(activeTrackStart, yOffset), activeIconColor
                                )
                            }
                            if (iconSize.toSize().width < inactiveTrackWidth - iconPadding.toPx() * 2 && !showIconLeft) {
                                trackIconStart(
                                    Offset(inactiveTrackStart, yOffset),
                                    inactiveIconColor,
                                )
                            }
                        },
                    trackCornerSize = 12.dp,
                    drawStopIndicator = null,
                    thumbTrackGapSize = thumbTrackGapSize,
                )
            },
        )
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TooltipLabel(
    text: String, visible: Boolean, modifier: Modifier = Modifier
) {

    val animatedScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f, // Dropped to 0.5f to better mimic the thumb size when hidden
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy
        ), label = "tooltip_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f, animationSpec = spring(
            stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy
        ), label = "tooltip_alpha"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f, // Increased translation to make it pull out of the thumb
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy
        ), label = "tooltip_translation"
    )

    Surface(
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
                translationY = animatedTranslationY
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .widthIn(min = 48.dp)
            .heightIn(min = 44.dp)
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy
                )
            )) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

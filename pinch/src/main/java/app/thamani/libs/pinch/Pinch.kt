package app.thamani.libs.pinch

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.thamani.libs.pinch.helpers.captureScreenshot
import kotlinx.coroutines.launch

private const val FULL_SCREEN_WEIGHT = 1f

class PinchState internal constructor() {
    var scale by mutableFloatStateOf(FULL_SCREEN_WEIGHT)
    var capturing by mutableStateOf(false)
}

@Composable
fun rememberPinchState() = remember { PinchState() }

/**
 * A wrapper component that enables a "pinch-to-capture" gesture. When the user pinches inwards
 * below a certain threshold, it triggers a screenshot capture of the provided content.
 *
 * @param state The [PinchState] used to track the current scale and capture status.
 * @param animation The [AnimationSpec] used to animate the scaling effect when pinching or resetting.
 * @param threshold The minimum scale factor allowed during the pinch gesture (e.g., 0.75f).
 * @param delay The duration in milliseconds to wait before capturing the screenshot, allowing UI updates to settle.
 * @param beforeTakingScreenshot A callback invoked immediately before the screenshot is captured.
 * @param afterTakingScreenshot A callback invoked immediately after the screenshot capture process is completed.
 * @param pinched A callback invoked with the resulting [Bitmap] after a successful capture.
 * @param content The composable content to be displayed and captured.
 */
@Composable
fun Pinch(
    state: PinchState = rememberPinchState(),
    animation: AnimationSpec<Float> =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
    threshold: Float = 0.75f,
    delay: Long = 360L,
    cornerShape: Shape = RoundedCornerShape(24.dp),
    beforeTakingScreenshot: () -> Unit = {},
    afterTakingScreenshot: () -> Unit = {},
    pinched: (Bitmap) -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val window = (context as? Activity)?.window

    val animatedScale by animateFloatAsState(
        targetValue = state.scale,
        label = "Scale",
        animationSpec = animation,
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            if (zoomChange != 1f) {
                                state.scale = (state.scale * zoomChange).coerceIn(threshold, 1f)
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } while (event.changes.any { it.pressed })

                        // --- RELEASE LOGIC ---
                        if (state.scale <= 0.85f) {
                            // Launch the capture process
                            if (window != null) {
                                scope.launch {
                                    try {
                                        val bitmap =
                                            captureScreenshot(
                                                window = window,
                                                view = view,
                                                delay = delay,
                                                state = state,
                                                before = beforeTakingScreenshot,
                                                after = afterTakingScreenshot,
                                            )
                                        pinched(bitmap)
                                    } catch (e: Exception) {
                                        Log.e("Pinch", "Failed taking screenshot", e)
                                    }
                                }
                            }
                        }

                        // Reset scale immediately for the user's eye
                        state.scale = FULL_SCREEN_WEIGHT
                    }
                }.graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    // We typically DON'T want the screenshot to look shrunk,
                    // so we don't apply clip/shape if isCapturing is true
                    if (animatedScale < 0.98f && !state.capturing) {
                        clip = true
                        shape = cornerShape
                    }
                },
    ) {
        content()
    }
}

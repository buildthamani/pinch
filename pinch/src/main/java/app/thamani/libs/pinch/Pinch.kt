package app.thamani.libs.pinch

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class PinchState internal constructor() {
    var scale by mutableFloatStateOf(1f)
    var capturing by mutableStateOf(false)
}

@Composable
fun rememberPinchState() = remember { PinchState() }

@Composable
fun Pinch(
    state: PinchState = rememberPinchState(),
    onScreenshotTaken: (Bitmap) -> Unit = {},
    animation: AnimationSpec<Float> =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
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
                                state.scale = (state.scale * zoomChange).coerceIn(0.75f, 1f)
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } while (event.changes.any { it.pressed })

                        // --- RELEASE LOGIC ---
                        if (state.scale <= 0.85f) {
                            // Launch the capture process
                            if (window != null) {
                                scope.launch {
                                    try {
                                        val bitmap = captureScreenshot(window, view, state)
                                        onScreenshotTaken(bitmap)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast
                                            .makeText(
                                                context,
                                                "screenshot failed",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                }
                            }
                        }

                        // Reset scale immediately for the user's eye
                        state.scale = 1f
                    }
                }.graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    // We typically DON'T want the screenshot to look shrunk,
                    // so we don't apply clip/shape if isCapturing is true
                    if (animatedScale < 0.98f && !state.capturing) {
                        clip = true
                        shape = RoundedCornerShape(24.dp)
                    }
                },
    ) {
        content()
    }
}

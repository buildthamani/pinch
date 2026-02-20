package app.thamani.libs.pinch.helpers

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.core.graphics.createBitmap
import app.thamani.libs.pinch.PinchState
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun captureScreenshot(
    window: Window,
    view: View,
    delay: Long = 150L,
    state: PinchState,
    before: () -> Unit,
    after: () -> Unit,
): Bitmap {
    // 1. run configurations before taking the screenshot
    before()
    state.capturing = true

    // 2. Wait for UI to update
    yield()
    delay(delay)

    // 3. Prepare the bitmap
    val bitmap = createBitmap(view.width, view.height)

    // 4. Calculate exactly where the view is on the screen
    val locationOfViewInWindow = IntArray(2)
    view.getLocationInWindow(locationOfViewInWindow)

    val rect =
        Rect(
            locationOfViewInWindow[0],
            locationOfViewInWindow[1],
            locationOfViewInWindow[0] + view.width,
            locationOfViewInWindow[1] + view.height,
        )

    // 5. Use PixelCopy to read from the Surface (GPU)
    return suspendCancellableCoroutine { continuation ->
        try {
            PixelCopy.request(
                window,
                rect,
                bitmap,
                { copyResult ->

                    state.capturing = false

                    if (copyResult == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        continuation.resumeWithException(
                            RuntimeException("PixelCopy failed with result: $copyResult"),
                        )
                    }
                },
                Handler(Looper.getMainLooper()),
            )
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        } finally {
            // 6. run configurations after taking the screenshot
            state.capturing = false
            after()
        }
    }
}

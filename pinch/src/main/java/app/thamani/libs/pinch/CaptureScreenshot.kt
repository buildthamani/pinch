package app.thamani.libs.pinch

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.graphics.createBitmap

internal suspend fun captureScreenshot(
    window: Window,
    view: View,
    state: PinchState,
): Bitmap {
    // 1. Force Privacy Mode ON
    state.capturing = true

    // 2. Wait for UI to update
    yield()
    delay(150)

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
                    // Reset Privacy Mode immediately after copy
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
            state.capturing = false
            continuation.resumeWithException(e)
        }
    }
}
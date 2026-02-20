# Pinch
[![](https://jitpack.io/v/buildthamani/pinch.svg)](https://jitpack.io/#buildthamani/pinch)

## Introduction

Pinch is a compose library that enables users to capture screenshots by performing a pinch-in gesture. 
It simplifies the process of adding screenshot functionality to your Android application, providing a smooth and interactive user experience.

## Demo

[![Watch the demo](https://placehold.co/600x400?text=Watch+Demo)](.github/assets/video/pinch.mp4)

## Installation

To add `Pinch` to your project, first add the JitPack repository to your root `build.gradle` or `settings.gradle`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then, add the dependency to your module's `build.gradle`:

```kotlin
dependencies {
    implementation("com.github.buildthamani:pinch:{tag}")
}
```

> Replace `{tag}` with the latest version available on JitPack.

## Usage

Using `Pinch` is straightforward. Simply wrap your Composable content with the `Pinch` composable.

```kotlin
import app.thamani.libs.pinch.Pinch

Pinch(
    pinched = { bitmap ->
        // Handle the captured screenshot bitmap here
        // e.g., save it, share it, or display it
    }
) {
    // Your UI content goes here
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // ...
    }
}
```

## Customization

You can customize the behavior of the pinch gesture and screenshot capture using the following parameters:

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `state` | `PinchState` | `rememberPinchState()` | Manages the state of the pinch gesture and capture process. |
| `animation` | `AnimationSpec<Float>` | `spring(...)` | Defines the animation for the scaling effect. |
| `threshold` | `Float` | `0.75f` | The scale threshold to trigger the screenshot capture. |
| `delay` | `Long` | `360L` | Time in milliseconds to wait before capturing the screenshot. |
| `cornerShape` | `Shape` | `RoundedCornerShape(24.dp)` | The shape of the content during the pinch animation. |
| `beforeTakingScreenshot` | `() -> Unit` | `{}` | Callback invoked before the screenshot is taken. |
| `afterTakingScreenshot` | `() -> Unit` | `{}` | Callback invoked after the screenshot is taken. |
| `pinched` | `(Bitmap) -> Unit` | `{}` | Callback providing the captured `Bitmap`. |

### Example with Customization

```kotlin
Pinch(
    threshold = 0.7f,
    delay = 500L,
    beforeTakingScreenshot = {
        // Hide sensitive info or UI controls
    },
    pinched = { bitmap ->
        // Process the bitmap
    },
    afterTakingScreenshot = {
        // Restore UI state
    }
) {
    // Content
}
```

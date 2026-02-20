package app.thamani.libs.pinch.sample

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.thamani.libs.pinch.Pinch
import app.thamani.libs.pinch.sample.ui.theme.PinchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            var bitmaps by remember { mutableStateOf(listOf<Bitmap>()) }

            PinchTheme {
                Pinch(
                    pinched = {
                        val update = bitmaps.toMutableList()
                        update.add(it)
                        bitmaps = update

                        scope.launch {
                            delay(2500)
                            val update = bitmaps.toMutableList()
                            update.removeFirst()
                            bitmaps = update
                        }
                    },
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "Pinch")
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                        bitmaps
                            .mapNotNull {
                                try {
                                    it.asImageBitmap()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }.forEachIndexed { index, image ->
                                val padding = (index + 8).dp
                                Card(
                                    modifier =
                                        Modifier
                                            .padding(bottom = padding, end = padding)
                                            .width(75.dp)
                                            .height(150.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                ) {
                                    Image(
                                        modifier = Modifier.fillMaxSize(),
                                        bitmap = image,
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

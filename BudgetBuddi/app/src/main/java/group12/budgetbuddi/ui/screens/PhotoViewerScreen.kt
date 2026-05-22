// Code Attributions

/* Stack Overflow (2023). How to Use Null as Default Value in Compose MutableState [Online].
* Available at: https://stackoverflow.com/questions/75425433/how-to-use-null-as-default-value-compose-mutablestate
* (Accessed: 29 April 2026)
*
* Stack Overflow (2024). Jetpack Compose Button Alignment [Online].
* Available at: https://stackoverflow.com/questions/79085592/jetpack-compose-button-alignment
* (Accessed: 29 April 2026)
*
* Android Developers (2024). Lists and Grids in Jetpack Compose [Online].
* Available at: https://developer.android.com/develop/ui/compose/lists
* (Accessed: 29 April 2026)
*/


package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    photoPath: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Now photoPath is either a Supabase URL (https://...) or empty
    val imageModel: Any? = when {
        photoPath.startsWith("https://") || photoPath.startsWith("http://") -> photoPath
        photoPath.startsWith("content://") -> android.net.Uri.parse(photoPath)
        photoPath.isNotBlank() -> {
            val file = java.io.File(photoPath)
            if (file.exists()) android.net.Uri.fromFile(file) else null
        }
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Viewer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full size expense photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Photo not found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The image may have been removed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
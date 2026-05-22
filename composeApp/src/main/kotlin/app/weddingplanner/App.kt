package app.weddingplanner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.weddingplanner.di.AppContainer
import app.weddingplanner.ui.nav.RootNavigation

@Composable
fun App(container: AppContainer) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RootNavigation(apiClient = container.apiClient)
        }
    }
}

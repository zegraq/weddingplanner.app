package app.weddingplanner.di

import app.weddingplanner.api.ApiClient
import app.weddingplanner.api.MockApiClient

class AppContainer {
    val apiClient: ApiClient = MockApiClient()
}

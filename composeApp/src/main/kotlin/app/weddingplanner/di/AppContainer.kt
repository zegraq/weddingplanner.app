package app.weddingplanner.di

import app.weddingplanner.api.ApiClient
import app.weddingplanner.api.MockApiClient
import app.weddingplanner.domain.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class AppContainer {
    private val clock: Clock = Clock {
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
    val apiClient: ApiClient = MockApiClient(clock = clock)
}

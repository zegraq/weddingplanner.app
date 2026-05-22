package app.weddingplanner.api

class MockApiClient : ApiClient {
    override suspend fun getWedding(): Wedding = Wedding(
        date = "2027-06-05",
        venue = null,
    )
}

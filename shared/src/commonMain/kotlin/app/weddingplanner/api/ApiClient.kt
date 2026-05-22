package app.weddingplanner.api

interface ApiClient {
    suspend fun getWedding(): Wedding
}

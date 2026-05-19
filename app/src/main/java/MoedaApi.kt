import retrofit2.http.GET
import retrofit2.http.Path

interface MoedaApi {
    @GET("json/last/{pair}")
    suspend fun getExchange(
        @Path("pair") pair: String
    ): Map<String, Moeda>
}
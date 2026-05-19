package com.jorge.mobile_03_wallet

import retrofit2.http.GET
import retrofit2.http.Path

data class CotacaoResponse(
    val bid: String,
    val ask: String
)

interface AwesomeApiService {
    @GET("last/{par}")
    suspend fun getCotacao(@Path("par") par: String): Map<String, CotacaoResponse>
}
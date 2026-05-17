package com.loyalt.loyaltclient.retrofit
import com.loyalt.loyaltclient.dto.PartnerResponse
import com.loyalt.loyaltclient.dto.PaymentRequest
import com.loyalt.loyaltclient.dto.TransactionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface LoyaltyApi {
    @GET("/api/partners")
    suspend fun getPartners(): List<PartnerResponse>

    @POST("/api/bank")
    suspend fun processPayment(@Body request: PaymentRequest)

    @POST("/api/analytics")
    suspend fun sendTransaction(@Body request: TransactionRequest)
}
package com.loyalt.loyaltclient.retrofit
import com.loyalt.loyaltclient.dto.PartnerResponse
import com.loyalt.loyaltclient.dto.PaymentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface LoyaltyApi {
    @GET("/api/partners")
    suspend fun getPartners(): List<PartnerResponse>

    @POST("/api/pay")
    suspend fun processPayment(@Body request: PaymentRequest)
}
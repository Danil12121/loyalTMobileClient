package com.loyalt.loyaltclient.dto

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    val clientId: Int,
    @SerializedName("partnerId")
    val partnerId: String,
    val balance: Int,
    val loyaltyType: String,
    val currValue: Int,
    val maxValueOrPercent: Int
)
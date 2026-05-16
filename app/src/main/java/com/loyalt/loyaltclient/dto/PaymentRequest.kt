package com.loyalt.loyaltclient.dto

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("id") val partnerId: String,
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: Int
)
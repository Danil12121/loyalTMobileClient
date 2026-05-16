package com.loyalt.loyaltclient.dto

import com.google.gson.annotations.SerializedName

data class PartnerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("type") val loyaltyType: String,
    @SerializedName("value") val currValue: Int,
)
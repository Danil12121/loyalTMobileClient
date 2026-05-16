package com.loyalt.loyaltclient.models

data class Partner(
    val clientId: Long,
    val partnerId: String,
    var balance: Int,
    val loyaltyType: LoyaltyType,
    var currentValueLoyalty: Int,
    val maxValueOrPercent: Int,
)
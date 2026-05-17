package com.loyalt.loyaltclient.dto;

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
        val transactionId: Int,
        @SerializedName("partnerId")
        val partnerId: String,
        val clientId: Int,
        val amount: Int,
        val date: String
)
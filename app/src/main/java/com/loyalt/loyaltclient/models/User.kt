package com.loyalt.loyaltclient.models

data class User(
    val name: String,
    var totalBalance: Int,
    var cashbackBalance: Int
)
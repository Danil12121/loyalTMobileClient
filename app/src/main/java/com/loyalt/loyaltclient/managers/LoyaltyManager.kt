package com.loyalt.loyaltclient.managers


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object LoyaltyManager{
    private const val PREFS_NAME = "loyalty_prefs"
    private const val KEY_STAMPS_PREFIX = "stamps_"
    private const val KEY_CASHBACK_PREFIX = "cashback_"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveStamps(partnerId: String, stamps: Int) {
        prefs.edit { putInt(KEY_STAMPS_PREFIX + partnerId, stamps) }
    }

    fun getStamps(partnerId: String): Int {
        return prefs.getInt(KEY_STAMPS_PREFIX + partnerId, 0)
    }

    fun saveCashback(partnerId: String, cashback: Int) {
        prefs.edit { putInt(KEY_CASHBACK_PREFIX + partnerId, cashback) }
    }

    fun getCashback(partnerId: String): Int {
        return prefs.getInt(KEY_CASHBACK_PREFIX + partnerId, 0)
    }
}
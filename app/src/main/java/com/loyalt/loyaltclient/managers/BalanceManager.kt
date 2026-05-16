package com.loyalt.loyaltclient.managers

import android.content.Context
import android.content.SharedPreferences

object BalanceManager {
    private const val PREFS_NAME = "balance_prefs"
    private const val KEY_BALANCE = "client_balance"
    private const val DEFAULT_BALANCE = 10000

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getBalance(): Int {
        return prefs.getInt(KEY_BALANCE, DEFAULT_BALANCE.toInt()).toInt()
    }

    fun deduct(amount: Int): Boolean {
        val current = getBalance()
        if (current >= amount) {
            prefs.edit().putInt(KEY_BALANCE, (current - amount).toInt()).apply()
            return true
        }
        return false
    }

    fun add(amount: Int) {
        val current = getBalance()
        prefs.edit().putInt(KEY_BALANCE, (current + amount)).apply()
    }
}
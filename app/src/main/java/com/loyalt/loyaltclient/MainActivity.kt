package com.loyalt.loyaltclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.loyalt.loyaltclient.adapter.PartnerAdapter
import com.loyalt.loyaltclient.databinding.ActivityMainBinding
import com.loyalt.loyaltclient.databinding.BottomSheetPaymentBinding
import com.loyalt.loyaltclient.dto.PartnerResponse
import com.loyalt.loyaltclient.dto.PaymentRequest
import com.loyalt.loyaltclient.dto.TransactionRequest
import com.loyalt.loyaltclient.managers.BalanceManager
import com.loyalt.loyaltclient.managers.LoyaltyManager
import com.loyalt.loyaltclient.models.LoyaltyType
import com.loyalt.loyaltclient.models.Partner
import com.loyalt.loyaltclient.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val COST = 320
    private lateinit var adapter: PartnerAdapter
    private var partnersList = mutableListOf<Partner>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BalanceManager.init(this)
        LoyaltyManager.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        updateBalanceDisplay()
        loadPartnersFromBackend()
    }

    private fun updateBalanceDisplay() {
        val balance = BalanceManager.getBalance()
        binding.tvTotalBalance.text = "Баланс: $balance ₽"
        android.util.Log.d("MainActivity", "Обновлён баланс: $balance")
    }

    private fun setupObservers() {
        binding.button2.setOnClickListener {
            loadPartnersFromBackend()
        }
    }

    private fun setupRecyclerView() {
        adapter = PartnerAdapter(partnersList) { partner ->
            showPaymentBottomSheet(partner)
        }
        binding.recyclerViewPartners.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPartners.adapter = adapter
    }

    private fun loadPartnersFromBackend() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("MainActivity", "Начинаем загрузку партнеров")
                val response = RetrofitClient.api.getPartners()
                android.util.Log.d("MainActivity", "Получен ответ: ${response.size} партнеров")

                val mappedList = response.map { it.toUiModel() }

                withContext(Dispatchers.Main) {
                    partnersList.clear()
                    partnersList.addAll(mappedList)
                    adapter.notifyDataSetChanged()

                    val totalCashback = partnersList
                        .filter { it.loyaltyType == LoyaltyType.CASHBACK }
                        .sumOf { it.currentValueLoyalty }

                    binding.tvTotalCashback.text = "Кэшбек: $totalCashback ₽"
                    updateBalanceDisplay()
                    android.util.Log.d("MainActivity", "UI обновлен")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Ошибка загрузки", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showPaymentBottomSheet(partner: Partner) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bsBinding = BottomSheetPaymentBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bsBinding.root)

        bsBinding.tvBsPartnerName.text = partner.partnerId
        bsBinding.tvBsPrice.text = "К оплате: $COST ₽"
        if (partner.loyaltyType == LoyaltyType.STAMP_CARD) {
            bsBinding.tvBsLoyaltyStatus.text = "Печати: ${partner.currentValueLoyalty} из ${partner.maxValueOrPercent}"
        } else {
            bsBinding.tvBsLoyaltyStatus.text = "Накоплено кэшбека: ${partner.currentValueLoyalty} ₽"
        }

        bsBinding.btnPay.setOnClickListener {
            val success = BalanceManager.deduct(COST)

            android.util.Log.d("MainActivity", "Попытка списания $COST, успех: $success, баланс: ${BalanceManager.getBalance()}")

            if (!success) {
                Toast.makeText(this, "Недостаточно средств на балансе!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bsBinding.btnPay.isEnabled = false
            bsBinding.btnPay.text = "Оплата..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 1. Вычисляем новые значения лояльности
                    var newLoyaltyValue = partner.currentValueLoyalty

                    when (partner.loyaltyType) {
                        LoyaltyType.STAMP_CARD -> {
                            newLoyaltyValue += 1
                            if (newLoyaltyValue >= partner.maxValueOrPercent) {
                                newLoyaltyValue = 0
                            }
                        }
                        LoyaltyType.CASHBACK -> {
                            val cashbackPercent = partner.maxValueOrPercent
                            val cashbackAmount = (COST * cashbackPercent) / 100
                            newLoyaltyValue += cashbackAmount
                        }
                    }

                    val updateRequest =
                        PaymentRequest(
                            clientId = if (partner.clientId == 0) 1 else partner.clientId,
                            partnerId = partner.partnerId,
                            balance = BalanceManager.getBalance(),
                            loyaltyType = partner.loyaltyType.name,
                            currValue = newLoyaltyValue,
                            maxValueOrPercent = partner.maxValueOrPercent
                    )

                    RetrofitClient.api.processPayment(updateRequest)

                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                    val currentDate = sdf.format(java.util.Date())

                    val transactionRequest = TransactionRequest(
                        transactionId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                        partnerId = partner.partnerId,
                        clientId = if (partner.clientId == 0) 1 else partner.clientId,
                        amount = COST,
                        date = currentDate
                    )

                    android.util.Log.d("MainActivity", "Отправка транзакции: $transactionRequest")
                    RetrofitClient.api.sendTransaction(transactionRequest)

                    if (partner.loyaltyType == LoyaltyType.STAMP_CARD) {
                        LoyaltyManager.saveStamps(partner.partnerId, newLoyaltyValue)
                    } else {
                        LoyaltyManager.saveCashback(partner.partnerId, newLoyaltyValue)
                    }

                    val updatedPartner = partner.copy(currentValueLoyalty = newLoyaltyValue)

                    withContext(Dispatchers.Main) {
                        val index = partnersList.indexOfFirst { it.partnerId == partner.partnerId }
                        if (index != -1) {
                            partnersList[index] = updatedPartner
                            adapter.notifyItemChanged(index)
                        }

                        val totalCashback = partnersList
                            .filter { it.loyaltyType == LoyaltyType.CASHBACK }
                            .sumOf { it.currentValueLoyalty }
                        binding.tvTotalCashback.text = "Кэшбек: $totalCashback ₽"

                        updateBalanceDisplay()
                        bottomSheetDialog.dismiss()

                        val message = if (partner.loyaltyType == LoyaltyType.CASHBACK) {
                            val earned = (COST * partner.maxValueOrPercent) / 100
                            "Оплата успешна! Начислено кэшбека: $earned ₽"
                        } else {
                            "Оплата успешна! Печать начислена."
                        }
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    BalanceManager.add(COST)
                    android.util.Log.e("MainActivity", "Ошибка оплаты на бэкенде", e)

                    withContext(Dispatchers.Main) {
                        bsBinding.btnPay.isEnabled = true
                        bsBinding.btnPay.text = "Оплатить"
                        updateBalanceDisplay()
                        Toast.makeText(this@MainActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun PartnerResponse.toUiModel(): Partner {
        val savedStamps = LoyaltyManager.getStamps(this.id)
        val savedCashback = LoyaltyManager.getCashback(this.id)

        val loyaltyType = if (this.loyaltyType == "CASHBACK") LoyaltyType.CASHBACK else LoyaltyType.STAMP_CARD

        val currentValue = when (loyaltyType) {
            LoyaltyType.STAMP_CARD -> savedStamps
            LoyaltyType.CASHBACK -> savedCashback
        }

        android.util.Log.d("MainActivity", "Партнёр ${this.id}: тип=$loyaltyType, сохранено=$currentValue")

        return Partner(
            clientId = 0,
            partnerId = this.id,
            loyaltyType = loyaltyType,
            maxValueOrPercent = this.currValue,
            currentValueLoyalty = currentValue,
            balance = BalanceManager.getBalance()
        )
    }
}
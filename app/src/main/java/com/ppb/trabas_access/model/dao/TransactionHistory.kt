package com.ppb.trabas_access.model.dao

data class TransactionHistory(
    val userId: String? = "",
    val email: String? = "",
    val transactionId: String? = "",
    val ticketPrice: Long? = 0,
    val timeStamp: String? = "",
    val status: String? = ""
)

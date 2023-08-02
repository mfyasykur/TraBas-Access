package com.ppb.trabas_access.model.dao

data class TransactionHistory(
    val uidUser: String,
    val transactionID: String,
    val ticketPrice: Int,
    val timeStamp: Long,
    val status: String
)

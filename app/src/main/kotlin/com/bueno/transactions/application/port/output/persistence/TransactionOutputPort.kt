package com.bueno.transactions.application.port.output.persistence

import com.bueno.transactions.domain.entity.Transaction

interface TransactionOutputPort {
    suspend fun save(transaction: Transaction) : Transaction
}
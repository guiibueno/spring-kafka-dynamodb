package com.bueno.transactions.application.port.input

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.domain.entity.Transaction

interface TransactionRequestPort {
    fun handle(requests: List<TransactionRequestDto>): List<Transaction>
}
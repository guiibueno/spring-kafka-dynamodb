package com.bueno.transactions.application.port.output.authorizer

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.domain.entity.Authorization

interface TransactionAuthorizer {
    fun authorize(request: TransactionRequestDto) : Authorization
}
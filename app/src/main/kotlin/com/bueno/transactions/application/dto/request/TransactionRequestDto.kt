package com.bueno.transactions.application.dto.request

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TransactionRequestDto (val timestamp: LocalDateTime,
                                  val id: UUID,
                                  val accountId: UUID,
                                  val type: String,
                                  val value: BigDecimal,
                                  val description: String) {
    constructor(id: UUID, accountId: UUID, type: String, value: BigDecimal, description: String) : this(LocalDateTime.now(), id, accountId, type, value, description)
}
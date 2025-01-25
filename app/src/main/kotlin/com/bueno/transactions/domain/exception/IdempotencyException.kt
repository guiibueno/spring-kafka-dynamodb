package com.bueno.transactions.domain.exception

import java.util.*

data class IdempotencyException(val id: UUID) : RuntimeException("Transaction $id already processed.")
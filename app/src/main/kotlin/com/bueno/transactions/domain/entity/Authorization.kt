package com.bueno.transactions.domain.entity

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@DynamoDbBean
data class Authorization (@get:DynamoDbAttribute(value = "timestamp") var timestamp: LocalDateTime = LocalDateTime.now(),
                          @get:DynamoDbAttribute(value = "code") var code: AuthorizationCode? = null,
                          @get:DynamoDbAttribute(value = "reason") var reason: String = "") {

    constructor(code: AuthorizationCode, reason: String) : this(LocalDateTime.now(), code, reason)

    companion object AuthorizationReasons {
        val AUTHORIZATION_REASON_APPROVED = ""
        val AUTHORIZATION_REASON_REJECTED_TIMEOUT = "TIMEOUT"
        val AUTHORIZATION_REASON_REJECTED_IDEMPOTENCY = "IDEMPOTENCY_ERROR"
    }
}

enum class AuthorizationCode {
    APPROVED,
    REJECTED
}




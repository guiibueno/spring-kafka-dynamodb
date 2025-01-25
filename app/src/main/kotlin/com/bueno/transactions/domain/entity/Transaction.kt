package com.bueno.transactions.domain.entity

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@DynamoDbBean
class Transaction(@get:DynamoDbPartitionKey
                  @get:DynamoDbAttribute(value = "id")
                  var id: UUID? = null,

                  @get:DynamoDbAttribute(value = "timestamp")
                  var timestamp: LocalDateTime = LocalDateTime.now(),

                  @get:DynamoDbAttribute(value = "accountId")
                  var accountId: UUID? = null,

                  @get:DynamoDbAttribute(value = "type")
                  var type: TransactionType? = null,

                  @get:DynamoDbAttribute(value = "value")
                  var value: BigDecimal = BigDecimal.ZERO,

                  @get:DynamoDbAttribute(value = "description")
                  var description: String = "",

                  @get:DynamoDbAttribute(value = "authorization")
                  var authorization: Authorization? = null) {

    constructor(id: UUID, accountId: UUID, type: TransactionType, value: BigDecimal, description: String) : this(id, LocalDateTime.now(), accountId, type, value, description)
    constructor(id: UUID, accountId: UUID, type: TransactionType, value: BigDecimal, description: String, authorization: Authorization) : this(id, LocalDateTime.now(), accountId, type, value, description, authorization)
}

enum class TransactionType {
    CREDITO,
    DEBITO
}
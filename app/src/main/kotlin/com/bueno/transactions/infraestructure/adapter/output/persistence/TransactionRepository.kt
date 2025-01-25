package com.bueno.transactions.infraestructure.adapter.output.persistence

import com.bueno.transactions.application.port.output.persistence.TransactionOutputPort
import com.bueno.transactions.domain.entity.Transaction
import com.bueno.transactions.domain.exception.IdempotencyException
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import kotlin.math.log

@Repository
class TransactionRepository(
    private val transactionsTable : DynamoDbAsyncTable<Transaction>
) : TransactionOutputPort {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun save(transaction: Transaction): Transaction {
        val putItemRequest: PutItemEnhancedRequest<Transaction> = PutItemEnhancedRequest
            .builder(Transaction::class.java)
            .item(transaction)
            .conditionExpression(Expression.builder().expression("attribute_not_exists(id)").build())
            .build()

        try{
            val putItemResponse = transactionsTable.putItem(putItemRequest).await()
        } catch (idempotentErr: ConditionalCheckFailedException){
            throw IdempotencyException(transaction.id!!)
        }

        return transaction
    }
}
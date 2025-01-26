package com.bueno.transactions.application.usecase

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.input.TransactionRequestPort
import com.bueno.transactions.application.port.output.authorizer.TransactionAuthorizer
import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import com.bueno.transactions.application.port.output.persistence.TransactionOutputPort
import com.bueno.transactions.domain.entity.Authorization
import com.bueno.transactions.domain.entity.AuthorizationCode
import com.bueno.transactions.domain.entity.Transaction
import com.bueno.transactions.domain.entity.TransactionType
import com.bueno.transactions.domain.exception.IdempotencyException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis

@Service
class TransactionRequestUseCase(
    private val transactionAuthorizer: TransactionAuthorizer,
    private val transactionOutputPort: TransactionOutputPort,
    private val metricsOutputPort: MetricsOutputPort
) : TransactionRequestPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(requests: List<TransactionRequestDto>): List<Transaction> {
        val transactions = Collections.synchronizedList(mutableListOf<Transaction>())

        runBlocking {
            requests.forEach {
                    request ->
                        run {
                            launch {
                                val result = handle(request)
                                transactions.add(result)
                            }
                        }
            }
        }

        return transactions
    }

    private suspend fun handle(request: TransactionRequestDto): Transaction {
        var transaction: Transaction

        val elapsedTime = measureTimeMillis {
            val type = TransactionType.valueOf(request.type)
            val authorization = transactionAuthorizer.authorize(request)
            transaction = Transaction(request.id, LocalDateTime.now(), request.accountId, type, request.value, request.description, authorization)

            try{
                transactionOutputPort.save(transaction)
            } catch (idempotenceException: IdempotencyException){
                transaction.authorization = Authorization(
                    code = AuthorizationCode.REJECTED,
                    reason = Authorization.AUTHORIZATION_REASON_REJECTED_IDEMPOTENCY
                )
            }
        }

        metricsOutputPort.transactionProcessed(
            transaction = transaction,
            elapsedTime = elapsedTime
        )

        return transaction
    }
}
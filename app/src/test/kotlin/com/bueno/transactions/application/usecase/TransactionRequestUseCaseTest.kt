package com.bueno.transactions.application.usecase

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.input.TransactionRequestPort
import com.bueno.transactions.application.port.output.authorizer.TransactionAuthorizer
import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import com.bueno.transactions.application.port.output.persistence.TransactionOutputPort
import com.bueno.transactions.application.service.RandomAuthorizer
import com.bueno.transactions.domain.entity.Authorization
import com.bueno.transactions.domain.entity.AuthorizationCode
import com.bueno.transactions.domain.entity.Transaction
import com.bueno.transactions.domain.entity.TransactionType
import com.bueno.transactions.domain.exception.IdempotencyException
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionRequestUseCaseTest {
    @MockK
    private lateinit var transactionAuthorizer: TransactionAuthorizer

    @MockK
    private lateinit var transactionOutputPort: TransactionOutputPort

    @MockK
    private lateinit var metricsOutputPort: MetricsOutputPort

    private val transactionRequestPort: TransactionRequestPort

    init {
        MockKAnnotations.init(this)
        transactionRequestPort = spyk(
            TransactionRequestUseCase(transactionAuthorizer, transactionOutputPort, metricsOutputPort)
        )
    }

    @Test
    fun `should process transactions successfully`() {
        val requestDto = TransactionRequestDto(
            id = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            type = "DEBIT",
            value = BigDecimal.TEN,
            description = "Test transaction"
        )

        val authorization = Authorization(code = AuthorizationCode.APPROVED, reason = "")

        val transaction = Transaction(
            id = requestDto.id,
            timestamp = requestDto.timestamp,
            accountId = requestDto.accountId,
            type = TransactionType.DEBIT,
            value = requestDto.value,
            description = requestDto.description,
            authorization = authorization
        )

        coEvery {  transactionOutputPort.save(any()) } returns transaction
        every {  transactionAuthorizer.authorize(any()) } returns authorization
        every {  metricsOutputPort.transactionProcessed(any(), any()) } returns Unit

        val result = transactionRequestPort.handle(listOf(requestDto))

        assertEquals(1, result.count())
        assertEquals(requestDto.id, result[0].id)
        assertEquals(requestDto.accountId, result[0].accountId)
        assertEquals(TransactionType.DEBIT, result[0].type)
        assertEquals(requestDto.value, result[0].value)
        assertEquals(requestDto.description, result[0].description)
        assertEquals(AuthorizationCode.APPROVED, result[0].authorization?.code)
        assertEquals("", result[0].authorization?.reason)

        coVerify (atLeast = 1) {
            transactionOutputPort.save(any())
        }

        verify (atLeast = 1) {
            transactionAuthorizer.authorize(any())
            metricsOutputPort.transactionProcessed(any(), any())
        }
    }

    @Test
    fun `should handle idempotency exception when processing transaction`() {
        val requestDto = TransactionRequestDto(
            id = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            type = "DEBIT",
            value = BigDecimal.TEN,
            description = "Test transaction"
        )

        val authorization = Authorization(code = AuthorizationCode.APPROVED, reason = "")

        coEvery {  transactionOutputPort.save(any()) } throws IdempotencyException(requestDto.id)
        every {  transactionAuthorizer.authorize(any()) } returns authorization
        every {  metricsOutputPort.transactionProcessed(any(), any()) } returns Unit

        val result = transactionRequestPort.handle(listOf(requestDto))

        assertEquals(1, result.count())
        assertEquals(requestDto.id, result[0].id)
        assertEquals(requestDto.accountId, result[0].accountId)
        assertEquals(TransactionType.DEBIT, result[0].type)
        assertEquals(requestDto.value, result[0].value)
        assertEquals(requestDto.description, result[0].description)
        assertEquals(AuthorizationCode.REJECTED, result[0].authorization?.code)
        assertEquals(Authorization.AuthorizationReasons.AUTHORIZATION_REASON_REJECTED_IDEMPOTENCY, result[0].authorization?.reason)

        coVerify (atLeast = 1) {
            transactionOutputPort.save(any())
        }

        verify (atLeast = 1) {
            transactionAuthorizer.authorize(any())
            metricsOutputPort.transactionProcessed(any(), any())
        }
    }
}
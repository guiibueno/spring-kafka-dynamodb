package com.bueno.transactions.application.service

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.output.authorizer.TransactionAuthorizer
import com.bueno.transactions.domain.entity.AuthorizationCode
import io.mockk.MockKAnnotations
import io.mockk.spyk
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue

class RandomAuthorizerTest {
    private val transactionAuthorizer: TransactionAuthorizer

    init {
        MockKAnnotations.init(this)
        transactionAuthorizer = spyk(
            RandomAuthorizer()
        )
    }

    @Test
    fun `should authorize transaction correctly`(){
        val requestDto = TransactionRequestDto(
            id = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            type = "DEBIT",
            value = BigDecimal.TEN,
            description = "Test transaction"
        )

        val authorization = transactionAuthorizer.authorize(requestDto)

        assertTrue { authorization.code == AuthorizationCode.APPROVED && authorization.reason == "" || authorization.code == AuthorizationCode.REJECTED }
    }
}
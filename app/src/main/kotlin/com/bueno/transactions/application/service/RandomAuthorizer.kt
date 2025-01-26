package com.bueno.transactions.application.service

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.output.authorizer.TransactionAuthorizer
import com.bueno.transactions.domain.entity.Authorization
import com.bueno.transactions.domain.entity.AuthorizationCode
import org.springframework.stereotype.Service
import java.util.*

@Service
class RandomAuthorizer : TransactionAuthorizer {
    private val random = Random()

    override fun authorize(request: TransactionRequestDto) : Authorization {
        val code = if (random.nextInt() % 2 == 0)  AuthorizationCode.APPROVED else AuthorizationCode.REJECTED
        val reason = ""

        return Authorization(code, reason)
    }
}
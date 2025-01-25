package com.bueno.transactions.application.port.output.metrics

import com.bueno.transactions.domain.entity.Transaction

interface MetricsOutputPort {
    fun eventSent(topic: String, partition: String, elapsedTime: Long)
    fun eventReceived(topic: String, partition: String, latency: Long)

    fun transactionProcessed(transaction: Transaction, elapsedTime: Long)

    fun batchProcessed(topic: String, partition: String, size: Int, elapsedTime: Long)
}
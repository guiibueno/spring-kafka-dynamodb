package com.bueno.transactions.infraestructure.adapter.output.event

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import kotlin.system.measureTimeMillis

@Component
class TransactionRequestProducer(
    @Value("\${topics.transactions.requests.name}")
    private val transactionRequestTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, TransactionRequestDto>,
    private val metricsOutputPort: MetricsOutputPort
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val random = Random()

    @Scheduled(fixedDelay = 1)
    fun sendTransaction() {
        val transaction = buildTransaction()

        sendTransactionToTopic(transaction)
    }

    fun sendTransactionToTopic(transaction: TransactionRequestDto){
        var offset = ""
        var partition = ""
        var key = ""


        val elapsed = measureTimeMillis {
            kafkaTemplate.send(transactionRequestTopic, transaction.id.toString(), transaction)
                .whenComplete({ result, exception ->
                    if(exception != null) {
                        logger.error("Error sending event", exception)
                    }
                    else {
                        offset = result.recordMetadata.offset().toString()
                        partition = result.recordMetadata.partition().toString()
                        key = result.producerRecord.key()
                        logger
                            .info("Event sent successfully")
                    }
                })
        }

        metricsOutputPort.eventSent(
            topic = transactionRequestTopic,
            partition = partition,
            elapsedTime = elapsed
        )
    }

    fun buildTransaction() : TransactionRequestDto{
        val operations: List<String> = listOf("CREDIT", "DEBIT")
        val value = BigDecimal.valueOf(random.nextDouble())

        return TransactionRequestDto(UUID.randomUUID(), UUID.randomUUID(), operations[(0..1).random()], value, UUID.randomUUID().toString())
    }
}
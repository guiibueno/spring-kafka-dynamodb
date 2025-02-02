package com.bueno.transactions.infraestructure.adapter.input.listener

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.input.TransactionRequestPort
import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

@Component
class BatchTransactionRequestListener(
    private val transactionRequestPort: TransactionRequestPort,
    private val metricsOutputPort: MetricsOutputPort
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        id = "\${topics.transactions.requests.name}",
        idIsGroup = true,
        topics = ["\${topics.transactions.requests.name}"],
        containerFactory = "batchTransactionRequestListenerBean")
    fun handle(
        consumerRecords: ConsumerRecords<String, TransactionRequestDto>,
        acknowledgment: Acknowledgment
    ){
        try {
            logger.info("Batch received")

            val elapsedTime = measureTimeMillis {
                consumerRecords.map {
                    metricsOutputPort.eventReceived(
                        topic = it.topic(),
                        partition = it.partition().toString(),
                        latency = Duration.between(it.value().timestamp, LocalDateTime.now()).toMillis())

                    it.value()
                }.run{
                    transactionRequestPort.handle(this)
                }
            }

            val (topic, partition) = consumerRecords.map { it.topic() }.first() to consumerRecords.map { it.partition().toString() }.first()

            metricsOutputPort.batchProcessed(
                topic = topic,
                partition = partition,
                size = consumerRecords.count(),
                elapsedTime = elapsedTime
            )

            acknowledgment.acknowledge()
            logger.info("Batch processed successfully")
        } catch (err: Exception) {
            logger
                .error("Error processing batch", err)
        }
    }
}
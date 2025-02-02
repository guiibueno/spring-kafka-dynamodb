package com.bueno.transactions.infraestructure.adapter.output.event

import com.bueno.transactions.application.dto.request.TransactionRequestDto
import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import io.micrometer.core.instrument.Meter
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TransactionRequestProducerTest {
    private val transactionRequestProducer: TransactionRequestProducer

    @MockK
    private lateinit var kafkaTemplate: KafkaTemplate<String, TransactionRequestDto>

    @MockK
    private lateinit var metricsOutputPort: MetricsOutputPort

    private val topic: String = ""

    init {
        MockKAnnotations.init(this)

        transactionRequestProducer = spyk(
            TransactionRequestProducer(
                transactionRequestTopic = topic,
                kafkaTemplate= kafkaTemplate,
                metricsOutputPort = metricsOutputPort
            )
        )
    }

    @Test
    fun `should produce a event`() {
        val recordMetadata = RecordMetadata(
            TopicPartition(topic, 1),
            1,
            1,
            1,
            1, 1
        )
        val sendResult = SendResult<String, TransactionRequestDto>(null, recordMetadata)
        val future: CompletableFuture<SendResult<String, TransactionRequestDto>> = completedFuture(sendResult)

        every { kafkaTemplate.send(any(), any(), any()) } returns future
        justRun { metricsOutputPort.eventSent(any(), any(), any()) }

        transactionRequestProducer.sendTransaction()

        verify (atLeast = 1) {
            kafkaTemplate.send(any(), any(), any())
            metricsOutputPort.eventSent(any(), any(), any())
        }
    }
}
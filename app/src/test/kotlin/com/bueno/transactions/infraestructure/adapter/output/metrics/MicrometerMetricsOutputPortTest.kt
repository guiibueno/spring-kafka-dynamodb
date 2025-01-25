package com.bueno.transactions.infraestructure.adapter.output.metrics

import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import com.bueno.transactions.domain.entity.Authorization
import com.bueno.transactions.domain.entity.Authorization.AuthorizationReasons.AUTHORIZATION_REASON_APPROVED
import com.bueno.transactions.domain.entity.AuthorizationCode
import com.bueno.transactions.domain.entity.Transaction
import com.bueno.transactions.domain.entity.TransactionType
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test

class MicrometerMetricsOutputPortTest {

    private val metricsOutputPort: MetricsOutputPort

    private val meterRegistry: MeterRegistry

    init {
        MockKAnnotations.init(this)

        meterRegistry = spyk(
            SimpleMeterRegistry(),
            recordPrivateCalls = true
        )

        metricsOutputPort = spyk(
            MicrometerMetricsOutputPort(meterRegistry)
        )
    }

    @Test
    fun `should register sent event metric`() {
        val topic = "topic1"
        val partition = "partition1"
        val elapsed = 100L

        val metricName = "event.sent"
        val expectedTags = arrayOf(
            "topic" to topic,
            "partition" to partition
        ).map {
            Tag.of(it.first, it.second)
        }

        metricsOutputPort.eventSent(topic, partition, elapsed)

        verify (exactly = 1) {
            meterRegistry invoke "counter" withArguments listOf(
                match<Meter.Id> {
                    it.name.startsWith(metricName)
                            && it.type == Meter.Type.COUNTER
                            && it.tags.containsAll(expectedTags)
                }
            )
        }
    }

    @Test
    fun `should register received event metric`() {
        val topic = "topic1"
        val partition = "partition1"
        val latency = 150L

        val metricName = "event.received"
        val expectedTags = arrayOf(
            "topic" to topic,
            "partition" to partition
        ).map {
            Tag.of(it.first, it.second)
        }

        metricsOutputPort.eventReceived(topic, partition, latency)

        verify (exactly = 1) {
            meterRegistry invoke "counter" withArguments listOf(
                match<Meter.Id> {
                    it.name.startsWith(metricName)
                            && it.type == Meter.Type.COUNTER
                            && it.tags.containsAll(expectedTags)
                }
            )
        }

    }

    @Test
    fun `should register batch processed metric`() {
        val topic = "topic1"
        val partition = "partition1"
        val size = 10
        val elapsedTime = 200L

        val metricName = "batch.processed"
        val expectedTags = arrayOf(
            "topic" to topic,
            "partition" to partition,
            "size" to size.toString()
        ).map {
            Tag.of(it.first, it.second)
        }

        metricsOutputPort.batchProcessed(topic, partition, size, elapsedTime)

        verify (exactly = 1) {
            meterRegistry invoke "counter" withArguments listOf(
                match<Meter.Id> {
                    it.name.startsWith(metricName)
                            && it.type == Meter.Type.COUNTER
                            && it.tags.containsAll(expectedTags)
                }
            )
        }

    }

    @Test
    fun `should register transaction processed metric`(){
        val transaction = Transaction(
            id = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            type = TransactionType.DEBITO,
            value = BigDecimal.TEN,
            description = "Test",
            authorization = Authorization(
                code = AuthorizationCode.APPROVED,
                reason = AUTHORIZATION_REASON_APPROVED
            )
        )
        val elapsedTime = 300L

        val metricName = "transaction.processed"
        val expectedTags = arrayOf(
            "account" to transaction.accountId.toString(),
            "type" to transaction.type.toString(),
            "status" to transaction.authorization!!.code.toString(),
            "reason" to transaction.authorization!!.reason
        ).map {
            Tag.of(it.first, it.second)
        }

        metricsOutputPort.transactionProcessed(transaction, elapsedTime)

        verify (exactly = 1) {
            meterRegistry invoke "counter" withArguments listOf(
                match<Meter.Id> {
                    it.name.startsWith(metricName)
                            && it.type == Meter.Type.COUNTER
                            && it.tags.containsAll(expectedTags)
                }
            )
        }
    }

    @Test
    fun `should log error when failing to register metrics`(){
        val topic = "topic1"
        val partition = "partition1"
        val elapsed = 100L

        val metricName = "event.sent"

        every { meterRegistry.counter(anyString(), anyList()) } throws Exception("error")
        every { meterRegistry.timer(anyString(), anyList()) } throws Exception("error")

        val logger = LoggerFactory.getLogger(MicrometerMetricsOutputPort::class.java)
        val captor = ArgumentCaptor.forClass(String::class.java)

        metricsOutputPort.eventSent(topic, partition, elapsed)

        verify (exactly = 1) {
            meterRegistry invoke "counter" withArguments listOf(
                match<Meter.Id> {
                    it.name.startsWith(metricName)
                            && it.type == Meter.Type.COUNTER
                }
            )

            logger.error(captor.capture(), captor.capture())
        }
    }
}
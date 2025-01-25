package com.bueno.transactions.infraestructure.adapter.output.metrics

import com.bueno.transactions.application.port.output.metrics.MetricsOutputPort
import com.bueno.transactions.domain.entity.Transaction
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MicrometerMetricsOutputPort(
    private val meterRegistry: MeterRegistry
) : MetricsOutputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun eventSent(topic: String, partition: String, elapsedTime: Long) {
        val metricName = "event.sent"
        val tags = arrayOf(
            "topic" to topic,
            "partition" to partition
        )

        register(metricName, tags.toList())
        registerTime(metricName, elapsedTime, tags.toList())
    }

    override fun eventReceived(topic: String, partition: String, latency: Long) {
        val metricName = "event.received"
        val tags = arrayOf(
            "topic" to topic,
            "partition" to partition
        )

        register(metricName, tags.toList())
        registerTime("$metricName.latency", latency, tags.toList())
    }

    override fun batchProcessed(topic: String, partition: String, size: Int, elapsedTime: Long) {
        val metricName = "batch.processed"
        val tags = arrayOf(
            "topic" to topic,
            "partition" to partition,
            "size" to size.toString()
        )

        register(metricName, tags.toList())
        registerTime(metricName, elapsedTime, tags.toList())
    }

    override fun transactionProcessed(transaction: Transaction, elapsedTime: Long) {
        val metricName = "transaction.processed"
        val tags = arrayOf(
            "account" to transaction.accountId.toString(),
            "type" to transaction.type.toString(),
            "status" to transaction.authorization!!.code.toString(),
            "reason" to transaction.authorization!!.reason
        )

        register(metricName, tags.toList())
        registerTime(metricName, elapsedTime, tags.toList())
    }

    private fun registerTime(name: String, value: Long, tags: List<Pair<String, String>>) {
        val metricName = "$name.elapsed"
        try{
            Timer
                .builder(metricName)
                .tags(tags.map { Tag.of( it.first, it.second ) })
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(value, TimeUnit.MILLISECONDS)
        }
        catch (ex: Exception){
            logger.error("Error when exporting metric: $metricName", ex)
        }
    }

    private fun register(name: String, tags: List<Pair<String, String>>) {
        val metricName = "$name.counter"
        try{
            Counter
                .builder(metricName)
                .tags(tags.map { Tag.of( it.first, it.second ) })
                .register(meterRegistry)
                .increment()
        }
        catch (ex: Exception){
            logger.error("Error when exporting metric: $metricName", ex)
        }
    }
}